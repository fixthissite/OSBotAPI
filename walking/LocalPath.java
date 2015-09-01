package lemons.api.walking;

import java.awt.Color;
import java.awt.Graphics2D;
import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import lemons.api.script.TaskScript;
import lemons.api.script.emulators.TaskScriptEmulator;
import lemons.api.script.entities.RS2Object;
import lemons.api.script.entities.WallObject;
import lemons.api.walking.listeners.BasicListener;
import lemons.api.walking.listeners.WalkListener;
import lemons.api.walking.map.Flags;
import lemons.api.walking.map.RawFlags;
import lemons.api.walking.map.Tile;
import lemons.api.walking.obstacles.EnergyBarrier;
import lemons.api.walking.obstacles.LooseRailing;
import lemons.api.walking.obstacles.Obstacle;
import lemons.api.walking.obstacles.SimpleDoor;
import lemons.api.walking.obstacles.Stile;
import lemons.api.walking.obstacles.WildernessDitch;

public class LocalPath extends TaskScriptEmulator<TaskScript> implements Path {
	
	public final Tile start, end;
	
	private Tile lastTile, tile;
	
    private Tile[] tiles;
	
	private boolean valid = false;

    private int currPlane, baseX, baseY, currX, currY, plane,
    			here, upper, step = 0, destX, destY;

	private double dx, dy, rand;
	
	private boolean result, use_t;
	
	private Node curr, dest, best, p;
	
	private HashSet<Node> open, closed;
	
	private LinkedList<Node> tiles3;
	
	private LinkedList<Tile> path;

	private Obstacle[] obstacles;

	private WalkListener<Boolean, Tile> callable;

	public final boolean ignoreObjects;

	private final boolean quiet;

	private Tile bestTile;

	private int bestIndex;

	private Tile fsTile;

	private int failsafe;

	private boolean randomize;

	public int[][] tileData;
	
	public LocalPath(TaskScript w, Tile start, Tile end, boolean ignoreObjects) {
		this(w, start, end, ignoreObjects, false);
	}
	public LocalPath(TaskScript w, Tile start, Tile end, boolean ignoreObjects, boolean quiet) {
		this(w, start, end, ignoreObjects, quiet, true);
	}
	public LocalPath(TaskScript w, Tile start, Tile end, boolean ignoreObjects, boolean quiet, boolean randomize) {
		super(w);
		this.start = start;
		this.end = end;
		this.ignoreObjects = ignoreObjects;
		this.quiet = quiet;
		this.randomize = randomize;
		
		obstacles = new Obstacle[] {
			new SimpleDoor(getScript()),
			new EnergyBarrier(getScript()),
			new LooseRailing(getScript()),
			new Stile(getScript()),
			new WildernessDitch(getScript())
		};
		
		findPath();
	}
    
    public Tile[] getTiles() {
    	return tiles;
    }
	
	public int length() {
		return tiles == null ? 0 : tiles.length;
	}

    public boolean valid() {
    	return valid;
    }

	public void invalidate() {
		valid = false;
	}

    public boolean objectsInPath() {
    	if (ignoreObjects) {
    		debug("Ignoring objects");
    		return false;
    	}
    	
    	int index = 0;
    	
    	for (int i = 0; i < tiles.length; i++) {
    		if (tiles[index].dist() > tiles[i].dist())
    			index = i;
    	}
    	
		for (int i = index; i < tiles.length; i++) {
			Tile tt = tiles[i];
			if (testForObjects(tt, false))
				return true;
		}
		
		return false;
	}
    
    public boolean containsTile(Tile t) {
    	for (Tile a : tiles) {
    		if (a.compare(t))
    			return true;
    	}
    	return false;
    }
	
	public boolean wallObjectInPath(RS2Object obj) {
		return wallObjectInPath(getWalker().getRegion().getRawFlag(getWalker().tile(obj)), obj);
	}

	public boolean wallObjectInPath(int flag, RS2Object obj) {
		if ((flag & RawFlags.WALL_NORTH) != 0 &&
				path.contains(getWalker().tile(obj)) && path.contains(getWalker().tile(obj).translate(0, 1))) {
			return true;
		}
		if ((flag & RawFlags.WALL_SOUTH) != 0 &&
				path.contains(getWalker().tile(obj)) && path.contains(getWalker().tile(obj).translate(0, -1))) {
			return true;
		}
		if ((flag & RawFlags.WALL_EAST) != 0 &&
				path.contains(getWalker().tile(obj)) && path.contains(getWalker().tile(obj).translate(1, 0))) {
			return true;
		}
		if ((flag & RawFlags.WALL_WEST) != 0 &&
				path.contains(getWalker().tile(obj)) && path.contains(getWalker().tile(obj).translate(-1, 0))) {
			return true;
		}
		return false;
	}

