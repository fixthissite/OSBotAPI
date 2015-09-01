package lemons.api.walking.map;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;

import lemons.api.script.TaskScript;
import lemons.api.script.emulators.TaskScriptEmulator;
import lemons.api.walking.GlobalPath;
import lemons.api.walking.LocalPath;

import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.input.mouse.MiniMapTileDestination;

public class Tile extends TaskScriptEmulator<TaskScript> {
	
	public final int x, y, z;

	public final String uid;

	private Color lastColor;

	private Polygon p2;

	private int x2;

	private int y2;

	private int xc;

	private LocalPath path;

	private Point tmpp;

	private Rectangle bounds;

	private Position tpos;
	
	public String toString() {
		return x+","+y+","+z;
	}

	public Tile(TaskScript s, int i, int j, int k) {
		super(s);
		x = i;
		y = j;
		z = k;
		tpos = new Position(x, y, z);
		uid = getWalker().getRegion().getId(this);
	}

	public boolean isOnMap() {
		return dist() < 15;
		//return pos().isOnMiniMap(getBot());
	}

	public float distanceTo(Tile t) {
		return dist(t);
	}
	public float distanceTo(Position p) {
		return dist(p);
	}
	public float distanceTo(Entity n) {
		return dist(n);
	}
	public float distanceTo() {
		return dist();
	}
	public float dist(Tile t) {
		return dist(t.pos());
	}

	public float dist(Position p) {
		if (p.getZ() != z)
			return Float.MAX_VALUE;
		return tpos.distance(p);
	}

	public float dist(Entity n) {
		Position pos = n.getPosition();
		if (pos.getZ() != z)
			return Float.MAX_VALUE;
		return tpos.distance(pos);
	}

	public float dist() {
		return dist(myPosition());
	}

	public Position pos() {
		return tpos;
	}

	public Point getPointOnMap() {
		try {
			bounds = (new MiniMapTileDestination(getBot(), pos())).getArea().getBounds();
			return new Point(bounds.x + (bounds.width / 2), bounds.y + (bounds.height / 2));
		} catch (Exception e) {
			return new Point(-1, -1);
		}
	}
	
	public Polygon getPolygon() {
		return tpos.getPolygon(getBot());
	}
	
	public void draw(Graphics2D g) {
		draw(g, Color.cyan);
	}
	
	public void draw(Graphics2D g, Color color) {
		draw(g, color, new Color(color.getRed(), color.getGreen(), color.getBlue(),
				(int) (color.getAlpha() * 0.4)));
	}
	
	public void draw(Graphics2D g, Color border, Color fill) {
		if (!isVisible())
			return;
		lastColor = g.getColor();
		g.setColor(border);
		if (border != null)
			g.drawPolygon(tpos.getPolygon(getClient().getBot()));
		g.setColor(fill);
		if (fill != null)
			g.fillPolygon(tpos.getPolygon(getClient().getBot()));
		g.setColor(lastColor);
	}

	
	public void draw(Graphics2D g, Color border, int borderOpac, Color fill, int fillOpac) {
		draw(g, new Color(border.getRed(), border.getGreen(), border.getBlue(), borderOpac),
				new Color(fill.getRed(), fill.getGreen(), fill.getBlue(), fillOpac));
	}

	public Point getPointOnScreen() {
		x2 = 0;
		y2 = 0;
		xc = 0;
		p2 = tpos.getPolygon(getClient().getBot());
		for (int a = 0; a < p2.xpoints.length; a++) {
			if (p2.xpoints[a] == 0 && p2.ypoints[a] == 0)
				continue;
			xc++;
			x2 += p2.xpoints[a];
			y2 += p2.ypoints[a];
		}
		if (xc == 0)
			return new Point(1000,1000);
		return new Point(x2 / xc, y2 / xc);
	}

	public boolean canReach(boolean failOnObstacles) {
		return getWalker().canReach(this, failOnObstacles);
	}

	public Point toPoint() {
		return new Point(x, y);
	}

	public int actualDistanceTo() {
		return actualDistanceTo(myLocation());
	}

	public int actualDistanceTo(Position p) {
		return actualDistanceTo(getWalker().tile(p));
	}

	public int actualDistanceTo(Entity e) {
		return actualDistanceTo(getWalker().tile(e));
	}

	public int actualDistanceTo(Tile t) {
		if (this.compare(t))
			return 0;
		if (t.z != z)
			return 9002;
		if (!getWalker().getRegion().contains(t))
			return 9000;
		
		path = new LocalPath(getScript(), this, getWalker().getRegion().findEnd(t), false, true, false);
		
		return path.valid() ? path.length() : 9001;
	}

	public boolean compare(Tile loc) {
		if (loc == null) return false;
		return compare(loc.getX(), loc.getY(), loc.getZ());
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	public boolean compare(Position position) {
		if (position == null) return false;
		return compare(getWalker().tile(position));
	}

	public boolean isVisible() {
		return tpos.isVisible(getBot());
	}

	public boolean compare(Entity e) {
		if (e == null) return false;
		return compare(getWalker().tile(e));
	}
	
	public Tile translate(int a, int b) {
		return getWalker().tile(x + a, y + b, z);
	}
	
	public Tile translate(int a, int b, int c) {
		return getWalker().tile(x + a, y + b, z + c);
	}
	
	public String getId() {
		return uid;
	}

	public void drawMap(Graphics2D g, Color color) {
		if (!isOnMap())
			return;
		
		lastColor = g.getColor();
		g.setColor(color);
		tmpp = getPointOnMap();
		
		g.fillRect(tmpp.x - 2, tmpp.y - 2, 4, 4);
		g.setColor(lastColor);
	}

	public void drawBoth(Graphics2D g, Color colour, Color color) {
		draw(g, colour, color);
		drawMap(g, color);
	}

	public boolean within(int x1, int y1, int x2, int y2) {
		if (x >= Math.min(x1, x2) && y >= Math.min(y1, y2)
				&& x <= Math.max(x1, x2) && y <= Math.max(y1, y2)) {
			return true;
		}
		return false;
	}
	
	public boolean within(Tile t1, Tile t2) {
		return within(t1.x, t1.y, t2.x, t2.y);
	}

	public boolean canWalk() {
		return (getWalker().getRegion().getRawFlag(this) & RawFlags.BLOCKED) == 0;
	}

	public boolean compare(int i, int j, int k) {
		return this.getX() == i && this.getY() == j && this.getZ() == k;
	}

	public Boolean isOnScreen() {
		return getPointOnScreen().x < 517 && getPointOnScreen().x > 3
				&& getPointOnScreen().y < 338 && getPointOnScreen().y > 3;
	}
	
	public double preciseDistanceTo() {
		return preciseDistanceTo(myLocation());
	}

	public double preciseDistanceTo(Tile n) {
		return Math.sqrt(((x - n.x) * (x - n.x))
				+ ((y - n.y) * (y - n.y)));
	}
	
	public boolean hover() {
		return tpos.hover(getBot());
	}

	public boolean isBlocked() {
		return (getWalker().getRegion().getFlag(this) & Flags.FLAG_BLOCKED) != 0;
	}

	public Zone getZone(int radius) {
		return new Zone(getScript(), new Area(x - radius, y - radius, x + radius, y + radius));
	}

	public int distanceToGlobal() {
		GlobalPath p = new GlobalPath(getScript(), myLocation(), this);
		return !p.valid() ? Integer.MAX_VALUE : p.distance();
	}
	
	public boolean interact() {
		return interact(this, null);
	}
	
	public boolean interact(String action) {
		return interact(this, action);
	}

}
