package lemons.api.tasks.templates;

import java.awt.Graphics2D;

import lemons.api.script.TaskScript;
import lemons.api.script.emulators.TaskScriptEmulator;
import lemons.api.script.entities.Entity;
import lemons.api.walking.GlobalPath;
import lemons.api.walking.map.Tile;

import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.ui.Message;

import com.thoughtworks.xstream.io.path.Path;

public abstract class AbstractTask extends TaskScriptEmulator<TaskScript> {
	
	public abstract void run();
	
	public abstract boolean isActive();
	
	private GlobalPath path;
	
	public final void rrun() {
		if (path != null && path.valid())
			path.walk();
		else
			run();
	}
	
	public void walkTo(Entity e) {
		path = getWalker().findPath(e);
	}
	
	public void walkTo(Tile t) {
		path = getWalker().findPath(t);
	}
	
	public void walkTo(Position p) {
		path = getWalker().findPath(p);
	}

	public int getChildCount() {
		return 0;
	}
	
	public String getTaskName() {
		return getClass().getName().substring(getClass().getName().lastIndexOf(".") + 1);
	}
	
	private int depth = -1;
	
	// Custom events for Tasks
	
	public void onPaint(Graphics2D g) {
		
	}
	
	public String getStatus() {
		return getTaskName();
	}
	
	public void onTaskStart() { }
	
	public void onTaskFinish() { }
	
	public void onStart() { }
	
	public void onExit() { }
	
	public void onReset() { }
	
	// Handle events from Script
	
	public void onMessage(Message message) { }

	public void onConfig(int a, int b) { }

	public void onResponseCode(int code) { }

	public void setDepth(int i) {
		depth  = i;
	}
	
	public int getDepth() {
		return depth;
	}
	
	public String depthString() {
		String s = "|-";
		for (int i = 0; i < depth; i++)
			s += "--";
		return s;
	}
	
	//
	
}
