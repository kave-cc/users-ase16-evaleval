package exec.demographics;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Maps;

public class Demographics implements Iterable<Demographic> {

	private final Set<Demographic> demographics = new LinkedHashSet<>();

	public void add(Demographic demographic) {
		demographics.add(demographic);
	}

	public Set<Demographic> getDemographics() {
		return demographics;
	}

	@Override
	public Iterator<Demographic> iterator() {
		return demographics.iterator();
	}

	public int size() {
		return demographics.size();
	}

	public String toCSV() {
		StringBuilder csv = new StringBuilder();
		csv.append("user, position, first, last, days, months, events, queries, contribution");
		csv.append("\n");
		for (Demographic demographic : demographics) {
			csv.append(demographic.user).append(", ");
			csv.append(demographic.position.toString()).append(", ");
			csv.append(demographic.firstEventDate).append(", ");
			csv.append(demographic.lastEventDate).append(", ");
			csv.append(demographic.numberOfParticipationDays).append(", ");
			csv.append(demographic.numberOfParticipationMonths).append(", ");
			csv.append(demographic.numberOfCompletionEvents).append(", ");
			csv.append(demographic.numberOfGenQueries).append(", ");
			csv.append(String.format("%.5f", demographic.getPercentageOfTotalQueries())).append("\n");
		}
		return csv.toString();
	}

	public void toCSVFile(File file) throws IOException {
		FileUtils.write(file, toCSV());
	}

	public void printRatios() {
		int numTotal = 0;
		Map<Positions, Integer> counts = Maps.newHashMap();

		for (Demographic d : demographics) {
			numTotal += d.numberOfGenQueries;
			Integer i = counts.get(d.position);
			if (i == null) {
				counts.put(d.position, d.numberOfGenQueries);
			} else {
				counts.put(d.position, i + d.numberOfGenQueries);
			}
		}

		System.out.println();
		System.out.println("ratios of contibutions by status group:");
		for (Positions p : counts.keySet()) {
			int count = counts.get(p);
			double perc = count / (double) numTotal;
			System.out.printf("%s: %d (%.1f%%)\n", p, count, perc * 100);
		}
		System.out.printf("----\ntotal: %d\n", numTotal);
	}
}
