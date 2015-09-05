package lemons.api.script.interaction;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;

import lemons.api.script.TaskScript;
import lemons.api.script.emulators.TaskScriptEmulator;
import lemons.api.script.entities.Character;
import lemons.api.script.entities.NPC;
import lemons.api.script.entities.Player;
import lemons.api.script.entities.RS2Object;
import lemons.api.script.entities.WallObject;
import lemons.api.utils.Timer;
import lemons.api.walking.LocalPath;
import lemons.api.walking.listeners.EntityListener;
import lemons.api.walking.map.Tile;
import lemons.api.walking.obstacles.Obstacle;

import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.ui.Option;
import org.osbot.rs07.api.util.GraphicUtilities;
import org.osbot.rs07.event.CameraPitchEvent;
import org.osbot.rs07.event.CameraYawEvent;

public class Interact extends TaskScriptEmulator<TaskScript> {

	public static final float RADIUS = 4;

	private static final int INTERACT_SIZE_MIN = 4,
						INTERACT_DISTANCE = 6,
						INTERACT_BUFFER = 2;
	private boolean firstAction;

	private int index;

	private LocalPath preparePath;

	private Area gameArea = new Area(new Rectangle(5, 5, 510, 330));
	
	SimpleDateFormat file_sdf = new SimpleDateFormat("YYYY-MM-dd_HH-mm-ss");

	private String strAction;

	private String strName;

	//private boolean useSelf = false;

	private Rectangle tmpBounds;
	
	private Color c;

	private boolean isObstacle;

	private AffineTransform tr;

	private Area tmpArea4;
	
	private ArrayList<Point> openPoints = new ArrayList<Point>();

	private Area a;

	private Area processedArea;

	private Area rawArea;

	private Area playerArea;

	private Area npcArea;

	private Timer timer = new Timer(3000);

	private boolean cleared;

	private boolean fromEntityWith;
	
	public Interact(TaskScript s) {
		super(s);
	}
	
	public boolean canInteract(Position translate) {
		return translate.isVisible(getBot()) || !tile(translate).canReach(true);
	}
	
	public boolean canInteract(Tile translate) {
		return translate.isVisible() || !translate.canReach(true);
	}

	public boolean canInteract(Entity entity) {
		if (entity == null || !entity.exists() || !getWalker().findEntityEnd(entity).canReach(true))
			return false;
		
		return isVisible(entity) && tile(entity).distanceTo() <= INTERACT_DISTANCE;
	}
	
	/* * * * * * * * * * * * * * * * * * * * *
	 *                                       *
	 *      Entity Visibility Checks         *
	 *                                       * 
	 * * * * * * * * * * * * * * * * * * * * */

	public boolean isVisible(Entity entity) {
		a = getArea(entity, false, false);
		return a != null && !a.isEmpty();
		//if (entity != null && "Fishing spot".equals(entity.getName()))
		//	return getWalker().tile(entity).isVisible();
		
		//return entity.isVisible();
	}

	public boolean isVisible(Tile tile) {
		return tile.isVisible();
	}
	
	/* * * * * * * * * * * * * * * * * * * * *
	 *                                       *
	 *      Entity Hover                     *
	 *                                       * 
	 * * * * * * * * * * * * * * * * * * * * */
    
    public boolean hover(final Entity entity) {
    	return hover(entity, true);
    }
    public boolean hover(final Entity entity, boolean correct) {
    	return hover(getArea(entity, correct, true));
    }
		
	private boolean hover(Tile t) {
		return hover(t, INTERACT_BUFFER, true);
	}
		
	private boolean hover(Tile t, int shrink) {
		return hover(t, shrink, true);
	}
	
	private boolean hover(Tile t, int shrink, boolean saveArea) {
		Area area = getArea(t, true, saveArea);
		if (saveArea) {
			cleared = true;
			processedArea = area;
		}
		if (!hover(area)) {
			try {
				getMouse().moveRandomly();
				warning("Randomly moving mouse as hover failed...");
			} catch (InterruptedException e) {
				exception(e);
			}
			return false;
		}
		return true;
	}
	
	private boolean hover(Area tmpArea) {
		if (tmpArea == null)
			return false;
			
		Point randomPoint = getRandomPoint(tmpArea);
		
		if (randomPoint != null) {
			getMouse().move(randomPoint.x, randomPoint.y);
			return tmpArea.contains(getMouse().getPosition());
		}
		warning("Failed to find a random point for hover() (area was "+(tmpArea.isEmpty()?"":"not")+" empty)");
		return false;
	}
	
