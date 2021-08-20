package kaktusz.kaktuszlogistics.util;

import java.util.Arrays;

public class ArrayUtils {
	/**
	 * @return A subarray of the given array, containing the elements who's index was greater than or equal to startIndex.
	 */
	public static <T> T[] subarray(T[] array, int startIndex) {
		if(startIndex >= array.length)
			return Arrays.copyOfRange(array, array.length, array.length); //empty array
		return Arrays.copyOfRange(array, startIndex, array.length);
	}
}
