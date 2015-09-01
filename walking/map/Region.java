package lemons.api.walking.map;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import lemons.api.script.TaskScript;
import lemons.api.script.emulators.TaskScriptEmulator;
import lemons.api.script.entities.RS2Object;
import lemons.api.utils.Base64;
import lemons.api.walking.ai.Node;
import lemons.api.walking.obstacles.Obstacle;

import org.osbot.rs07.api.map.Area;

public class Region extends TaskScriptEmulator<TaskScript> {

	private static final int REGION_MIN = 1, REGION_MAX = 99;
	
	private boolean[] canInteractCache = new boolean[99999];

	private String curRawRegion;

	private int[][] rawFlags;

	private ArrayList<Callable<Boolean>> regionUpdates = new ArrayList<Callable<Boolean>>();

	private int[][] flags = new int[104][104];

	private int flag;

	private int newFlag;

	private boolean canInteract;

	private int tmpId;

	private Tile[] tiles2;

	private boolean rescan = false;

	private ArrayList<String> scannedRegions = new ArrayList<String>();

	private HashMap<Integer, String> intEncodeCache = new HashMap<Integer, String>();

	private List<RS2Object> objCache = new ArrayList<RS2Object>();

	private HashMap<Integer, HashMap<Integer, ArrayList<RS2Object>>> objRefCache
			= new HashMap<Integer, HashMap<Integer, ArrayList<RS2Object>>>();

	private String quickRegion;
	
	public Region(TaskScript w) {
		super(w);
	}
	
	private void resetLocalCache() {
		for (int x = 0; x < 104; x++) {
			for (int y = 0; y < 104; y++) {
				flags[x][y] = Flags.FLAG_BLOCKED;
			}
		}
	}

	public int getFlag(int a, int b) {
		scan();
		if (a >= REGION_MIN && a <= REGION_MAX
				&& b >= REGION_MIN && b <= REGION_MAX)
			return flags[a][b];
		return 0b1111111111; // All flags
	}

	public int getFlag(Tile t) {
		return getFlag(t.x - getX(), t.y - getY());
	}
	
	public int getRawFlag(int a, int b) {
		if (rawFlags == null)
			getRawFlags();
		if (a > -1 && a < rawFlags.length &&
			b > -1 && b < rawFlags[a].length)
			return rawFlags[a][b];
		return RawFlags.BLOCKED;
	}

	public int getRawFlag(Tile t) {
		return getRawFlag(t.getX() - getMap().getBaseX(), t.getY() - getMap().getBaseY());
	}
	
	private int[][] getRawFlags() {
		if (curRawRegion != getRegionId()) {
			curRawRegion = getRegionId();
			rawFlags = getMap().getRegion().getClippingPlanes()[myLocation().getZ()].getTileFlags();
		}
		
		return rawFlags;
	}
	
	public boolean rescan() {
		rescan = true;
		return scan();
	}
	
	public boolean scan() {
		if (rescan || !regionScanned()) {
			if (!getClient().isLoggedIn() || getClient().getLoginStateValue() != 30)
				return false;
			try {
				resetLocalCache();
				
				rescan = false;
				
				debug("Started.");
				
				if (!regionScanned()) {
					scannedRegions.clear();
					scannedRegions.add(getCurrent());
				}
				
				rawFlags = getRawFlags();
				newFlag = 0;
				
				updateObjectCache();
				
				for (int a = REGION_MIN; a < REGION_MAX; a++) {
					for (int b = REGION_MIN; b < REGION_MAX; b++) {
						if (getClient().getLoginStateValue() != 30) {
							warning("Games login state unexpectedly changed to "+getClient().getLoginStateValue());
							return false;
						}
						
						scanTile(a, b);
					}
				}
			} catch (Exception e) {
				exception(e);;
			}
			// */
			
			for (Callable<Boolean> u : regionUpdates)
				try {
					u.call();
				} catch (Exception e) {
					exception(e);;
				}
			
			debug("Scanned region");
		
		}
		
		return true;
	}
	
