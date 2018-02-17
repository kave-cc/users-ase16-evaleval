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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import cc.kave.commons.model.events.completionevents.CompletionEvent;
import cc.kave.commons.model.events.completionevents.Context;
import cc.kave.commons.model.events.completionevents.ICompletionEvent;
import cc.kave.commons.model.events.completionevents.Proposal;
import cc.kave.commons.model.events.completionevents.ProposalSelection;
import cc.kave.commons.model.events.completionevents.TerminationState;
import cc.kave.commons.model.naming.IName;
import cc.kave.commons.model.naming.Names;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.model.ssts.IStatement;
import cc.kave.commons.model.ssts.expressions.IAssignableExpression;
import cc.kave.commons.model.ssts.expressions.assignable.ICompletionExpression;
import cc.kave.commons.model.ssts.expressions.assignable.IInvocationExpression;
import cc.kave.commons.model.ssts.impl.SST;
import cc.kave.commons.model.ssts.impl.declarations.MethodDeclaration;
import cc.kave.commons.model.ssts.impl.expressions.assignable.CompletionExpression;
import cc.kave.commons.model.ssts.impl.expressions.assignable.InvocationExpression;
import cc.kave.commons.model.ssts.impl.references.VariableReference;
import cc.kave.commons.model.ssts.impl.statements.ExpressionStatement;
import cc.kave.commons.model.ssts.impl.statements.VariableDeclaration;
import cc.kave.commons.model.ssts.references.IVariableReference;
import cc.kave.commons.model.ssts.statements.IExpressionStatement;
import cc.kave.commons.model.ssts.statements.IVariableDeclaration;
import cc.kave.commons.utils.io.Directory;
import cc.kave.commons.utils.io.IReadingArchive;
import cc.kave.commons.utils.io.WritingArchive;
import cc.kave.rsse.calls.usages.CallSites;
import cc.kave.rsse.calls.usages.DefinitionSites;
import cc.kave.rsse.calls.usages.Query;
import exec.validate_evaluation.microcommits.MicroCommit;
import exec.validate_evaluation.microcommits.MicroCommitGenerationLogger;
import exec.validate_evaluation.microcommits.MicroCommitGenerationRunner;
import exec.validate_evaluation.microcommits.MicroCommitIo;
import exec.validate_evaluation.queryhistory.IUsageExtractor;
import exec.validate_evaluation.queryhistory.QueryHistoryCollector;
import exec.validate_evaluation.queryhistory.QueryHistoryGenerationLogger;
import exec.validate_evaluation.queryhistory.QueryHistoryGenerationRunner;
import exec.validate_evaluation.queryhistory.QueryHistoryIo;
import exec.validate_evaluation.queryhistory.UsageExtractor;
import exec.validate_evaluation.streaks.EditStreakGenerationIo;
import exec.validate_evaluation.streaks.EditStreakGenerationLogger;
import exec.validate_evaluation.streaks.EditStreakGenerationRunner;

public abstract class BaseIntegrationTest {

	@Rule
	public TemporaryFolder dirTmp = new TemporaryFolder();
	private File dirEvents;
	private File dirStreaks;
	private File dirHistories;
	private File dirCommits;

	private EditStreakGenerationRunner esGen;
	private MicroCommitGenerationRunner mcGen;
	private QueryHistoryGenerationRunner qhGen;

	@Before
	public void baseSetup() throws IOException {
		dirEvents = dirTmp.newFolder("events");
		dirStreaks = dirTmp.newFolder("streaks");
		dirHistories = dirTmp.newFolder("histories");
		dirCommits = dirTmp.newFolder("commits");

		EditStreakGenerationIo esIo = new EditStreakGenerationIo(path(dirEvents), path(dirStreaks));
		EditStreakGenerationLogger esLog = new EditStreakGenerationLogger();
		esGen = new EditStreakGenerationRunner(esIo, esLog);

		QueryHistoryIo qhIo = new QueryHistoryIo(path(dirHistories));
		QueryHistoryGenerationLogger qhLog = new QueryHistoryGenerationLogger();
		QueryHistoryCollector histCollector = new QueryHistoryCollector(qhLog);
		IUsageExtractor usageExtractor = new UsageExtractor();
		qhGen = new QueryHistoryGenerationRunner(esIo, qhIo, qhLog, histCollector, usageExtractor);

		MicroCommitIo mcIo = new MicroCommitIo(path(dirCommits));
		MicroCommitGenerationLogger mcLog = new MicroCommitGenerationLogger();
		mcGen = new MicroCommitGenerationRunner(qhIo, mcIo, mcLog);
	}

	private String path(File f) {
		return f.getAbsolutePath();
	}

