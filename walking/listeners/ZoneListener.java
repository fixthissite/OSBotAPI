package lemons.api.walking.listeners;

import lemons.api.script.TaskScript;
import lemons.api.script.interaction.Interact;
import lemons.api.walking.map.AbstractZone;
import lemons.api.walking.map.Tile;

public class ZoneListener extends WalkListener<Boolean, Tile> {

	private AbstractZone zone;

	public ZoneListener(TaskScript s, AbstractZone zone) {
		super(s, null);
		this.zone = zone;
		end = zone.getCenterTile();
	}

	@Override
	public Boolean walkWhile(Tile tile) {
		if (tile == null)
			tile = end;
		
		if (zone.contains(myPosition())) {
			return false;
		}
		if (tile == null)
			tile = end;
		if (tile.dist(end) > Interact.RADIUS) {
			return pathWalk(tile);
		}
		return true;
	}

}
