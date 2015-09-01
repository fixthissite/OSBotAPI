package lemons.api.walking;

import java.util.ArrayList;
import java.util.Collections;

import lemons.api.script.TaskScript;
import lemons.api.script.emulators.TaskScriptEmulator;
import lemons.api.script.entities.RS2Object;
import lemons.api.utils.Timer;
import lemons.api.walking.ai.Node;
import lemons.api.walking.listeners.BasicListener;
import lemons.api.walking.listeners.WalkListener;
import lemons.api.walking.map.Tile;
import lemons.api.walking.obstacles.Obstacle;

public class GlobalPath extends TaskScriptEmulator<TaskScript> implements Path {

	public final Tile start, end;
	
	private Tile tmpTile, t, curT;
	
	private int curPlane, step, minX, maxX, minY, maxY, diffX, diffY, startC, endC, inc;
	
	private double tmpDistBetween, curDistBetween;
	
	private ArrayList<Node> foundPath;

	private ArrayList<Tile> foundTiles;

	private Obstacle[] obstacles;

	private Node startNode, endNode;

	private RS2Object nearest, tmp;

	private Boolean result;

	private String lastRegion, str;

	private Timer limitWalk;

	private boolean valid, d;

	private WalkListener<Boolean, Tile> listener;

	private boolean sameRegion;

	private int failsafe;

	private int fsStep = -1;

	private LocalPath path;

	public GlobalPath(TaskScript s, Tile start, Tile end) {
		super(s);
		this.start = start;
		this.end = end;
		valid = false;
		step = 0;
		
		obstacles  = getWalker().getGlobalObstacles();
		
		findPath();
	}
	
	public boolean valid() {
		return valid;
	}
	
	private void findPath() {
		try {
			startNode = null;
			endNode = null;
			
			curPlane = myPosition().getZ();
				
			// Find start node
			for (Node node : getWalker().nodes()) {
				tmpTile = node.toTile(getScript());
				tmpDistBetween = tmpTile.dist(start);
				if (tmpTile.getZ() != curPlane)
					continue;
				
				if (startNode == null || tmpDistBetween < curDistBetween) {
					curDistBetween = tmpDistBetween;
					startNode = node;
				}
			}
			
			curDistBetween = Integer.MAX_VALUE;
			
			// Find end node
			for (Node node : getWalker().nodes()) {
				tmpDistBetween = node.toTile(getScript()).dist(end);
				if (endNode == null || tmpDistBetween < curDistBetween) {
					curDistBetween = tmpDistBetween;
					endNode = node;
				}
			}
			
			foundPath = getWalker().pathfinder().dijkstra(startNode, endNode);
			
			foundTiles = new ArrayList<Tile>();
			
			for (Node n : foundPath) {
				foundTiles.add(n.toTile(getScript()));
			}
			
			if (foundTiles.get(0).dist(start) > foundTiles.get(foundTiles.size() - 1).dist(start))
				Collections.reverse(foundTiles);
			
			foundTiles.add(end);
			
			valid = foundPath.size() > 1 || getWalker().getRegion().contains(end);
			sameRegion = end.canReach(false);
			
			if (!valid) {
				error("Path from "+start.toString()+" to "+end.toString()+" is invalid!");
			}
		} catch (Exception e) {
			error("Failed to find path from "+(start== null ? "null" : start.toString())+" to "+(end == null ? "null" : end.toString()));
			exception(e);;
		}
	}
	
