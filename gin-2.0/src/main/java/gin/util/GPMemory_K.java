package gin.util;

import java.io.File;
import java.util.List;

import gin.SourceFile;
import gin.test.UnitTest;
import gin.test.UnitTestResult;
import gin.test.UnitTestResultSet;


/**
 * Method-based GenProg-like GPFix search.
 *
 */

public class GPMemory_K extends GPSimple_KPoints {
    
    public static void main(String[] args) {
        GPMemory_K sampler = new GPMemory_K(args);
        sampler.sampleMethods();
    }   

    public GPMemory_K(String[] args) {
        super(args);
    }   

    // Constructor used for testing
    public GPMemory_K(File projectDir, File methodFile) {
        super(projectDir, methodFile);
    }   

    // Use parent's search strategy
    @Override
    protected void search(String className, List<UnitTest> tests, SourceFile sourceFile) {
        super.search(className, tests, sourceFile);
    }   

    /*============== Implementation of abstract methods  ==============*/

    // Calculate fitness
    protected long fitness(UnitTestResultSet results) {
    
        return results.totalMemoryUsage();
    }   

    // Calculate fitness threshold, for selection to the next generation
    protected boolean fitnessThreshold(UnitTestResultSet results, long orig) {
    
        return results.allTestsSuccessful();
    }   


    // Compare two fitness values, result of comparison printed on commandline if > 0
    protected long compareFitness(long newFitness, long best) {

        return best - newFitness;
    }


}
