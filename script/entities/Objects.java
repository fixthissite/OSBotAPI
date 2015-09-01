package lemons.api.script.entities;

import java.util.ArrayList;
import java.util.List;

import lemons.api.script.TaskScript;

public class Objects extends EntityAPI<RS2Object> {

	public Objects(TaskScript s) {
		super(s);
	}

	@Override
	public List<RS2Object> getUncached() {
		ArrayList<RS2Object> a = new ArrayList<RS2Object>();
		for (org.osbot.rs07.api.model.RS2Object o : getScript().getObjects().getAll())
			if (o instanceof org.osbot.rs07.api.model.InteractableObject)
				a.add(new InteractableObject(getScript(), (org.osbot.rs07.api.model.InteractableObject) o));
			else if (o instanceof org.osbot.rs07.api.model.GroundDecoration)
				a.add(new GroundDecoration(getScript(), (org.osbot.rs07.api.model.GroundDecoration) o));
			else if (o instanceof org.osbot.rs07.api.model.WallObject)
				a.add(new WallObject(getScript(), (org.osbot.rs07.api.model.WallObject) o));
			else if (o instanceof org.osbot.rs07.api.model.WallDecoration)
				a.add(new WallDecoration(getScript(), (org.osbot.rs07.api.model.WallDecoration) o));
		return a;
	}
	
}
