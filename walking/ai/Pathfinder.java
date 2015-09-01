package lemons.api.walking.ai;

// Pathfinder class, Aaron Steed 2007

// Some code and structure still intact from Tom Carden's version:
// <http://www.tom-carden.co.uk/p5/a_star_web/applet/index.html>
// Some links that helped me:
// <http://theory.stanford.edu/~amitp/GameProgramming/AStarComparison.html>
// <http://www.policyalmanac.org/games/aStarTutorial.htm>
// <http://www.geocities.com/jheyesjones/astar.html>
// <http://www-b2.is.tokushima-u.ac.jp/~ikeda/suuri/dijkstra/Dijkstra.shtml>
// <http://www.cs.usask.ca/resources/tutorials/csconcepts/1999_8/tutorial/advanced/dijkstra/dijkstra.html>

import java.util.ArrayList;

public class Pathfinder{

	public ArrayList<Node> nodes; // Storage ArrayList<Node> for the Nodes
	public ArrayList<Node> open = new ArrayList<Node>(); // Possible Nodes for consideration
	public ArrayList<Node> closed = new ArrayList<Node>(); // Best of the Nodes
	public boolean wrap = false; // Setting for makeCuboidWeb() for grid wrap
																// around
	public boolean corners = true; // Setting for makeCuboidWeb() for connecting
																	// nodes at corners
	public boolean manhattan = false; // Setting for using Manhattan distance
																		// measuring method (false uses Euclidean
																		// method)
	public float offsetX = 0.0f, offsetY = 0.0f, offsetZ = 0.0f;// Offset to added
																															// to Nodes made
																															// with
																															// makeCuboidWeb
	private Node n;
	private float lowest;
	private int c;
	private Node temp;
	private Node current;
	private Connector a;
	private Node adjacent;
	private ArrayList<Node> path;
	private Node pathNode;
	private Node test;
	private Node bestNode;
	private Node n2;
	private float nDist;
	private Node n3;
	private float lowest2;
	private int c2;
	private Node current2;
	private Connector a2;
	private Node adjacent2;
	private ArrayList<Node> path2;
	private Node pathNode2;
	private Node test2;
	private Node bestNode2;
	private Node n4;
	private float nDist2;
	private float leastDist;
	private Node n5;
	private Node current3;
	private Connector a3;
	private Node adjacent3;
	private ArrayList<Node> path3;
	private Node pathNode3;
	private ArrayList<Node> path4;
	private ArrayList<Node> world;
	private Node temp2;
	private int totalLength;
	private int [] intPs;
	private float [] ps;
	private int directions;
	private int [] ps2;
	private Node myNode;
	private int [] bs;
	private int [] ws;
	private boolean valid;
	private int combinations;
	private Node connectee;
	private Node temp3;
	private Node temp4;
	private Node o;
	private Node o2;
	private int [] coords;
	private int level;
	private int level2;
	private int coord;
	private int level3;
	private float leastDist2;

	// Constructors

	public Pathfinder(){
		this.nodes = new ArrayList<Node>();
	}

	public Pathfinder(ArrayList<Node> nodes){
		this.nodes = nodes;
	}

	public Pathfinder(int w, int h, float scale){
		setCuboidNodes(w, h, scale);
	}

	public Pathfinder(int w, int h, int d, float scale){
		setCuboidNodes(w, h, d, scale);
	}

	//
	// Search algortihms
	//

	// ASTAR

	public ArrayList<Node> aStar(Node start, Node finish){
		for(int i = 0; i < nodes.size(); i++){
			n = (Node) nodes.get(i);
			n.reset();
		}
		open.clear();
		closed.clear();
		open.add(start);
		while(open.size() > 0){
			lowest = Float.MAX_VALUE;
			c = -1;
			for(int i = 0; i < open.size(); i++){
				temp = (Node) open.get(i);
				if(temp.f < lowest){
					lowest = temp.f;
					c = i;
				}
			}
			current = (Node) open.remove(c);
			closed.add(current);
			if(current == finish){
				break;
			}
			for(int i = 0; i < current.links.size(); i++){
				a = (Connector) current.links.get(i);
				adjacent = a.n;
				if(adjacent.walkable && !arrayListContains(closed, adjacent)){
					if(!arrayListContains(open, adjacent)){
						open.add(adjacent);
						adjacent.parent = current;
						adjacent.setG(a);
						if(manhattan)
							adjacent.MsetF(finish);
						else
							adjacent.setF(finish);
					}else{
						if(adjacent.g > current.g + a.d){
							adjacent.parent = current;
							adjacent.setG(a);
							if(manhattan)
								adjacent.MsetF(finish);
							else
								adjacent.setF(finish);
						}
					}
				}
			}
		}
		path = new ArrayList<Node>();
		pathNode = finish;
		while(pathNode != null){
			path.add(pathNode);
			pathNode = pathNode.parent;
		}
		test = (Node) path.get(path.size() - 1);
		if(test == finish){
			leastDist2 = Float.MAX_VALUE;
			bestNode = null;
			for(int i = 0; i < closed.size(); i++){
				n2 = (Node) closed.get(i);
				nDist = n2.dist(finish);
				if(nDist < leastDist2){
					leastDist2 = nDist;
					bestNode = n2;
				}
			}
			if(bestNode.parent != null){
				pathNode = bestNode;
				path = new ArrayList<Node>();
				while(pathNode != null){
					path.add(pathNode);
					pathNode = pathNode.parent;
				}
			}
		}
		return path;
	}

