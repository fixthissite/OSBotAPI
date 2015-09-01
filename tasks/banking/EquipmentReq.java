package lemons.api.tasks.banking;

import java.util.ArrayList;

import org.osbot.rs07.api.ui.Skill;


public class EquipmentReq {

	private ArrayList<EquipItemReq> useReqs = new ArrayList<EquipItemReq>();
	public final boolean required, immediate;
	
	public EquipmentReq(boolean required, boolean immediate) {
		this.required = required;
		this.immediate = immediate;
	}
	
	public EquipmentReq addItem(String name) {
		useReqs.add(new EquipItemReq(name));
		return this;
	}
	
	public EquipmentReq setUseLevel(Skill skill, int levelReq) {
		useReqs.get(useReqs.size() - 1).setUseLevel(skill, levelReq);
		return this;
	}
	
	public EquipmentReq setEquipLevel(Skill skill, int levelReq) {
		useReqs.get(useReqs.size() - 1).setEquipLevel(skill, levelReq);
		return this;
	}
	
	public ArrayList<EquipItemReq> getReqs() {
		return useReqs;
	}
	
}