	@Test
	public void integration() {
		writeEvents();

		esGen.run();
		qhGen.run();
		mcGen.run();

		validate();
	}

	private void writeEvents() {
		List<ICompletionEvent> input = getEvents();
		Directory dir = new Directory(dirEvents.getAbsolutePath());

		try (WritingArchive wa = dir.getWritingArchive("a.zip")) {
			for (ICompletionEvent e : input) {
				wa.add(e);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	protected abstract List<ICompletionEvent> getEvents();

	private void validate() {
		Set<MicroCommit> actuals = readCommits();
		Set<MicroCommit> expecteds = getExpectedMicroCommits();
		assertEquals(expecteds, actuals);
	}

	private Set<MicroCommit> readCommits() {
		Set<MicroCommit> commits = Sets.newHashSet();
		Directory dir = new Directory(path(dirCommits));
		if (!dir.exists("a.zip")) {
			return commits;
		}
		try (IReadingArchive ra = dir.getReadingArchive("a.zip")) {
			while (ra.hasNext()) {
				commits.add(ra.getNext(MicroCommit.class));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return commits;
	}

	protected abstract Set<MicroCommit> getExpectedMicroCommits();

	/* utils for completion events */

	protected CompletionEvent completionEvent(int typeNum, MethodDeclaration md) {
		return completionEvent(typeNum, md, null);
	}

	protected CompletionEvent completionEvent(int typeNum, MethodDeclaration md, IName selection) {
		SST sst = new SST();
		sst.setEnclosingType(t(typeNum));
		sst.getMethods().add(md);

		CompletionEvent ce = new CompletionEvent();
		ce.TriggeredAt = ZonedDateTime.now();

		ce.context = new Context();
		ce.context.setSST(sst);

		if (selection != null) {
			addSelection(ce, selection);
		}
		return ce;
	}

	private void addSelection(CompletionEvent ce, IName selection) {
		Proposal proposal = new Proposal();
		proposal.Name = selection;
		proposal.Relevance = 100;
		ce.proposalCollection = Lists.newArrayList(proposal);

		ProposalSelection proposalSelection = new ProposalSelection();
		proposalSelection.Proposal = proposal;

		ce.terminatedState = TerminationState.Applied;

		ce.selections = Lists.newArrayList(proposalSelection);
	}

	protected Query _q(IMethodName ctx, ITypeName type, IMethodName... calls) {
		Query q = new Query();
		q.setType(type);
		q.setClassContext(Names.getUnknownType());
		q.setMethodContext(ctx);
		q.setDefinition(DefinitionSites.createDefinitionByConstant());
		for (IMethodName i : calls) {
			q.addCallSite(CallSites.createReceiverCallSite(i));
		}
		return q;
	}

	protected IMethodName m(int typeNum, int methodNum) {
		String name = String.format("[T,P] [%s].m%d()", t(typeNum).getIdentifier(), methodNum);
		return Names.newMethod(name);
	}

	protected ITypeName t(int typeNum) {
		String name = String.format("T%d,P", typeNum);
		return Names.newType(name);
	}

	/* utils for ssts */

	protected MethodDeclaration newMethodDeclaration(int typeNum, int methodNum, IStatement... stmtArr) {
		MethodDeclaration md = new MethodDeclaration();
		md.setName(m(typeNum, methodNum));
		md.setEntryPoint(true);
		md.setBody(Lists.newArrayList(stmtArr));
		return md;
	}

	protected static IExpressionStatement stmt(IAssignableExpression compl) {
		ExpressionStatement stmt = new ExpressionStatement();
		stmt.setExpression(compl);
		return stmt;
	}

	protected static IExpressionStatement complStmt(String id) {
		ExpressionStatement exprStmt = new ExpressionStatement();
		exprStmt.setExpression(compl(id));
		return exprStmt;
	}

	protected static ICompletionExpression compl(String id) {
		CompletionExpression expr = new CompletionExpression();
		expr.setObjectReference(ref(id));
		return expr;
	}

	protected static IExpressionStatement invStmt(String id, IMethodName m) {
		ExpressionStatement exprStmt = new ExpressionStatement();
		exprStmt.setExpression(inv(id, m));
		return exprStmt;
	}

	protected static IInvocationExpression inv(String id, IMethodName m) {
		InvocationExpression expr = new InvocationExpression();
		expr.setReference(ref(id));
		expr.setMethodName(m);
		return expr;
	}

	protected static IVariableReference ref(String id) {
		VariableReference ref = new VariableReference();
		ref.setIdentifier(id);
		return ref;
	}

	protected static IVariableDeclaration varDecl(ITypeName type, String id) {
		VariableDeclaration decl = new VariableDeclaration();
		decl.setReference(ref(id));
		decl.setType(type);
		return decl;
	}
}