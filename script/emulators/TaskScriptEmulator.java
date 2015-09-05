package lemons.api.script.emulators;

import java.awt.Rectangle;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.Supplier;

import lemons.api.Bank;
import lemons.api.Logger;
import lemons.api.script.TaskScript;
import lemons.api.script.entities.GroundItems;
import lemons.api.script.entities.NPCS;
import lemons.api.script.entities.Objects;
import lemons.api.script.entities.Player;
import lemons.api.script.entities.Players;
import lemons.api.script.interaction.Interact;
import lemons.api.utils.Timer;
import lemons.api.walking.Walker;
import lemons.api.walking.map.Tile;

import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.utility.ConditionalSleep;

public class TaskScriptEmulator<TS extends TaskScript> extends ScriptEmulator<TS> {
	
	public TaskScriptEmulator() {
		super();
	}
	
	public TaskScriptEmulator(TS script) {
		super(script);
	}
	
	// Below are methods exposed by our API
	
	protected Player myPlayer() {
		return new Player(getScript(), getScript().myPlayer());
	}

	protected Walker getWalker() {
		return getScript().getWalker();
	}
	
	protected Logger getLogger() {
		return getScript().getLogger();
	}
	
	protected Interact getInteract() {
		return getScript().getInteract();
	}
	
	protected Objects getObjects() {
		return getScript().getObjects2();
	}
	
	protected Bank getBank() {
		return getScript().getBank2();
	}
	
	protected GroundItems getGroundItems() {
		return getScript().getGroundItems2();
	}
	
	protected NPCS getNpcs() {
		return getScript().getNpcs2();
	}
	
	protected Players getPlayers() {
		return getScript().getPlayers2();
	}
	
	protected Tile myLocation() {
		return getWalker().tile();
	}
	
	protected boolean isInGame() {
		return getClient().isLoggedIn() && getClient().getLoginStateValue() == 30
				&& (getWidgets().get(378, 13) == null || !getWidgets().get(378, 13).isVisible());
	}
	
	// Random functions
	protected long random(long a, long b)  {
		return getScript().random(a, b);
	}
	protected double random(double a, double b)  {
		return getScript().random(a, b);
	}
	protected float random(float a, float b)  {
		return getScript().random(a, b);
	}
	
	// ------- Copy/paste below this line to TaskgetScript(). Ugly, I know :(
	
	// Interact helpers
	protected boolean interact(Tile t, String name) {
		return getInteract().tile(t, name);
	}
	
	protected boolean interact(Rectangle r, String name) {
		return getInteract().rectangle(r, name);
	}
	
	protected boolean interact(Entity e, String name) {
		return getInteract().entity(e, name);
	}
	
	protected boolean interact(Entity e, boolean prepareWalk, String name) {
		return getInteract().entity(e, prepareWalk, name);
	}
	
	protected boolean interact(Entity e, boolean prepareWalk, boolean useTile, String name) {
		return getInteract().entity(e, prepareWalk, useTile, name);
	}
	
	protected boolean interactWith(Entity e, String name, Item item) {
		return getInteract().entityWith(e, name, item);
	}
	
	protected boolean interactWith(Entity e, String name, String... itemNames) {
		return getInteract().entityWith(e, name, itemNames);
	}
	
	protected boolean hover(Entity e) {
		return getInteract().hover(e);
	}
	
	protected boolean canInteract(Entity e) {
		return getInteract().canInteract(e);
	}
	
	protected boolean canInteract(Position e) {
		return getInteract().canInteract(e);
	}
	
	protected boolean canInteract(Tile e) {
		return getInteract().canInteract(e);
	}
	
	protected boolean isVisible(Entity e) {
		return getInteract().isVisible(e);
	}
	
	protected boolean isVisible(Position e) {
		return getInteract().isVisible(e);
	}
	
	protected boolean isVisible(Tile e) {
		return getInteract().isVisible(e);
	}
	
	// Menu helpers
	
	protected boolean menuContains(String action) {
		return getInteract().menuContains(action, null);
	}
	
	protected boolean menuContains(String action, String name) {
		return getInteract().menuContains(action, name);
	}
	
	// Walker helpers
	
	protected Tile tile(Entity e) {
		return getWalker().tile(e);
	}
	
	protected Tile tile(Position e) {
		return getWalker().tile(e);
	}
	
	protected Tile tile(int x, int y, int z) {
		return getWalker().tile(x, y, z);
	}
	
	// Logger helpers
	
	protected void debug(String message) {
		getLogger().debug(message);
	}
	
	protected void info(String message) {
		getLogger().info(message);
	}
	
	protected void warning(String message) {
		getLogger().warning(message);
	}
	
	protected void error(String message) {
		getLogger().error(message);
	}
	
	protected void exception(Exception message) {
		getLogger().exception(message);
	}
	
	public boolean sleep(int time, Supplier<Boolean> wwhile) {
		Timer timer = new Timer(time);
		while (timer.isRunning() && wwhile.get())
			sleep(100, 200);
		return !wwhile.get();
	}
	
	public boolean sleepAnimating(int time, int animDelay) {
		return sleepAnimating(time, animDelay, () -> true);
	}
	
	public boolean sleepAnimating(int time, int animDelay, Supplier<Boolean> wwhile) {
		Timer timer = new Timer(animDelay);
		return sleep(time, () -> {
			if (myPlayer().isAnimating())
				timer.reset();
			if (!timer.isRunning())
				return false;
			return wwhile.get();
		});
	}
	
	public boolean sleepMoving() {
		return sleepMoving(30000);
	}
	
	public boolean sleepMoving(int time) {
		return sleepMoving(time, () -> true);
	}
		
	public boolean sleepMoving(int time, Supplier<Boolean> wwhile) {
		Timer initTimer = new Timer(random(600, 1000)); // Compensates for lag
		return sleep(time, () -> {
			if (initTimer.isRunning())
				return wwhile.get();
			return myPlayer().isMoving() && wwhile.get();
		});
	}
	
	private final int[][] widgetIds = new int[][] {
			new int[] {125, 88}, // Leprechaun store
	};
	
	private boolean closeAllInterfaces(Collection<RS2Widget> widgets) {
		for (int[] ids : widgetIds) {
			if (ids.length == 2 && getWidgets().get(ids[0], ids[1]) != null
					 && getWidgets().get(ids[0], ids[1]).isVisible()) {
				interact(getWidgets().get(ids[0], ids[1]).getRectangle(), null);
			} else if (ids.length == 3 && getWidgets().get(ids[0], ids[1], ids[2]) != null
					 && getWidgets().get(ids[0], ids[1], ids[2]).isVisible()) {
				interact(getWidgets().get(ids[0], ids[1], ids[2]).getRectangle(), null);
			}
		}
		if (getWidgets().getOpenInterface() != null && getWidgets().closeOpenInterface())
			return true;
		return false;
	}
	
	public boolean closeAllInterfaces() {
		return closeAllInterfaces(getWidgets().getAll());
	}
	
}
