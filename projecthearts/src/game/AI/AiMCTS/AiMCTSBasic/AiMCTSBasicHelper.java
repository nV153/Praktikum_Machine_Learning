package game.AI.AiMCTS.AiMCTSBasic;
import java.util.LinkedList;
import java.util.List;

/**
 * This class provides auxiliary methods for MCTS-based AI.
 */
public class AiMCTSBasicHelper {

    /**
     * This inner class provides methods to determine permutations.
     * Its only functionality is to generate all possible subsets of a size r of a given array with distinct elements.
     * Source: https://www.geeksforgeeks.org/print-subsets-given-size-set/
     * The original code and comments were contributed by Devesh Agrawal but amended here. 
     * Essentially, a program to print all combinations of size r in an array of size n.
     */  
    class Permutation {
    
        /* arr[]  ---> Input Array
        data[] ---> Temporary array to store current combination
        start & end ---> Starting and Ending indexes in arr[]
        index  ---> Current index in data[]
        r ---> Size of a combination to be printed */
        static void combinationUtil(int arr[], int n, int r,
                            int index, int data[], int i, List<int[]> resultList){
            // Current combination is ready to be printed, 
            // print it
            if (index == r) {
                int[] combination = new int[3];
                for (int j = 0; j < r; j++) {                    
                    combination[j] = data[j];
                }                
                resultList.add(combination);                
                return;
            }
    
            // When no more elements are there to put in data[]
            if (i >= n)
                return;
    
            // current is included, put next at next
            // location
            data[index] = arr[i];
            combinationUtil(arr, n, r, index + 1, 
                                data, i + 1, resultList);
    
            // current is excluded, replace it with
            // next (Note that i+1 is passed, but
            // index is not changed)
            combinationUtil(arr, n, r, index, data, i + 1, resultList);
        }
    
        // The main function that prints all combinations
        // of size r in arr[] of size n. This function 
        // mainly uses combinationUtil()
        static void getCombinations(int arr[], int n, int r, List<int[]> resultList){
            // A temporary array to store all combination
            // one by one
            int data[] = new int[r];
    
            // Print all combination using temporary
            // array 'data[]'
            combinationUtil(arr, n, r, 0, data, 0, resultList);
        }
    
        /** 
         * This method determines all possible subsets of a specific size r and with distinct elements for an array of elements.
         * @param arr The array with the elements.
         * @param sizeOfsubsets The size r of subsets.
         * @return List<int[]> The list of all possible subsets.
         */
        public static List<int[]> getAllPossibleSubsetsOfASizeOfArrayWithDistinctElements(int arr[], int sizeOfsubsets){

            int r = sizeOfsubsets;
            int n = arr.length;
            List<int[]> resultList = new LinkedList<>();
            getCombinations(arr, n, r, resultList);
            return resultList;        
        }
    
    
    }

}