	public Point getRandomPoint(Area tmpArea) {
		tmpBounds = tmpArea.getBounds();
		
		debug("Bounds are "+tmpBounds.x+","+tmpBounds.y+" "+tmpBounds.width+"x"+tmpBounds.height);
		
		openPoints.clear();
		
		for (int x = tmpBounds.x; x < tmpBounds.x + tmpBounds.width; x++) {
			for (int y = tmpBounds.y; y < tmpBounds.y + tmpBounds.height; y++) {
				if (tmpArea.contains(x, y))
					openPoints.add(new Point(x, y));
			}
		}
		
		if (openPoints.size() > 0) {
			Collections.shuffle(openPoints);
			Collections.shuffle(openPoints);
			
			return openPoints.get(0);
		}
		return null;
	}

	public Area getProcessedArea() {
		return processedArea;
	}
	
	public Area getRawArea() {
		return rawArea;
	}
	
	public Area getPlayerArea() {
		return playerArea;
	}
	
	public Area getNpcArea() {
		return npcArea;
	}
	
	
	/* * * * * * * * * * * * * * * * * * * * *
	 *                                       *
	 *      Entity Interaction               *
	 *                                       * 
	 * * * * * * * * * * * * * * * * * * * * */
	
	public boolean tile(Tile t, String... actions) {
		return tile(t, INTERACT_BUFFER, actions);
	}
	
	public boolean tile(Tile t, int shrink, String... actions) {
		if (!hover(t, shrink)) {
			debug("Failed to hover tile "+t.toString());
			return false;
		}
		
		String action = actions[0];
		
		if (action == null || menuIndex(action) == 0) {
			if (getMouse().click(false)) {
				debug("Interacted with "+t.toString());
				return checkColor(action.equals("Walk here"));
			} else {
				debug("Failed to click tile "+t.toString());
				return false;
			}
		}
		
		debug("Interacting via Menu with "+t.toString());
		getMouse().click(true);
		
		return menuSelect(action);
	}
	public boolean entityWith(Entity entity, String action, String... itemNames) {
		return entityWith(entity, action, getInventory().getItem(itemNames));
	}
	public boolean entityWith(Entity entity, String action, Item i) {
		if (!interactPrepareWalk(entity)) {
			debug("Walking to or couldn't walk to "+entity.getName()+" at "+getWalker().tile(entity).toString());
			return false;
		}
		
		if (!getInventory().isItemSelected() || !getInventory().getSelectedItemName().equals(i.getName())) {
		
			debug("Selecting item "+i.getName());
			deselect(i.getName());
			
			// Select one of the items
			
			if ((action.equals("Use") || i.hasAction(action)) && i.interact(action)) {
				sleep(3000, () -> !getInventory().isItemSelected());
			}
			
		}
		if (!getInventory().isItemSelected())
			return false;
		
		if (!getInventory().getSelectedItemName().equals(i.getName())) {
			return false;
		}
		
		debug("Starting interact with "+entity.getName());
		
		fromEntityWith = true;
		return entity(entity, action);
	}

	private boolean deselect(String... sttr) {
		if (!getInventory().isItemSelected())
			return false;
		for (String s : sttr)
			if (s.equals(getInventory().getSelectedItemName()))
				return false;
		
		debug("The item "+getInventory().getSelectedItemName()+" should not be selected!");
		myLocation().interact();
		sleep(500, 1000);
		return true;
	}
	
	public boolean entity(final Entity entity, String action) {
		return interact(entity, true, action);
	}
	
	public boolean entity(Entity entity, boolean prepareWalk, String action) {
		return interact(entity, prepareWalk, false, action);
	}
	
