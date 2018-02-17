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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;

import cc.kave.commons.assertions.Asserts;
import cc.kave.commons.model.events.completionevents.CompletionEvent;
import cc.kave.commons.model.events.completionevents.Context;
import cc.kave.commons.model.events.completionevents.ICompletionEvent;
import cc.kave.commons.model.ssts.ISST;
import cc.kave.commons.model.ssts.impl.visitor.inlining.InliningContext;
import cc.kave.commons.utils.io.IWritingArchive;
import cc.kave.commons.utils.io.ReadingArchive;
import cc.kave.commons.utils.io.WritingArchive;

public class CompletionEventProcessor {

	private String dirIn;
	private String dirOut;

	public CompletionEventProcessor(String dirIn, String dirOut) {
		this.dirIn = dirIn;
		this.dirOut = dirOut;
	}

	public void run() {
		List<String> zips = findAllZips(dirIn);
		log("processing %d zips...", zips.size());
		int current = 1;
		for (String inZip : zips) {
			log("#### %d/%d ####################", current++, zips.size());
			String outZip = getOutName(inZip);
			log("in: %s", inZip);
			log("out: %s", outZip);
			process(inZip, outZip);
		}
	}

	private List<String> findAllZips(String dir) {
		List<String> zips = Lists.newLinkedList();
		for (File f : FileUtils.listFiles(new File(dir), new String[] { "zip" }, true)) {
			zips.add(f.getAbsolutePath());
		}
		return zips;
	}

	private String getOutName(String inName) {
		Asserts.assertTrue(inName.startsWith(dirIn));
		String relativeName = inName.substring(dirIn.length());
		String outName = dirOut + relativeName;
		return outName;
	}

	private void process(String inZip, String outZip) {
		log("reading... ");
		List<CompletionEvent> events = read(inZip);
		append("found %d events", events.size());
		log("");
		for (CompletionEvent event : events) {
			append(".");
			inline(event);
		}
		log("writing...");
		write(events, outZip);
		append("done");
	}

	private List<CompletionEvent> read(String inZip) {
		List<CompletionEvent> events = Lists.newLinkedList();
		try (ReadingArchive ra = new ReadingArchive(new File(inZip))) {
			while (ra.hasNext()) {
				CompletionEvent e = (CompletionEvent) ra.getNext(ICompletionEvent.class);
				events.add(e);
			}
		}
		return events;
	}

	private void write(List<CompletionEvent> outEvents, String outZip) {
		try (IWritingArchive wa = new WritingArchive(new File(outZip))) {
			for (CompletionEvent e : outEvents) {
				wa.add(e);
			}
		}
	}

	private void inline(CompletionEvent orig) {
		orig.context = inline(orig.context);
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

	private static void log(String msg, Object... args) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
		String date = LocalDateTime.now().format(formatter);
		System.out.printf("\n%s %s", date, String.format(msg, args));
	}

	private static void append(String msg, Object... args) {
		System.out.printf(msg, args);
	}
}