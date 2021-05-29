package kaktusz.kaktuszlogistics.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SetUtils {

	@SafeVarargs
	public static <T> Set<T> setFromElements(T... elements) {
		return new HashSet<>(Arrays.asList(elements));
	}

}
