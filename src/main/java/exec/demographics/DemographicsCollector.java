package exec.demographics;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

import cc.kave.commons.model.events.completionevents.ICompletionEvent;

public class DemographicsCollector {

	private IDemographicsIO demographicsIO;

	public DemographicsCollector(IDemographicsIO demographicsIO) {
		this.demographicsIO = demographicsIO;
	}

	public Demographics collect() {
		Demographics demographics = new Demographics();
		int totalNumberOfGenQueries = 0;

		Set<String> users = demographicsIO.findUsers();
		System.out.printf("found %d users...\n", users.size());
		int num = 1;
		for (String user : users) {
			System.out.printf("%s\t%d: %s\n", new Date(), num++, user);
			Demographic demographic = collect(user);
			demographics.add(demographic);
			totalNumberOfGenQueries += demographic.numberOfGenQueries;
		}

		for (Demographic demographic : demographics) {
			demographic.totalNumberOfGenQueries = totalNumberOfGenQueries;
		}

		return demographics;
	}

	private Demographic collect(String user) {
		Demographic demographic = new Demographic();
		demographic.user = user;
		demographic.position = demographicsIO.getPosition(user);
		demographic.numberOfGenQueries = demographicsIO.getNumQueries(user);
		Set<ICompletionEvent> events = demographicsIO.readEvents(user);
		demographic.numberOfCompletionEvents = events.size();
		demographic.numberOfParticipationDays = countUnique(events, this::getDate);
		demographic.numberOfParticipationMonths = countUnique(events, this::getMonth);
		if (events.size() > 0) {
			demographic.firstEventDate = getDate(getFirst(events));
			demographic.lastEventDate = getDate(getLast(events));
		}
		return demographic;
	}

	private ICompletionEvent getLast(Set<ICompletionEvent> events) {
		ICompletionEvent ce = null;
		Iterator<ICompletionEvent> it = events.iterator();
		while (it.hasNext()) {
			ce = it.next();
		}
		return ce;
	}

	private ICompletionEvent getFirst(Set<ICompletionEvent> events) {
		return events.iterator().next();
	}

	private <T> int countUnique(Collection<ICompletionEvent> events, Function<ICompletionEvent, T> unit) {
		return (int) events.stream().map(unit).distinct().count();
	}

	private LocalDate getMonth(ICompletionEvent event) {
		return getDate(event).withDayOfMonth(1);
	}

	private LocalDate getDate(ICompletionEvent event) {
		return event.getTriggeredAt().toLocalDate();
	}
}
