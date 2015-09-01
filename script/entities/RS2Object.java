package lemons.api.script.entities;

import org.osbot.rs07.api.def.ObjectDefinition;

import lemons.api.script.TaskScript;

public class RS2Object extends Entity
		implements org.osbot.rs07.api.model.RS2Object {

	private org.osbot.rs07.api.model.RS2Object obj;

	public RS2Object(TaskScript s, org.osbot.rs07.api.model.RS2Object e) {
		super(s, e);
		obj = e;
	}

	@Override
	public int getConfig() {
		return obj.getConfig();
	}

	@Override
	public ObjectDefinition getDefinition() {
		return obj.getDefinition();
	}

	@Override
	public int getOrientation() {
		return obj.getOrientation();
	}

	@Override
	public int getType() {
		return obj.getType();
	}

	@Override
	public int getUID() {
		return obj.getUID();
	}

	
	
}
