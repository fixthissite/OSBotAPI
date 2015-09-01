package lemons.api.script.entities;

import org.osbot.rs07.accessor.XNPC;
import org.osbot.rs07.api.def.NPCDefinition;

import lemons.api.script.TaskScript;

public class NPC extends Character<XNPC> {

	private org.osbot.rs07.api.model.NPC npc;

	public NPC(TaskScript s, org.osbot.rs07.api.model.NPC e) {
		super(s, e);
		npc = e;
	}

	public int getLevel() {
		return npc.getLevel();
	}
	
	@Override
	public NPCDefinition getDefinition() {
		return npc.getDefinition();
	}
	
}
