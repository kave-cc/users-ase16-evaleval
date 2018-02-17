package exec.demographics;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class UserDemographicTest {
	@Test
	public void calculatesPercentageOfTotalQueries() {
		Demographic demographic = new Demographic();
		demographic.numberOfGenQueries = 5;
		demographic.totalNumberOfGenQueries = 25;
		assertThat(demographic.getPercentageOfTotalQueries(), is(0.2));
	}
}
