package exec.demographics;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;

import cc.kave.commons.model.events.completionevents.CompletionEvent;
import cc.kave.commons.model.events.completionevents.ICompletionEvent;

public class DemographicsCollectorTest {

	@Test
	public void iteratesUsers() {
		IDemographicsIO demographicsIO = mock(IDemographicsIO.class);
		when(demographicsIO.findUsers()).thenReturn(Sets.newHashSet("A", "B", "C"));

		Demographics demographics = collectDemographics(demographicsIO);

		assertThat(demographics.size(), is(3));
	}

	@Test
	public void collectsUser() {
		IDemographicsIO demographicsIO = mock(IDemographicsIO.class);
		when(demographicsIO.findUsers()).thenReturn(Sets.newHashSet("user"));

		Demographics demographics = collectDemographics(demographicsIO);

		assertThat(getSingleDemographic(demographics).user, is("user"));
	}

	@Test
	public void collectsPosition() {
		IDemographicsIO demographicsIO = mock(IDemographicsIO.class);
		when(demographicsIO.findUsers()).thenReturn(Sets.newHashSet("user"));
		when(demographicsIO.getPosition("user")).thenReturn(Positions.SoftwareEngineer);

		Demographics demographics = collectDemographics(demographicsIO);

		assertThat(getSingleDemographic(demographics).position, is(Positions.SoftwareEngineer));
	}

	@Test
	public void collectsFirstEventDate() {
		IDemographicsIO demographicsIO = mock(IDemographicsIO.class);
		when(demographicsIO.findUsers()).thenReturn(Sets.newHashSet("user"));
		ICompletionEvent firstEvent = completionEventOn(1987, 6, 20);
		when(demographicsIO.readEvents("user")).thenReturn(Sets.newLinkedHashSet(Arrays.asList(firstEvent)));

		Demographics demographics = collectDemographics(demographicsIO);

		assertThat(getSingleDemographic(demographics).firstEventDate, is(firstEvent.getTriggeredAt().toLocalDate()));
	}

	@Test
	public void collectsLastEventDate() {
		IDemographicsIO demographicsIO = mock(IDemographicsIO.class);
		when(demographicsIO.findUsers()).thenReturn(Sets.newHashSet("user"));
		CompletionEvent lastEvent = completionEventOn(2016, 4, 28);
		when(demographicsIO.readEvents("user")).thenReturn(events(someCompletionEvent(1), lastEvent));

		Demographics demographics = collectDemographics(demographicsIO);

		assertThat(getSingleDemographic(demographics).lastEventDate, is(lastEvent.TriggeredAt.toLocalDate()));
	}

	@Test
	public void collectsNumberOfCompletionEvents() {
		IDemographicsIO demographicsIO = mock(IDemographicsIO.class);
		when(demographicsIO.findUsers()).thenReturn(Sets.newHashSet("user"));
		when(demographicsIO.readEvents("user")).thenReturn(
				events(someCompletionEvent(1), someCompletionEvent(2), someCompletionEvent(3), someCompletionEvent(4)));

		Demographics demographics = collectDemographics(demographicsIO);

		assertThat(getSingleDemographic(demographics).numberOfCompletionEvents, is(4));
	}

	@Test
	public void collectsNumberOfQueries() {
		IDemographicsIO demographicsIO = mock(IDemographicsIO.class);
		when(demographicsIO.findUsers()).thenReturn(Sets.newHashSet("user"));
		when(demographicsIO.getNumQueries("user")).thenReturn(42);

		Demographics demographics = collectDemographics(demographicsIO);

		assertThat(getSingleDemographic(demographics).numberOfGenQueries, is(42));
	}

	@Test
	public void collectsTotalNumberOfQueries() {
		IDemographicsIO demographicsIO = mock(IDemographicsIO.class);
		when(demographicsIO.findUsers()).thenReturn(Sets.newHashSet("user1", "user2", "user3"));
		when(demographicsIO.getNumQueries("user1")).thenReturn(42);
		when(demographicsIO.getNumQueries("user2")).thenReturn(5);
		when(demographicsIO.getNumQueries("user3")).thenReturn(23);

		Demographics demographics = collectDemographics(demographicsIO);

		for (Demographic demographic : demographics) {
			assertThat(demographic.totalNumberOfGenQueries, is(70));
		}
	}

	@Test
	public void collectsNumberOfDays() {
		IDemographicsIO demographicsIO = mock(IDemographicsIO.class);
		when(demographicsIO.findUsers()).thenReturn(Sets.newHashSet("user"));
		when(demographicsIO.readEvents("user")).thenReturn(
				events(completionEventOn(2014, 1, 1), completionEventOn(2014, 1, 1), completionEventOn(2014, 1, 2)));

		Demographics demographics = collectDemographics(demographicsIO);

		assertThat(getSingleDemographic(demographics).numberOfParticipationDays, is(2));
	}

	private Set<ICompletionEvent> events(ICompletionEvent... eventArr) {
		LinkedHashSet<ICompletionEvent> events = Sets.newLinkedHashSet();
		for (ICompletionEvent e : eventArr) {
			events.add(e);
		}
		return events;
	}

	@Test
	public void collectsNumberOfMonths() {
		IDemographicsIO demographicsIO = mock(IDemographicsIO.class);
		when(demographicsIO.findUsers()).thenReturn(Sets.newHashSet("user"));
		when(demographicsIO.readEvents("user")).thenReturn(events(completionEventOn(2014, 1, 1),
				completionEventOn(2014, 1, 19), completionEventOn(2014, 2, 1), completionEventOn(2014, 5, 30)));

		Demographics demographics = collectDemographics(demographicsIO);

		assertThat(getSingleDemographic(demographics).numberOfParticipationMonths, is(3));
	}

	private CompletionEvent someCompletionEvent(int num) {
		CompletionEvent event = new CompletionEvent();
		event.TriggeredAt = zoned(LocalDateTime.MIN.plusSeconds(num));
		return event;
	}

	private ZonedDateTime zoned(LocalDateTime ldt) {
		return ZonedDateTime.of(ldt, ZoneId.of("ECT", ZoneId.SHORT_IDS));
	}

	private CompletionEvent completionEventOn(int year, int month, int day) {
		CompletionEvent lastEvent = someCompletionEvent(1);
		lastEvent.TriggeredAt = zoned(LocalDateTime.of(year, month, day, 11, 17));
		return lastEvent;
	}

	private Demographics collectDemographics(IDemographicsIO demographicsIO) {
		DemographicsCollector collector = new DemographicsCollector(demographicsIO);
		Demographics demographics = collector.collect();
		return demographics;
	}

	private Demographic getSingleDemographic(Demographics demographics) {
		assertThat(demographics.size(), is(1));
		return demographics.iterator().next();
	}
}
