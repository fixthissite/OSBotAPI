package lemons.api.script.entities;

import java.util.ArrayList;
import java.util.List;

import lemons.api.script.TaskScript;

public class GroundItems extends EntityAPI<GroundItem> {

	public GroundItems(TaskScript s) {
		super(s);
	}

	@Override
	public List<GroundItem> getUncached() {
		ArrayList<GroundItem> a = new ArrayList<GroundItem>();
		for (org.osbot.rs07.api.model.GroundItem n : getScript().getGroundItems().getAll())
			a.add(new GroundItem(getScript(), n));
		return a;
	}
	
}
