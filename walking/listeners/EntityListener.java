package lemons.api.walking.listeners;

import lemons.api.script.TaskScript;
import lemons.api.script.interaction.Interact;
import lemons.api.walking.map.Tile;

import org.osbot.rs07.api.model.Entity;

public class EntityListener extends WalkListener<Boolean, Tile> {

	private Entity entity;

	public EntityListener(TaskScript s, Entity t) {
		super(s, s.getWalker().findEntityEnd(t));
		entity = t;
		end = getWalker().tile(entity);
	}

	@Override
	public Boolean walkWhile(Tile tile) {
		if (!entity.exists() || end.dist(entity) > 1)
			return false;
		
		if (tile.dist(end) > Interact.RADIUS) {
			return pathWalk(tile);
		}
		return !canInteract(entity);
	}

}
