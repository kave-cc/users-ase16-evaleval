/**
 * Copyright 2016 Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package exec.validate_evaluation.categorized;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.google.common.collect.Maps;

import cc.kave.commons.assertions.Asserts;
import cc.kave.commons.evaluation.Boxplot;
import cc.kave.commons.evaluation.BoxplotData;

public class CategorizedResults<T> {

	private Map<T, BoxplotData> categories = Maps.newHashMap();

	public Set<T> getCategories() {
		return categories.keySet();
	}

	public Boxplot get(T c) {
		Asserts.assertTrue(categories.containsKey(c), String.format("category is unknown: '%s'", c));
		return categories.get(c).getBoxplot();
	}

	public void add(T category, double val) {
		BoxplotData bpd = categories.get(category);
		if (bpd == null) {
			bpd = new BoxplotData();
			categories.put(category, bpd);
		}
		bpd.add(val);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getName());
		sb.append('@');
		sb.append(hashCode());
		sb.append(" {\n");
		for (T category : getCategories()) {
			sb.append("\t");
			sb.append(category);
			sb.append(':');
			sb.append(categories.get(category).getBoxplot());
			sb.append('\n');
		}
		sb.append("}");
		return sb.toString();
	}

	public static <T> CategorizedResults<T> create() {
		return new CategorizedResults<T>();
	}

	public static <T> CategorizedResults<T> merge(Collection<CategorizedResults<T>> allRes) {
		CategorizedResults<T> out = create();
		for (CategorizedResults<T> res : allRes) {
			for (T category : res.getCategories()) {
				out.add(category, res.get(category).getMean());
			}
		}
		return out;
	}
}