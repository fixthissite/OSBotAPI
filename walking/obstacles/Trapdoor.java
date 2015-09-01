package lemons.api.walking.obstacles;

import lemons.api.script.TaskScript;
import lemons.api.script.entities.RS2Object;

public class Trapdoor extends Obstacle {

    public Trapdoor(TaskScript s) {
        super(s);
    }

    private String[] names = new String[] { "Trapdoor" };
    private int currentPlane;
    private boolean success;


    @Override
    public boolean isActive(RS2Object o, boolean solving) {
        return o != null && canHandle(o.getName());
    }

    private boolean canHandle(String name) {
        for (String s : names) {
            if (s.equalsIgnoreCase(name))
                return true;
        }
        return false;
    }

    @Override
    public boolean solve(RS2Object trapdoor) {
        if (script.camera.getPitchAngle() < 60)
            script.camera.movePitch(random(60, 90));

        currentPlane = script.map.getPlane();
        success = false;

        if (containsAction(trapdoor, "Open")) {
            if (!trapdoor.interact("Open")) {
                return false;
            }
        }else if(containsAction(trapdoor, "Enter")){
            return trapdoor.interact("Enter");
        }

        if (script.camera.getYawAngle() < 170 || script.camera.getYawAngle() > 270) {
            script.camera.moveYaw(170+random(0, 100));
            script.zzz(1000, 3000);
        }

        for (int h = 0; h < 3 && !success; h++) {
            success = trapdoor.interact("Climb-down");

            if (success) {
                for (int w = 0; w < 30 && (script.map.getPlane() == currentPlane || trapdoor.exists());
                     w++) {
                    script.zzz(100, 200);
                }

                success = (script.map.getPlane() != currentPlane || !trapdoor.exists());
            }
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
        return names;
    }

}