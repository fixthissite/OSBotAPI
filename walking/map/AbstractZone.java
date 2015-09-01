package lemons.api.walking.map;

import java.awt.Rectangle;
import java.util.ArrayList;

import lemons.api.script.TaskScript;
import lemons.api.script.emulators.TaskScriptEmulator;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Entity;

/**
 * A zone is an advanced Area. It attempts to emulate an area
 * @author person
 *
 */
public abstract class AbstractZone extends TaskScriptEmulator<TaskScript> {
	
	private ArrayList<Tile> tiles = new ArrayList<Tile>();
	
	private int bx1 = Integer.MAX_VALUE,
			bx2 = Integer.MIN_VALUE,
			by1 = Integer.MAX_VALUE,
			by2 = Integer.MIN_VALUE;

	private Rectangle rect;

	private boolean everywhere = false;
	
	public AbstractZone(TaskScript s) {
		super(s);
	}
	
	public AbstractZone(TaskScript s, Area p) {
		super(s);
		for (Position x : p.getPositions()) {
			add(tile(x));
		}
	}
	
	public Area toArea() {
		ArrayList<Position> list = new ArrayList<Position>();
		
		for (Tile t : tiles)
			list.add(t.pos());
		
		return new Area(list.toArray(new Position[list.size()]));
	}
	
	public AbstractZone rect(Position corner1, Position corner2) {
		return rect(tile(corner1), tile(corner2));
	}
	
	public AbstractZone rect(Tile corner1, Tile corner2) {
		int maxX = Math.max(corner1.x, corner2.x),
			maxY = Math.max(corner1.y, corner2.y);
		
		for (int x = Math.min(corner1.x, corner2.x); x <= maxX; x++) {
			for (int y = Math.min(corner1.y, corner2.y); y <= maxY; y++) {
				add(tile(x, y, corner1.z));
			}
		}
		return this;
	}
	
	public AbstractZone add(Tile t) {
		if (!tiles.contains(t)) {
			tiles.add(t);
			if (bx1 > t.x)
				bx1 = t.x;
			if (bx2 < t.x)
				bx2 = t.x;
			if (by1 > t.y)
				by1 = t.y;
			if (by2 < t.y)
				by2 = t.y;
		}
		return this;
	}
	
	public ArrayList<Tile> getTiles() {
		return tiles;
	}
	
	public boolean contains(Entity e) {
		return contains(tile(e));
	}
	
	public boolean contains(Position p) {
		return contains(tile(p));
	}
	
	public boolean contains(Tile t) {
		return everywhere || tiles.contains(t);
	}
	
	public Tile getCenterTile() {
		return tile(getBounds().x + (getBounds().width / 2), getBounds().y + (getBounds().height / 2), tiles.get(0).z);
	}
	
	public Tile getRandomTile() {
		return tiles.get(random(0, tiles.size()));
	}
	
	public Rectangle getBounds() {
		if (rect != null && rect.x == bx1 && rect.x + rect.width == bx2
				&& rect.y == by1 && rect.y + rect.height == by2)
			return rect;
		rect = new Rectangle(bx1, bx2, bx2 - bx1, by2 - by1);
		return rect;
	}
	
	public int getPlane() {
		return tiles.get(0).z;
	}
	
	public AbstractZone everywhere() {
		everywhere = true;
		return this;
	}
}
