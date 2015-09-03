package lemons.api.tasks;

import java.util.ArrayList;

import lemons.api.tasks.templates.AbstractTask;

import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.Tab;

public class DropperTask extends AbstractTask {

	private boolean dropping = false;
	private int[] dropPattern;
	private int index;
	private Item item;
	
	public static final int[] SNAKE_DROP1 = new int[] {
		3, 2, 1, 0,
		4, 5, 6, 7,
		11, 10, 9, 8,
		12, 13, 14, 15,
		19, 18, 17, 16,
		20, 21, 22, 23,
		27, 26, 25, 24
	}, SNAKE_DROP2 = new int[] {
		0, 1, 2, 3,
		7, 6, 5, 4,
		8, 9, 10, 11,
		15, 14, 13, 12,
		16, 17, 18, 19,
		23, 22, 21, 20,
		24, 25, 26, 27
	}, DUMB_DROP = new int[] {
		0, 1, 2, 3,
		4, 5, 6, 7,
		8, 9, 10, 11,
		12, 13, 14, 15,
		16, 17, 18, 19,
		20, 21, 22, 23,
		24, 25, 26, 27
	};
	
	private ArrayList<String> itemNames = new ArrayList<String>();
	private int minSlots;
	private boolean proactive;
	
	public DropperTask() {
		this(0);
	}
	
	public DropperTask(int min) {
		super();
		minSlots = min;
	}
	
	public DropperTask drop(String... n) {
		for (String s : n)
			itemNames.add(s);
		return this;
	}
	
	public DropperTask proactive(boolean b) {
		proactive = b;
		return this;
	}
	
	@Override
	public String getStatus() {
		return "Dropping";
	}
	
	@Override
	public boolean isActive() {
		return ((getInventory().getEmptySlots() <= minSlots && invContains())
				|| (proactive && invContains())
				|| dropping)
				&& index < 28;
	}
	
	private boolean invContains() {
		return getInventory().contains(itemNames.toArray(new String[itemNames.size()]));
	}

	@Override
	public void onTaskFinish() {
		super.onTaskFinish();
		index = 0;
		dropping = false;
	}
	
	@Override
	public void onTaskStart() {
		super.onTaskStart();
		dropPattern = random(0, 100) % 2 == 0 ? SNAKE_DROP1 : SNAKE_DROP2;
		index = 0;
		dropping = true;
		debug("inited");
	}

	@Override
	public void run() {
		if (!Tab.INVENTORY.isOpen(getScript().bot)) {
			getTabs().open(Tab.INVENTORY);
		}
		
		for (int index = 0; index < 28; index++) {
			// Check if we selected an item
			if (getInventory().isItemSelected()) {
				hover(getScript().myPlayer());
				getMouse().click(false);
			}
			
			if (getMenuAPI().isOpen()) {
				getInteract().menuSelect("Cancel");
			}
			
			item = getInventory().getItemInSlot(dropPattern[index]);
			
			if (item == null || item.getName() == null)
				continue;
			
			for (String s : itemNames) {
				if (item.getName().equalsIgnoreCase(s)) {
					// TODO: Very non-resizable friendly code lol (if ever supported)
					debug("Item "+s+" dropped");
					// Get a random point
					int x = 563 + ((dropPattern[index] % 4) * 42),
						y = 213 + ((int) Math.floor((float) dropPattern[index] / (float) 4) * 36);
					getMouse().move(random(x, x + 31), random(y, y + 31));
						if (menuContains("Drop") && getMouse().click(true)
							&& getMenuAPI().isOpen()
							&& getMenuAPI().selectAction("Drop")) {
						long qty = getInventory().getEmptySlots();
						for (int i = 0; i < 30 && getInventory().getEmptySlots() == qty; i++)
							sleep(100);
						sleep(0, 500);
					}
					dropping = getInventory().contains(itemNames.toArray(new String[itemNames.size()]));
					return;
				}
			}
			dropping = getInventory().contains(itemNames.toArray(new String[itemNames.size()]));
		}
	}


}
