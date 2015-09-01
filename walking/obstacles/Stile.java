package lemons.api.walking.obstacles;

import lemons.api.script.TaskScript;
import lemons.api.script.entities.RS2Object;

public class Stile extends Obstacle {

	public Stile(TaskScript s) {
		super(s);
	}

	private String[] names = new String[] { "Stile" };
	
	public boolean needSkip() {
		return true;
	}

    public boolean isActive(RS2Object o, boolean solving) {
        if (o == null || o.getName() == null) return false;
        
        for (String name : names) {
        	if (!o.getName().equalsIgnoreCase(name))
        		continue;
        	
        	return true;
        	/*
        	if (!o.getName().equalsIgnoreCase(name) || walker.getLastPath().rewind(-1) == null)
        		continue;
        	
    		RS2Object nextStile = getObjs().getAt(walker.getLastPath().rewind(-1)).get(0);
    		if (nextStile == null || !nextStile.isVisible() || nextStile.getName() == null)
    			continue;
    		
	        for (String name2 : names) {
	        	if (nextStile.getName().equalsIgnoreCase(name2)) {
	        		return true;
	        	}
	        }
	        */
        }
        
        return false;
    }

    public boolean solve(RS2Object gameObject) {
    	if (!isVisible(gameObject)) {
    		getCamera().toEntity(gameObject);
    	}
    	
        if (gameObject.interact("Climb-over", names[0])) {
            for (int i = 0; i < 50 && myPlayer().getAnimation() != 839; i++)
            	sleep(100,200);
            
            if (myPlayer().getAnimation() != 839)
            	return false;
            
            for (int i = 0; i < 50 && myPlayer().getAnimation() == 839; i++)
            	sleep(100,200);
            
            return true;
        }
		
		return false;
    }

	public String[] getObjectNames() {
		return names;
	}
    
}
