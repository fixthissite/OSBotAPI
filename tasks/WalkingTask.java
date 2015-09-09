package lemons.api.tasks;

import lemons.api.tasks.templates.AbstractTask;
import lemons.api.walking.GlobalPath;
import lemons.api.walking.listeners.WalkListener;
import lemons.api.walking.listeners.ZoneListener;
import lemons.api.walking.map.AbstractZone;
import lemons.api.walking.map.Tile;
import lemons.api.walking.map.Zone;

import org.osbot.rs07.api.map.Position;

public class WalkingTask extends AbstractTask {

	private AbstractZone zone;
	private Tile tile;

	private GlobalPath path;

	private WalkListener<Boolean, Tile> listener;
	private int radius;
	private Position position;
	
	public void setZone(AbstractZone a) {
		zone = a;
		if (getScript() != null && this.listener == null)
			this.listener = new ZoneListener(getScript(), zone);
	}
	
	public AbstractZone getZone() {
		return zone;
	}

	public WalkingTask(AbstractZone a, WalkListener<Boolean, Tile> listener) {
		this.zone = a;
		this.listener = listener;
	}

	public WalkingTask(AbstractZone a) {
		this.zone = a;
	}
	

	public WalkingTask(Tile tile, int r) {
		this.tile = tile;
		radius = r;
	}
	

	public WalkingTask(Position tile, int r) {
		this.position = tile;
		radius = r;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		if (this.position != null)
			this.tile = tile(this.position);
		if (this.zone == null && tile != null)
			this.zone = tile.getZone(radius);
		if (this.listener == null && zone != null)
			this.listener = new ZoneListener(getScript(), zone);
		
		if (this.zone != null)
			setZone(zone);
	}

	@Override
	public void onTaskStart() {
		super.onTaskStart();
		path = null;
	}
	
	private GlobalPath path() {
		if (path == null || !path.valid()) {
			path = getWalker().findPath(zone.getRandomTile());
		}
		path.setListener(listener);
		
		return path;
	}
	
	@Override
	public void run() {
		path().walk();
	}

	@Override
	public boolean isActive() {
		if (listener != null && listener.walkWhile(null)) {
			return true;
		}
		return false;
	}

}
