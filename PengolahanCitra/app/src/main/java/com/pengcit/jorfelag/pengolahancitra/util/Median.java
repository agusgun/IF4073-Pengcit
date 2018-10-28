package com.pengcit.jorfelag.pengolahancitra.util;

import java.util.Arrays;

// https://github.com/email4rohit/interview-java-algo/blob/master/MedianOfMedians.java
public class Median {

    public static int findMedian(int[] arr) {
        return findMedianUtil(arr, (arr.length) / 2 + 1, 0, arr.length - 1);
    }

    private static int findMedianUtil(int[] arr, int k, int low, int high) {
        if (low == high) {
            return arr[low];
        }

        // sort the mth largest element in the given array
        int m = partition(arr, low, high);

        // Adjust position relative to the current subarray being processed
        int length = m - low + 1;

        // If mth element is the median, return it
        if (length == k) {
            return arr[m];
        }

        // If mth element is greater than median, search in the left subarray
        if (length > k) {
            return findMedianUtil(arr, k, low, m - 1);
        }
        // otherwise search in the right subArray
        else {
            return findMedianUtil(arr, k - length, m + 1, high);
        }
    }


    private static int partition(int arr[], int low, int high) {
        // Get pivotvalue by finding median of medians
        int pivotValue = getPivotValue(arr, low, high);

        // Find the sorted position for pivotVale and return it's index
        while (low < high) {
            while (arr[low] < pivotValue) {
                low++;
            }

            while (arr[high] > pivotValue) {
                high--;
            }

            if (arr[low] == arr[high]) {
                low++;
            } else if (low < high) {
                int temp = arr[low];
                arr[low] = arr[high];
                arr[high] = temp;
            }

        }
        return high;
    }

    // Find pivot value, such the it is always 'closer' to the actual median
    private static int getPivotValue(int arr[], int low, int high) {
        // If number of elements in the array are small, return the actual median
        if (high - low + 1 <= 9) {
            Arrays.sort(arr);
            return arr[arr.length / 2];
        }

        //Otherwise divide the array into subarray of 5 elements each, and recursively find the median

        // Array to hold '5 element' subArray, last subArray may have less than 5 elements
        int temp[] = null;

        // Array to hold the medians of all '5-element SubArrays'
        int medians[] = new int[(int) Math.ceil((double) (high - low + 1) / 5)];
        int medianIndex = 0;

        while (low <= high) {
            // get size of the next element, it can be less than 5
            temp = new int[Math.min(5, high - low + 1)];

            // copy next 5 (or less) elements, in the subarray
            for (int j = 0; j < temp.length && low <= high; j++) {
                temp[j] = arr[low];
                low++;
            }

            // sort subArray
            Arrays.sort(temp);

            // find mean and store it in median Array
            medians[medianIndex] = temp[temp.length / 2];
            medianIndex++;
        }

        // Call recursively to find median of medians
        return getPivotValue(medians, 0, medians.length - 1);
    }
}
