package gin.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.pmw.tinylog.Logger;

import gin.Patch;
import gin.SourceFile;
import gin.edit.Edit;
import gin.test.UnitTest;
import gin.test.UnitTestResultSet;

import org.apache.commons.math3.exception.NumberIsTooLargeException;

/**
 * Method-based GPSimple search.
 *
 */

public abstract class GPSimple_KPoints extends GP {
    
    public GPSimple_KPoints(String[] args) {
        super(args);
    }   

    // Constructor used for testing
    public GPSimple_KPoints(File projectDir, File methodFile) {
        super(projectDir, methodFile);
    }   

    // Calculate fitness
    @Override
    protected abstract long fitness(UnitTestResultSet results);

    // Calculate fitness threshold, for selection to the next generation
    @Override
    protected abstract boolean fitnessThreshold(UnitTestResultSet results, long orig);
    
     // Compare two fitness values, result of comparison printed on commandline if > 0
    @Override
    protected abstract long compareFitness(long newFitness, long best);

    /*============== Implementation of abstract methods  ==============*/

    // Simple GP search (based on GenProg)
    protected void search(String className, List<UnitTest> tests, SourceFile sourceFile) {

        Patch origPatch = new Patch(sourceFile);

        // Run original code
        UnitTestResultSet results = testPatch(className, tests, origPatch);

        // Calculate fitness and record result
        long orig = fitness(results);
        super.writePatch(results, className, orig, 0);

        // Keep best 
        long best = orig;

        // Generation 0
        Map<Patch, Long> population = new HashMap<>();
        population.put(origPatch, orig);

        for (int g = 0; g < genNumber; g++) {

            // Previous generation
            List<Patch> patches = new ArrayList(population.keySet());

            Logger.info("Creating generation: " + (g + 1));

            // Current generation
            Map<Patch, Long> newPopulation = new HashMap<>();

            // Keep a list of patches after crossover
            List<Patch> crossoverPatches = createCrossoverPatches(patches, sourceFile);

            // If less than indNumber variants produced, add random patches from the previous generation
            while (crossoverPatches.size() < indNumber) {
                crossoverPatches.add(select(patches));
            }
            
            // Mutate the newly created population and check runtime
            for (Patch patch : crossoverPatches) {

                // Add a mutation
                patch = mutate(patch);

                Logger.info("Testing patch: " + patch);

                // Test the patched source file
                results = testPatch(className, tests, patch);
                long newFitness = fitness(results);

                // If all tests pass, add patch to the mating population, check for new bestTime 
                if (fitnessThreshold(results, orig)) {
                    super.writePatch(results, className, newFitness, compareFitness(newFitness, orig));
                    newPopulation.put(patch, newFitness);
                    long better = compareFitness(newFitness, best);
                    if (better > 0) {
                        Logger.info("Better patch found: " + patch);
                        Logger.info("Fitness improvement over best found so far: " + better);
                        best = newFitness;
                    }
                } else {
                    super.writePatch(results, className, newFitness, 0);
                }
            }

            population = new HashMap<Patch, Long>(newPopulation);
            if (population.isEmpty()) {
                population.put(origPatch, orig);
            }
            
              }

    }

    // Simple patch selection, returns a clone of the selected patch
    protected Patch select(List<Patch> patches) {
        return patches.get(super.individualRng.nextInt(patches.size())).clone();
    }

    // Mutation operator, returns a clone of the old patch
    protected Patch mutate(Patch oldPatch) {
        
        Patch patch = oldPatch.clone();
        if(Math.random() < 0.7){
            patch.addRandomEdit(super.mutationRng, super.editType);
            return patch;
        }
        //remove node where edit was added with 0.3 probability
        List<Edit> list = oldPatch.getEdits();
        for (int i = 0; i < oldPatch.size(); i++) {
            patch.remove(0);
        }
        int a = (int)(Math.random() * oldPatch.size());

        for (int i = 0; i < oldPatch.size(); i++) {
            if(i==a){
                patch.addRandomEdit(super.mutationRng, super.editType);
            }
            patch.add(list.get(i));
        }
        return patch;
    }

    // Returns a list of patches after crossover
    protected List<Patch> createCrossoverPatches(List<Patch> patches, SourceFile sourceFile) {

        List<Patch> crossoverPatches = new ArrayList<>();

        // Crossover produces four individuals 
        for (int i = 0; i < super.indNumber / 4; i++) {

            // Select a patch from previous generation
            Patch patch1 = select(patches);
            crossoverPatches.add(patch1);
            Patch patch2 = select(patches);
            crossoverPatches.add(patch2);

            
            // Patch patch3 = crossover(patch1, patch2, sourceFile);
            // crossoverPatches.add(patch3);
            // Patch patch4 = crossover(patch2, patch1, sourceFile);
            // crossoverPatches.add(patch4);
            
            int crossoverPoints = 0;
            if (patch1.size() == 0){
                crossoverPoints = patch1.size();
            }
            else{
                Random random = new Random();
                crossoverPoints = random.nextInt(patch1.size());
            }

            List<Patch> patchList = kPointCrossover(patch1, patch2, crossoverPoints, sourceFile);
            crossoverPatches.add(patchList.get(0));
            crossoverPatches.add(patchList.get(1));

        }

        return crossoverPatches;

    }

    /*============== Helper methods  ==============*/

    // Returns a patch which contains the first half of edits in patch1 and second half of edits in patch2 
    // private Patch crossover(Patch patch1, Patch patch2, SourceFile sourceFile) {
    //     List<Edit> list1 = patch1.getEdits();
    //     List<Edit> list2 = patch2.getEdits();
    //     Patch patch = new Patch(sourceFile);
    //     for (int i = 0; i < patch1.size() / 2; i++) {

    //         patch.add(list1.get(i));
    //     }
    //     for (int i = patch2.size() / 2; i < patch2.size(); i++) {
    //         patch.add(list2.get(i));
    //     }
    //     return patch;
    // }


    private List<Patch> kPointCrossover(Patch patch1, Patch patch2, int crossoverPoints, SourceFile sourceFile)
        throws NumberIsTooLargeException{
        List<Edit> list1 = patch1.getEdits();
        List<Edit> list2 = patch2.getEdits();
        Patch patch = new Patch(sourceFile);

        Patch childPatch1 = new Patch(sourceFile);
        Patch childPatch2 = new Patch(sourceFile);
        
        int length = patch1.size();
        int remainPoints = crossoverPoints;
        int lastIndex = 0;
        Random random = new Random();

        if (length == 0 || length == 1){
            childPatch1 = patch2;
            childPatch2 = patch1;
        }
        else{
            if (crossoverPoints >= length) {
            throw new NumberIsTooLargeException(crossoverPoints, length, false);
            }

            for(int i = 0; i< crossoverPoints; i++, remainPoints --){
                final int crossoverIndex = 1 + lastIndex + random.nextInt(length - lastIndex - remainPoints);

                for(int j = lastIndex; j < crossoverIndex; j++){
                    childPatch1.add(list1.get(j));
                    childPatch2.add(list2.get(j));
                }
                
                Patch temp = childPatch1;
                childPatch1 = childPatch2;
                childPatch2 = temp;

                lastIndex = crossoverIndex;
            }

            for (int j = lastIndex; j < length; j++){
                childPatch1.add(list1.get(j));
                childPatch2.add(list2.get(j));
            }
        }

        List<Patch> patchList = new ArrayList<>();
        patchList.add(childPatch1);
        patchList.add(childPatch2);

        return patchList;
    }

}

