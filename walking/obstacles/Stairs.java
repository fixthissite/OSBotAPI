package lemons.api.walking.obstacles;

import lemons.api.script.TaskScript;
import lemons.api.script.entities.RS2Object;
import lemons.api.walking.map.Tile;

public class Stairs extends Obstacle {

    public Stairs(TaskScript s) {
        super(s);
    }

    private int currentPlane;
    private boolean success;
    private String[] names = new String[] {"Stairs", "Staircase", "Ladder", "Passageway"};
    private Tile stairsTile;

    @Override
    public boolean isActive(RS2Object o, boolean solving) {
        return o != null && canHandle(o.getName());
    }

    private boolean canHandle(String name) {
        for (String s : names) {
            if (s.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean solve(RS2Object stairs) {
        // Interact with stairs

        currentPlane = getMap().getPlane();
        success = false;

        debug("Starting!");
        if (getWalker().getZLevelDiff() > 0 && !containsAction(stairs, "Enter"))
            success = interact(stairs, "Climb-up");
        else if (getWalker().getZLevelDiff() < 0 && !containsAction(stairs, "Enter"))
            success = interact(stairs, "Climb-down");
        else if (containsAction(stairs, "Climb-up"))
            success = interact(stairs, "Climb-up");
        else if (containsAction(stairs, "Climb-down"))
            success = interact(stairs, "Climb-down");
        else if(containsAction(stairs, "Enter")){
            success = interact(stairs, "Enter");
        }
        debug("Solved stairs!");

        stairsTile = getWalker().tile(stairs.getPosition());

        if (success) {
            getWalker().wait(stairs);

            for (int w = 0; w < 30 && (getMap().getPlane() == currentPlane
                    && stairsTile.dist() < 200); w++) {
                sleep(100);
            }

            success = (getMap().getPlane() != currentPlane || stairsTile.dist() > 200);
        }

        return success;
    }

    public boolean containsAction(RS2Object tmpObj, String string) {
        for (String s : tmpObj.getActions())
            if (string.equalsIgnoreCase(s))
                return true;
        return false;
    }

    @Override
    public String[] getObjectNames() {
        return names ;
    }

}