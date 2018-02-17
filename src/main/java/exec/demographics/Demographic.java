package exec.demographics;

import java.time.LocalDate;

public class Demographic {
	public String user;
	public Positions position;
	public LocalDate firstEventDate;
	public LocalDate lastEventDate;
	public int numberOfCompletionEvents;
	public int numberOfGenQueries;
	public int totalNumberOfGenQueries;
	public int numberOfParticipationDays;
	public int numberOfParticipationMonths;

	public double getPercentageOfTotalQueries() {
		return numberOfGenQueries / (double) totalNumberOfGenQueries;
	}
}
