package lemons.api.tasks.woodcutting;

import lemons.api.script.TaskScript;

public class TreeTask {
	
	public final int amount;
	public final TreeType tree;
	private TaskScript script;
	
	public TreeTask(TaskScript s, TreeType type, int amount) {
		this.script = s;
		this.amount = amount;
		tree = type;
	}
	
	public void setScript(TaskScript s) {
		script = s;
	}
	
	public int have() {
		if (script == null)
			return 0;
		return (int) script.getInventory().getAmount(tree.logName);
	}
	
	public boolean isDone(int offset) {
		return have() >= amount + offset;
	}
	
}
