package lemons.api.tasks.woodcutting;

import lemons.api.script.entities.GroundItem;
import lemons.api.tasks.templates.AbstractTask;

public class BirdsNestTask extends AbstractTask {

	private GroundItem nest;

	@Override
	public boolean isActive() {
		if (nest != null && nest.exists()) {
			return true;
		}
		for (GroundItem g : getGroundItems().getAll()) {
			if (g.getName().toLowerCase().contains("nest") && g.getName().toLowerCase().contains("bird")
					&& tile(g).canReach(true)) {
				nest = g;
				return true;
			}
		}
		return false;
	}

	@Override
	public void run() {
		if (tile(nest).dist() > 5) {
			getWalker().findLocalPath(nest).walk();
		} else {
			if (nest.interact("Take")) {
				nest = null;
			}
		}
	}

}
