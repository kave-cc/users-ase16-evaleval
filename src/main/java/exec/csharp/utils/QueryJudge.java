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

import javax.annotation.Nonnull;

import cc.kave.commons.assertions.Asserts;
import cc.kave.rsse.calls.usages.DefinitionSite;
import cc.kave.rsse.calls.usages.NoUsage;
import cc.kave.rsse.calls.usages.Usage;
import exec.csharp.evaluation.impl.QueryContent;
import exec.validate_evaluation.microcommits.MicroCommit;

public class QueryJudge {

	private Usage start;
	private Usage end;

	private NoiseMode noiseMode;
	private QueryContent queryContent;

	private int numAdditions;
	private int numRemovals;

	public QueryJudge(@Nonnull MicroCommit mc) {
		Asserts.assertNotNull(mc);
		init(mc.getStart(), mc.getEnd());
	}

	public QueryJudge(@Nonnull Usage start, @Nonnull Usage end) {
		init(start, end);
	}

	private void init(Usage start, Usage end) {
		Asserts.assertNotNull(start);
		Asserts.assertNotNull(end);

		this.start = start;
		this.end = end;

		numAdditions = QueryUtils.countAdditions(start, end);
		numRemovals = QueryUtils.countRemovals(start, end);

		noiseMode = calcNoiseMode();
		queryContent = calcQueryContentCategorization();
	}

	private NoiseMode calcNoiseMode() {
		if (start instanceof NoUsage && end instanceof NoUsage) {
			return NoiseMode.SKIPPED;
		}

		if (start instanceof NoUsage) {
			boolean hasAdditions = !end.getReceiverCallsites().isEmpty();
			return hasAdditions ? NoiseMode.FROM_SCRATCH : NoiseMode.SKIPPED;
		}

		if (end instanceof NoUsage) {
			boolean hasRemovals = !start.getReceiverCallsites().isEmpty();
			return hasRemovals ? NoiseMode.PURE_REMOVAL : NoiseMode.SKIPPED;
		}
		
		if(!(hasAdditions() || hasRemovals())) {
			return NoiseMode.SKIPPED;
		}

		if (hasRemovals() && hasDefChange()) {
			if (hasAdditions()) {
				return NoiseMode.DEF_AND_REMOVAL;
			} else {
				return NoiseMode.PURE_REMOVAL;
			}
		}

		if (hasRemovals()) {
			return hasAdditions() ? NoiseMode.REMOVAL : NoiseMode.PURE_REMOVAL;
		}

		if (hasDefChange()) {
			return hasAdditions() ? NoiseMode.DEF : NoiseMode.SKIPPED;
		}

		return NoiseMode.NO_NOISE;
	}

	public NoiseMode getNoiseMode() {
		return noiseMode;
	}

	public boolean hasAdditions() {
		return getNumAdditions() > 0;
	}

	public int getNumAdditions() {
		return numAdditions;
	}

	public int getNumRemovals() {
		return numRemovals;
	}

	public boolean hasRemovals() {
		return getNumRemovals() > 0;
	}

	public boolean hasDefChange() {
		if (start instanceof NoUsage || end instanceof NoUsage) {
			return true;
		}
		DefinitionSite def1 = start.getDefinitionSite();
		DefinitionSite def2 = end.getDefinitionSite();
		return !def1.equals(def2);
	}

	public QueryContent getQueryContentCategorization() {
		return queryContent;
	}

	private QueryContent calcQueryContentCategorization() {
		if (start instanceof NoUsage && end instanceof NoUsage) {
			return QueryContent.SKIPPED;
		}

		if (start instanceof NoUsage) {
			boolean hasAdditions = !end.getReceiverCallsites().isEmpty();
			return hasAdditions ? QueryContent.FROM_SCRATCH : QueryContent.SKIPPED;
		}

		if (end instanceof NoUsage) {
			boolean hasRemovals = !start.getReceiverCallsites().isEmpty();
			return hasRemovals ? QueryContent.PURE_REMOVAL : QueryContent.SKIPPED;
		}

		int numStart = start.getReceiverCallsites().size();
		int numAdded = QueryUtils.countAdditions(start, end);
		int numRemoved = QueryUtils.countRemovals(start, end);
		int numStartWithoutNoise = numStart - numRemoved;
		Asserts.assertGreaterOrEqual(numStartWithoutNoise, 0);
		int numEnd = end.getReceiverCallsites().size();
		Asserts.assertEquals(numStartWithoutNoise + numAdded, numEnd);

		if (numAdded == 0) {
			return numRemoved == 0 ? QueryContent.SKIPPED : QueryContent.PURE_REMOVAL;
		}

		boolean hasNoise = hasDefChange() || getNumRemovals() > 0;
		if (numStartWithoutNoise == 0) {
			if (getNumAdditions() == 1) {
				return hasNoise ? QueryContent.ZERO_ONE_DEF : QueryContent.ZERO_ONE;
			} else {
				return hasNoise ? QueryContent.ZERO_DEF : QueryContent.ZERO;
			}
		}
		if (numStartWithoutNoise == 1) {
			if (getNumAdditions() == 1) {
				return hasNoise ? QueryContent.ONE_TWO_DEF : QueryContent.ONE_TWO;
			}
		}

		if (numStartWithoutNoise == numEnd - 1) {
			return hasNoise ? QueryContent.MINUS1_DEF : QueryContent.MINUS1;
		}

		return hasNoise ? QueryContent.NM_DEF : QueryContent.NM;
	}
}