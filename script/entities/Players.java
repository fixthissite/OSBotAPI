package lemons.api.script.entities;

import java.util.ArrayList;
import java.util.List;

import lemons.api.script.TaskScript;

public class Players extends EntityAPI<Player> {

	public Players(TaskScript s) {
		super(s);
	}

	@Override
	public List<Player> getUncached() {
		ArrayList<Player> a = new ArrayList<Player>();
		for (org.osbot.rs07.api.model.Player n : getScript().getPlayers().getAll())
			a.add(new Player(getScript(), n));
		return a;
	}
	
}
