package lemons.api.tasks;

import java.util.function.Predicate;

import lemons.api.script.entities.RS2Object;
import lemons.api.tasks.templates.AbstractTask;
import lemons.api.tasks.templates.ComplexTask;
import lemons.api.tasks.templates.ConditionalTask;
import lemons.api.walking.listeners.BasicListener;

import org.osbot.rs07.api.model.Entity;

public class ObjectInteractionTask extends ComplexTask {

	private WalkingTask walking;
	private Predicate<RS2Object> pred;
	private String action;

	public ObjectInteractionTask(WalkingTask w, String action, Predicate<RS2Object> p) {
		super(); 
		
		this.action  = action;
		walking = w;
		this.pred = p;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		addTask(new AbstractTask() {
			
			@Override
			public void run() {
				Entity o = getObject();
				if (interact(o, action)) {
					// Wait for movement
					getWalker().wait(tile(o), new BasicListener(getScript(), tile(o)));
					for (int i = 0; i < 30 && myPlayer().isMoving(); i++)
						sleep(100);
				}
			}
			
			@Override
			public boolean isActive() {
				Entity o = getObject();
				return o != null && o.exists();
			}
		});
		addTask(() -> {
			Entity o = getObject();
			return o == null || !o.exists();
		}, walking);
	}
	
	private Entity getObject() {
		Entity o = getObjects().closest(pred);
		if (o != null && o.exists())
			return o;
		return null;
	}
}
