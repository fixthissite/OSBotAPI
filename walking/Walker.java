package lemons.api.walking;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import lemons.api.script.TaskScript;
import lemons.api.script.emulators.TaskScriptEmulator;
import lemons.api.script.entities.WallObject;
import lemons.api.script.interaction.Interact;
import lemons.api.utils.IntCache;
import lemons.api.utils.Timer;
import lemons.api.walking.ai.Node;
import lemons.api.walking.ai.Pathfinder;
import lemons.api.walking.listeners.BasicListener;
import lemons.api.walking.listeners.EntityListener;
import lemons.api.walking.listeners.WalkListener;
import lemons.api.walking.map.Flags;
import lemons.api.walking.map.Region;
import lemons.api.walking.map.Tile;
import lemons.api.walking.obstacles.EnergyBarrier;
import lemons.api.walking.obstacles.LooseRailing;
import lemons.api.walking.obstacles.Obstacle;
import lemons.api.walking.obstacles.SimpleDoor;
import lemons.api.walking.obstacles.Stairs;
import lemons.api.walking.obstacles.Stile;
import lemons.api.walking.obstacles.Trapdoor;
import lemons.api.walking.obstacles.WildernessDitch;

import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.input.mouse.MiniMapTileDestination;

/**
 * Public interface to web walking.
 * 
 * @author person
 */
public class Walker extends TaskScriptEmulator<TaskScript> {
	
	public static final Rectangle VIEWPORT = new Rectangle(5, 5, 509, 332);

	public static final int INTERACT_RADIUS2 = 7, RANDOM_RADIUS = 3;

	public LocalPath lastLocalPath;

	private int runEnergyThreshold = 25, lastZLevelDiff;
	private Region region;
	private FileManager filemanager;
	private GlobalPath lastPath;
	private Timer timer;
	public int[][] tileData;

	private int flag, a, x, y, z, bx, by, minX, maxX, minY, maxY, edgeDetect = 0;

	private ArrayList<Tile> walledTiles = new ArrayList<Tile>(),
			blockedTiles = new ArrayList<Tile>(),
			obstacleTiles = new ArrayList<Tile>();

	private Tile t, t2, endEnt, start, tmpTile;

	private Node here;

	private ArrayList<Node> potentials, nodes;

	private Polygon poly;

	private Point pn, px, py, pxy;

	private Color startColor;

	private LocalPath path;

	private Tile destTile;

	private int[][] paths;

	private HashMap<Integer, HashMap<Integer, HashMap<Integer, Tile>>> tileCache
			= new HashMap<Integer, HashMap<Integer, HashMap<Integer, Tile>>>();

	private Obstacle[] localObstacles;

	private Obstacle[] globalObstacles;

	public Tile curTile, randTile;

	private boolean handleRun = true;
	
	public Walker(TaskScript s) {
		super(s);
		filemanager = new FileManager(s);
		region = new Region(s);
		
		filemanager.loadWeb();
	}
	
	public void loadObstacles() {
		localObstacles = new Obstacle[] {
			new SimpleDoor(getScript()),
			new EnergyBarrier(getScript()),
			new LooseRailing(getScript()),
			new Stile(getScript()),
			new WildernessDitch(getScript())
		};
		globalObstacles = new Obstacle[] {
			new Stairs(getScript()),
			new Trapdoor(getScript())
		};
	}

	public void wait(Position b, WalkListener<Boolean, Tile> callable) {
		wait(tile(b), callable);
	}

	public void wait(Entity b, WalkListener<Boolean, Tile> callable) {
		wait(tile(b), callable);
	}

	public void wait(Tile b, WalkListener<Boolean, Tile> callable) {
		try {
			int startDist = b.actualDistanceTo(),
				curDist = startDist;
			debug("Starting wait with "+callable.getClass().getName());
			for (int i = 0; i < 10 && !isMoving(); i++)
				sleep(100);
			for (int i = 0; i < 300 && isMoving()
					&& (callable == null || callable.walkWhile(b)); i++) {
				curDist = b.actualDistanceTo();
				if (curDist > startDist) {
					warning("We are walking away from the target, we should be correcting!");
					return;
				}
				startDist = curDist;
				sleep(100);
			}
			debug("Done waiting!");
		} catch (Exception e) {
			exception(e);;
			debug("Done waiting, exception!");
		}
	}

