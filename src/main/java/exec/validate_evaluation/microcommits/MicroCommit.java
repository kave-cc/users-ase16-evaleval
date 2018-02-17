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

package exec.validate_evaluation.microcommits;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import cc.kave.commons.assertions.Asserts;
import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.utils.ToStringUtils;
import cc.kave.rsse.calls.usages.NoUsage;
import cc.kave.rsse.calls.usages.Usage;

public class MicroCommit {
	private Usage Item1;
	private Usage Item2;

	public Usage getStart() {
		return Item1;
	}

	public Usage getEnd() {
		return Item2;
	}

	public ITypeName getType() {
		if (hasOnlyNoUsages()) {
			return Names.getUnknownType();
		}
		return getFirstNoUsage().getType();
	}

	private boolean hasOnlyNoUsages() {
		return Item1 instanceof NoUsage && Item2 instanceof NoUsage;
	}

	public IMethodName getMethodContext() {
		if (hasOnlyNoUsages()) {
			return Names.getUnknownMethod();
		}
		return getFirstNoUsage().getMethodContext();
	}

	private Usage getFirstNoUsage() {
		if (!(Item1 instanceof NoUsage)) {
			return Item1;
		}
		return Item2;
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
		return ToStringUtils.toString(this);
	}

	public static MicroCommit create(@Nonnull Usage start, @Nonnull Usage end) {
		Asserts.assertNotNull(start);
		Asserts.assertNotNull(end);

		MicroCommit mc = new MicroCommit();
		mc.Item1 = start;
		mc.Item2 = end;
		return mc;
	}
}