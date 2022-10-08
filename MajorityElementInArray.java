
public class MajorityElementInArray {
    public static void main(String[] args) {
        int[] arr = new int[]{0, 5, 2, 5, 5, 5, 0, 3, 3};
        //if method turned nothing then there is no frequent else there is frequent

        System.out.println("The answer is: " + findFrequentNum(arr) + ". Because this number is " + findCountOfFrequentNum(arr) + " times inside the array.");

    }

    public static int findFrequentNum(int[] arr) {
        int count;
        int max = 0;
        int currentMax = 0;
        int length = arr.length;
        //Nested for loop
        for (int t = 0; t < length; t++) {
            count = 0;
            for (int j = 0; j < length; j++) {
                //if they equal then count will increase by 1
                if (arr[t] == arr[j]) {
                    count++;
                }
            }
            //if max is lower than count then max is now count
            if (max < count) {
                max = count;
                //saved the current max number in currentMax
                currentMax = arr[t];
            }
        }

        //returned the final max number
        return currentMax;
    }

    public static int findCountOfFrequentNum(int[] arrI) {
        //found count of the frequent number using nested for loop
        int count = 0;
        for (int i : arrI) {
            for (int j : arrI) {
                if (i == j) {
                    count++;
                }
            }
            return count;
        }
        return 0;
    }
}