	// BEST FIRST SEARCH

	public ArrayList<Node> bfs(Node start, Node finish){
		for(int i = 0; i < nodes.size(); i++){
			n3 = (Node) nodes.get(i);
			n3.reset();
		}
		open.clear();
		closed.clear();
		open.add(start);
		while(open.size() > 0){
			lowest2 = Float.MAX_VALUE;
			c2 = -1;
			for(int i = 0; i < open.size(); i++){
				Node temp = (Node) open.get(i);
				if(temp.h < lowest2){
					lowest2 = temp.h;
					c2 = i;
				}
			}
			current2 = (Node) open.remove(c2);
			closed.add(current2);
			if(current2 == finish){
				break;
			}
			for(int i = 0; i < current2.links.size(); i++){
				a2 = (Connector) current2.links.get(i);
				adjacent2 = a2.n;
				if(adjacent2.walkable && !arrayListContains(closed, adjacent2)){
					if(!arrayListContains(open, adjacent2)){
						open.add(adjacent2);
						adjacent2.parent = current2;
						if(manhattan)
							adjacent2.MsetH(finish);
						else
							adjacent2.setH(finish);
					}
				}
			}
		}
		path2 = new ArrayList<Node>();
		pathNode2 = finish;
		while(pathNode2 != null){
			path2.add(pathNode2);
			pathNode2 = pathNode2.parent;
		}
		test2 = (Node) path2.get(path2.size() - 1);
		if(test2 == finish){
			leastDist = Float.MAX_VALUE;
			bestNode2 = null;
			for(int i = 0; i < closed.size(); i++){
				n4 = (Node) closed.get(i);
				nDist2 = n4.dist(finish);
				if(nDist2 < leastDist){
					leastDist = nDist2;
					bestNode2 = n4;
				}
			}
			if(bestNode2.parent != null){
				pathNode2 = bestNode2;
				path2 = new ArrayList<Node>();
				while(pathNode2 != null){
					path2.add(pathNode2);
					pathNode2 = pathNode2.parent;
				}
			}
		}
		return path2;
	}

	// DIJKSTRA

	public void dijkstra(Node start){
		dijkstra(start, null);
	}

	public ArrayList<Node> dijkstra(Node start, Node finish){
		for(int i = 0; i < nodes.size(); i++){
			n5 = (Node) nodes.get(i);
			n5.reset();
		}
		open.clear();
		closed.clear();
		open.add(start);
		start.g = 0;
		while(open.size() > 0){
			current3 = (Node) open.remove(0);
			closed.add(current3);
			if(current3 == finish){
				break;
			}
			for(int i = 0; i < current3.links.size(); i++){
				a3 = (Connector) current3.links.get(i);
				adjacent3 = a3.n;
				if(adjacent3.walkable && !arrayListContains(closed, adjacent3)){
					if(!arrayListContains(open, adjacent3)){
						open.add(adjacent3);
						adjacent3.parent = current3;
						adjacent3.setG(a3);
					}else{
						if(adjacent3.g > current3.g + a3.d){
							adjacent3.parent = current3;
							adjacent3.setG(a3);
						}
					}
				}
			}
		}
		path3 = new ArrayList<Node>();
		pathNode3 = finish;
		while(pathNode3 != null){
			path3.add(pathNode3);
			pathNode3 = pathNode3.parent;
		}
		return path3;
	}

	public ArrayList<Node> getPath(Node pathNode){
		path4 = new ArrayList<Node>();
		while(pathNode != null){
			path4.add(pathNode);
			pathNode = pathNode.parent;
		}
		return path4;
	}

	// Shortcut to adding a makeCuboidWeb construct to Pathfinder

	public void setCuboidNodes(int w, int h, float scale){
		nodes = new ArrayList<Node>();
		nodes = createCuboidNodes(new int[]{
				w, h}, scale);
	}

	public void setCuboidNodes(int w, int h, int d, float scale){
		nodes = new ArrayList<Node>();
		nodes = createCuboidNodes(new int[]{
				w, h, d}, scale);
	}

	public void addNodes(ArrayList<Node> nodes){
		this.nodes.addAll(nodes);
	}

	// Creates a construct of Nodes and connects them across adjacent dimensions.
	// Adapts for corners and wrap-around but at a cost of speed - only for init

	public ArrayList<Node> createCuboidNodes(int w, int h, float scale){
		return createCuboidNodes(new int[]{
				w, h}, scale);
	}

	public ArrayList<Node> createCuboidNodes(int w, int h, int d, float scale){
		return createCuboidNodes(new int[]{
				w, h, d}, scale);
	}