	private double heuristic(final Node start, final Node end) {
    	if ((getWalker().getRegion().getFlag(start.x, start.y) & Flags.FLAG_OBSTACLE) != 0) {
    		return 1000.0;
    	}
    	rand = random(0.8, 10.0);
        dx = Math.abs(start.x - end.x);
        dy = Math.abs(start.y - end.y);
        return Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2)) * (randomize ? rand : 1);
    }

    private double dist(final Node start, final Node end) {
        if (start.x != end.x && start.y != end.y) {
            return 1.41421356;
        } else {
            return 1.0;
        }
    }

    private Node lowest_f(final Set<Node> open) {
        best = null;
        for (final Node t : open) {
            if (best == null || t.f < best.f) {
                best = t;
            }
        }
        return best;
    }

    private java.util.List<Node> successors(final Node t) {
        tiles3 = new LinkedList<Node>();
		if (t.x < 0 || t.x > 103 || t.y < 0 || t.y > 103)
        	return tiles3;
        here = getWalker().getRegion().getFlag(t.x, t.y);
        upper = 103;
        
        if (t.y > 0 && (//(t.x == destX && t.y - 1 == destY
        			//&& (getWalker().getRegion().getFlag(t.x, t.y - 1) & FLAG_BLOCKED) != 0)
        		((here & Flags.FLAG_SOUTH_BLOCKED) == 0
        		&& (isEnd(t.x, t.y - 1) || (getWalker().getRegion().getFlag(t.x, t.y - 1) & Flags.FLAG_BLOCKED) == 0
        		&& (getWalker().getRegion().getFlag(t.x, t.y - 1) & Flags.FLAG_NORTH_BLOCKED) == 0)))) {
            tiles3.add(new Node(t.x, t.y - 1, t.z));
        }
        if (t.x > 0 && (//(t.x - 1 == destX && t.y == destY
        			//&& (getWalker().getRegion().getFlag(t.x - 1, t.y) & FLAG_BLOCKED) != 0)
        		((here & Flags.FLAG_WEST_BLOCKED) == 0
        		&& (isEnd(t.x - 1, t.y) || (getWalker().getRegion().getFlag(t.x - 1, t.y) & Flags.FLAG_BLOCKED) == 0
        		&& (getWalker().getRegion().getFlag(t.x - 1, t.y) & Flags.FLAG_EAST_BLOCKED) == 0)))) {
            tiles3.add(new Node(t.x - 1, t.y, t.z));
        }
        if (t.y < upper && (//(t.x == destX && t.y + 1 == destY
        			//&& (getWalker().getRegion().getFlag(t.x, t.y + 1) & FLAG_BLOCKED) != 0)
        		((here & Flags.FLAG_NORTH_BLOCKED) == 0
        		&& (isEnd(t.x, t.y + 1) || (getWalker().getRegion().getFlag(t.x, t.y + 1) & Flags.FLAG_BLOCKED) == 0
        		&& (getWalker().getRegion().getFlag(t.x, t.y + 1) & Flags.FLAG_SOUTH_BLOCKED) == 0)))) {
            tiles3.add(new Node(t.x, t.y + 1, t.z));
        }
        if (t.x < upper && (//(t.x + 1 == destX && t.y == destY
        			//&& (getWalker().getRegion().getFlag(t.x + 1, t.y) & FLAG_BLOCKED) != 0)
        		((here & Flags.FLAG_EAST_BLOCKED) == 0
        		&& (isEnd(t.x + 1, t.y) || (getWalker().getRegion().getFlag(t.x + 1, t.y) & Flags.FLAG_BLOCKED) == 0
        		&& (getWalker().getRegion().getFlag(t.x + 1, t.y) & Flags.FLAG_WEST_BLOCKED) == 0)))) {
            tiles3.add(new Node(t.x + 1, t.y, t.z));
        }
        
        /*
        // Southeast
        if ((here & FLAG_SOUTHEAST_BLOCKED) == 0) {
            tiles3.add(new Node(x + 1, y - 1, z));
        }
        // Southwest
        if ((here & FLAG_SOUTHWEST_BLOCKED) == 0) {
            tiles3.add(new Node(x - 1, y - 1, z));
        }
        
        // Northwest
        if ((here & FLAG_NORTHWEST_BLOCKED) == 0) {
            tiles3.add(new Node(x - 1, y + 1, z));
        }
        // Northeast
        if ((here & FLAG_NORTHEAST_BLOCKED) == 0) {
            tiles3.add(new Node(x + 1, y + 1, z));
        }// */        
        return tiles3;
    }

    private boolean isEnd(int x, int y) {
		return x == destX && y == destY;
	}

	private Tile[] path(final Node end, final int base_x, final int base_y) {
        path = new LinkedList<Tile>();
        p = end;
        while (p != null) {
            path.addFirst(p.get(base_x, base_y));
            p = p.prev;
        }
        return path.toArray(new Tile[path.size()]);
    }

    public final class Node {
        public final int x, y, z;
        public int length = 0;
        public Node prev;
        public double g, f;

        public Node(final int x, final int y, final int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            g = f = 0;
        }

        @Override
        public int hashCode() {
            return x << 4 | y;
        }

        @Override
        public boolean equals(final Object o) {
            if (o instanceof Node) {
                final Node n = (Node) o;
                return x == n.x && y == n.y && z == n.z;
            }
            return false;
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }

        public Tile get(final int baseX, final int baseY) {
            return getWalker().tile(x + baseX, y + baseY, z);
        }
    }

	public boolean loop() {
		return loop(2);
	}
	
	public boolean loop(int count) {
		for (int i = 0; i < count && valid() && !walk(); i++);
		return !valid();
	}

	private boolean failsafe() {
		if (fsTile == null || fsTile.dist(myLocation()) > Walker.RANDOM_RADIUS) {
			fsTile = myLocation();
			failsafe = 0;
		} else {
			failsafe++;
			if (failsafe > 5)
				exception(new Exception("Failsafe was triggered for LocalPath!"));
			else
				warning("Failsafe triggering? ("+failsafe+")");
		}
		return failsafe > 5;
	}
    
    public boolean walk() {
    	if (end.getZ() != myPlayer().getZ()) {
    		valid = false;
    		warning("Use GlobalPath for z-levels! End: "+end.toString());
    		return true;
    	}
    	
    	debug("Walking to "+end.toString());
    	
    	if (callable == null) {
    		setListener(new BasicListener(getScript(), end));
    	}
    	
    	if (failsafe()) {
    		valid = false;
    		return true;
    	}
    	
    	getWalker().getRegion().updateObjectCache();
    	
		try {
			// info("Local walk begins");
			if (start.compare(end)) {
				info("Were starting at the end!");
				return true;
			}
			if (tiles == null || (tiles.length == 0 && start.dist(end) > 1)) {
				error("Could not find path from "+start.toString()+" to "+end.toString());
				valid = false;
				getWalker().getRegion().rescan();
				return true;
			}
			lastTile = tiles[tiles.length - 1];
			info("Checking objects in path...");
			if (!callable.walkWhile(end) && !objectsInPath()) {
				info("Within range of target, done walking using "+callable.getClass().getName()+".");
				valid = false;
				return true;
			}
			info("Checked objects in path!");
			bestTile = null;
			bestIndex = -1;
			
			if (getWalker().handleRun())
				getWalker().checkRun();
			
			for (int curIndex = step; curIndex < tiles.length; curIndex++) {
				if (curIndex < 0)
					curIndex = 0;
				tile = tiles[curIndex];
				
				if (!ignoreObjects && testForObjects(tile, true)) {
					step = curIndex;
					info("Solved object in our path");
					valid = false;
					return true;
				}
				
				if (curIndex > 0 && curIndex + 1 < tiles.length) {
					if (tile.isOnMap() && !tiles[curIndex + 1].isOnMap()) {
						bestTile = tile;
						bestIndex = curIndex;
					}
				}
				
				if (!ignoreObjects && testForObjects(tile, false)) {
					valid = false;
					return true;
				}
			}
			if (bestIndex == -1) {
				if (callable.walkWhile(end) || objectsInPath()) {
					debug("Interacting with last tile ("+lastTile.toString()+") in the path.");
					getWalker().clickOnTile(tiles[tiles.length - 1], callable);
				} else if (myPlayer().isMoving()) {
					debug("Waiting on last tile ("+lastTile.toString()+" in the path.");
					getWalker().wait(end, callable);
				}
				
				valid = callable.walkWhile(end) && !objectsInPath();
				
				info("At last tile ("+lastTile.toString()+")? "+(!valid ? "Yes" : "No")+" "+callable.getClass().getName());
				return !valid;
			} else {
				debug("Interacting with best tile "+bestTile.toString());
				result = getWalker().clickOnTile(bestTile, callable);
				if (result) {
					step = bestIndex;
				}
				
				if (result) {
					debug("Walked segment successfully!");
				} else if (!callable.walkWhile(bestTile)) {
					debug("Walking finished, already too close to end!");
				} else if (!bestTile.isOnMap()) {
					warning("Walking failed, could not find point on map!");
				} else {
					warning("Walking might have gone wrong, just not sure what.");
				}
				valid = callable.walkWhile(lastTile);
				return !valid;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return false;
	}

	public boolean testForObjects(Tile t, boolean doSolve) {
		//exception(new Exception());
		for (final RS2Object obj : getWalker().getRegion().getObjectCache(t.x, t.y)) {
			if (obj == null || obj.getName() == null || !t.compare(obj))
				continue;
			
			if (obj instanceof WallObject && !wallObjectInPath(obj)) {
				continue;
			}
			
			for (Obstacle o : obstacles) {
				if (!o.isActive(obj, doSolve))
					continue;
				
				if (!doSolve) {
					debug("Found an obstacle ("+obj.getName()+" at "+getWalker().tile(obj).toString()+") in our path!");
					return true;
				}
				
				if (!o.solve(obj)) {
					warning("Found "+obj.getName()+", unsolved!!!!!");
					return false;
				}
				info("Found "+obj.getName()+", solved");
				return true;
			}
		}
		return false;
	}

	private void findPath() {
    	if (start.compare(end)) {
    		valid = true;
    		tiles = new Tile[0];
    		return;
    	}
    	
        if (start == null || end == null || start.getZ() != end.getZ()) {
        	getWalker().getRegion().rescan();
        	valid = false;
        	throw new InvalidParameterException(
        			(start == null ? "start = null   " : "")
        			+(end == null ? "end = null   " : "")
        			+(start != null && end != null && start.getZ() != end.getZ()
        				? "different planes ("+start.toString()+" != "+end.toString()+")" : ""));
        }
        currPlane = start.getZ();
        baseX = getMap().getBaseX();
		baseY = getMap().getBaseY();
		currX = start.getX() - baseX;
		currY = start.getY() - baseY;
		destX = end.getX() - baseX;
        destY = end.getY() - baseY;

        plane = getMap().getPlane();
        if (currPlane != plane) {
        	error("Plane mismatch");
            return;
        }


        if (currX < 0 || currY < 0 || currX >= 104 || currY >= 104) {
        	warning("Outside current region");
            return;
        } else if (destX < 0 || destY < 0 || destX >= 104 || destY >= 104) {
            if (destX < 0) {
                destX = 0;
            } else if (destX >= 104) {
                destX = 103;
            }
            if (destY < 0) {
                destY = 0;
            } else if (destY >= 104) {
                destY = 103;
            }
        }

        open = new HashSet<Node>();
        closed = new HashSet<Node>();
        curr = new Node(currX, currY, currPlane);
        dest = new Node(destX, destY, currPlane);
        int[][] tileData = new int[104][104];
        for (int x = 0; x < 104; x++) {
        	for (int y = 0; y < 104; y++){
        		tileData[x][y] = 0;
        	}
        }

        curr.f = heuristic(curr, dest);
        open.add(curr);
        tileData[curr.x][curr.y] = 1;
        while (!open.isEmpty()) {
            curr = lowest_f(open);
            if (curr.equals(dest)) {
            	valid = true;
                tiles = path(curr, baseX, baseY);
                return;
            }
            open.remove(curr);
            closed.add(curr);
            tileData[curr.x][curr.y] = 2;
           // if (curr.g > 5)
            //	continue;
            
            for (final Node next : successors(curr)) {
                if (!closed.contains(next)) {
                    use_t = false;
                    if (!open.contains(next)) {
                        open.add(next);
                        tileData[next.x][next.y] = 1;
                        use_t = true;
                    } else if (curr.g < next.g) {
                        use_t = true;
                    }
                    if (use_t) {
                        next.prev = curr;
                        next.length = curr.length + 1;
                        next.g = curr.g + dist(curr, next);
                        next.f = curr.f + heuristic(next, dest);
                    }
                }
            }
        }
        
        //getWalker().tileData = tileData;
        
        if (!quiet) {
        	warning("Path could not be found from "+start.toString()+" to "+end.toString()+"!");
        	getWalker().getRegion().rescan();
        }
    }

	public LocalPath setListener(WalkListener<Boolean, Tile> g) {
		callable = g;
		return this;
	}
	
	public void render(Graphics2D g) {
		for (Tile t : getTiles()) {
			if (t.isOnMap())
				t.drawMap(g, new Color(0, 255, 0, 35));
			t.draw(g, null, new Color(0, 255, 0, 35));
		}
	}
}
