package lemons.api.walking.listeners;

import lemons.api.script.TaskScript;
import lemons.api.walking.map.Tile;

public class BasicListener extends WalkListener<Boolean, Tile> {

	public BasicListener(TaskScript s, Tile end) {
		super(s, end);
	}

	@Override
	public Boolean walkWhile(Tile tile) {
		if (!tile.compare(end)) {
			return pathWalk(tile);
		}
		boolean cont = false;
		for (int x = -2; x <= 2; x++) {
			for (int y = -2; y <= 2; y++) {
				if (myLocation().compare(end.translate(x, y)))
					return false;
				if (!tile.translate(x, y).isBlocked())
					cont = true;
			}
		}
		return cont;
	}

}
