package lemons.api.walking.obstacles;

import lemons.api.script.TaskScript;
import lemons.api.script.entities.RS2Object;
import lemons.api.walking.listeners.EntityListener;

public class SimpleDoor extends Obstacle {

	public SimpleDoor(TaskScript s) {
		super(s);
	}

	private String tmpName;
	private String[] names = new String[] { "Door", "Gate", "Large door" };
	private boolean success;
	private RS2Object tmpObj;
	
	public boolean isTut() {
		return false;
	}
	
	public boolean needSkip() {
		return isTut();
	}

    public boolean isActive(RS2Object o, boolean solving) {
        if (o == null || o.getName() == null) return false;
        
        tmpName = o.getName();
        
        if (tmpName == null) return false;
        
		// Next 3 lines skip Al Kharid gate
		if (tmpName.equalsIgnoreCase("Gate") 
				&& o.getX() == 3268
				&& (o.getY() == 3227 || o.getY() == 3228)) {
			return false;
		}
        
        for (String name : names) {
        	if (tmpName.equalsIgnoreCase(name)) {
    			if (containsAction(o, "Open") || (!solving && containsAction(o, "Close"))) {
    				return true;
    			} else {
    			//	log2("Found "+name+" with no actions?");
    			}
        	}
        }
        
        return false;
    }
    
    public boolean solve(RS2Object tmpObj) {
		tmpName = tmpObj.getName();
		
		success = false;

		if (containsAction(tmpObj, "Open")) {
			for (int i = 0; i < 5 && !success; i++)
				success = openObject(tmpObj, i > 0);
		}
		return success;
    }

	public boolean containsAction(RS2Object tmpObj, String string) {
    	for (String s : tmpObj.getActions())
    		if (string.equalsIgnoreCase(s))
    			return true;
		return false;
	}

	private boolean openObject(RS2Object obj, boolean exactFacing){
		if (interact(obj, "Open")) {
			tmpObj = obj;
			getWalker().wait(getWalker().tile(obj), new EntityListener(script, obj));
			for (int i = 0; i < 30 && tmpObj.exists(); i++)
				sleep(100);
		}
		
		return !obj.exists();
    }

	public String[] getObjectNames() {
		return names;
	}

}
