package lemons.api.script.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import lemons.api.script.TaskScript;
import lemons.api.script.emulators.TaskScriptEmulator;
import lemons.api.utils.Timer;

public abstract class EntityAPI<T extends Entity> extends TaskScriptEmulator<TaskScript> {

	private static final long REFRESH_RATE = 100;
	private static final boolean DEFAULT_DISTANCE_REAL = false;
	private Timer timer;
	private ArrayList<T> cache = new ArrayList<T>();
	private boolean forceReset = false;

	public EntityAPI(TaskScript s) {
		super(s);
	}
	
	// Lambdas? fun!
	
	public T closest(Predicate<T> p) {
		return closest(DEFAULT_DISTANCE_REAL, filter(p));
	}
	
	public T closest(boolean realDistance, Predicate<T> p) {
		return closest(realDistance, filter(p));
	}
	
	public List<T> filter(Predicate<T> p) {
		return filter(getAll(), p);
	}

	public List<T> filter(Collection<T> objects, Predicate<T> p) {
		ArrayList<T> objs = new ArrayList<T>();
		for (T o : objects) {
			if (p.test(o))
				objs.add(o);
		}
		return objs;
	}
	
	// End the lambda fun

	public T closest(boolean realDistance, Collection<T> objects) {
		if (objects == null || objects.isEmpty())
			return null;
		
		T f = null;
		double lastDist = Double.MAX_VALUE;
		for (T o : objects) {
			double dist = !realDistance ? tile(o).preciseDistanceTo() : tile(o).actualDistanceTo();
			if (dist < lastDist) {
				lastDist = dist;
				f = o;
			}
		}
		return f;
	}

	public T closest(boolean realDistance, int... ids) {
		return closest(realDistance, o -> o.hasId(ids));
	}

	public T closest(boolean realDistance, String... names) {
		return closest(realDistance, o -> o.hasName(names));
	}
	
	public T closest(Collection<T> objects) {
		return closest(DEFAULT_DISTANCE_REAL, objects);
	}

	public T closest(int... ids) {
		return closest(DEFAULT_DISTANCE_REAL, ids);
	}

	public T closest(String... names) {
		return closest(DEFAULT_DISTANCE_REAL, names);
	}

	public List<T> getAll() {
		if (timer != null && timer.isRunning() && !forceReset)
			return cache;
		
		// Caching we can control :)
		if (timer != null)
			timer.reset();
		else
			timer = new Timer(REFRESH_RATE);
		
		ArrayList<T> c = new ArrayList<T>();
		forceReset = false;
		
		for (T o : getUncached()) {
			// Possibly purge junk?
			if (o == null || o.getName() == null)
				continue;
			c.add(o);
		}
		
		Collections.unmodifiableList(c);
		cache = c;
		
		return cache;
	}
	
	public abstract List<T> getUncached();
	
	public EntityAPI<T> purge() {
		forceReset = true;
		return this;
	}
}
