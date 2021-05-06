package kaktusz.kaktuszlogistics.util;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {

	public static <T> List<T> listFromSingle(T single) {
		List<T> list = new ArrayList<>();
		list.add(single);
		return list;
	}

}
