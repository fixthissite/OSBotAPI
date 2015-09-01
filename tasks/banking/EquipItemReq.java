package lemons.api.tasks.banking;

import java.util.HashMap;

import lemons.api.script.TaskScript;

import org.osbot.rs07.api.ui.Skill;

public class EquipItemReq {
	
	private HashMap<Skill, Integer> useReqs = new HashMap<Skill, Integer>(),
			equipReqs = new HashMap<Skill, Integer>();
	private String name;
	
	public EquipItemReq(String name) {
		this.name = name;
	}
	
	public EquipItemReq setUseLevel(Skill skill, int levelReq) {
		useReqs.put(skill, levelReq);
		return this;
	}
	
	public EquipItemReq setEquipLevel(Skill skill, int levelReq) {
		equipReqs.put(skill, levelReq);
		return this;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean canUse(TaskScript script) {
		boolean canUse = true;
		
		for (Skill s : useReqs.keySet()) {
			if (script.getSkills().getStatic(s) < useReqs.get(s))
				canUse = false;
		}
		
		return canUse;
	}
	
	public boolean canEquip(TaskScript script) {
		if (!canUse(script))
			return false;
		
		boolean canUse = true;
		
		for (Skill s : equipReqs.keySet()) {
			if (script.getSkills().getStatic(s) < equipReqs.get(s))
				canUse = false;
		}
		
		return canUse;
	}
	
}