	private void scanTile(int a, int b) {
		flag = rawFlags[a][b];
		newFlag = Flags.FLAG_ACCESSED;
		
		if ((flag & RawFlags.BLOCKED) != 0 && !canInteract(getWalker().tile(a + getMap().getBaseX(), b + getMap().getBaseY(), getMap().getPlane()), 0, 0)) {
			newFlag = newFlag | Flags.FLAG_BLOCKED;
		} else {
			if ((flag & RawFlags.WALL_NORTH) != 0
					|| (rawFlags[a][b + 1] & RawFlags.BLOCKED) != 0) {
				if (!canInteract(getWalker().tile(a + getMap().getBaseX(), b + getMap().getBaseY(), getMap().getPlane()), 0, 1))
					newFlag = newFlag | Flags.FLAG_NORTH_BLOCKED;
				else
					newFlag = newFlag | Flags.FLAG_OBSTACLE;
			}
			if ((flag & RawFlags.WALL_EAST) != 0
					|| (rawFlags[a + 1][b] & RawFlags.BLOCKED) != 0) {
				if (!canInteract(getWalker().tile(a + getMap().getBaseX(), b + getMap().getBaseY(), getMap().getPlane()), 1, 0))
					newFlag = newFlag | Flags.FLAG_EAST_BLOCKED;
				else
					newFlag = newFlag | Flags.FLAG_OBSTACLE;
			}
			if ((flag & RawFlags.WALL_SOUTH) != 0
					|| (rawFlags[a][b - 1] & RawFlags.BLOCKED) != 0) {
				if (!canInteract(getWalker().tile(a + getMap().getBaseX(), b + getMap().getBaseY(), getMap().getPlane()), 0, -1))
					newFlag = newFlag | Flags.FLAG_SOUTH_BLOCKED;
				else
					newFlag = newFlag | Flags.FLAG_OBSTACLE;
			}
			if ((flag & RawFlags.WALL_WEST) != 0
					|| (rawFlags[a - 1][b] & RawFlags.BLOCKED) != 0) {
				if (!canInteract(getWalker().tile(a + getMap().getBaseX(), b + getMap().getBaseY(), getMap().getPlane()), -1, 0))
					newFlag = newFlag | Flags.FLAG_WEST_BLOCKED;
				else
					newFlag = newFlag | Flags.FLAG_OBSTACLE;
			}
	        if ((rawFlags[a + 1][b - 1] & RawFlags.BLOCKED) != 0 || (
	        			!((rawFlags[a][b - 1] & RawFlags.BLOCKED) == 0
						    && (rawFlags[a][b] & RawFlags.WALL_SOUTHEAST) == 0
						    && (rawFlags[a][b] & RawFlags.WALL_SOUTH) == 0
							&& (rawFlags[a][b] & RawFlags.WALL_SOUTH) == 0
			        		&& (rawFlags[a][b - 1] & RawFlags.WALL_NORTH) == 0
			        		&& (rawFlags[a][b - 1] & RawFlags.WALL_EAST) == 0
			        		&& (rawFlags[a + 1][b - 1] & RawFlags.WALL_WEST) == 0
	        			) && !((rawFlags[a + 1][b] & RawFlags.BLOCKED) == 0
			        		&& (rawFlags[a][b] & RawFlags.WALL_EAST) == 0
			        		&& (rawFlags[a + 1][b] & RawFlags.WALL_WEST) == 0
			        		&& (rawFlags[a + 1][b] & RawFlags.WALL_SOUTH) == 0
			        		&& (rawFlags[a + 1][b - 1] & RawFlags.WALL_NORTH) == 0)
	            	)) {
	        	newFlag = newFlag | Flags.FLAG_SOUTHEAST_BLOCKED;
	        }
	        if (a > 0 && b > 0
			        && (rawFlags[a - 1][b - 1] & RawFlags.BLOCKED) != 0 || (
	        			!((rawFlags[a][b - 1] & RawFlags.BLOCKED) == 0
			        		&& (rawFlags[a][b] & RawFlags.WALL_SOUTH) == 0
			        		&& (rawFlags[a][b - 1] & RawFlags.WALL_NORTH) == 0
			        		&& (rawFlags[a][b - 1] & RawFlags.WALL_WEST) == 0
			        		&& (rawFlags[a - 1][b - 1] & RawFlags.WALL_EAST) == 0
	        			) && !((rawFlags[a - 1][b] & RawFlags.BLOCKED) == 0
			        		&& (rawFlags[a][b] & RawFlags.WALL_WEST) == 0
			        		&& (rawFlags[a - 1][b] & RawFlags.WALL_EAST) == 0
			        		&& (rawFlags[a - 1][b] & RawFlags.WALL_SOUTH) == 0
			        		&& (rawFlags[a - 1][b - 1] & RawFlags.WALL_NORTH) == 0)
	            	)) {
	        	newFlag = newFlag | Flags.FLAG_SOUTHWEST_BLOCKED;
	        }
	        if ((rawFlags[a - 1][b + 1] & RawFlags.BLOCKED) != 0 || (
	        			!((rawFlags[a][b + 1] & RawFlags.BLOCKED) == 0
			        		&& (rawFlags[a][b] & RawFlags.WALL_NORTH) == 0
			        		&& (rawFlags[a][b + 1] & RawFlags.WALL_SOUTH) == 0
			        		&& (rawFlags[a][b + 1] & RawFlags.WALL_WEST) == 0
			        		&& (rawFlags[a - 1][b + 1] & RawFlags.WALL_EAST) == 0
	        			) && !((rawFlags[a - 1][b] & RawFlags.BLOCKED) == 0
			        		&& (rawFlags[a][b] & RawFlags.WALL_WEST) == 0
			        		&& (rawFlags[a - 1][b] & RawFlags.WALL_EAST) == 0
			        		&& (rawFlags[a - 1][b] & RawFlags.WALL_NORTH) == 0
			        		&& (rawFlags[a - 1][b + 1] & RawFlags.WALL_SOUTH) == 0)
	            	)) {
	        	newFlag = newFlag | Flags.FLAG_NORTHWEST_BLOCKED;
	        }
	        if ((rawFlags[a + 1][b + 1] & RawFlags.BLOCKED) != 0 || (
	        			!((rawFlags[a][b + 1] & RawFlags.BLOCKED) == 0
			        		&& (rawFlags[a][b] & RawFlags.WALL_NORTH) == 0
			        		&& (rawFlags[a][b + 1] & RawFlags.WALL_SOUTH) == 0
			        		&& (rawFlags[a][b + 1] & RawFlags.WALL_EAST) == 0
			        		&& (rawFlags[a + 1][b + 1] & RawFlags.WALL_WEST) == 0
	        			) && !((rawFlags[a + 1][b] & RawFlags.BLOCKED) == 0
			        		&& (rawFlags[a][b] & RawFlags.WALL_EAST) == 0
			        		&& (rawFlags[a + 1][b] & RawFlags.WALL_WEST) == 0
			        		&& (rawFlags[a + 1][b] & RawFlags.WALL_NORTH) == 0
			        		&& (rawFlags[a + 1][b + 1] & RawFlags.WALL_SOUTH) == 0)
	            	)) {
	        	newFlag = newFlag | Flags.FLAG_NORTHEAST_BLOCKED;
	        }// */
		}
		
		flags[a][b] = newFlag;
	}

