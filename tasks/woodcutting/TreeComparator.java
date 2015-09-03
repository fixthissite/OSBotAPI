package lemons.api.tasks.woodcutting;

import java.util.Comparator;

import lemons.api.tasks.TaskPriority;

public class TreeComparator implements Comparator<TreeZone> {
	
	private TaskPriority priority;

	public TreeComparator(TaskPriority priority) {
		this.priority = priority;
	}
	
    @Override
    public int compare(TreeZone t1, TreeZone t2) {
        switch (priority) {
			default:
				return 0;
        }
    }
}