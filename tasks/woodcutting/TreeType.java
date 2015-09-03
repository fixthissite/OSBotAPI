package lemons.api.tasks.woodcutting;

import lemons.api.script.TaskScript;
import lemons.api.script.entities.RS2Object;

public enum TreeType {
	NORMAL(1, "Logs", "Jungle Tree", "Dead tree", "Evergreen", "Tree"),
	ACHEY(1, "Achey logs", "Achey"),
	OAK(15, "Oak logs", "Oak"),
	WILLOW(30, "Willow logs", "Willow"),
	TEAK(35, "Teak logs", "Teak"),
	MAPLE(45, "Maple logs", "Maple"),
	HOLLOW(45, "Hollow logs", "Hollow tree"),
	MAHOGANY(50, "Mahogany logs", "Mahogany"),
	ARCTIC_PINE(54, "Arctic pine logs", "Arctic pine"),
	YEW(60, "Yew logs", "Yew"),
	MAGIC(75, "Magic logs", "Magic tree");
	
	public final int level;
	public final String logName;
	public final String[] treeNames;
	
	private TreeType(int level, String logName, String... treeNames) {
		this.treeNames = treeNames;
		this.logName = logName;
		this.level = level;
	}

	public boolean isTree(RS2Object r, TaskScript script) {
		for (String s : treeNames) {
			if (s.equals(r.getName()))
				return true;
		}
		return false;
	}
}
