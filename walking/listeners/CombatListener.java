package lemons.api.walking.listeners;

import lemons.api.script.TaskScript;
import lemons.api.script.entities.NPC;
import lemons.api.script.interaction.Interact;
import lemons.api.walking.map.Tile;

public class CombatListener extends WalkListener<Boolean, Tile> {

	private NPC npc;

	public CombatListener(TaskScript s, NPC c) {
		super(s, null);
		end = tile(c);
		npc = c;
	}
	
	@Override
	public Boolean walkWhile(Tile tile) {
		if (!npc.exists() || !npc.isAttackable() || end.dist(npc) > 1)
			return false;
		
		if (tile.dist(end) > Interact.RADIUS) {
			return pathWalk(tile);
		}
		return !canInteract(npc);
	}

}
