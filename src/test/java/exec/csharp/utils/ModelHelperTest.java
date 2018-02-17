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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

import cc.kave.commons.model.naming.types.ITypeName;
import cc.kave.commons.utils.io.NestedZipFolders;
import cc.kave.rsse.calls.ICallsRecommender;
import cc.kave.rsse.calls.mining.NoCallRecommender;
import cc.kave.rsse.calls.pbn.PBNMiner;
import cc.kave.rsse.calls.usages.Query;
import cc.kave.rsse.calls.usages.Usage;

public class ModelHelperTest {

	@Mock
	private PBNMiner miner;
	@Mock
	private StorageHelper storageHelper;
	@Mock
	private NestedZipFolders<ITypeName> folder;

	@Mock
	private ITypeName typeWithoutUsages;
	@Mock
	private ITypeName typeWithEmptyList;
	@Mock
	private ITypeName regularType;
	@Mock
	private ITypeName typeWithManyUsages;
	@Mock
	private ICallsRecommender<Query> recommender;

	@Captor
	private ArgumentCaptor<List<Usage>> usageCaptor;

	private ModelHelper sut;
	private List<Query> usages;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);

		when(storageHelper.getNestedZipFolder(any(StorageCase.class))).thenReturn(folder);

		when(folder.hasZips(typeWithoutUsages)).thenReturn(false);
		when(folder.hasZips(typeWithEmptyList)).thenReturn(true);
		when(folder.hasZips(regularType)).thenReturn(true);
		when(folder.hasZips(typeWithManyUsages)).thenReturn(true);

		when(folder.readAllZips(typeWithEmptyList, Query.class)).thenReturn(Lists.newArrayList());
		Query q = mock(Query.class);
		usages = Lists.newArrayList(q);
		when(folder.readAllZips(regularType, Query.class)).thenReturn(usages);
		when(folder.readAllZips(typeWithManyUsages, Query.class))
				.thenReturn(manyUsages(ModelHelper.MAX_NUM_USAGES + 1));

		when(miner.createRecommender(anyListOf(Usage.class))).thenReturn(recommender);

		sut = new ModelHelper(miner, storageHelper);
	}

	private List<Query> manyUsages(int num) {
		List<Query> qs = Lists.newLinkedList();
		for (int i = 0; i < num; i++) {
			qs.add(mock(Query.class));
		}
		return qs;
	}

	@Test
	public void typeWithoutUsages() {
		ICallsRecommender<Query> actual = sut.get(typeWithoutUsages);
		assertTrue(actual instanceof NoCallRecommender);
	}

	@Test
	public void typeWithEmptyList() {
		ICallsRecommender<Query> actual = sut.get(typeWithEmptyList);
		assertTrue(actual instanceof NoCallRecommender);
	}

	@Test
	public void regularType() {
		ICallsRecommender<Query> actual = sut.get(regularType);
		assertSame(recommender, actual);
	}

	@Test
	public void typeWithManyUsages() {
		when(miner.createRecommender(usageCaptor.capture())).thenReturn(recommender);
		ICallsRecommender<Query> actual = sut.get(typeWithManyUsages);
		assertSame(recommender, actual);

		List<Usage> actualUsages = usageCaptor.getValue();
		assertEquals(ModelHelper.MAX_NUM_USAGES, actualUsages.size());
	}
}