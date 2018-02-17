/**
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
package exec.validate_evaluation;

import java.io.File;

import cc.kave.commons.model.events.completionevents.Context;
import cc.kave.commons.model.ssts.ISST;
import cc.kave.commons.model.ssts.impl.visitor.inlining.InliningContext;
import cc.kave.commons.utils.io.IReadingArchive;
import cc.kave.commons.utils.io.ReadingArchive;
import cc.kave.commons.utils.ssts.SSTPrintingUtils;

public class SSTComparison {

	private int counter = 0;
	private String zipTestCases;

	public SSTComparison(String zipTestCases) {
		this.zipTestCases = zipTestCases;
	}

	public void run() {
		try {
			IReadingArchive ra = new ReadingArchive(new File(zipTestCases));
			while (ra.hasNext()) {
				printSeparator();
				Context ctx = ra.getNext(Context.class);
				print(ctx, "before");
				Context outCtx = inline(ctx);
				print(outCtx, "after");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void printSeparator() {
		System.out.printf("\n### %d ########################################\n", counter++);
	}

	private void print(Context ctx, String title) {
		System.out.printf("\n--- %s ---\n\n", title);
		String pseudoCode = SSTPrintingUtils.printSST(ctx.getSST());
		System.out.println(pseudoCode);
	}

	private Context inline(Context orig) {
		Context inlined = new Context();
		inlined.setTypeShape(orig.getTypeShape());
		inlined.setSST(inline(orig.getSST()));
		return inlined;
	}

	private ISST inline(ISST sst) {
		InliningContext context = new InliningContext();
		sst.accept(context.getVisitor(), context);
		return context.getSST();
	}
}