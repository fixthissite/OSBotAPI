package lemons.api.tasks.woodcutting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import lemons.api.script.entities.InteractableObject;
import lemons.api.script.entities.RS2Object;
import lemons.api.tasks.TaskPriority;
import lemons.api.tasks.WalkingTask;
import lemons.api.tasks.templates.AbstractTask;
import lemons.api.tasks.templates.ComplexTask;
import lemons.api.utils.IntCache;
import lemons.api.walking.listeners.EntityListener;
import lemons.api.walking.map.Tile;

public class ChopTreeTask extends ComplexTask {

	private ArrayList<TreeZone> treeZones = new ArrayList<TreeZone>();
	private final int maxAmount;
	private TreeComparator comparator;
	@SuppressWarnings("unused")
	private TaskPriority priority;
	private HashMap<String, ArrayList<Integer>> stumpIds = new HashMap<String, ArrayList<Integer>>();

	private RS2Object tree;
	private Tile curTreeTile;
	private ArrayList<Tile> stumpTiles = new ArrayList<Tile>();
	public boolean sleepAfterChop;

	public boolean chopping = false;

	public boolean triedChopping = false;
	private TreeZone selectedZone;
	private WalkingTask walkingTask;
	
	public ChopTreeTask(int amount) {
		maxAmount = amount;
	}
	
	public ChopTreeTask() {
		maxAmount = 28;
	}
	
	@Override
	public void onTaskFinish() {
		super.onTaskFinish();
		
		debug("Clean up current tree");
		tree = null;
	}
	
	private void status(String string) {
		// TODO Auto-generated method stub
		
	}

	private boolean isChopping() {
		return tree != null && tree.exists() && (myPlayer().getAnimation() == 879
				|| myPlayer().getAnimation() == 877
				|| myPlayer().getAnimation() == 875
				|| myPlayer().getAnimation() == 873
				|| myPlayer().getAnimation() == 871
				|| myPlayer().getAnimation() == 869
				|| myPlayer().getAnimation() == 867
				|| myPlayer().getAnimation() == 2846);
	}

	private void scanStump(RS2Object tree) {
		if (tree == null || tree.exists())
			return;
		
		String name = tree.getName();
		
		if (!stumpIds.containsKey(name))
			stumpIds.put(name, new ArrayList<Integer>());
		
		for (RS2Object o : getObjects().getAll()) {
			if ("Tree stump".equalsIgnoreCase(o.getName())
					&& tile(o).compare(tile(tree))) {
				if (stumpIds.get(name).contains(IntCache.valueOf(o.getId())))
					return; // Already got it, this would be spam
				debug("Found stump ID for "+name+" to be "+o.getId());
				stumpIds.get(name).add(IntCache.valueOf(o.getId()));
				return;
			}
		}
		
		debug("Failed to find stump ID for "+name);
	}
	
