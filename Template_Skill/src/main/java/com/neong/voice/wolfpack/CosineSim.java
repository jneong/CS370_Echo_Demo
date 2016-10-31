package com.neong.voice.wolfpack;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.lang.Math;

public class CosineSim {

	/**
	 * @param string1
	 * @param string2
	 * 
	 * @return A map with the union of the characters in string1 and string2 as
	 *         the keys and 0 as all of the values.
	 */
	private static Map<Character, Integer> BuildUncountedMap(String string1, String string2) {

		// LinkedHashMaps store the keys in the order that they are entered.
		// That way if the counts in the map are converted to an array, the
		// order than they are entered in can be known.
		Map<Character, Integer> result = new LinkedHashMap<Character, Integer>();
		Set<Character> union = new HashSet<Character>();

		for (char oneChar : string1.toCharArray()) {
			union.add(oneChar);
		}
		for (char oneChar : string2.toCharArray()) {
			union.add(oneChar);
		}

		for (char oneChar : union) {
			result.put(oneChar, 0);
		}
		return result;
	}

	/**
	 * Given a string and a blank map that has all of the unique characters in
	 * the string in it (and optionally other characters that may not be in the
	 * string), the method will count how many times each character appears in
	 * the string and stores it in the map.
	 * 
	 * @param inString
	 *            A string to count character occurrences from.
	 * @param countMap
	 *            A map who's keys are all characters in the string. There may
	 *            be keys for additional characters that are not in the string.
	 *            All values for the map should be set to 0.
	 * 
	 * @post countMap will be modified so that all of the values are the number
	 *       of times that each character appeared in the string.
	 * 
	 * @return An int array that contains the contains the counts for each
	 *         character's occurrences (including for characters that were part
	 *         of the map but did not appear at all). If the Map is a
	 *         LinkedHashMap, then the order of the numbers in the array will be
	 *         the same order that the keys were entered in the Map.
	 */
	private static int[] GetCounts(String inString, Map<Character, Integer> countMap) {
		for (char oneChar : inString.toCharArray()) {
			countMap.put(oneChar, countMap.get(oneChar) + 1);
		}

		int[] countArray = new int[countMap.size()];
		int idx = 0;
		for (Entry<Character, Integer> entry : countMap.entrySet()) {
			countArray[idx] += countMap.get(entry.getKey());
			idx++;
		}
		return countArray;

	}

	/**
	 * Calculates the dot product of the integers in two arrays as if the arrays
	 * were mathematical vectors.
	 * 
	 * @param charCounts1
	 * @param charCounts2
	 * 
	 * @pre charCounts1 and charCounts2 must be of the same length.
	 * 
	 * @return The dot product of the two arrays.
	 */
	private static int DotProduct(int[] charCounts1, int[] charCounts2) {
		int product = 0;
		for (int i = 0; i < charCounts1.length; i++) {
			product += charCounts1[i] * charCounts2[i];
		}
		return product;
	}

	/**
	 * Calculates the magnitude of an array of integers as if it were a
	 * mathematical vector.
	 * 
	 * @param charCounts
	 * 
	 * @return The magnitude of charCounts
	 */
	private static double Magnitude(int[] charCounts) {
		double magnitude = 0;

		for (int i = 0; i < charCounts.length; i++) {
			magnitude += Math.pow(charCounts[i], 2);
		}
		return Math.sqrt(magnitude);
	}

	/**
	 * Calculates the cosine similarity of two strings by using the formula: (A
	 * dot B)/(||A|| x ||B||) to determine how similar the two strings are to
	 * each other. Information about cosine similarity can be found at:
	 * https://en.wikipedia.org/wiki/Cosine_similarity
	 * 
	 * The method is case sensitive.
	 * 
	 * @param s1
	 *            First string to compare.
	 * @param s2
	 *            Second string to compare.
	 * @return A rating such that 0 <= rating <= 1, where 0 means that there is
	 *         no similarity at all and a rating of 1 means that the strings
	 *         were identical.
	 */
	public static double getRating(String s1, String s2) {
		Map<Character, Integer> m1 = BuildUncountedMap(s1, s2);
		Map<Character, Integer> m2 = new LinkedHashMap<Character, Integer>(m1);
		int[] charCounts1 = GetCounts(s1, m1);
		int[] charCounts2 = GetCounts(s2, m2);

		return DotProduct(charCounts1, charCounts2) / (Magnitude(charCounts1) * Magnitude(charCounts2));

	}

	/**
	 * Given innerString, the method will check all of the strings in
	 * outerStrings to determine which one is the most similar, based on cosine
	 * similarity.
	 * 
	 * @param innerString
	 *            A string to compare to all other strings in outerStrings
	 * @param outerStrings
	 *            All possible strings that innerString will be compared to to
	 *            find the one that is the most similar.
	 * @return A string from outerStrings that is the most similar to
	 *         innerString. If none of the strings have any similarities,
	 *         the first string in the array will be returned.
	 */
	public static String getBestMatch(String innerString, Iterable<String> outerStrings) {
		String bestMatch = "";
		double bestSimilarity = -1;
		double similarity;

		System.out.println("CosineSim: innerString = \"" + innerString + "\"");

		for (String possibility : outerStrings) {
			similarity = getRating(innerString, possibility);
			if (similarity > bestSimilarity) {
				bestMatch = possibility;
				bestSimilarity = similarity;
			}
		}

		System.out.println("CosineSime: bestMatch = \"" + bestMatch + "\"");

		return bestMatch;
	}
}
