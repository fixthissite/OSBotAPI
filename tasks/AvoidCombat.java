package lemons.api.tasks;

import lemons.api.tasks.templates.AbstractTask;

import org.osbot.rs07.api.map.Position;

public class AvoidCombat extends AbstractTask {

	public final Position[] tiles;
	
	public AvoidCombat(Position... tiles) {
		this.tiles = tiles;
	}
	
	@Override
	public void run() {
		Position tile = null;
		for (Position t : tiles) {
			if (tile == null || tile(t).dist() < tile(tile).dist())
				tile = t;
		}
		
		// Use their walking, its faster atm
		getWalker().findPath(tile).walk();
		sleep(500, 2000);
	}

	@Override
	public boolean isActive() {
		return !getScript().myPlayer().isMoving() && getScript().myPlayer().isUnderAttack();
	}

}