	public boolean entity(Entity entity, boolean prepareWalk, boolean useTile, String action) {
		boolean usingItem = fromEntityWith;
		fromEntityWith = false;
		if (entity == null) {
			warning("Null was passed as entity!");
			return false;
		}
		
		String name;
		
		if (!usingItem && getInventory().isItemSelected()) {
			myLocation().interact(null);
		}
		
		if (getInventory().isItemSelected()) {
			name = getInventory().getSelectedItemName()+" -> "+entity.getName();
		} else {
			name = entity.getName();
		}
		
		// Find a copy of this entity
		//entity = confirmEntity(entity);
		
		if (prepareWalk && !interactPrepareWalk(entity)) {
			debug("Walking to or couldn't walk to "+entity.getName()+" at "+getWalker().tile(entity).toString());
			return false;
		}
		
		debug("Starting interact with "+entity.getName());
		
		if (!interactPrepareCamera(entity, useTile, false)) {
			warning("Camera failed to get "+entity.getName()+" in the viewport!");
		}
		
		debug("Camera positioned for "+entity.getName());
		
		for (int i = 0; i < 5; i++) {
			if (useTile ? !hover(getWalker().tile(entity)) : !hover(entity)) {
				if (i == 0 && !interactPrepareCamera(entity, useTile, true)) {
					warning("Forced camera failed to get "+entity.getName()+" in the viewport!");
				}
				warning("Failed to hover "+entity.getName()+" ("+(i+1)+"/5)!");
				continue;
			}
			
			// Sleep while were not in the model
			for (int b = 0; b < 100 && !getInteract().menuContains(action, name)
					&& !getModelArea(entity).contains(getMouse().getPosition()); b++)
				sleep(10);
			
			if (!getModelArea(entity).contains(getMouse().getPosition())) {
				debug("Mouse was not over the entity!");
				continue;
			}
			
			if (!getInteract().menuContains(action, name)) {
				debug("Missed the target (or no action \""+action+"\" was found for) "+entity.getName()+" ("+(i+1)+"/5)!");
				continue;
			}
			debug("Hover was successful!");
			break;
		}
		
		debug("Hover was called "+entity.getName());
		
		if (interactClick(entity, action, name)) {
			debug("Interact finished for "+entity.getName());
			return true;
		}
		
		warning("Failed to interact with "+entity.getName());
		
		failed();
		
		return false; // */
	}
	
	public boolean rectangle(Rectangle r, String action) {
		
		int x = random(r.x, r.x + r.width),
			y = random(r.y, r.y + r.height);
		
		getMouse().move(x, y);
		if (r.contains(getMouse().getPosition())) {
			int index = action == null ? 0 : menuIndex(action);
			if (index == 0) {
				getMouse().click(false);
				return true;
			} else if (index > 0 && getMouse().click(true)) {
				return menuSelect(menuIndex(action));
			}
		}
		
		return false;
	}
		
	/* * * * * * * * * * * * * * * * * * * * *
	 *                                       *
	 *      Entity Area Calculations         *
	 *                                       * 
	 * * * * * * * * * * * * * * * * * * * * */

	public Area getArea(Entity entity, boolean correct, boolean saveArea) {
		return getArea(entity, correct, INTERACT_BUFFER, saveArea);
	}

	public Area getArea(Entity entity, boolean correct, int shrink, boolean saveArea) {
		Area area = getEntityArea(entity, correct, shrink, saveArea);
		if (saveArea) {
			cleared = true;
			processedArea = area;
		}
		return area;
	}
	
	public Area getArea(Tile tile, boolean correct, boolean saveArea) {
		return getArea(tile, correct, INTERACT_BUFFER, saveArea);
	}
	
	public Area getArea(Tile tile, boolean correct, int shrink, boolean saveArea) {
		Area area = getTileArea(tile, correct, shrink, saveArea);
		if (saveArea) {
			cleared = true;
			processedArea = area;
		}
		return area;
	}
	
	private Area getModelArea(Entity entity) {
		return GraphicUtilities.getModelArea(getBot(),
				entity.getGridX(), entity.getGridY(), myPlayer().getZ(), entity.getModel());
	}
	
	private Area getTileArea(Tile tile) {
		return new Area(tile.pos().getPolygon(getBot()));
	}
	
	private Area getTileArea(Tile tile, boolean correct, int shrink, boolean saveArea) {
		if (tile == null)
			return null;
		
		Area area = new Area(getTileArea(tile));
		
		if (saveArea)
			rawArea = (Area) area.clone();
		
		if (correct) {
			area = predictPlayerArea(area);
			if (saveArea) 
				playerArea = (Area) area.clone();
		}
		
		area = shrinkArea(area, shrink);
		
		if (saveArea) 
			processedArea = (Area) area.clone();
		
		return area;
	}
	
