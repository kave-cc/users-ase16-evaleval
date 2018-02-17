package exec.demographics;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Test;

import cc.kave.commons.utils.LocaleUtils;

public class DemographicsTest {

	private Demographics sut;

	@Before
	public void setup() {
		LocaleUtils.setDefaultLocale();

		Demographic demographic1 = new Demographic();
		demographic1.user = "user1";
		demographic1.position = Positions.ResearcherAcademic;
		demographic1.firstEventDate = LocalDate.of(1942, 5, 23);
		demographic1.lastEventDate = LocalDate.of(2016, 4, 28);
		demographic1.numberOfCompletionEvents = 5;
		demographic1.numberOfGenQueries = 6;
		demographic1.totalNumberOfGenQueries = 100;
		demographic1.numberOfParticipationDays = 2;
		demographic1.numberOfParticipationMonths = 1;

		Demographic demographic2 = new Demographic();
		demographic2.user = "user2";
		demographic2.position = Positions.SoftwareEngineer;
		demographic2.firstEventDate = LocalDate.of(2012, 1, 4);
		demographic2.lastEventDate = LocalDate.of(2013, 3, 31);
		demographic2.numberOfCompletionEvents = 99;
		demographic2.numberOfGenQueries = 4;
		demographic2.totalNumberOfGenQueries = Integer.MAX_VALUE;
		demographic2.numberOfParticipationDays = 12;
		demographic2.numberOfParticipationMonths = 3;

		sut = new Demographics();
		sut.add(demographic1);
		sut.add(demographic2);
	}

	@Test
	public void toCSV() {

		String csv = sut.toCSV();

		assertThat(csv,
				is("user, position, first, last, days, months, events, queries, contribution\n"
						+ "user1, ResearcherAcademic, 1942-05-23, 2016-04-28, 2, 1, 5, 6, 0.06000\n"
						+ "user2, SoftwareEngineer, 2012-01-04, 2013-03-31, 12, 3, 99, 4, 0.00000\n"));
	}

	@Test
	public void printRatios() {
		sut.printRatios();
	}
}