	private void scanStumps() {
		if (tree == null)
			return;
		String name = tree.getName();
		for (RS2Object o : getObjects().getAll()) {
			if (o instanceof InteractableObject
					&& selectedZone.contains(o)) {
				if (o.getName().equalsIgnoreCase("Tree stump")
						&& !stumpTiles.contains(tile(o))) {
					// This is a stump but not in the list, add it to the end
					debug(name+" turned to a stump at "+tile(o).toString()+"!");
					stumpTiles.add(tile(o));
				} else if (!o.getName().equalsIgnoreCase("Tree stump")
						&& stumpTiles.contains(tile(o))) {
					// This is a stump but not in the list, add it to the end
					int i = stumpTiles.indexOf(tile(o));
					debug(name+" regrew at "+tile(o).toString()+"!");
					stumpTiles.remove(i);
				}
				// We got a new stump, add it to 
			}
		}
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		comparator = new TreeComparator(TaskPriority.ORDER);
		priority = TaskPriority.ORDER;
		walkingTask = new WalkingTask(null);
		
		addTask(new BirdsNestTask());
		addTask(walkingTask);
		addTask(new AbstractTask() {
		
			
			@Override
			public boolean isActive() {
				if (getInventory().isFull())
					return false;
				
				int total = 0;
				for (TreeZone t : treeZones) {
					for (String s : t.getLogNames())
						total += getInventory().getAmount(s);
				}
				
				if (total >= maxAmount)
					return false;
				
				boolean hasJobs = false;
				for (TreeZone t : treeZones) {
					if (!t.isDone(0))
						hasJobs = true;
				}
				
				return hasJobs;
			}
		
			@Override
			public void run() {
				scanStump(tree);
				scanStumps();
				
				// Chopping logic
				if (tree != null && tree.exists()) {
					curTreeTile = getWalker().findEntityEnd(tree);
					if (!canInteract(tree)) {
						status("Walking to our selected tree");
						getWalker().findLocalPath(curTreeTile)
							.setListener(new EntityListener(getScript(), tree))
							.walk();
					} else if (isChopping() && !getDialogues().isPendingContinuation()) {
						chopping = true;
						sleep(100);
					} else {
						if (sleepAfterChop && !triedChopping) {
							status("Sleeping a bit before interacting...");
							sleep(500, 1500);
							chopping = false;
						}
						
						triedChopping = true;
						
						if (interact(tree, tree.hasAction("Chop down") ? "Chop down" : "Chop-down")) {
							getWalker().wait(tree);
							triedChopping = false;
							for (int i = 0; i < 20 && myPlayer().isMoving(); i++)
								sleep(100);
							for (int i = 0; i < 20 && !isChopping(); i++)
								sleep(100);
						}
					}
					return;
				} else if (tree != null && (chopping || !tree.exists())) {
					debug("Done chopping this tree!");
					if (sleepAfterChop) {
						status("Sleeping a bit before searching...");
						sleep(1000, 4000);
					}
					tree = null;
					chopping = false;
					return;
				}
				
				debug("Finding trees");
				
				// Find trees
				
				for (TreeZone t : treeZones) {
					if (!t.canChop())
						continue;
					if (t.isDone(isChopping() ? -1 : 0))
						continue;
				
					selectedZone = t;
					
					t.updateBanking();
					t.updateTreeCache(stumpIds);
					
					if (!t.contains(myPlayer())) {
						debug("Trees no where in sight, walking to tree area (not really, fix this)...");
						walkingTask.setZone(t);
						return;
					}
					
					//for (String name : t.getTreeNames()) {
						
						curTreeTile = null;
						tree = t.getTree();
						
						if (tree != null && tree.exists()) {
							curTreeTile = tile(tree);
							return;
						}
					//}
					
					if (!stumpTiles.isEmpty() && stumpTiles.get(0).dist() > 7) {
						status("Walking to tree pre-emptively.");
						debug("Trees no where in sight, but we know which will come up first!");
						// Find the stump
						for (RS2Object o : getObjects().getAll()){
							if (o != null && o instanceof InteractableObject
									&& "Tree stump".equalsIgnoreCase(o.getName())
									&& stumpTiles.get(0).compare(o)) {
								
								walkingTask.setZone(getWalker().findEntityEnd(o).getZone(2));
								if (sleepAfterChop) {
									sleep(1500, 3000);
								}
								return;
							}
						}
						walkingTask.setZone(stumpTiles.get(0).getZone(2));
						if (sleepAfterChop) {
							sleep(500, 2000);
						}
					} else {
						status("No trees, waiting...");
						debug("Trees no where in sight, now we wait...");
						sleep(1000, 2000);
					}
					
					return;
				}
			}
		});
	}
	
	public ChopTreeTask setPriority(TaskPriority priority) {
		this.priority = priority;
		this.comparator = new TreeComparator(priority);
		return this;
	}
	
	public ChopTreeTask addArea(TreeZone zone) {
		treeZones.add(zone);
		
		// Now we need to sort the list by Priority
		Collections.sort(treeZones, comparator);
		return this;
	}

}
