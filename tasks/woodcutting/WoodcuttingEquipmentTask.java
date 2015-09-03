package lemons.api.tasks.woodcutting;

import lemons.api.tasks.banking.BankLocation;
import lemons.api.tasks.banking.EquipmentReq;
import lemons.api.tasks.banking.EquipmentReqTask;

import org.osbot.rs07.api.ui.EquipmentSlot;
import org.osbot.rs07.api.ui.Skill;

public class WoodcuttingEquipmentTask extends EquipmentReqTask {

	public WoodcuttingEquipmentTask() {
		super();
	}
	
	public WoodcuttingEquipmentTask(BankLocation bank) {
		super(bank);
	}

	@Override
	public void onStart() {
		super.onStart();
		
		addReq(EquipmentSlot.HANDS, new EquipmentReq(true, false)
				.addItem("Dragon axe")
					.setUseLevel(Skill.WOODCUTTING, 60)
					.setEquipLevel(Skill.ATTACK, 60)
				.addItem("Rune axe")
					.setUseLevel(Skill.WOODCUTTING, 41)
					.setEquipLevel(Skill.ATTACK, 40)
				.addItem("Adamant axe")
					.setUseLevel(Skill.WOODCUTTING, 31)
					.setEquipLevel(Skill.ATTACK, 30)
				.addItem("Mithril axe")
					.setUseLevel(Skill.WOODCUTTING, 21)
					.setEquipLevel(Skill.ATTACK, 20)
				.addItem("Black axe")
					.setUseLevel(Skill.WOODCUTTING, 11)
					.setEquipLevel(Skill.ATTACK, 10)
				.addItem("Steel axe")
					.setUseLevel(Skill.WOODCUTTING, 6)
					.setEquipLevel(Skill.ATTACK, 5)
				.addItem("Iron axe")
					.setUseLevel(Skill.WOODCUTTING, 1)
					.setEquipLevel(Skill.ATTACK, 1)
				.addItem("Bronze axe")
					.setUseLevel(Skill.WOODCUTTING, 1)
					.setEquipLevel(Skill.ATTACK, 1)
		);
	}

}
