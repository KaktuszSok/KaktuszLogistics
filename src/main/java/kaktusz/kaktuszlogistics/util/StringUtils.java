package kaktusz.kaktuszlogistics.util;

import java.text.DecimalFormat;
import java.util.StringJoiner;

public class StringUtils {
	private static final DecimalFormat TWO_DECIMAL_PRECISION = new DecimalFormat("#.##");
	private static final DecimalFormat FOUR_DECIMAL_PRECISION = new DecimalFormat("#.####");

	/**
	 * @return A string representation of the given double, accurate up to 2 decimal places
	 */
	public static String formatDouble(double d) {
		return TWO_DECIMAL_PRECISION.format(d);
	}

	/**
	 * @return A string representation of the given double, accurate up to 4 decimal places
	 */
	public static String formatDoublePrecise(double d) {
		return FOUR_DECIMAL_PRECISION.format(d);
	}

	/**
	 * @return A modified version of the given string, where each word's first letter is uppercase and the rest are lowercase
	 */
	public static String fixCapitalisation(String words) {
		return fixCapitalisation(words.split(" "));
	}

	/**
	 * @return A string comprised of the given words, where each word's first letter is uppercase and the rest are lowercase
	 */
	public static String fixCapitalisation(String[] words) {
		StringJoiner name = new StringJoiner(" ");
		for (String word : words) {
			String capitalisedWord = word.substring(0, 1);
			if(word.length() > 1)
				capitalisedWord += word.substring(1).toLowerCase();
			name.add(capitalisedWord);
		}

		return name.toString();
	}
}
