package lemons.api.walking.listeners;

import java.awt.Rectangle;

import lemons.api.script.TaskScript;
import lemons.api.script.interaction.Interact;
import lemons.api.walking.map.Tile;

import org.osbot.rs07.api.map.Area;

public class AreaListener extends WalkListener<Boolean, Tile> {

	private Area area;

	public AreaListener(TaskScript s, Area t) {
		super(s, null);
		area = t;
		Rectangle r = t.getPolygon().getBounds();
		end = getWalker().tile(r.x + (r.width / 2),
				r.y + (r.height / 2),
				0);
	}

	@Override
	public Boolean walkWhile(Tile tile) {
		if (area.contains(myPosition())) {
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
