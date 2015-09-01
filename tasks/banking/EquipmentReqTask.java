package lemons.api.tasks.banking;

import java.util.ArrayList;
import java.util.HashMap;

import lemons.api.tasks.templates.AbstractTask;
import lemons.api.tasks.templates.ComplexTask;

import org.osbot.rs07.api.ui.EquipmentSlot;

public class EquipmentReqTask extends ComplexTask {
	
	private BankTask bankTask;
	private HashMap<EquipmentSlot, ArrayList<EquipmentReq>> equipment
			= new HashMap<EquipmentSlot, ArrayList<EquipmentReq>>();
	
	public EquipmentReqTask() {
		bankTask = new BankTask();
	}
	
	public EquipmentReqTask(BankLocation bank) {
		bankTask = new BankTask(bank);
	}
	
	public void setBankLocation(BankLocation bank) {
		bankTask.setBankLocation(bank);
	}
	
	public BankTask getBankTask() {
		return bankTask;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		addTask(new AbstractTask() {

			@Override
			public void run() {
				for (EquipmentSlot slot : equipment.keySet()) {
					for (EquipmentReq reqs : equipment.get(slot)) {
						for (EquipItemReq req : reqs.getReqs()) {
							if (getEquipment().contains(req.getName()))
								return;
							
							if (getInventory().contains(req.getName())) {
								if (req.canEquip(getScript())) {
									getInventory().getItem(req.getName()).interact();
									sleep(500, 1000);
								}
								return;
							}
						}
					}
				}
			}

			@Override
			public boolean isActive() {
				// TODO Auto-generated method stub
				return canEquip();
			}
			
		});
		addTask(bankTask);
	}
	 
	public boolean needEquipment() {
		boolean need = false;
		for (EquipmentSlot slot : equipment.keySet()) {
			for (EquipmentReq reqs : equipment.get(slot)) {
				for (EquipItemReq req : reqs.getReqs()) {
					if (getEquipment().contains(req.getName()) || getInventory().contains(req.getName()))
						return false;
					
					if (req.canUse(getScript())) {
						if (reqs.immediate)
							return true;
						else
							need = true;
					}
				}
			}
		}
		return need;
	}
	 
	public boolean canEquip() {
		boolean need = false;
		for (EquipmentSlot slot : equipment.keySet()) {
			for (EquipmentReq reqs : equipment.get(slot)) {
				for (EquipItemReq req : reqs.getReqs()) {
					if (getEquipment().contains(req.getName()))
						return false;
					
					if (getInventory().contains(req.getName())) {
						if (req.canEquip(getScript()))
							return true;
						return false;
					}
				}
			}
		}
		return need;
	}
	
	@Override
	public void onTaskStart() {
		super.onTaskStart();
	}
	
	public EquipmentReqTask addReq(EquipmentSlot slot, EquipmentReq req) {
		if (!equipment.containsKey(slot))
			equipment.put(slot, new ArrayList<EquipmentReq>());
		
		equipment.get(slot).add(req);
		bankTask.equip(slot, req);
		return this;
	}

	
	
	public EquipmentReqTask setWithdrawOften(boolean often) {
		bankTask.setWithdrawOften(often);
		return this;
	}
	
	public EquipmentReqTask deposit(String string) {
		bankTask.deposit(string);
		return this;
	}

	public EquipmentReqTask except(String string) {
		bankTask.except(string);
		return this;
	}
	
	public EquipmentReqTask require(String item, int max, int min) {
		bankTask.require(item, max, min);
		return this;
	}
	
	public EquipmentReqTask require(String item, int max) {
		bankTask.require(item, max, 0);
		return this;
	}
	
	public EquipmentReqTask require(String item) {
		bankTask.require(item, 1);
		return this;
	}
	
}
