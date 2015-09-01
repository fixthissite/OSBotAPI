package lemons.api.walking.obstacles;

import lemons.api.script.TaskScript;
import lemons.api.script.entities.RS2Object;
import lemons.api.walking.map.Tile;
import lemons.api.walking.map.Zone;

import org.osbot.rs07.api.map.Position;

public class WildernessDitch extends Obstacle {
	
	private Zone area;
	
	public WildernessDitch(TaskScript s) {
		super(s);
		area = (Zone) new Zone(s)
				.rect(new Position(3136, 3521, 0), new Position(3329, 3522, 0))
				.rect(new Position(3042, 3521, 0), new Position(3122, 3522, 0))
				.rect(new Position(2946, 3521, 0), new Position(2991, 3522, 0))
				.rect(new Position(2996, 3530, 0), new Position(2997, 3533, 0));
	}

	private String[] names = new String[] { "Wilderness Ditch" };

    public boolean isActive(RS2Object o, boolean solving) {
        if (o == null || !o.exists()) return false;
        
        return area.contains(o);
    }

	public boolean solve(RS2Object obj) {
		Tile start = getWalker().getRegion().findEnd(getWalker().tile(obj));
		boolean isNorth = start.getY() > getWalker().tile(obj).getY();
		
		if(interact(obj, "Cross")){
			getWalker().wait(obj);
			
			for (int i = 0; i < 20 && myPlayer().getAnimation() == -1
					&& !getWidgets().isVisible(382); i++)
				sleep(100);
			
			for (int i = 0; i < 20 && myPlayer().getAnimation() != -1
					&& !getWidgets().isVisible(382); i++)
				sleep(100);
		}
		
		if (getWidgets().isVisible(382)){
			getWidgets().get(382, 18).interact();
			for (int i = 0; i < 20 && getWidgets().isVisible(382); i++)
				sleep(100);
			for (int i = 0; i < 30 && myPlayer().getAnimation() != -1
					&& !getWidgets().isVisible(382); i++)
				sleep(100);
		}
		
		return isNorth ? myLocation().getY() < getWalker().tile(obj).getY()
						: myLocation().getY() > getWalker().tile(obj).getY();
	}

	public String[] getObjectNames() {
		return names;
	}

}