	/*
	 * // Just some notes incase I have to remove the array method of building a
	 * map public ArrayList<Node> createCuboidNodes(int w, int h, int d, float scale){
	 * ArrayList<Node> world = new ArrayList<Node>(); int totalLength = w * h * d; for(int i =
	 * 0; i < totalLength; i++){ float x = offsetX + ((i % (w * h)) % w) * scale;
	 * float y = offsetY + ((i % (w * h)) / w) * scale; float z = offsetZ + i / (w *
	 * h); world.add(new Node(x, y, z)); } }
	 */

	// This beast I'd rather leave as is. Sorry.
	// I'm hiding it though. I may rely on array building ArrayList3Ds in the
	// future.
	private ArrayList<Node> createCuboidNodes(int [] dim, float scale){
		world = new ArrayList<Node>();
		totalLength = 1;
		for(int i = 0; i < dim.length; i++){
			if(dim[i] > 0){
				totalLength *= dim[i];
			}
		}
		for(int i = 0; i < totalLength; i++){
			intPs = getFolded(i, dim);
			ps = new float[intPs.length];
			for(int j = 0; j < ps.length; j++){
				ps[j] = intPs[j] * scale;
			}
			temp2 = new Node(ps);
			temp2.x += offsetX;
			temp2.y += offsetY;
			temp2.z += offsetZ;
			world.add(temp2);
		}
		directions = (int) Math.pow(3, dim.length);
		for(int i = 0; i < totalLength; i++){
			ps2 = getFolded(i, dim);
			myNode = (Node) world.get(i);
			bs = new int[ps2.length];
			ws = new int[ps2.length];
			for(int j = 0; j < bs.length; j++){
				bs[j] = ps2[j] - 1;
				ws[j] = 0;
			}
			for(int j = 0; j < directions; j++){
				valid = true;
				for(int k = 0; k < dim.length; k++){
					if(bs[k] > dim[k] - 1 || bs[k] < 0){
						if(wrap){
							if(bs[k] > dim[k] - 1){
								bs[k] -= dim[k];
								ws[k]--;
							}
							if(bs[k] < 0){
								bs[k] += dim[k];
								ws[k]++;
							}
						}else{
							valid = false;
						}
					}
					if(!corners){
						combinations = 0;
						for(int l = 0; l < dim.length; l++){
							if(bs[l] != ps2[l]){
								combinations++;
							}
						}
						if(combinations > 1){
							valid = false;
						}
					}
				}
				if(valid){
					connectee = (Node) world.get(getUnfolded(bs, dim));
					if(myNode != connectee){
						myNode.connect(connectee);
					}
				}
				if(wrap){
					for(int k = 0; k < dim.length; k++){
						switch(ws[k]){
							case 1:
								bs[k] -= dim[k];
								ws[k] = 0;
								break;
							case -1:
								bs[k] += dim[k];
								ws[k] = 0;
								break;
						}
					}
				}
				bs[0]++;
				for(int k = 0; k < bs.length - 1; k++){
					if(bs[k] > ps2[k] + 1){
						bs[k + 1]++;
						bs[k] -= 3;
					}
				}
			}
		}
		return world;
	}

	// The next two functions are shortcut methods for disconnecting unwalkables

	public void disconnectUnwalkables(){
		for(int i = 0; i < nodes.size(); i++){
			temp3 = (Node) nodes.get(i);
			if(!temp3.walkable){
				temp3.disconnect();
			}
		}
	}

	public void radialDisconnectUnwalkables(){
		for(int i = 0; i < nodes.size(); i++){
			temp4 = (Node) nodes.get(i);
			if(!temp4.walkable){
				temp4.radialDisconnect();
			}
		}
	}

	//
	// Utilities
	//

	// Faster than running ArrayList.contains - we only need the reference, not an
	// object match

	public boolean arrayListContains(ArrayList<Node> c, Node n){
		for(int i = 0; i < c.size(); i++){
			o = (Node) c.get(i);
			if(o == n){
				return true;
			}
		}
		return false;
	}

	// Faster than running ArrayList.indexOf - we only need the reference, not an
	// object match

	public int indexOf(Node n){
		for(int i = 0; i < nodes.size(); i++){
			o2 = (Node) nodes.get(i);
			if(o2 == n){
				return i;
			}
		}
		return -1;
	}

	// Returns an n-dimensional ArrayList<Node> from a point on a line of units given an
	// n-dimensional space

	public int [] getFolded(int n, int [] d){
		coords = new int[d.length];
		for(int i = 0; i < d.length; i++){
			coords[i] = n;
			for(int j = d.length - 1; j > i; j--){
				level = 1;
				for(int k = 0; k < j; k++){
					level *= d[k];
				}
				coords[i] %= level;
			}
			level2 = 1;
			for(int j = 0; j < i; j++){
				level2 *= d[j];
			}
			coords[i] /= level2;
		}
		return coords;
	}

	// Returns a point on a line of units from an n-dimensional ArrayList<Node> in an
	// n-dimensional space

	public int getUnfolded(int [] p, int [] d){
		coord = 0;
		for(int i = 0; i < p.length; i++){
			level3 = 1;
			for(int j = 0; j < i; j++){
				level3 *= d[j];
			}
			coord += p[i] * level3;
		}
		return coord;
	}

}
