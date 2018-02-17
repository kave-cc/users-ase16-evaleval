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
package exec.validate_evaluation.streaks;

import java.time.ZonedDateTime;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import cc.kave.commons.assertions.Asserts;
import cc.kave.commons.model.events.completionevents.Context;
import cc.kave.commons.model.naming.codeelements.IMethodName;

public class Snapshot {

	private ZonedDateTime date;
	private Context context;
	private IMethodName selection;

	private Snapshot() {
		// for de-/serialization only; use "create()" instead.
	}

	public ZonedDateTime getDate() {
		return date;
	}

	public Context getContext() {
		return context;
	}

	public IMethodName getSelection() {
		return selection;
	}

	public boolean hasSelection() {
		return selection != null;
	}

	public static Snapshot create(ZonedDateTime date, Context context, IMethodName selection) {
		Asserts.assertNotNull(date);
		Asserts.assertNotNull(context);

		Snapshot e = new Snapshot();
		e.date = date;
		e.context = context;
		e.selection = selection;
		return e;
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
}