	public void wait(Tile loc) {
		wait(loc, new BasicListener(getScript(), loc));
	}

	public void wait(Position loc) {
		wait(tile(loc));
	}

	public void wait(Entity e) {
		wait(tile(e), new EntityListener(getScript(), e));
	}
	
	public boolean isMoving() {
		return myPlayer().isMoving();
	}

	public boolean isRunning() {
		return getSettings().isRunning();
	}
	
	public void setHandleRun(boolean h) {
		handleRun  = h;
	}
	
	public boolean handleRun() {
		return handleRun;
	}

	public void checkRun() {
		if (!isRunning() && getEnergy() >= runEnergyThreshold) {
			getMouse().move(random(544, 591), random(126, 150));
			getMouse().click(false);
			runEnergyThreshold = random(25, 100);
			sleep(random(250, 500));
		} 
	}

	private int getEnergy() {
		return getSettings().getRunEnergy();
	}
	
	public boolean clickOnTile(Tile t) {
		return clickOnTile(t, new BasicListener(getScript(), t), false, Walker.RANDOM_RADIUS);
	}
	
	public boolean clickOnTile(Tile t, WalkListener<Boolean, Tile> callable) {
		return clickOnTile(t, callable, false, Walker.RANDOM_RADIUS);
	}
	
	public boolean clickOnTile(Tile t, boolean forceClick) {
		return clickOnTile(t, new BasicListener(getScript(), t), forceClick, Walker.RANDOM_RADIUS);
	}
	
	public boolean clickOnTile(Tile t, WalkListener<Boolean, Tile> callable, boolean forceClick) {
		return clickOnTile(t, callable, forceClick, Walker.RANDOM_RADIUS);
	}

	public boolean clickOnTile(Entity tmpObj) {
		try {
			return clickOnTile(findEntityEnd(tmpObj));
		} catch (Exception e) {
			return clickOnTile(tile(tmpObj));
		}
	}
	
	private boolean clickOnTile(Tile t, WalkListener<Boolean, Tile> callable, boolean forceClick, int dist) {
		curTile = t;
		
		if (!t.isOnMap()) {
			warning("Safety Override (tile off map) at "+t.toString()+" using "+callable.getClass().getName());
			return false;
		}
		if (!forceClick && !callable.walkWhile(t)) {
			warning("Safety Override (listener finished) at "+t.toString()+" using "+callable.getClass().getName());
			return true;
		}
		if (getFlag() != null && t.dist(getFlag()) < Interact.RADIUS
				&& t.actualDistanceTo(getFlag()) < Interact.RADIUS) {
			info("Safety Override (flag already exists) at "+t.toString()+" using "+callable.getClass().getName());
			return true;
		}
		
		a = 0;
		
		do {
			randTile = findRandom(t, dist);
			if (clickOnMap(randTile)) {
				for (int w = 0; w < 10 && getFlag() == null; w++)
					sleep(40);
				destTile = getFlag();
				getWalker().getRegion().updateObjectCache();
				if (destTile != null && !destTile.canReach(true)) {
					debug("Can not reach the destination!");
					return true;
				}
				if (destTile != null && randTile.actualDistanceTo(destTile) < dist * 1.5) {
					break;
				}
				sleep(100, 500);
			}
		} while (a++ < 10);
		wait(t, callable);
		
		debug("Clicked on "+randTile.toString()+"!");
		
		randTile = null;
		
		return callable.walkWhile(t);
	}
	
	public Tile getFlag() {
		if (getMap().getDestination() == null)
			return null;
		
		tmpTile = tile(getMap().getDestination());
		if (tmpTile.compare(tile(region.getX(), region.getY(), myPlayer().getZ()))) {
			return null;
		}
		return tmpTile;
	}
	
	private boolean clickOnMap(Tile t) {
		if (t.isOnMap()) {
			if (getMouse().move(new MiniMapTileDestination(getBot(), t.pos()))) {
				return getMouse().click(false);
			}
			/*Point p = t.getPointOnMap();
			int rand = 2, overflow = 2;
			if (getMouse().move(p.x - rand + random(0, rand * 2),
					p.y - rand + random(0, rand * 2))) {
				Point p2 = getMouse().getPosition();
				if (p2.x >= p.x - rand - overflow && p2.x <= p.x + rand + overflow
						&& p2.y >= p.y - rand - overflow && p2.y <= p.y + rand + overflow) {
					return getMouse().click(false);
				}
			}*/
		}
		return false;
	}