	private Area getEntityArea(Entity entity, boolean correct, int shrink, boolean saveArea) {
		if (entity == null || !entity.exists())
			return null;
		
		Area area = getModelArea(entity);
		
		if (saveArea)
			rawArea = (Area) area.clone();
		
		if (correct) {
			area = predictPlayerArea(area);
			if (saveArea) 
				playerArea = (Area) area.clone();
			area = predictEntityArea(entity, area);
			if (saveArea) 
				npcArea = (Area) area.clone();
		}
		
		area = shrinkArea(area, shrink);
		
		if (saveArea) 
			processedArea = (Area) area.clone();
		
		return area;
	}
	
	private Area predictEntityArea(Entity entity, Area area) {
		if (entity instanceof NPC && ((NPC) entity).isMoving()) {
			int[] dir = getMovementOffset((NPC) entity);
			
			area = movementOffset(entity, area, getWalker().tile(entity).translate(dir[0], dir[1]));
		}
		return area;
	}
	
	private Area predictPlayerArea(Area area) {
		if (myPlayer().isMoving()) {
			int[] dir = getMovementOffset(myPlayer());
			
			area = movementOffset(myPlayer(), area, myLocation().translate(dir[0] * -1, dir[1] * -1));
		}
		return area;
	}

	private Area shrinkArea(Area area, int amount) {
		// Format the click area
		int i = amount;
		tmpArea4 = (Area) area.clone();
		do {
			tmpArea4 = shrinkArea(tmpArea4, i, 1, 1);
			tmpArea4 = shrinkArea(tmpArea4, i, -1, -1);
			tmpArea4 = shrinkArea(tmpArea4, i, 1, -1);
			tmpArea4 = shrinkArea(tmpArea4, i, -1, 1);
		} while (i-- >= 0 && !tmpArea4.isEmpty());
		if (!tmpArea4.isEmpty()
				&& tmpArea4.getBounds().width > INTERACT_SIZE_MIN
				&& tmpArea4.getBounds().height > INTERACT_SIZE_MIN)
			area = tmpArea4;
		area.intersect(gameArea);
		return area;
	}

	private Area shrinkArea(Area area, int b, int i, int j) {
		tr = new AffineTransform();
		tr.translate(i * b, j * b);
		area.intersect(new Area(tr.createTransformedShape(area)));
		return area;
	}

	private Area movementOffset(Entity entity, Area area, Tile lastTile) {
		Tile curTile = getWalker().tile(entity);
		int cx = curTile.getPointOnScreen().x,
			cy = curTile.getPointOnScreen().y,
			nx = lastTile.getPointOnScreen().x,
			ny = lastTile.getPointOnScreen().y;
		
		if (entity.getSizeX() > 1 || entity.getSizeY() > 1) {
			// Loop through all the tiles
			int len = 0, ax = 0, ay = 0;
			for (int x = entity.getX(); x < entity.getX() + entity.getSizeX(); x++) {
				for (int y = entity.getY(); x < entity.getY() + entity.getSizeY(); y++) {
					// This is a tile!
					ax += getWalker().tile(x, y, entity.getZ()).getPointOnScreen().x;
					ay += getWalker().tile(x, y, entity.getZ()).getPointOnScreen().y;
				}
			}
			int cx2 = ax / len,
				cy2 = ay / len;
			
			nx += cx2 - cx;
			ny += cy2 - cy;
			
			cx = cx2;
			cy = cy2;
		}
		
		double dx = nx - cx, dy = ny - cy;
		
		return new Area(AffineTransform.getTranslateInstance(dx, dy)
				.createTransformedShape(area));
	}

	@SuppressWarnings("rawtypes")
	private int[] getMovementOffset(Character character) {
		/*int r = -1;
		for (int i = 0; i < 20 && r != character.getRotation(); i++) {
			r = character.getRotation();
			Time.sleep(100);
		}*/
		return getMovementOffsetFromRotation(character.getRotation());
	}

