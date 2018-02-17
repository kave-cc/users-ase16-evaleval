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
package exec.demographics;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;

import cc.kave.commons.model.events.completionevents.ICompletionEvent;
import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.utils.io.NestedZipFolders;
import exec.validate_evaluation.microcommits.MicroCommit;
import exec.validate_evaluation.microcommits.MicroCommitIo;
import exec.validate_evaluation.streaks.EditStreakGenerationIo;

public class DemographicsIO implements IDemographicsIO {

	private Map<String, Positions> positions = Maps.newHashMap();
	private MicroCommitIo mcIO;
	private EditStreakGenerationIo esIo;
	private NestedZipFolders<ITypeName> usages;

	public DemographicsIO(MicroCommitIo mcIO, EditStreakGenerationIo esIo, NestedZipFolders<ITypeName> usages) {
		this.mcIO = mcIO;
		this.esIo = esIo;
		this.usages = usages;

		positions.put("KaVE/2016-05-09/0.zip", Positions.ResearcherAcademic);
		positions.put("KaVE/2016-05-09/10.zip", Positions.SoftwareEngineer);
		positions.put("KaVE/2016-05-10/0.zip", Positions.HobbyProgrammer);
		positions.put("KaVE/2016-05-11/0.zip", Positions.SoftwareEngineer);
		positions.put("KaVE/2016-05-11/13.zip", Positions.SoftwareEngineer);
		positions.put("KaVE/2016-05-11/22.zip", Positions.SoftwareEngineer);
		positions.put("KaVE/2016-05-12/0.zip", Positions.Student);
		positions.put("KaVE/2016-05-13/0.zip", Positions.SoftwareEngineer);
		positions.put("KaVE/2016-05-13/100.zip", Positions.ResearcherAcademic);
		positions.put("KaVE/2016-05-17/0.zip", Positions.HobbyProgrammer);
		positions.put("KaVE/2016-05-18/0.zip", Positions.Unknown);
		positions.put("KaVE/2016-05-18/10.zip", Positions.SoftwareEngineer);
		positions.put("KaVE/2016-05-19/0.zip", Positions.HobbyProgrammer);
		positions.put("KaVE/2016-05-24/0.zip", Positions.Unknown);
		positions.put("KaVE/2016-05-31/0.zip", Positions.SoftwareEngineer);
		positions.put("KaVE/2016-06-04/22.zip", Positions.Unknown);
		positions.put("KaVE/2016-06-06/0.zip", Positions.SoftwareEngineer);
		positions.put("KaVE/2016-06-07/0.zip", Positions.Unknown);
		positions.put("KaVE/2016-06-08/0.zip", Positions.SoftwareEngineer);
		positions.put("KaVE/2016-06-08/10.zip", Positions.SoftwareEngineer);
		positions.put("KaVE/2016-06-10/10.zip", Positions.Unknown);
		positions.put("KaVE/2016-06-14/0.zip", Positions.SoftwareEngineer);
		positions.put("KaVE/2016-06-14/10.zip", Positions.Unknown);
		positions.put("KaVE/2016-06-16/0.zip", Positions.Unknown);
		positions.put("KaVE/2016-06-16/47.zip", Positions.SoftwareEngineer);
		positions.put("KaVE/2016-06-20/0.zip", Positions.SoftwareEngineer);
		positions.put("KaVE/earlier/data/0000-0099/79.zip", Positions.Unknown);
		positions.put("KaVE/earlier/data/0000-0099/95.zip", Positions.Unknown);
		positions.put("KaVE/earlier/data/1000-1099/1020.zip", Positions.Student);
		positions.put("KaVE/earlier/data/1000-1099/1022.zip", Positions.SoftwareEngineer);
		positions.put("KaVE/earlier/data/1100-1199/1161.zip", Positions.SoftwareEngineer);
		positions.put("KaVE/earlier/data/1100-1199/1179.zip", Positions.Unknown);
		positions.put("KaVE/earlier/data/1200-1299/1271.zip", Positions.HobbyProgrammer);
	}

	@Override
	public Set<String> findUsers() {
		return mcIO.findZips();
	}

	@Override
	public Positions getPosition(String user) {
		if (positions.containsKey(user)) {
			return positions.get(user);
		}
		return Positions.Unknown;
	}

	@Override
	public int getNumQueries(String user) {
		int num = 0;
		List<MicroCommit> mcs = mcIO.read(user);
		for (MicroCommit mc : mcs) {
			if (shouldCount(mc)) {
				num++;
			}
		}

		return num;
	}

	private boolean shouldCount(MicroCommit mc) {
		ITypeName type = mc.getType();
		return usages.hasZips(type);
	}

	@Override
	public Set<ICompletionEvent> readEvents(String user) {
		return esIo.readCompletionEvents(user);
	}
}