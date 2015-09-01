package lemons.api.tasks.banking;

import lemons.api.walking.Walker;
import lemons.api.walking.map.Tile;

import org.osbot.rs07.api.map.Position;

public enum BankLocation {
	LUMBRIDGE(new Position(3208, 3220, 2)),
	DRAYNOR(new Position(3094, 3243, 0)),
	GRAND_EXCHANGE(new Position(3167, 3489, 0)),
	CATHERBY(new Position(2809, 3441, 0)),
	ARDOUGNE_NORTH(new Position(2617, 3334, 0)),
	ARDOUGNE_EAST(new Position(2653, 3283, 0)),
	FALADOR_EAST(new Position(2947, 3368, 0)),
	FALADOR_WEST(new Position(3013, 3355, 0)),
	PHASMATYS(new Position(3688, 3467, 0));
	
	private Position p;

	private BankLocation(Position p) {
		this.p = p;
	}
	
	public Tile getTile(Walker w) {
		return w.tile(p);
	}
}