	private int[] getMovementOffsetFromRotation(int rotation) {
		int x = 0, y = 0;
		
		if (rotation < 127) {
			// South
			x = 0;
			y = -1;
		} else if (rotation < 383) {
			// South west
			x = -1;
			y = -1;
		} else if (rotation < 639) {
			// West
			x = -1;
			y = 0;
		} else if (rotation < 895) {
			// North west
			x = -1;
			y = 1;
		} else if (rotation < 1153) {
			// North
			x = 0;
			y = 1;
		} else if (rotation < 1409) {
			// North east
			x = 1;
			y = 1;
		} else if (rotation < 1665) {
			// East
			x = 1;
			y = 0;
		} else if (rotation < 1921) {
			// South east
			x = 1;
			y = -1;
		} else {
			// South again
			x = 0;
			y = -1;
		}
		
		return new int[]{x,y};
	}

	private boolean interactClick(Entity entity, String action, String name) {
		firstAction = true;
		index = 0;
		
		// Quick check if the entity is still there
		if (!getModelArea(entity).contains(getMouse().getPosition())) {
			debug("Missed interaction with "+entity.getName());
			return false;
		}
		
		for (Option o : getMenuAPI().getMenu()) {
			if (index > 26)
				break; // Cant see this option
			
			if (action == null || compareActions(o, action, name)) {
				if (firstAction) {
					if (!getMouse().click(false)) {
						failed();
						return false;
					}
									
					return checkColor(action.equalsIgnoreCase("Walk here"));
				}
				
				if (getMenuAPI().isOpen() || getMouse().click(true)) {
					sleep(200, 500);
					if (!menuSelect(menuIndex(action, name))) {
						failed();
						return false;
					}
					
					return checkColor(action.equalsIgnoreCase("Walk here"));
				} 
				
				failed();
				return false; // Failed, don't wait
			}
			firstAction = false;
			index++;
		}
		return false;
	}

	private boolean interactPrepareCamera(Entity entity, boolean useTile, boolean force) {
		if (getWalker().tile(entity).dist() > INTERACT_DISTANCE && !isVisible(entity)) {
			debug("Too far away to try and use the camera, should walk!");
			return false;
		}
		
		if (isVisible(entity) && !force) {
			// Camera already good
			return true;
		}
		
		debug("Moving camera to entity "+entity.getName());
		
		// Calculate Yaw
		int tx = getWalker().tile(entity).x, ty = getWalker().tile(entity).y;
		int mx = myLocation().x, my = myLocation().y;
		int deltaY = ty - my;
		int deltaX = tx - mx;
		int degs;
		degs = (int)(Math.atan2(deltaY, deltaX) * 180 / Math.PI) - 90;
		int val = (degs - 30 + random(0, 60)) % 360;
		if (val < 0)
			val = 360 + val;
		
		// Guess pitch based on a nasty distance algo
		
		execute(new CameraYawEvent(val).setBlocking());
		
		int c = (int) Math.max(3, Math.floor(tile(entity).distanceTo() / 3));
		if (useTile ? !getWalker().tile(entity).isVisible() : !isVisible(entity)) {
			debug("Could not view object using Yaw, use pitch?");
			execute(new CameraPitchEvent(
				67 - (int) random(
						5 * c,
						5 * c + 5)).setBlocking());
		}
		
		return useTile ? getWalker().tile(entity).isVisible() : isVisible(entity);
	}

	private boolean interactPrepareWalk(Entity entity) {
		Tile tileTmp;
		if (entity instanceof WallObject) {
			tileTmp = getWalker().findClosestWallTile((WallObject) entity);
		} else {
			//tileTmp = getWalker().getRegion().findEnd(getWalker().tile(entity));
			//if (tileTmp == null)
			tileTmp = getWalker().findEntityEnd(entity);
		}
		
		if (!canInteract(entity)) {
			debug("Trying to walk to "+entity.getName()+" at "+getWalker().tile(entity).toString());
			isObstacle = false;
			if (entity instanceof RS2Object)
				for (Obstacle o : getWalker().getLocalObstacles()) {
					if (o.isActive((RS2Object) entity, false))
						isObstacle = true;
				}
			debug("Creating local path to an obstacle? "+(isObstacle ? "Yes" : "No"));
			preparePath = new LocalPath(getScript(), myLocation(), tileTmp, isObstacle);
			getWalker().setLastPath(preparePath);
			preparePath.setListener(new EntityListener(getScript(), entity));
			if (!preparePath.valid())
				return false;
			preparePath.walk();
			return canInteract(entity);
		} else {
			debug(entity.getName()+" at "+getWalker().tile(entity).toString()+" is visible and close enough");
			return true;
		}
	}

