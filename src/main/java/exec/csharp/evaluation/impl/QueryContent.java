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
package exec.csharp.evaluation.impl;

public enum QueryContent {
	ZERO_ONE, ZERO_ONE_DEF, // 0|1[~D]
	ONE_TWO, ONE_TWO_DEF, // 1|2[~D]
	ZERO, ZERO_DEF, // 0|M[~D],
	NM, NM_DEF, // N|M[~D]
	MINUS1, MINUS1_DEF, // M-1|M[~D]
	FROM_SCRATCH, // ~[+x]
	PURE_REMOVAL, // N-y[~D], ~[-x]
	SKIPPED // 0[~D], N[~D], ~[+/-0], in general "nn" queries
}