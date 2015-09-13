package lemons.api.tasks;

import java.util.HashMap;

import lemons.api.script.entities.GroundItem;
import lemons.api.tasks.templates.AbstractTask;
import lemons.api.tasks.templates.ComplexTask;
import lemons.api.tasks.templates.ConditionalTask;
import lemons.api.walking.map.AbstractZone;

public class PickupTask extends ComplexTask {
	
	private HashMap<String, Integer> limits = new HashMap<String, Integer>();
	private AbstractZone zone = null;
	private WalkingTask walkingTask;
	private AbstractTask pickupTask;
	
	public PickupTask() {
		walkingTask = new WalkingTask(null);
	}

	@Override
	public void onStart() {
		super.onStart();
		pickupTask = new AbstractTask() {
			@Override
			public void run() {
				for (String item : limits.keySet()) {
					Integer limit = limits.get(item);
					if (limit == -1 || getInventory().getAmount(item) < limit) {
						// Get this item!
						GroundItem g = getGroundItems().closest(true, item);
						if (g == null || !g.exists())
							continue;
						if (tile(g).canReach(true)) {
							if (!canInteract(g)) {
								walkTo(g);
							} else if (g.interact("Take")) {
								long c = getInventory().getAmount(item);
								for (int i = 0; i < 30 && c == getInventory().getAmount(item); i++)
									sleep(100);
							}
						}
						break;
					}
				}
			}
		
			@Override
			public boolean isActive() {
				if (zone != null && !zone.contains(myPlayer()))
					return true;
				for (String item : limits.keySet()) {
					int limit = limits.get(item);
					if (limit != -1 && getInventory().getAmount(item) >= limit) {
						continue;
					}
					GroundItem g = getGroundItems().closest(
							gi ->
								gi.hasName(item)
								&& (zone == null || zone.contains(gi)));
					if (g == null) {
						continue;
					}
					return !getInventory().isFull();
				}
				return false;
			}
		};
		
		addTask(() -> pickupTask.isActive() && walkingTask.getZone() != null
				&& !walkingTask.getZone().contains(myPlayer()), walkingTask);
		addTask(pickupTask);
	}

	public PickupTask addItem(String string, int i) {
		limits.put(string, i);
		return this;
	}

	public PickupTask addItem(String string) {
		addItem(string, -1);
		return this;
	}

	public PickupTask setZone(AbstractZone rect) {
		zone = rect;
		walkingTask.setZone(zone);
		return this;
	}
	
	
	
}
