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

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import cc.kave.commons.model.events.completionevents.Context;
import cc.kave.commons.model.events.completionevents.ICompletionEvent;
import cc.kave.commons.model.events.completionevents.IProposal;
import cc.kave.commons.model.events.completionevents.TerminationState;
import cc.kave.commons.model.naming.IName;
import cc.kave.commons.model.naming.codeelements.IMethodName;
import cc.kave.commons.model.naming.types.ITypeName;

public class EditStreakGenerationRunner {

	private final EditStreakGenerationLogger log;
	private final EditStreakGenerationIo io;
	private final Set<IRemovalFilter> filters;

	private Map<ITypeName, EditStreak> editStreaks;

	public EditStreakGenerationRunner(EditStreakGenerationIo io, EditStreakGenerationLogger log) {
		this.io = io;
		this.log = log;
		filters = Sets.newLinkedHashSet();
	}

	public void add(IRemovalFilter filter) {
		filters.add(filter);
	}

	public void run() {
		log.starting(filters);

		Set<String> zips = io.findCompletionEventZips();
		log.foundZips(zips);

		for (String zip : zips) {
			log.startingZip(zip);
			editStreaks = Maps.newLinkedHashMap();

			Set<ICompletionEvent> events = io.readCompletionEvents(zip);
			log.foundEvents(events);

			for (ICompletionEvent e : events) {
				log.processingEvent(e);
				extractEdits(e);
			}

			filterRemovals();

			Set<EditStreak> streaks = Sets.newLinkedHashSet();
			streaks.addAll(editStreaks.values());
			log.endZip(streaks);
			io.storeEditStreaks(streaks, zip);
		}

		log.finish();
	}

	private void extractEdits(ICompletionEvent e) {
		ZonedDateTime date = e.getTriggeredAt();
		Context ctx = e.getContext();

		IMethodName selection = null;
		if (e.getTerminatedState() == TerminationState.Applied) {
			selection = getSelection(e);
		}
		register(date, ctx, selection);
	}

	private IMethodName getSelection(ICompletionEvent e) {
		IProposal lsp = e.getLastSelectedProposal();
		if (lsp != null) {
			IName proposalName = lsp.getName();
			boolean isMethodName = proposalName instanceof IMethodName;
			if (isMethodName) {
				return (IMethodName) proposalName;
			}
		}
		return null;
	}

	private void register(ZonedDateTime date, Context ctx, IMethodName selection) {
		ITypeName encType = ctx.getSST().getEnclosingType();
		if (!encType.isUnknown()) {
			Snapshot se = Snapshot.create(date, ctx, selection);
			getEdits(encType).add(se);
		}
	}

	private void filterRemovals() {
		log.startingRemovalFiltering();
		Iterator<Entry<ITypeName, EditStreak>> entries = editStreaks.entrySet().iterator();
		while (entries.hasNext()) {
			EditStreak streak = entries.next().getValue();
			for (IRemovalFilter filter : filters) {
				if (filter.apply(streak)) {
					entries.remove();
					log.removedEditStreak();
					break;
				}
			}
		}
	}

	private EditStreak getEdits(ITypeName type) {
		EditStreak streak = editStreaks.get(type);
		if (streak == null) {
			streak = new EditStreak();
			editStreaks.put(type, streak);
		}
		return streak;
	}

	public static interface IRemovalFilter extends Function<EditStreak, Boolean> {
		public Boolean apply(EditStreak e);
	}

	public static class EmptyOrSingleEditStreakRemovalFilter implements IRemovalFilter {
		@Override
		public Boolean apply(EditStreak e) {
			return e.isEmptyOrSingleEdit();
		}
	}
}