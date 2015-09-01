package lemons.api.script.entities;

import org.osbot.rs07.api.def.EntityDefinition;
import org.osbot.rs07.api.map.Area;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.model.Model;

import lemons.api.script.TaskScript;
import lemons.api.script.emulators.TaskScriptEmulator;
import lemons.api.walking.map.Tile;

public class Entity extends TaskScriptEmulator<TaskScript> implements org.osbot.rs07.api.model.Entity {
	
	private org.osbot.rs07.api.model.Entity entity;
	
	public Entity(TaskScript s, org.osbot.rs07.api.model.Entity e) {
		super(s);
		this.entity = e;
	}
	
	public float distanceTo() {
		return myLocation().distanceTo(this);
	}
	
	public float distanceTo(Tile t) {
		return t.distanceTo(this);
	}
	
	public float distanceTo(Entity e) {
		return distanceTo(tile(e));
	}
	
	public float distanceTo(Position p) {
		return distanceTo(tile(p));
	}
	
	public float dist(Tile t) {
		return distanceTo(t);
	}
	
	public float dist(Entity t) {
		return distanceTo(t);
	}
	
	public float dist(Position t) {
		return distanceTo(t);
	}
	
	public boolean compare(Tile t) {
		return t.compare(this);
	}

	public boolean hasName(String... n) {
		if (n.length == 0)
			return getName() != null && !getName().equalsIgnoreCase("null");
		for (String name : n) {
			if (name != null && name.equals(getName()))
				return true;
		}
		return false;
	}

	public boolean hasId(int... ids) {
		for (int id : ids) {
			if (id == getId())
				return true;
		}
		return false;
	}
	
	// Standard methods below

	@Override
	public String[] getActions() {
		return entity.getActions();
	}

	@Override
	public int getId() {
		return entity.getId();
	}

	@Override
	public int[] getModelIds() {
		return entity.getModelIds();
	}

	@Override
	public String getName() {
		return entity.getName();
	}

	@Override
	public boolean hasAction(String... a) {
		return entity.hasAction(a);
	}

	@Override
	public boolean hover() {
		return entity.hover(); // TODO: Make option?
	}

	@Override
	public boolean interact(String... arg0) {
		return entity.interact(arg0);
	}

	@Override
	public boolean examine() {
		return entity.examine();
	}

	@Override
	public boolean exists() {
		return entity.exists();
	}

	@Override
	public Area getArea(int arg0) {
		return entity.getArea(arg0);
	}

	@Override
	public EntityDefinition getDefinition() {
		return entity.getDefinition();
	}

	@Override
	public int getGridX() {
		return entity.getGridX();
	}

	@Override
	public int getGridY() {
		return entity.getGridY();
	}

	@Override
	public int getHeight() {
		return entity.getHeight();
	}

	@Override
	public int getLocalX() {
		return entity.getLocalX();
	}

	@Override
	public int getLocalY() {
		return entity.getLocalY();
	}

	@Override
	public Model getModel() {
		return entity.getModel();
	}

	@Override
	public Position getPosition() {
		return entity.getPosition();
	}

	@Override
	public int getSizeX() {
		return entity.getSizeX();
	}

	@Override
	public int getSizeY() {
		return entity.getSizeY();
	}

	@Override
	public int getX() {
		return entity.getX();
	}

	@Override
	public int getY() {
		return entity.getY();
	}

	@Override
	public int getZ() {
		return entity.getZ();
	}

	@Override
	@Deprecated
	public boolean isOnScreen() {
		return entity.isOnScreen();
	}

	@Override
	public boolean isVisible() {
		return entity.isVisible();
	}
	
}