	public void updateObjectCache() {
		objCache.clear();
		objRefCache.clear();
		
		for (RS2Object obj : getObjects().getAll()) {
			for (Obstacle obstacle : getWalker().getLocalObstacles()) {
				if (obstacle.isActive(obj, false)) {
					objCache.add(obj);
					if (!objRefCache.containsKey(obj.getX()))
						objRefCache.put(obj.getX(), new HashMap<Integer, ArrayList<RS2Object>>());
					if (!objRefCache.get(obj.getX()).containsKey(obj.getY()))
						objRefCache.get(obj.getX()).put(obj.getY(), new ArrayList<RS2Object>());
					objRefCache.get(obj.getX()).get(obj.getY()).add(obj);
					break;
				}
			}
		}
	}

	public List<RS2Object> getObjectCache() {
		return objCache;
	}

	public List<RS2Object> getObjectCache(int x, int y) {
		if (!objRefCache.containsKey(x)
				|| !objRefCache.get(x).containsKey(y))
			return new ArrayList<RS2Object>();
		return objRefCache.get(x).get(y);
	}
	
	private boolean regionScanned() {
		if (quickRegion != null && quickRegion == getCurrent())
			return true;
		for (String s : scannedRegions) {
			if (s.equals(getCurrent())) {
				quickRegion = getCurrent();
				return true;
			}
		}
		return false;
	}

	public String getId2(int x, int y, int z) {
		return ""+Base64.encode(x)
				+Base64.encode(y)
				+Base64.encode(z);
	}
	
