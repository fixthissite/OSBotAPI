package lemons.api.walking.listeners;

import lemons.api.script.TaskScript;
import lemons.api.script.emulators.TaskScriptEmulator;
import lemons.api.walking.map.Tile;

public abstract class WalkListener<G, F> extends TaskScriptEmulator<TaskScript> {
	
	protected Tile last, end;
	private int rand;

	public WalkListener(TaskScript s, Tile end) {
		super(s);
		this.end = end;
		setScript(s);
	}
	
	public abstract G walkWhile(F tile);
	
	protected boolean pathWalk(Tile t) {
		if (last == null || !last.compare(t)) {
			last = t;
			rand = random(5, 10);
		}
		return t.dist() > rand;
	}
	
}
