package lemons.api.script.entities;

import java.util.ArrayList;
import java.util.List;

import lemons.api.script.TaskScript;

public class NPCS extends EntityAPI<NPC> {

	public NPCS(TaskScript s) {
		super(s);
	}

	@Override
	public List<NPC> getUncached() {
		ArrayList<NPC> a = new ArrayList<NPC>();
		for (org.osbot.rs07.api.model.NPC n : getScript().getNpcs().getAll())
			a.add(new NPC(getScript(), n));
		return a;
	}
	
}
