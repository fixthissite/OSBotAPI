package lemons.api;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import lemons.api.script.TaskScript;
import lemons.api.script.emulators.TaskScriptEmulator;
import lemons.api.script.entities.NPC;
import lemons.api.script.entities.RS2Object;

import org.osbot.rs07.Bot;
import org.osbot.rs07.api.Bank.BankMode;
import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.filter.IdFilter;
import org.osbot.rs07.api.filter.NameFilter;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.script.MethodProvider;

public class Bank extends TaskScriptEmulator<TaskScript> implements Monitor {

	private ArrayList<Item> bankCache;
	public Bank(TaskScript s) {
		super(s);
	}
	
	public void update() {
		if (!isOpen())
			return;
		
		ArrayList<Item> items = new ArrayList<Item>();
		
		for (Item item : filter()) {
			items.add(item);
		}
		if (isOpen())
			bankCache = items;
	}
	
	// Start caching bank info
	
	public long getAmount(Filter<Item>... arg0) {
		if (bankCache == null)
			return 0;
		long amount = 0;
		for (Item item : bankCache) {
			for (Filter<Item> f : arg0) {
				if (f.match(item))
					amount += item.getAmount();
			}
		}
		return amount;
	}
	public long getAmount(List<Item> arg0) {
		if (bankCache == null)
			return 0;
		long amount = 0;
		for (Item item : bankCache) {
			amount += item.getAmount();
		}
		return amount;
	}
	
	public long getAmount(int... arg0) {
		return getAmount(new IdFilter<Item>(arg0));
	}
	public long getAmount(String... arg0) {
		return getAmount(new NameFilter<Item>(arg0));
	}
	public boolean contains(Filter<Item>... arg0) {
		return getAmount(arg0) > 0;
	}
	public boolean contains(int... arg0) {
		return getAmount(arg0) > 0;
	}
	public boolean contains(List<Item> arg0) {
		return getAmount(arg0) > 0;
	}
	public boolean contains(String... arg0) {
		return getAmount(arg0) > 0;
	}
	
	// Link to standard library here
	
	public void clickToSlot(int arg0, int arg1) {
		getScript().getBank().clickToSlot(arg0, arg1);
	}
	public boolean isOpen() {
		return getScript().getBank().isOpen();
	}
	
	public boolean close() {
		return getScript().getBank().close();
	}
	public boolean deposit(Filter<Item> arg0, int arg1) {
		return getScript().getBank().deposit(arg0, arg1);
	}
	public boolean deposit(int arg0, int arg1) {
		return getScript().getBank().deposit(arg0, arg1);
	}
	public boolean deposit(String arg0, int arg1) {
		return getScript().getBank().deposit(arg0, arg1);
	}
	public boolean depositAll() {
		return getScript().getBank().depositAll();
	}
	public boolean depositAll(Filter<Item>... arg0) {
		return getScript().getBank().depositAll(arg0);
	}
	public boolean depositAll(int... arg0) {
		return getScript().getBank().depositAll(arg0);
	}
	public boolean depositAll(List<Item> arg0) {
		return getScript().getBank().depositAll(arg0);
	}
	public boolean depositAll(String... arg0) {
		return getScript().getBank().depositAll(arg0);
	}
	public boolean depositAllExcept(Filter<Item>... arg0) {
		return getScript().getBank().depositAllExcept(arg0);
	}
	public boolean depositAllExcept(int... arg0) {
		return getScript().getBank().depositAllExcept(arg0);
	}
	public boolean depositAllExcept(List<Item> arg0) {
		return getScript().getBank().depositAllExcept(arg0);
	}
	public boolean depositAllExcept(String... arg0) {
		return getScript().getBank().depositAllExcept(arg0);
	}
	public boolean depositWornItems() {
		return getScript().getBank().depositWornItems();
	}
	public boolean enableMode(BankMode arg0) {
		return getScript().getBank().enableMode(arg0);
	}
	public boolean equals(Object obj) {
		return getScript().getBank().equals(obj);
	}
	public MethodProvider exchangeContext(Bot arg0) {
		return getScript().getBank().exchangeContext(arg0);
	}
	public List<Item> filter(Filter<Item>... arg0) {
		return getScript().getBank().filter(arg0);
	}
	public Rectangle getAbsoluteSlotPosition(Bot arg0, int arg1) {
		return getScript().getBank().getAbsoluteSlotPosition(arg0, arg1);
	}
	public Rectangle getAbsoluteSlotPosition(int arg0, int arg1) {
		return getScript().getBank().getAbsoluteSlotPosition(arg0, arg1);
	}
	public boolean open() {
		if (getDialogues().isPendingContinuation()) {
			getKeyboard().typeString(" ", false);
			return false;
		}
		debug("Openning bank at ");
		RS2Object booth = getObjects().closest(
				o ->
					o.hasName("Bank booth", "Bank chest")
					&& o.hasAction("Bank"));
		
		NPC npc = getNpcs().closest(
				n ->
					n.hasName("Banker", "Ghost banker")
					&& n.hasAction("Bank"));
		if (npc != null && npc.exists()
				&& (booth == null || !booth.exists() || npc.distanceTo(booth) > 5)) {
			if (interact(npc, "Bank")) {
				debug("Banking with NPC!");
				return waitForBank();
			}
			return false;
		}
		if (booth != null && booth.exists())
			if (interact(booth, "Bank")) {
				debug("Banking with booth!");
				return waitForBank();
			}
		return false;
	}
	
	private boolean waitForBank() {
		debug("Wait for bank...");
		sleep(3000, () -> !isOpen() && !getDialogues().isPendingContinuation());
		if (getDialogues().isPendingContinuation()) {
			sleep(500, 800);
			getDialogues().clickContinue();
			sleep(3000, () -> !isOpen());
		}
		debug("Wait for bank returns "+(isOpen()?"true":"false"));
		sleep(500, 1000);
		return isOpen();
	}

	public boolean withdraw(HashMap<String, Integer> items) {
		return getScript().getBank().withdraw(items);
	}
	
	public boolean withdraw(Filter<Item> filters, int qty) {
		return getScript().getBank().withdraw(filters, qty);
	}
	
	public boolean withdraw(String name, int qty) {
		return getScript().getBank().withdraw(name, qty);
	}
	
	public boolean withdraw(int id, int qty) {
		return getScript().getBank().withdraw(id, qty);
	}

	@Override
	public void look() {
		update();
	}
	
	@Override
	public int sleep() {
		return 500;
	}

	public boolean isCached() {
		return bankCache != null;
	}

	public boolean cacheContains(String... req) {
		if (!isCached() && !isOpen()) {
			return true;
		}
		
		return contains(req);
	}
}
