package lemons.api.walking.obstacles;

import lemons.api.script.TaskScript;
import lemons.api.script.entities.RS2Object;

public class LooseRailing extends Obstacle {
	
	public LooseRailing(TaskScript s) {
		super(s);
	}

	private String[] names = new String[] { "Loose Railing" };
	
	public boolean needSkip() {
		return true;
	}

    public boolean isActive(RS2Object o, boolean solving) {
        if (o == null || o.getName() == null) return false;
        
        for (String name : names) {
        	if (name != null && o.getName().equalsIgnoreCase(name))
        		return true;
        }
        
        return false;
    }

    public boolean solve(RS2Object gameObject) {
		
    	if (!isVisible(gameObject)) {
    		getCamera().toEntity(gameObject);
    	}
    	
        if(gameObject.interact("Squeeze-through", names[0])) {
            for (int i = 0; i < 50 && myPlayer().getAnimation() != 3844; i++)
            	sleep(100,200);
            
            if (myPlayer().getAnimation() != 3844)
            	return false;
            
            for (int i = 0; i < 50 && myPlayer().getAnimation() == 3844; i++)
            	sleep(100,200);
            
            return true;
        }
		
		return false;
    }

	public String[] getObjectNames() {
		return names;
	}

}
