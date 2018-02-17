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

public enum NoiseMode {
	NO_NOISE, // n+x
	DEF, // n+x~D
	REMOVAL, // n+x-y
	DEF_AND_REMOVAL, // n+x-y~D
	PURE_REMOVAL, // n-y[~D], ~[-X]
	FROM_SCRATCH, // ~[+x]
	SKIPPED // n[~D], ~[+/-0]
}