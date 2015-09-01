package lemons.api.script.entities;

import lemons.api.script.TaskScript;

import org.osbot.rs07.accessor.XCharacter;
import org.osbot.rs07.api.def.EntityDefinition;

public class Character<T extends XCharacter<?>> extends Entity {

	private org.osbot.rs07.api.model.Character<T> character;

	public Character(TaskScript s, org.osbot.rs07.api.model.Character<T> e) {
		super(s, e);
		character = e;
	}

	@Override
	public EntityDefinition getDefinition() {
		return character.getDefinition();
	}

	public int getIndex() {
		return character.getIndex();
	}
	
	public boolean isUnderAttack() {
		return character.isUnderAttack();
	}
	
	public Character<?> getInteracting() {
		return new Character<>(getScript(), character.getInteracting());
	}
	
	public int getHealth() {
		return character.getHealth();
	}
	
	public boolean isAttackable() {
		return character.isAttackable();
	}
	
	public boolean isMoving() {
		return character.isMoving();
	}
	
	public int getRotation() {
		return character.getRotation();
	}
	
	public int getAnimation() {
		return character.getAnimation();
	}

	public boolean isAnimating() {
		return character.isAnimating();
	}
	
}