	public Tile findRandom(Tile t2) {
		return findRandom(t2, Walker.RANDOM_RADIUS);
	}
	
	public Tile findRandom(Tile t2, int radius) {
		here = new Node(t2.getX() - getMap().getBaseX(), t2.getY() - getMap().getBaseY(), t2.getZ());
		potentials = region.radiusSearch(here, radius);
		Collections.shuffle(potentials);
		t = null;
		for (Node n : potentials) {
			t = tile((int) n.x + getMap().getBaseX(), (int) n.y + getMap().getBaseY(), (int) n.z);
			if (t.isOnMap() && !t.isBlocked() && t2.actualDistanceTo(t) < radius * 2)
				return t;
		}
		warning("Couild not find a random tile!");
		return null; // */
	}

	public Region getRegion() {
		return region;
	}

	public BufferedImage getMapImage() {
		return filemanager.getMapImage();
	}

	public Tile tile(Node b) {
		return tile((int) b.x, (int) b.y, (int) b.z);
	}

	public Tile tile(String s) {
		if (s.length() < 9)
			return null;
		x = Integer.parseInt(s.substring(0, 4));
		y = Integer.parseInt(s.substring(5, 9));
		z = s.length() > 9 ? Integer.parseInt(s.substring(10, s.length())) : 0;
		return tile(x, y, z);
	}

	public GlobalPath findPath(Position end) {
		return findPath(myLocation(), tile(end));
	}

	public GlobalPath findPath(Entity end) {
		return findPath(myLocation(), findEntityEnd(end));
	}

	public GlobalPath findPath(Tile end) {
		return findPath(myLocation(), end);
	}

	public GlobalPath findPath(Tile start, Tile end) {
		if (lastPath != null) {
			lastPath.invalidate();
			lastPath = null;
		}
		lastPath = new GlobalPath(getScript(), start, end);
		return lastPath;
	}

	public LocalPath findLocalPath(Position end) {
		return findLocalPath(myLocation(), tile(end));
	}

	public LocalPath findLocalPath(Entity end) {
		return findLocalPath(myLocation(), findEntityEnd(end));
	}

	public LocalPath findLocalPath(Tile end) {
		return findLocalPath(myLocation(), end);
	}

	public LocalPath findLocalPath(Tile start, Tile end) {
		setLastPath(new LocalPath(getScript(), start, region.findEnd(end), false));
		return lastLocalPath;
	}

	public void setLastPath(LocalPath p) {
		if (lastLocalPath != null) {
			lastLocalPath.invalidate();
			lastLocalPath = null;
		}
		lastLocalPath = p;
	}

	public void render(Graphics2D g) {
		try {
			if (!getClient().isLoggedIn())
				return;
			startColor = g.getColor();
			g.setFont(new Font("", Font.PLAIN, 12));
			
			g.setColor(Color.white);
			g.setColor(startColor);
			if (lastLocalPath != null && lastLocalPath.getTiles() != null) {
				lastLocalPath.render(g);
			}
			
			if (filemanager.isRender()) {
				g.setColor(Color.white);
				
				bx = getMap().getBaseX();
				by = getMap().getBaseY();
				z = getMap().getPlane();
				if (timer == null || !timer.isRunning()) {
					if (timer == null)
						 timer = new Timer(3000);
					obstacleTiles.clear();
					blockedTiles.clear();
					walledTiles.clear();
					for (int x = 0; x < 104; x++) {
						for (int y = 0; y < 104; y++) {
							t2 = tile(bx + x, by + y, z);
							
							flag = region.getFlag(t2);
							
							if ((flag & Flags.FLAG_BLOCKED) != 0)
								blockedTiles.add(t2);
							else if ((flag & Flags.FLAG_WEST_BLOCKED) != 0
								|| (flag & Flags.FLAG_EAST_BLOCKED) != 0
								|| (flag & Flags.FLAG_SOUTH_BLOCKED) != 0
								|| (flag & Flags.FLAG_NORTH_BLOCKED) != 0)
								walledTiles.add(t2);
						}
					}
					timer.reset();
				}
				
				for (Tile t : blockedTiles) {
					if (t != null && isVisible(t)) {
						t.draw(g, null, new Color(255, 0, 0, 30));
						g.setColor(Color.white);
					}
				}
				
				for (Tile t : obstacleTiles) {
					if (t != null && isVisible(t)) {
						t.draw(g, null, new Color(255, 153, 0, 30));
						g.setColor(Color.white);
					}
				}
				for (Tile t : walledTiles) {
					if (t != null && isVisible(t))
						drawWalls(t, g, new Color(0, 255, 255, 50), region.getFlag(t));
				}
			}
			g.setColor(startColor);
		} catch (Exception e) {
			exception(e);;
		}
	}
	
