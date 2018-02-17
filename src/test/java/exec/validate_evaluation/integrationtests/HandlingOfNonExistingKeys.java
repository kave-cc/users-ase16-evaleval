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
package exec.validate_evaluation.integrationtests;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import cc.kave.commons.model.events.completionevents.CompletionEvent;
import cc.kave.commons.model.events.completionevents.ICompletionEvent;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.commons.model.ssts.expressions.IAssignableExpression;
import cc.kave.commons.model.ssts.impl.declarations.MethodDeclaration;
import cc.kave.commons.model.ssts.impl.expressions.simple.ConstantValueExpression;
import cc.kave.commons.model.ssts.impl.statements.Assignment;
import cc.kave.commons.model.ssts.statements.IAssignment;
import cc.kave.rsse.calls.usages.NoUsage;
import cc.kave.rsse.calls.usages.Usage;
import exec.validate_evaluation.microcommits.MicroCommit;

public class HandlingOfNonExistingKeys extends BaseIntegrationTest {

	@Override
	protected List<ICompletionEvent> getEvents() {
		return Lists.newArrayList(first(), second(), third());
	}

	private CompletionEvent first() {

		MethodDeclaration md = newMethodDeclaration(10, 1, //
				varDecl(t(1), "o1"), //
				assign("o1", new ConstantValueExpression()), //
				invStmt("o1", m(1, 1)), //
				invStmt("o1", m(1, 2)));

		return completionEvent(10, md);
	}

	private CompletionEvent second() {

		MethodDeclaration md = newMethodDeclaration(10, 1, //
				varDecl(t(1), "o1"), //
				assign("o1", new ConstantValueExpression()), //
				invStmt("o1", m(1, 1)), //
				//
				varDecl(t(2), "o2"), //
				assign("o2", new ConstantValueExpression()), //
				invStmt("o2", m(2, 1)));

		return completionEvent(10, md);
	}

	private CompletionEvent third() {

		MethodDeclaration md = newMethodDeclaration(10, 1, //
				varDecl(t(2), "o2"), //
				assign("o2", new ConstantValueExpression()), //
				invStmt("o2", m(2, 1)), //
				invStmt("o2", m(2, 2)));

		return completionEvent(10, md);
	}

	private IAssignment assign(String id, IAssignableExpression expr) {
		Assignment assignment = new Assignment();
		assignment.setReference(ref(id));
		assignment.setExpression(expr);
		return assignment;
	}

	@Override
	protected Set<MicroCommit> getExpectedMicroCommits() {
		IMethodName ctx = m(10, 1);
		Usage a1 = _q(ctx, t(1), m(1, 1), m(1, 2));
		Usage a2 = _q(ctx, t(1), m(1, 1));
		Usage a3 = new NoUsage();
		Usage b1 = new NoUsage();
		Usage b2 = _q(ctx, t(2), m(2, 1));
		Usage b3 = _q(ctx, t(2), m(2, 1), m(2, 2));

		return Sets.newHashSet(com(a1, a2), com(a1, a3), com(a2, a3), com(b1, b2), com(b1, b3), com(b2, b3));
	}

	private MicroCommit com(Usage a1, Usage a2) {
		return MicroCommit.create(a1, a2);
	}
}