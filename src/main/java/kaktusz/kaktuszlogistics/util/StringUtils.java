package kaktusz.kaktuszlogistics.util;

import java.text.DecimalFormat;

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
}
