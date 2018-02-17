package exec.demographics;

import java.util.Set;

import cc.kave.commons.model.events.completionevents.ICompletionEvent;

public interface IDemographicsIO {
	Set<String> findUsers();
	Positions getPosition(String user);
	int getNumQueries(String user);

	Set<ICompletionEvent> readEvents(String user);
}