	public void drawWalls(final Tile t, final Graphics g, final Color color, final int flag) {
		if (getClient().isLoggedIn() && isVisible(t)) {
			poly = t.pos().getPolygon(getBot());
			pn = new Point(poly.xpoints[2], poly.ypoints[2]);
			px = new Point(poly.xpoints[1], poly.ypoints[1]);
			py = new Point(poly.xpoints[3], poly.ypoints[3]);
			pxy = new Point(poly.xpoints[0], poly.ypoints[0]);
			if (VIEWPORT.contains(py) && VIEWPORT.contains(pxy) && VIEWPORT.contains(px) && VIEWPORT.contains(pn)) {
				g.setColor(color);
				if ((flag & Flags.FLAG_NORTH_BLOCKED) != 0)
					g.drawLine(py.x, py.y, pxy.x, pxy.y);
				if ((flag & Flags.FLAG_EAST_BLOCKED) != 0)
					g.drawLine(pxy.x, pxy.y, px.x, px.y);
				if ((flag & Flags.FLAG_SOUTH_BLOCKED) != 0)
					g.drawLine(px.x, px.y, pn.x, pn.y);
				if ((flag & Flags.FLAG_WEST_BLOCKED) != 0)
					g.drawLine(pn.x, pn.y, py.x, py.y);
			}
		}
	}

	public int getZLevelDiff() {
		return lastZLevelDiff;
	}
	
	public void setZLevelDiff(int z) {
		lastZLevelDiff = z;
	}

	public FileManager getFileManager() {
		return filemanager;
	}

	public boolean canReach(Entity n) {
		return canReach(tile(n), false);
	}
	
	public boolean canReach(Position n) {
		return canReach(tile(n), false);
	}
	
	public boolean canReach(Tile makeTile) {
		return canReach(makeTile, false);
	}

	public boolean canReach(Entity n, boolean failOnObstacles) {
		return canReach(tile(n), failOnObstacles);
	}
	
	public boolean canReach(Position n, boolean failOnObstacles) {
		return canReach(tile(n), failOnObstacles);
	}

	public boolean canReach(Tile makeTile, boolean failOnObstacles) {
		if (makeTile == null || !region.contains(makeTile))
			return false;
		
		start = myLocation();
		/*for (Node n : nodes()) {
			tmpTile2 = n.toTile(getScript());
			if (tmpTile2.compare(makeTile)) {
				return region.contains(tmpTile2) && (region.getFlag(tmpTile2) & Flags.FLAG_BLOCKED) == 0;
			}
			
			if (region.contains(tmpTile2) && (region.getFlag(tmpTile2) & Flags.FLAG_BLOCKED) == 0
					&& tmpTile2.distanceTo(makeTile) < start.distanceTo(makeTile))
				start = tmpTile2;
		}*/
		path = new LocalPath(getScript(), start, makeTile, false, true);
		
		if (!path.valid() || (failOnObstacles && path.objectsInPath())) {
			return false;
		}
		
		return true;
	}

	public Tile tile(Position position) {
		// Check the tile cache
		return tile(position.getX(), position.getY(), position.getZ());
	}

	public Tile tile(Entity n) {
		return tile(n.getX(), n.getY(), n.getZ());
	}