	public String getId(Tile t) {
		return ""+encodeInt(t.x)
				+encodeInt(t.y)
				+encodeInt(t.z);
	}
	
	private String encodeInt(Integer integer) {
		if (!intEncodeCache.containsKey(integer))
			intEncodeCache .put(integer, Base64.encode(integer));
		return intEncodeCache.get(integer);
	}

	public boolean canInteract(Tile t) {
		return canInteract(t, 0, 0);
	}

	public boolean canInteract(Tile t, int offsetX, int offsetY) {
		if (!canInteractTile(t)
				&& ((offsetX == 0 && offsetY == 0) || !canInteractTile(t.translate(offsetX, offsetY))))
			return false;
		return true;
	}
	
	private boolean canInteractTile(Tile t) {
		if (!objRefCache.containsKey(t.x)
				|| !objRefCache.get(t.x).containsKey(t.y))
			return false;
		for (RS2Object obj : objRefCache.get(t.x).get(t.y)) {
			if (obj == null || !obj.exists())
				continue;
			
			for (Obstacle obstacle : getWalker().getLocalObstacles()) {
				if (obstacle.isActive(obj, false)) {
					return processInteract(obj);
				}
			}
		}
		return false;
	}
	
	protected boolean processInteract(RS2Object obj) {
		tmpId = obj.getId();
		//if (canInteractCache[tmpId] != false) {
		//	return canInteractCache[tmpId];
		//}
		
		canInteract = false;
		for (Obstacle o : getWalker().getLocalObstacles()) {
			if (o.isActive(obj, false)) {
				canInteract = true;
				break;
			}
		}
		canInteractCache[tmpId] = canInteract;

		return canInteract;
	}
	
	private String getRegionId() {
		return getMap().getBaseX()+","+getMap().getBaseY()+","+getMap().getPlane();
	}

	private double distanceBetween(Node t, Node t2) {
		// The cheap stuff, used only for radial search anyways
		return Math.abs(t.x - t2.x) + Math.abs(t.y - t2.y);
	}
	
	public ArrayList<Node> radiusSearch(Node t, int radius) {
		return radiusSearch(t, new ArrayList<Node>(), radius);
	}
	
	public ArrayList<Node> radiusSearch(Node t, ArrayList<Node> list, int radius) {
		if (list.size() > 0 && (distanceBetween(t, list.get(0)) > radius || list.contains(t)))
			return list;
		list.add(t);
		final int x = (int) t.x, y = (int) t.y, z = (int) t.z;
		final int f_x = x, f_y = y;
		final int here = getRawFlag(f_x, f_y),
				south = getRawFlag(f_x, f_y - 1),
				north = getRawFlag(f_x, f_y + 1),
				east = getRawFlag(f_x + 1, f_y),
				west = getRawFlag(f_x- 1, f_y );
		if ((here & RawFlags.WALL_SOUTH) == 0
				&& (south & RawFlags.BLOCKED) == 0
				&& (south & RawFlags.WALL_NORTH) == 0
				&& !listContainsTile(list, new Node(x, y - 1, z))) {
			list = radiusSearch(new Node(x, y - 1, z), list, radius);
		}
		if ((here & RawFlags.WALL_WEST) == 0
				&& (west & RawFlags.BLOCKED) == 0
				&& (west & RawFlags.WALL_EAST) == 0
				&& !listContainsTile(list, new Node(x - 1, y, z))) {
			list = radiusSearch(new Node(x - 1, y, z), list, radius);
		}
		if ((here & RawFlags.WALL_NORTH) == 0
				&& (north & RawFlags.BLOCKED) == 0
				&& (north & RawFlags.WALL_SOUTH) == 0
				&& !listContainsTile(list, new Node(x, y + 1, z))) {
			list = radiusSearch(new Node(x, y + 1, z), list, radius);
		}
		if ((here & RawFlags.WALL_EAST) == 0
				&& (east & RawFlags.BLOCKED) == 0
				&& (east & RawFlags.WALL_WEST) == 0
				&& !listContainsTile(list, new Node(x + 1, y, z))) {
			list = radiusSearch(new Node(x + 1, y, z), list, radius);
		}
		return list;
	}
	
	private boolean listContainsTile(List<Node> list, Node t) {
		for (Node t2 : list) {
			if (t.x == t2.x && t.y == t2.y && t.z == t2.z)
				return true;
		}
		return false;
	}

