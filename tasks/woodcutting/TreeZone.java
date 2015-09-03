package lemons.api.tasks.woodcutting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import lemons.api.script.TaskScript;
import lemons.api.script.entities.InteractableObject;
import lemons.api.script.entities.RS2Object;
import lemons.api.utils.IntCache;
import lemons.api.walking.map.ResourceZone;
import lemons.api.walking.map.Tile;

import org.osbot.rs07.api.model.Entity;
import org.osbot.rs07.api.ui.Skill;

public class TreeZone extends ResourceZone {
	
	private static final float RANDOM_DIST = 5;
	private ArrayList<Tile> treeTiles = new ArrayList<Tile>();
	private HashMap<TreeType, Integer> treeTypes = new HashMap<TreeType, Integer>();
	private int amount;
	
	public TreeZone(TaskScript s) {
		super(s);
		amount = 28;
	}
	
	public TreeZone(TaskScript s, int amount) {
		super(s);
		this.amount = amount;
	}
	
	public TreeZone addTree(TreeType type, int amount) {
		treeTypes.put(type, amount);
		return this;
	}
	
	public TreeZone addTree(TreeType type) {
		addTree(type, 28);
		return this;
	}

	public Tile treeTile() {
		if (treeTiles.isEmpty())
			return null;
		debug("Returning tree location!");
		return treeTiles.get(random(0, treeTiles.size() - 1));
	}

	public void updateTreeCache(HashMap<String, ArrayList<Integer>> stumpIds) {
		// Scan for tree tiles
		for (RS2Object o : getObjects().getAll()) {
			if (o instanceof InteractableObject && o.exists()) {
				if (!isTree(o) && !stumpIds.containsKey(IntCache.valueOf(o.getId()))) {
					//debug("Not a tree! This is gonna spam :(");
					continue;
				}
				if (treeTiles.contains(tile(o))) {
					//debug("We already have this tree, continuing...");
					continue;
				}
				if (!contains(o)) {
					//debug("This zone doesn't support this tree!");
					continue;
				}
				Tile t = getWalker().findEntityEnd(o);
				if (t == null || !t.canReach(false)) {
					debug("Can not reach this tree!");
					continue;
				}
				debug("Tree is being cached at "+tile(o).toString());
				if (!treeTiles.contains(tile(o)))
					treeTiles.add(tile(o));
			}
		}
		debug("We have "+treeTiles.size()+" trees to pick from...");
	}

	private boolean isTree(Entity o) {
		for (TreeType t : treeTypes.keySet()) {
			if (t.level > getSkills().getDynamic(Skill.WOODCUTTING))
				continue;
			for (String s : t.treeNames) {
				if (s.equalsIgnoreCase(o.getName()))
					return true;
			}
		}
		return false;
	}

	public void clearTreeCache() {
		debug("Clearing tree cache!");
		treeTiles.clear();
	}
	
	public int have() {
		int amount = 0;
		for (TreeType tree : treeTypes.keySet())
			amount += getInventory().getAmount(tree.logName);
		return amount;
	}
	
	public boolean isDone(int offset) {
		return have() >= amount + offset;
	}
	
	public int minLevel() {
		int min = 99;
		for (TreeType t : treeTypes.keySet()) {
			if (t.level < min)
				min = t.level;
		}
		return min;
	}
	
	public int maxLevel() {
		int max = 0;
		for (TreeType t : treeTypes.keySet()) {
			if (t.level > max)
				max = t.level;
		}
		return max;
	}

	public boolean canChop() {
		return minLevel() <= getSkills().getDynamic(Skill.WOODCUTTING);
	}

	public ArrayList<String> getTreeNames() {
		ArrayList<String> names = new ArrayList<String>();
		for (TreeType type : treeTypes.keySet()) {
			if (type.level > getSkills().getDynamic(Skill.WOODCUTTING))
				continue;
			for (String name : type.treeNames)
				names.add(name);
		}
		return names;
	}

	public ArrayList<String> getLogNames() {
		ArrayList<String> names = new ArrayList<String>();
		for (TreeType type : treeTypes.keySet()) {
			names.add(type.logName);
		}
		return names;
	}

	public Tile getCenter() {
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE,
			maxX = 0, maxY = 0;
		for (Tile t : getTiles()) {
			if (t.x < minX)
				minX = t.x;
			if (t.y < minY)
				minY = t.y;
			if (t.x > maxX)
				maxX = t.x;
			if (t.y > maxY)
				maxY = t.y;
		}
		return tile(minX + ((maxX - minX) / 2), minY + ((maxY - minY) / 2), getTiles().get(0).z);
	}

	public RS2Object getTree() {
		ArrayList<RS2Object> trees = new ArrayList<RS2Object>();
		ArrayList<String> treeIds = new ArrayList<String>();
		for (RS2Object o : getObjects().getAll()) {
			if (isTree(o)) {
				if (!treeTiles.contains(tile(o))) {
					//debug("This tree isn't one of our available trees.");
					continue;
				}
				if (!o.hasAction("Chop down") && !o.hasAction("Chop-down")) {
					//debug("This tree doesn't have a chop down action!");
					continue;
				}
				if (treeIds.contains(tile(o).getId())) {
					//debug("Already have this tree cached, continue...");
					continue;
				}
				trees.add(o);
				treeIds.add(tile(o).getId());
			}
		}
		
				
		debug("We found "+trees.size()+" potential trees.");
		
		RS2Object tree = null;
		Tile tmpTreeTile = null;
		Collections.shuffle(trees); // Ensure randomness
		for (RS2Object tr : trees) {
			Tile tile = tile(tr);
			if (tree == null
					|| tile.dist() < tmpTreeTile.dist()
					|| (tile.dist(tree) < RANDOM_DIST
							&& random(0, 10) > 4)) {
				tree = tr;
				tmpTreeTile = tile;
			}
		}
		
		debug(tree != null
				? "Found tree to cut at "+tile(tree).toString()
				: "Still try to find a tree...");
		
		return tree;
	}

	public Tile getRandom() {
		return getTiles().get(random(0, getTiles().size()));
	}
}