	private boolean checkColor(boolean walkingHere) {
		// We add 1 to y to avoid detecting OSBots cursor
		int x = getMouse().getPosition().x,
			y = getMouse().getPosition().y + 1,
			i = 0;
		
		do {
			sleep(10);
			c = getColorPicker().colorAt(x, y);
			if (c.getRed() == 255 && c.getGreen() == 0 && c.getBlue() == 0) {
				debug("Interact color check: "+(walkingHere ? "failure" : "success")+" (red)!");
				return walkingHere ? false : true;
			}
			if (c.getRed() == 255 && c.getGreen() == 255 && c.getBlue() == 0) {
				warning("Interact color check: "+(!walkingHere ? "failure" : "success")+" (yellow)!");
				return walkingHere ? true : false;
			}
		} while (i++ < 50);
		
		// Default to true, as no color we want was found!
		warning("Interact color check: defaulted");
		return true;
	}
	
	/* * * * * * * * * * * * * * * * * * * * *
	 *                                       *
	 *      Menu Calculations                *
	 *                                       * 
	 * * * * * * * * * * * * * * * * * * * * */
	public boolean menuSelect(String str) {
		return menuSelect(menuIndex(str));
	}
	
	public boolean menuSelect(int index) {
		this.index = index;
		
		debug("Selecting menu item "+index);
		
		for (int i = 0; i < 10 && !getMenuAPI().isOpen(); i++)
			sleep(100);
		
		int x = getMenuAPI().getX() + 3,
				y = getMenuAPI().getY() + 19 + (15 * index) + 1,
				width = getMenuAPI().getWidth() - 6,
				height = 13;
		getMouse().move(random(x, x + width), random(y, y + height));
		Point p = getMouse().getPosition();
		sleep(200, 500);
		if (p.x >= x && p.x <= x + width && p.y >= y && p.y <= y + width) {
			if (getMouse().click(false)) {
				return true;
			} else {
				debug("Failed to click the given menu item.");
			}
		} else {
			debug("Failed to move the mouse to the given menu item.");
		}
		return false;
	}
	
	public boolean b(String action) {
		return menuContains(action, "");
	}
	
	@Override
	public boolean menuContains(String action, String name) {
		return menuIndex(action, name) > -1;
	}

	public int menuIndex(String action) {
		return menuIndex(action, "");
	}

	public int menuIndex(String action, String name) {
		int index = 0;
		for (Option o : getMenuAPI().getMenu()) {
			if (index > 26)
				break; // Cant see this option
			if (compareActions(o, action, name))
				return index;
			index++;
		}
		return -1;
	}
	
	private boolean compareActions(Option o, String action2, String name2) {
		strAction = o.action.replaceAll("<[^>]+>", "");
		strName = o.name.replaceAll("<[^>]+>", "");
		return (action2.isEmpty()
				|| strAction.toLowerCase().contains(action2.toLowerCase()))
				&& (name2 == null || name2.isEmpty()
						|| strName.toLowerCase().indexOf(name2.toLowerCase()) > -1);
	}
	
	public Option getMenuOption(String action, String name) {
		for (Option o : getMenuAPI().getMenu()) {
			strAction = o.action.replaceAll("<[^>]+>", "");
			strName = o.name.replaceAll("<[^>]+>", "");
			if (action.isEmpty() || strAction.toLowerCase().contains(action.toLowerCase())
					&& (name.isEmpty() || strName.toLowerCase().indexOf(name.toLowerCase()) > -1))
					return o;
		}
		return null;
	}

	private void failed() {
		if (getMenuAPI().isOpen()) {
			// Move mouse outside menu
			int x;
			if (getMouse().getPosition().x > getMenuAPI().getX() + (getMenuAPI().getWidth() / 2)) {
				// Move to the right of the menu,
				x = getMenuAPI().getX() + getMenuAPI().getWidth() + random(0, 30);
			} else {
				x = getMenuAPI().getX() - random(0, 30);
			}
			getMouse().move(x, getMouse().getPosition().y - 15 + random(0, 30));
		}
	}

	public void clearAreas() {
		if (rawArea != null && cleared) {
			cleared = false;
			timer.reset();
		}
		
		if (!timer.isRunning()) {
			cleared = true;
			rawArea = null;
			playerArea = null;
			npcArea = null;
			processedArea = null;
		}
	}
}
