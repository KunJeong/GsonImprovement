package gin.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.pmw.tinylog.Logger;

import gin.Patch;
import gin.SourceFile;
import gin.edit.Edit;
import gin.test.UnitTest;
import gin.test.UnitTestResultSet;


/**
 * Method-based GenProg-like GPSimple search.
 *
 */

public abstract class GPSimple_Circular extends GP {
    
    public GPSimple_Circular(String[] args) {
        super(args);
    }   

    // Constructor used for testing
    public GPSimple_Circular(File projectDir, File methodFile) {
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

            Patch[] patch_array = crossover(patch1, patch2, sourceFile);
            Patch patch3 = patch_array[0];
            crossoverPatches.add(patch3);
            Patch patch4 = patch_array[1];
            crossoverPatches.add(patch4);

        }

        return crossoverPatches;

    }

    /*============== Helper methods  ==============*/

    // Returns a patch which contains the first half of edits in patch1 and second half of edits in patch2 
    private Patch[] crossover(Patch patch1, Patch patch2, SourceFile sourceFile) {
        List<Edit> list1 = patch1.getEdits();
        List<Edit> list2 = patch2.getEdits();
        Patch result_patch1 = new Patch(sourceFile);
        Patch result_patch2 = new Patch(sourceFile);

        int fullsize, iPoint1, iPoint2;
        double dPoint1, dPoint2;
        fullsize = patch1.size() + patch2.size();
        dPoint1 = Math.random();
        iPoint1 = (int) (dPoint1 * fullsize);

        do {
            dPoint2 = Math.random();
            iPoint2 = (int) (dPoint2 * fullsize);
        } while (iPoint1 == iPoint2);

        if (iPoint2 < iPoint1) {
            int temp = iPoint1;
            iPoint1 = iPoint2;
            iPoint2 = temp;
        }

        Patch temp_patch = new Patch(sourceFile);
        for (int j = 0 ; j < patch1.size() ; j++) {
            temp_patch.add(list1.get(j));
        }
        for (int k = 0 ; k < patch2.size() ; k++) {
            temp_patch.add(list2.get(k));
        }
        List<Edit> total_list = temp_patch.getEdits();

        int i = 0;
        while (i < fullsize) {
            if (i < iPoint1) {
                result_patch1.add(total_list.get(i));
            } else if (i < iPoint2) {
                result_patch2.add(total_list.get(i));
            } else {
                result_patch1.add(total_list.get(i));
            }
            i++;
        }
        return new Patch[] {result_patch1, result_patch2};
    }

}