	public boolean walk() {
		if (!getClient().isLoggedIn()) {
			sleep(100);
			return false;
		}
		
		debug("Starting walk...");
		
		if (listener == null)
			listener = new BasicListener(getScript(), end);
		
		if (!valid) {
			if (!getWalker().getRegion().contains(end))
				error("GlobalPath from "+start.toString()+" to "+end.toString()+" is invalid, ask dev to add area!");
			else
				error("GlobalPath from "+start.toString()+" to "+end.toString()+" is inaccessible, make sure the tile is valid!");
			
			debug("Segment walked!");
			return true;
		}
		
		if (failsafe()) {
			valid = false;
			debug("Segment walked!");
			return true;
		}
		
    	if (limitWalk != null && limitWalk.isRunning()) {
    		debug("GlobalPath.step is being called too fast, sleeping "+(int) limitWalk.getRemaining()+"!");
    		sleep((int) limitWalk.getRemaining());
    		debug("GlobalPath.step has been awoken!");
    	}
    	if (limitWalk == null)
    		limitWalk = new Timer(1000);
    	else
    		limitWalk.reset();
		
		if (lastRegion == null)
			lastRegion = getWalker().getRegion().getCurrent();
		
		if (path != null && path.valid()) {
			str = "Step "+step+" of "+foundTiles.size()+", tile is "+foundTiles.get(step).toString();
			result = path.walk();
			debug("Walk"+(result?"ed":"ing")+" cached LocalPath; "+str);
			if (result)
				path = null;
			valid = listener.walkWhile(end);
			return !valid;
		}
    	
    	if (sameRegion) {
    		path = findLocalPath(end);
    		path.setListener(listener);
    		debug("Same region, finding path to "+end.toString());
			return false;
    	}
    	
		str = "Step "+step+" of "+foundTiles.size()+", tile is "+foundTiles.get(step).toString();
		
		int i;
		
		for (i = step; i < foundTiles.size(); i++) {
			curT = foundTiles.get(i);
			
			t = i + 1 < foundTiles.size() ? foundTiles.get(i + 1) : null;
			
			if (t != null && (t.z != curT.z || t.dist(curT) > 200)) {
				getWalker().setZLevelDiff(t.getZ() - curT.getZ());
				
				curT = getWalker().getRegion().findEnd(curT);
				
				if (t.z != curT.z)
					info("Obstacle triggered by Z-level ("+curT.toString()+" -> "+t.toString()+")");
				if (t.dist(curT) > 200)
					info("Obstacle triggered by distance ("+curT.toString()+" -> "+t.toString()+")");
			
				for (Obstacle o : obstacles) {
					nearest = getNearestObstacle(curT);
					if (o.isActive(nearest, true)) {
						info("Solving "+o.getObjectNames()[0]+" near "+curT.toString());
						
						if (!o.solve(nearest)) {
							warning("Failed to solve "+o.getObjectNames()[0]+"; "+str);
						} else {
							debug("Object solved!; "+str);
							step = i + 1;
						}
						valid = false;
						return true;
					}
				}
				
				warning("Object left unsolved?");
				valid = listener.walkWhile(end);
				return !valid;
			} else if (canSeeTile(t, true)) {
			} else {
				if (t != null && step + 1 < foundTiles.size()) {
					Tile tmp = null;
					// Find a halfway point
						minX = Math.min(t.x, curT.x);
						maxX = Math.max(t.x, curT.x);
						minY = Math.min(t.y, curT.y);
						maxY = Math.max(t.y, curT.y);
						diffX = maxX - minX;
						diffY = maxY - minY;
					if (Math.abs(t.x - curT.x) < Math.abs(t.y - curT.y)) {
						startC = maxY == t.y ? t.x : curT.x;
						endC = maxY == t.y ? curT.x : t.x;
						inc = startC > endC ? -1 : 1;
						d = t.y > curT.y;
						for (double y = d ? maxY : minY; d ? y >= minY : y <= maxY;) {
							tmp = getWalker().tile((int) (startC + (inc * (diffX * (Math.abs(y - maxY) / diffY) ))), (int) y, curT.z);
							if (canSeeTile(tmp, false))
								break;
							
							debug("Can't see tile at "+tmp.toString());
							if (t == null)
								debug("  Tile was null!");
							else if (!(t.getZ() == curT.getZ() && t.getZ() == myLocation().getZ()))
								debug("  Diff z levels");
							else if (!getWalker().getRegion().contains(t))
								debug("  Region does not contain this Tile");
							else if (!t.canReach(false))
								debug(" Tile is not reachable");
							y += (t.y > curT.y) ? -1 : 1;
						}
					} else {
						startC = maxX == t.x ? t.y : curT.y;
						endC = maxX == t.x ? curT.y : t.y;
						inc = startC > endC ? -1 : 1;
						d = t.x > curT.x;
						for (double x = d ? maxX : minX; d ? x >= minX : x <= maxY;) {
							tmp = getWalker().tile((int) x, (int) (startC + (inc * (diffY * (Math.abs(x - maxX) / diffX) ))), curT.z);
							if (canSeeTile(tmp, false))
								break;
							
							debug("Can't see tile at "+tmp.toString());
							if (t == null)
								debug("  Tile was null!");
							else if (!(t.getZ() == curT.getZ() && t.getZ() == myLocation().getZ()))
								debug("  Diff z levels");
							else if (!getWalker().getRegion().contains(t))
								debug("  Region does not contain this Tile");
							else if (!t.canReach(false))
								debug(" Tile is not reachable");
							
							x += (t.x > curT.x) ? -1 : 1;
						}
					}
					if (tmp != null && canSeeTile(tmp, false)) {
						debug("Found best point for "+curT.toString()+" <-> "+t.toString()+" at "+tmp.toString());
						curT = tmp;
					} else {
						getWalker().getRegion().rescan();
						debug("Failed to find best point for "+curT.toString()+" <-> "+t.toString());
					}
				}
				
				path = findLocalPath(
						getWalker().getRegion().findClosestEnd(curT));
				path.setListener(listener);
				lastRegion = getWalker().getRegion().getCurrent();
				
				if (path.length() == 0) {
					getWalker().getRegion().rescan();
					warning("Invalid local path from "+myLocation().toString()+" to "+curT.toString()+" step "+i+"; "+str);
					path = null;
					valid = listener.walkWhile(end);
					debug("Segment walked!");
					return !valid;
				}
				debug("Found path, will walk next iteration!");
				return false;
			}
		}
		
		warning("Next waypoint could not be found");
		if (end == null || listener == null)
			valid = false;
		else
			valid = listener.walkWhile(end);
		debug("Segment walked!");
		return !valid;
	}

