/*
 * Copyright 2014 Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package exec.csharp.utils;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.Maps;

public class MapSorter {
	public static <K extends Comparable<K>> Map<K, Integer> sortByCount(Map<K, Integer> map) {

		return sortCustom(map, new Comparator<K>() {
			@Override
			public int compare(K a, K b) {
				Integer ca = map.get(a);
				Integer cb = map.get(b);

				int ia = ca == null ? 0 : ca;
				int ib = cb == null ? 0 : cb;

				if (ia == ib) {
					return a.compareTo(b);
				}
				return ib - ia;
			}
		});
	}

	public static <K extends Comparable<K>, V extends Comparable<V>> Map<K, V> sort(Map<K, V> map) {

		return sortCustom(map, new Comparator<K>() {
			@Override
			public int compare(K a, K b) {
				V v1 = map.get(a);
				V v2 = map.get(b);

				int res = v1.compareTo(v2);
				if (res == 0) {
					return b.compareTo(a);
				}
				return res;
			}
		});
	}

	public static <K extends Comparable<K>, V> Map<K, V> sortCustom(Map<K, V> map, Comparator<K> comparator) {

		Set<K> sortedKeys = new TreeSet<K>(comparator);

		sortedKeys.addAll(map.keySet());

		Map<K, V> sortedMap = Maps.newLinkedHashMap();
		for (K key : sortedKeys) {
			sortedMap.put(key, map.get(key));
		}

		return sortedMap;
	}
}