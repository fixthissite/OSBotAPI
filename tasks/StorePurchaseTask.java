package lemons.api.tasks;

import java.util.HashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

import lemons.api.script.entities.NPC;
import lemons.api.tasks.templates.AbstractTask;
import lemons.api.walking.map.Tile;

public class StorePurchaseTask extends AbstractTask {

	private Predicate<NPC> pred;
	private HashMap<String, Integer> items = new HashMap<String, Integer>();
	private HashMap<String, String[]> itemNames = new HashMap<String, String[]>();
	private Supplier<Tile> tile;
	
	public StorePurchaseTask(Supplier<Tile> t, Predicate<NPC> p) {
		pred = p;
		tile = t;
	}
	
	public StorePurchaseTask addItem(String name, int qty) {
		addItem(name, qty, name);
		return this;
	}

	public StorePurchaseTask addItem(String name, int qty, String... dontHave) {
		items.put(name, qty);
		itemNames.put(name, dontHave);
		return this;
	}
	
	@Override
	public void run() {
		if (!getStore().isOpen()) {
			if (closeAllInterfaces())
				return;
				
			if (getNpc() == null)
				walkTo(tile.get());
			else {
				if (interact(getNpc(), "Trade")) {
					sleepMoving();
					sleep(1000, 1500);
				}
			}
			return;
		}
		
		for (String item : items.keySet()) {
			for (int i = 0; i < 10 && getInventory().getAmount(itemNames.get(item)) < items.get(item)
					&& !getInventory().isFull(); i++) {
				long diff = items.get(item) - getInventory().getAmount(itemNames.get(item));
				int qty = 1;
				if (diff >= 5)
					qty = 5;
				if (diff >= 10)
					qty = 10;
				getStore().buy(item, (int) qty);
				sleep(1000, 1500);
			}
		}
		
		if (getStore().isOpen())
			getStore().close();
	}

	@Override
	public boolean isActive() {
		if (getInventory().isFull())
			return false;
		for (String item : items.keySet()) {
			if (getInventory().getAmount(itemNames.get(item)) < items.get(item)) {
				return true;
			}
		}
		return false;
	}
	
	public NPC getNpc() {
		return getNpcs().closest(pred);
	}

}