	public String getCurrent() {
		return getWalker().tile(getMap().getBaseX(), getMap().getBaseY(), getMap().getPlane()).uid;
	}

	public int getX() {
		return getMap().getBaseX();
	}

	public int getY() {
		return getMap().getBaseY();
	}
	
	public int getZ() {
		return getMap().getPlane();
	}

	public void onUpdate(Callable<Boolean> listener) {
		regionUpdates.add(listener);
	}

	public boolean contains(Tile t) {
		if (t == null)
			return false;
		return t.x > getX() + REGION_MIN && t.x < getX() + REGION_MAX
				&& t.y > getY() + REGION_MIN && t.y < getY() + REGION_MAX
				&& t.z == getZ();
	}

	

	public Tile findEnd(Tile end) {
    	//log2("Finding end tile...");
		if ((getFlag(end) & Flags.FLAG_BLOCKED) == 0) {
			return end;
		}
		Tile n = null;
		Tile b = null;
		tiles2 = new Tile[]{
			end.translate(1, 0),
			end.translate(-1, 0),
			end.translate(0, 1),
			end.translate(0, -1)
		};
		for (Tile t : tiles2) {
			int f, f2;
			if (t.y == end.y)
				if (t.x < end.x) {
					f = RawFlags.WALL_EAST;
					f2 = RawFlags.WALL_WEST;
				} else {
					f = RawFlags.WALL_WEST;
					f2 = RawFlags.WALL_EAST;
				}
			else
				if (t.y < end.y) {
					f = RawFlags.WALL_NORTH;
					f2 = RawFlags.WALL_SOUTH;
				} else { 
					f = RawFlags.WALL_SOUTH;
					f2 = RawFlags.WALL_NORTH;
				}
			
			if ((getFlag(t) & Flags.FLAG_BLOCKED) != 0) {
				//debug(t.toString()+" is blocked");
				continue;
			}
			if (n != null && n.dist() < t.dist()) {
				//debug(t.toString()+" is further away");
				continue;
			}
			if (!t.canReach(false)) {
				//debug(t.toString()+" can't be reached!");
				continue;
			}
			// Set this as best w/o a wall check
			b = t;
			
			if ((getRawFlag(t) & f) != 0) {
				//debug(t.toString()+" is blocked via a wall (#1)");
				continue;
			}
			if ((getRawFlag(end) & f2) != 0) {
				//debug(t.toString()+" is blocked via a wall (#2)");
				continue;
			}
			
			n = t;
		}
		if (n != null)
			return n;
		//if (b != null)
		//	return b;
		warning("Unable to find an end for "+end.toString()+", might cause issues.");
		return end;
	}

	public String getCurrentStr() {
		return getX()+","+getY()+","+getZ();
	}

	public Tile findClosestEnd(Tile t) {
		if (t == null) {
			warning("Cannot findClosestEnd(null)!!!");
			return null;
		}
		if (!contains(t) || t.canReach(false))
			return t;
		
		Tile s = null;
		for (int i = 0; i < 7 && s == null; i++) {
			// Make a pass on the x side
			for (int x = t.x - i; x < t.x + i; x++) {
				s = findBestClosestEnd(s, getWalker().tile(x, t.y, t.z));
			}
			// Make a pass on the y side
			for (int y = t.y - i + 1; y < t.y + i - 1; y++) {
				s = findBestClosestEnd(s, getWalker().tile(t.x, y, t.z));
			}
		}
		return s != null ? s : t;
	}

	private Tile findBestClosestEnd(Tile s, Tile tile) {
		if (!tile.canReach(false))
			return s;
		
		if (s == null || s.dist() > tile.dist())
			return tile;
		
		return s;
	}

	public boolean contains(Area location) {
		Rectangle r = location.getPolygon().getBounds();
		return contains(getWalker().tile(r.x, r.y, location.getPlane()))
				&& contains(getWalker().tile(r.x + r.width, r.y + r.height, location.getPlane()));
	}

	public boolean contains(Zone location) {
		Rectangle r = location.getBounds();
		return contains(getWalker().tile(r.x, r.y, location.getPlane()))
				&& contains(getWalker().tile(r.x + r.width, r.y + r.height, location.getPlane()));
	}
	
}