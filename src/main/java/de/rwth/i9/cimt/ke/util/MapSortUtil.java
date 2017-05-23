package de.rwth.i9.cimt.ke.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MapSortUtil {

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueAsc(Map<K, V> unsortedMap) {
		List<Map.Entry<K, V>> list = new LinkedList<>(unsortedMap.entrySet());
		Comparator<Map.Entry<K, V>> cAsc = new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return o1.getValue().compareTo(o2.getValue());

			}
		};
		Collections.sort(list, cAsc);
		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueDesc(Map<K, V> unsortedMap) {
		List<Map.Entry<K, V>> list = new LinkedList<>(unsortedMap.entrySet());
		Comparator<Map.Entry<K, V>> cDesc = new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return o2.getValue().compareTo(o1.getValue());

			}
		};
		Collections.sort(list, cDesc);
		Map<K, V> result = new LinkedHashMap<>();
		for (Map.Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

}
