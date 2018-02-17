/**
 * Copyright 2016 Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package exec.validate_evaluation;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;

import cc.kave.commons.assertions.Asserts;
import cc.kave.commons.model.events.completionevents.Context;
import cc.kave.commons.model.ssts.ISST;
import cc.kave.commons.model.ssts.impl.visitor.inlining.InliningContext;
import cc.kave.commons.utils.io.IWritingArchive;
import cc.kave.commons.utils.io.ReadingArchive;
import cc.kave.commons.utils.io.WritingArchive;

public class ContextBatchInlining {

	private String inDir;
	private String outDir;
	private List<Integer> ignoreZipList;
	private List<Integer> ignoreContextList;

	public ContextBatchInlining(String inDir, String outDir) {
		this.inDir = inDir;
		this.outDir = outDir;
		this.ignoreZipList = new ArrayList<>();
		this.ignoreContextList = new ArrayList<>();
		addIgnore(195, 2405);
	}

	private void addIgnore(int i, int j) {
		this.ignoreZipList.add(i);
		this.ignoreContextList.add(j);
	}

	public void run() {
		List<String> zipFiles = findAllZips(inDir);
		log("Processing %d zips...", zipFiles.size());
		int zipIndex = 1;
		for (String zipFile : zipFiles) {
			log("Reading Zip %d/%d, %s ", zipIndex++, zipFiles.size(), zipFile);
			try (ReadingArchive ra = new ReadingArchive(new File(zipFile))) {

				String outFile = getOutName(zipFile);
				List<Context> contexts = ra.getAll(Context.class);
				List<Context> inlinedContexts = new ArrayList<>();
				int index = 1;
				log("Inlining %d Contexts: ", contexts.size());
				log("");
				for (Context context : contexts) {
					if (context != null && !isIgnored(zipIndex, index)) {
						append(" %d ", index);
						inlinedContexts.add(inline(context));
					} else if (context != null && isIgnored(zipIndex, index)) {
						inlinedContexts.add(context);
					}
					index++;
					if (index % 10 == 0) {
						append("\n");
					}
				}
				log("Writing to %s", outFile);
				try (IWritingArchive writer = new WritingArchive(new File(outFile))) {
					for (Context context : inlinedContexts) {
						writer.add(context);
					}
				}
				log("####################\n");
			}
		}
	}

	private boolean isIgnored(int zipIndex, int index) {
		for (int i = 0; i < ignoreZipList.size(); i++) {
			if (ignoreZipList.get(i).equals(Integer.valueOf(zipIndex))
					&& ignoreContextList.get(i).equals(Integer.valueOf(index))) {
				return true;
			}
		}
		return false;
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

	private List<String> findAllZips(String dir) {
		List<String> zips = Lists.newLinkedList();
		for (File f : FileUtils.listFiles(new File(dir), new String[] { "zip" }, true)) {
			zips.add(f.getAbsolutePath());
		}
		return zips;
	}

	private String getOutName(String inName) {
		Asserts.assertTrue(inName.startsWith(inDir));
		String relativeName = inName.substring(inDir.length());
		String outName = outDir + relativeName;
		return outName;
	}

	private static void log(String msg, Object... args) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd-HH:mm:ss");
		String date = LocalDateTime.now().format(formatter);
		System.out.printf("\n[%s] %s", date, String.format(msg, args));
	}

	private static void append(String msg, Object... args) {
		System.out.printf(msg, args);
	}
}