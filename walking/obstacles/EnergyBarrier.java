package lemons.api.walking.obstacles;

import lemons.api.script.TaskScript;
import lemons.api.script.entities.RS2Object;
import lemons.api.script.entities.WallObject;

public class EnergyBarrier extends Obstacle {

	public EnergyBarrier(TaskScript s) {
		super(s);
	}

	private String[] names = new String[] { "Energy Barrier" };
	
	public boolean needSkip() {
		return true;
	}

    public boolean isActive(RS2Object o, boolean solving) {
        if (o == null || o.getName() == null || o.getName().equals("null")) return false;
        
        for (String name : names) {
        	if (name != null && o.getName().equalsIgnoreCase(name)) {
        		return true;
        	}
        }
        
        return false;
    }

    public boolean solve(RS2Object gameObject) {
    	if (interact(gameObject, "Pay-toll(2-Ecto)")) {
    		sleepMoving();
    		sleep(2000, 4000);
    	}
		
		return false;
    }

	public String[] getObjectNames() {
		return names;
	}

}
