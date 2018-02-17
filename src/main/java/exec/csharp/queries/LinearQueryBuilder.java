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
package exec.csharp.queries;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import cc.kave.rsse.calls.usages.CallSite;
import cc.kave.rsse.calls.usages.Query;
import cc.kave.rsse.calls.usages.Usage;
import exec.csharp.utils.QueryUtils;

public class LinearQueryBuilder extends AbstractQueryBuilder {

	@Override
	public List<Query> createQueries(Usage start, Usage end) {
		Query q = Query.createAsCopyFrom(end);
		q.setAllCallsites(Sets.newLinkedHashSet());

		int numAdditions = QueryUtils.countAdditions(start, end);
		int numBefore = end.getReceiverCallsites().size() - numAdditions;

		Iterator<CallSite> it = end.getReceiverCallsites().iterator();
		int i = 0;
		while (i++ < numBefore && it.hasNext()) {
			CallSite cs = it.next();
			q.addCallSite(cs);
		}

		return Lists.newArrayList(q);
	}
}