	public Tile tile(int x, int y, int z) {
		// See if we got this
		if (tileCache.containsKey(IntCache.valueOf(x))
				&& tileCache.get(IntCache.valueOf(x)).containsKey(IntCache.valueOf(y))
				&& tileCache.get(IntCache.valueOf(x)).get(IntCache.valueOf(y)).containsKey(IntCache.valueOf(z))) {
			return tileCache.get(IntCache.valueOf(x)).get(IntCache.valueOf(y)).get(IntCache.valueOf(z));
		}
		if (!tileCache.containsKey(IntCache.valueOf(x)))
			tileCache.put(IntCache.valueOf(x), new HashMap<Integer, HashMap<Integer, Tile>>());
		if (!tileCache.get(IntCache.valueOf(x)).containsKey(IntCache.valueOf(y)))
			tileCache.get(IntCache.valueOf(x)).put(IntCache.valueOf(y), new HashMap<Integer, Tile>());
		
		tmpTile = new Tile(getScript(), IntCache.valueOf(x), IntCache.valueOf(y), IntCache.valueOf(z));
		
		tileCache.get(IntCache.valueOf(x)).get(IntCache.valueOf(y)).put(IntCache.valueOf(z), tmpTile);
		return tileCache.get(IntCache.valueOf(x)).get(IntCache.valueOf(y)).get(IntCache.valueOf(z));
	}

	public Tile tile() {
		return tile(myPosition());
	}

	public Tile findEntityEnd(Entity entity) {
		endEnt = getWalker().getRegion().findEnd(getWalker().tile(entity));
		if (entity instanceof WallObject) {
			if (endEnt.canReach(true))
				return endEnt;
			
			switch (((WallObject) entity).getOrientation()) {
			case 0:
				return endEnt.translate(-1, 0);
			case 1:
				return endEnt.translate(0, 1);
			case 2:
				return endEnt.translate(1, 0);
			case 3:
				return endEnt.translate(0, -1);
			}
		} else {
			minX = entity.getX();
			maxX = entity.getX() + entity.getSizeX();
			minY = entity.getY();
			maxY = entity.getY() + entity.getSizeY();
			for (int x = minX; x < maxX; x++) {
				// Only test min/max Y for each, as to avoid testing centers
				endEnt = getBestEntityEnd(endEnt, getWalker().tile(x, minY, myLocation().getZ()));
				endEnt = getBestEntityEnd(endEnt, getWalker().tile(x, maxY - 1, myLocation().getZ()));
			}
			for (int y = minY; y < maxY; y++) {
				// Only test min/max Y for each, as to avoid testing centers
				endEnt = getBestEntityEnd(endEnt, getWalker().tile(minX, y, myLocation().getZ()));
				endEnt = getBestEntityEnd(endEnt, getWalker().tile(maxX - 1, y, myLocation().getZ()));
			}
		}
		return endEnt;
	}
	
    public Tile findClosestWallTile(WallObject obj) {
    	Tile tile = getWalker().tile(obj);
    	Tile end = null;
    	
		// Pick the closest tile
		switch (((WallObject) obj).getOrientation()) {
		case 0: // west
			end = tile.translate(-1, 0);
			break;
		case 1: // north
			end = tile.translate(0, 1);
			break;
		case 2: // east
			end = tile.translate(1, 0);
			break;
		case 3: // south
			end = tile.translate(0, -1);
			break;
		}
    	
		return end;
	}

	private Tile getBestEntityEnd(Tile end, Tile newEnd) {
		newEnd = getWalker().getRegion().findEnd(newEnd);
		
		int c = -1;
		if (newEnd != null) {
			if (end == null) {
				return newEnd;
			}
			
			int n = newEnd.actualDistanceTo(),
				e = end.actualDistanceTo();
			
			c = n < 9000 ? 1 : 0;
			if (c == 1 && n < e) {
				return newEnd;
			} else if (c == 1 && n == e
					&& newEnd.preciseDistanceTo() < end.preciseDistanceTo()) {
				return newEnd;
			}
		}
		return end;
	}

	public ArrayList<Node> nodes() {
		if (paths == null)
			paths = filemanager.getWeb();
		
		if (nodes == null) {
			nodes = new ArrayList<Node>();
			
			// Create nodes
			for (int[] node : paths) {
				nodes.add(new Node(node[0], node[1], node[2]));
			}
			
			// Add connections
			for (int index = 0; index < nodes.size(); index++) {
				for (int i = 3; i < paths[index].length; i++) {
					if (paths[index][i] == -1)
						continue;
					
					nodes.get(index).connect(nodes.get(paths[index][i]),
							nodes.get(index).distanceTo(nodes.get(paths[index][i])));
				}
			}
		}
		
		return nodes;
	}
	
	public Pathfinder pathfinder() {
		return new Pathfinder(nodes());
	}

	public Obstacle[] getLocalObstacles() {
		return localObstacles;
	}

	public Obstacle[] getGlobalObstacles() {
		return globalObstacles;
	}
	
}
