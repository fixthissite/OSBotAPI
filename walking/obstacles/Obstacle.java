package lemons.api.walking.obstacles;

import lemons.api.script.TaskScript;
import lemons.api.script.emulators.TaskScriptEmulator;
import lemons.api.script.entities.RS2Object;

public abstract class Obstacle extends TaskScriptEmulator<TaskScript> {
	
	public TaskScript script;
	
	public Obstacle(TaskScript s) {
		super(s);
	}
	
	public abstract boolean isActive(RS2Object o, boolean solving);
	
	public abstract boolean solve(RS2Object o);
	
	public abstract String[] getObjectNames();
	
}
