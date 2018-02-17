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

import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.utils.io.NestedZipFolders;
import exec.csharp.queries.QueryBuilderFactory;
import exec.csharp.utils.ModelHelper;
import exec.csharp.utils.NoiseMode;
import exec.csharp.utils.QueryJudge;
import exec.validate_evaluation.microcommits.MicroCommit;

public class NoiseCategorizedEvaluation extends CategorizedEvaluation<NoiseMode> {

	public NoiseCategorizedEvaluation(NestedZipFolders<ITypeName> usages, ModelHelper mh, MicroCommitIoExtension mcIo,
			QueryBuilderFactory qbf) {
		super(usages, mh, mcIo, CategorizedEvaluationLogger.create(), qbf);
	}

	@Override
	protected NoiseMode getCategory(MicroCommit mc) {
		QueryJudge judge = new QueryJudge(mc.getStart(), mc.getEnd());
		return judge.getNoiseMode();
	}

	@Override
	protected boolean shouldEvaluate(NoiseMode c) {
		return !(c == NoiseMode.SKIPPED || c == NoiseMode.PURE_REMOVAL);
	}
}