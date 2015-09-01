package lemons.api.tasks.banking;

import java.util.ArrayList;
import java.util.HashMap;

import lemons.api.tasks.ObjectInteractionTask;
import lemons.api.tasks.WalkingTask;
import lemons.api.tasks.templates.AbstractTask;
import lemons.api.tasks.templates.ComplexTask;
import lemons.api.tasks.templates.ConditionalTask;
import lemons.api.walking.map.Zone;

import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.EquipmentSlot;

public class BankTask extends ComplexTask {
	
	private BankLocation bank = null;
	private boolean blacklist = true, withdrawOften = true;
	private ArrayList<String> itemList = new ArrayList<String>();
	private HashMap<EquipmentSlot, ArrayList<EquipmentReq>> equipment
		= new HashMap<EquipmentSlot, ArrayList<EquipmentReq>>();
	private AbstractTask bankTask;
	private HashMap<String, Integer[]> require = new HashMap<String, Integer[]>();
	private WalkingTask walkingTask;
	private Zone zone;

	public BankTask() {
		
	}
	
	public BankTask(BankLocation bank) {
		this.bank = bank;
	}
	
	public void setBankLocation(BankLocation bank) {
		debug("Set bank() location to "+bank.toString());
		this.bank = bank;
		zone = bank.getTile(getWalker()).getZone(4);
		walkingTask.setZone(zone);
	}
	
	private Zone getZone() {
		return zone;
	}

	@Override
	public void onStart() {
		super.onStart();
		
		bankTask = new AbstractTask() {

			@Override
			public void run() {
				if (!getBank().isOpen() && !getBank().open())
					return;
				
				sleep(500, 1000);
				boolean dontDepositAll = false;
				boolean[] doDeposits = new boolean[28];
				String[] depositNames = new String[28];
				
				int n = 0;
				for (Item i : getInventory().filter()) {
					
					boolean doDeposit = blacklist;
					for (String name : itemList) {
						if (name.equalsIgnoreCase(i.getName())) {
							doDeposit = !blacklist;
						}
					}
					
					for (String name : require.keySet())
						if (name.equalsIgnoreCase(i.getName()) && require.get(name)[1] > 0) {
							doDeposit = false;
						}
				
					for (EquipmentSlot slot : equipment.keySet()) {
						for (EquipmentReq reqs : equipment.get(slot)) {
							boolean betterEquip = false;
							for (EquipItemReq req : reqs.getReqs()) {
								if (req.getName().equals(i.getName())) {
									doDeposit = betterEquip;
									break;
								}
								if (getEquipment().contains(req.getName()) || getInventory().contains(req.getName())) {
									betterEquip = true;
								}
							}
						}
					}
					if (i.isNote())
						doDeposit = true;
					
					debug("Deposit "+i.getName()+"? "+(doDeposit?"true":"false"));
					
					depositNames[n] = i.getName();
					doDeposits[n] =  doDeposit;
					
					if (!doDeposit)
						dontDepositAll = true;
					n++;
				}
				
				if (dontDepositAll) {
					// Deposit items
					for (n = 0; n < 28; n++) {
						if (!doDeposits[n] || depositNames[n] == null
								|| !getInventory().contains(depositNames[n]))
							continue;
						
						if (!getBank().depositAll(depositNames[n]))
							n--;
						
						sleep(400, 600);
					}
				} else {
					getBank().depositAll();
					sleep(400, 600);
				}
				
				if (needEquipment()) {
					for (EquipmentSlot slot : equipment.keySet()) {
						for (EquipmentReq reqs : equipment.get(slot)) {
							for (EquipItemReq req : reqs.getReqs()) {
								if (getEquipment().contains(req.getName()) || getInventory().contains(req.getName()))
									break;
								
								if (getBank().contains(req.getName()) && req.canUse(getScript())) {
									getBank().withdraw(req.getName(), 1);
									sleep(400, 600);
									break;
								}
							}
						}
					}
				}
				
				if (needRequiredItems() || withdrawOften) {
					for (String req : require.keySet()) {
						if (getInventory().getAmount(req) < require.get(req)[withdrawOften?1:0]
								|| (withdrawOften && getInventory().getAmount(req) < require.get(req)[1])) {
							// Stock up on this item
							getBank().withdraw(req, (int) require.get(req)[1]
									- (int) getInventory().getAmount(req));
							sleep(400, 600);
						}
					}
				}
			}

			@Override
			public boolean isActive() {
				if (getInventory().contains("Tinderbox")
						&& getInventory().contains("Small fishing net")
						&& getInventory().contains("Shrimps")
						&& getInventory().contains("Bucket")
						&& getInventory().contains("Pot")
						&& getInventory().contains("Bread"))
					return true;
				if (needEquipment()) {
					return true;
				}
				if (needRequiredItems()) {
					return true;
				}
				if (getInventory().isFull()) {
					return true;
				}
				return false;
			}
			
		};
		walkingTask = new WalkingTask(bank() == null ? null : bank().getTile(getWalker()).getZone(4));
		addTask(new AbstractTask() {
			
			@Override
			public void run() {
				if (bank().getTile(getWalker()).dist() > 10) {
					walkTo(bank().getTile(getWalker()));
				} else {
					getBank().open();
				}
			}
			
			@Override
			public boolean isActive() {
				return bankTask.isActive() && !getBank().isOpen();
			}
		});
		addTask(bankTask);
		
		if (bank != null)
			setBankLocation(bank);
	}

	private BankLocation bank() {
		if (bank != null)
			return bank();
		
		// Find the closest bank, way of GlobalPath
		BankLocation a = null;
		for (BankLocation b : BankLocation.values()) {
			if (a == null
					|| b.getTile(getWalker()).distanceToGlobal()
						< a.getTile(getWalker()).distanceToGlobal())
				a = b;
				zone = a.getTile(getWalker()).getZone(4);
		}
		return a;
	}

	@Override
	public void onReset() {
		super.onReset();
	}
	
	public BankTask setWithdrawOften(boolean often) {
		withdrawOften = often;
		return this;
	}
	
	public BankTask deposit(String string) {
		blacklist = false;
		itemList.add(string);
		return this;
	}

	public BankTask except(String... strings) {
		blacklist = true;
		for (String string : strings)
			itemList.add(string);
		return this;
	}
	
	public BankTask require(String item, int max, int min) {
		require.put(item, new Integer[] { min, max });
		return this;
	}
	
	public BankTask require(String item, int max) {
		require(item, max, 1);
		return this;
	}
	
	public BankTask require(String item) {
		require(item, 1);
		return this;
	}
	
	public BankTask equip(EquipmentSlot slot, EquipmentReq req) {
		if (!equipment.containsKey(slot))
			equipment.put(slot, new ArrayList<EquipmentReq>());
		
		equipment.get(slot).add(req);
		return this;
	}
	 
	private boolean needEquipment() {
		boolean need = false;
		for (EquipmentSlot slot : equipment.keySet()) {
			for (EquipmentReq reqs : equipment.get(slot)) {
				for (EquipItemReq req : reqs.getReqs()) {
					if (getEquipment().contains(req.getName()) || getInventory().contains(req.getName()))
						return false;
					
					if (req.canUse(getScript())) {
						need = true;
					}
				}
			}
		}
		return need;
	}
	 
	private boolean needRequiredItems() {
		for (String req : require.keySet()) {
			if (getInventory().getAmount(req) < require.get(req)[0]
					&& (getBank().cacheContains(req)))
				return true;
		}
		return false;
	}

}