	private LocalPath findLocalPath(Tile end) {
		getWalker().setLastPath(new LocalPath(getScript(), myLocation(),
				getWalker().getRegion().findEnd(end), false));
		return getWalker().lastLocalPath;
	}

	private boolean failsafe() {
		if (fsStep != step) {
			fsStep = step;
			failsafe = 0;
		} else {
			failsafe++;
		}
		return failsafe > 5;
	}

	private boolean canSeeTile(Tile t, boolean closestEnd) {
		return t != null && (t.getZ() == curT.getZ() && t.getZ() == myLocation().getZ())
					&& getWalker().getRegion().contains(t)
					&& (closestEnd ? getWalker().getRegion().findClosestEnd(t) : t).canReach(false);
	}

	private RS2Object getNearestObstacle(Tile t) {
		tmp = null;
		for (RS2Object o : getObjects().getAll()) {
			for (Obstacle ob : obstacles)
				if (ob.isActive(o, true) && (tmp == null || t.dist(o) < t.dist(tmp)))
					tmp = o;
		}
		return tmp;
	}

	@Override
	public int length() {
		return foundTiles.size();
	}

	public ArrayList<Tile> getTiles() {
		return foundTiles;
	}

	public boolean loop() {
		return loop(3);
	}

	public boolean loop(int count) {
		for (int i = 0; i < count && !walk(); i++);
		return !valid();
	}

	public void setListener(WalkListener<Boolean, Tile> callable) {
		listener = callable;
	}

	public void invalidate() {
		valid = false;
	}

	public int distance() {
		int d = 0;
		Tile lastT = null;
		for (Tile t : getTiles()) {
			if (lastT != null) {
				d += t.dist(lastT);
			}
			lastT = t;
		}
		return d;
	}
	
}
