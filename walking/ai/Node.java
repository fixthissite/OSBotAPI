package lemons.api.walking.ai;

// AStar Node class, Aaron Steed 2006

import java.util.ArrayList;
import java.lang.Math;

import lemons.api.script.TaskScript;
import lemons.api.walking.map.Tile;

public class Node{

	public float x, y, z; // Location, variable dimensions
	public Node parent = null; // Parent Node setting
	public float f = 0.0f; // Sum of goal and heuristic calculations
	public float g = 0.0f; // Cost of reaching goal
	public float h = 0.0f; // Heuristic distance calculation
	public ArrayList<Connector> links = new ArrayList<Connector>(); // Connectors to other Nodes
	public boolean walkable = true; // Is this Node to be ignored?
	private ArrayList<Connector> temp;
	private Connector c;
	private Connector c2;
	private Connector c3;
	private Connector c4;
	private Connector temp2;
	private Connector temp3;
	private Connector c5;
	private Connector myLink;
	private Connector myLink2;
	private ArrayList<Node> removeMe;
	private Connector myLinkLink;
	private Node temp4;
	private Node temp5;

	// Constructors

	public Node(){
		this(0.0f, 0.0f, 0.0f);
	}

	public Node(float x, float y){
		this.x = x;
		this.y = y;
		this.z = 0.0f;
	}

	public Node(float x, float y, float z){
		this.x = x;
		this.y = y;
		this.z = z;
	}

	// Undocumented constructor - used by makeCuboidNodes(int [] dim, float scale)

	public Node(float [] p){
		x = p[0];
		y = p[1];
		z = 0.0f;
		if(p.length > 2){
			z = p[2];
		}
	}
	
	public String toString() {
		return ((int) x)+", "+((int) y)+", "+((int) z);
	}
	
	public Tile toTile(TaskScript script) {
		return script.getWalker().tile((int) x, (int) y, (int) z);
	}

	public Node(float x, float y, ArrayList<Connector> links){
		this.x = x;
		this.y = y;
		this.z = 0.0f;
		this.links = links;
	}

	public Node(float x, float y, float z, ArrayList<Connector> links){
		this.x = x;
		this.y = y;
		this.z = z;
		this.links = links;
	}

	//
	// Field utilities
	//

	public void reset(){
		parent = null;
		f = g = h = 0;
	}

	// Calculate G

	public void setG(Connector o){
		g = parent.g + o.d;
	}

	// Euclidean field methods calculate F & H

	public void setF(Node finish){
		setH(finish);
		f = g + h;
	}

	public void setH(Node finish){
		h = dist(finish);
	}

	// Manhattan field methods calculate F & H

	public void MsetF(Node finish){
		MsetH(finish);
		f = g + h;
	}

	public void MsetH(Node finish){
		h = manhattan(finish);
	}

	//
	// Linking tools
	//

	public Node copy(){
		temp = new ArrayList<Connector>();
		temp.addAll(links);
		return new Node(x, y, z, temp);
	}

	public void connect(Node n){
		links.add(new Connector(n, dist(n)));
	}

	public void connect(Node n, float d){
		links.add(new Connector(n, d));
	}

	public void connect(ArrayList<Connector> links){
		this.links.addAll(links);
	}

	public void connectBoth(Node n){
		links.add(new Connector(n, dist(n)));
		n.links.add(new Connector(this, dist(n)));
	}

	public void connectBoth(Node n, float d){
		links.add(new Connector(n, d));
		n.links.add(new Connector(this, d));
	}

	public int indexOf(Node n){
		for(int i = 0; i < links.size(); i++){
			c = (Connector) links.get(i);
			if(c.n == n){
				return i;
			}
		}
		return -1;
	}

	public boolean connectedTo(Node n){
		for(int i = 0; i < links.size(); i++){
			c2 = (Connector) links.get(i);
			if(c2.n == n){
				return true;
			}
		}
		return false;
	}

	public boolean connectedTogether(Node n){
		for(int i = 0; i < links.size(); i++){
			c3 = (Connector) links.get(i);
			if(c3.n == n){
				for(int j = 0; j < n.links.size(); j++){
					Connector o = (Connector) n.links.get(j);
					if(o.n == this){
						return true;
					}
				}
			}
		}
		return false;
	}

	public void mulDist(float m){
		for(int i = 0; i < links.size(); i++){
			c4 = (Connector) links.get(i);
			c4.d *= m;
		}
	}

	public void setDist(Node n, float d){
		int i = indexOf(n);
		if(i > -1){
			temp2 = (Connector) links.get(i);
			temp2.d = d;
		}
	}

	public void setDistBoth(Node n, float d){
		int i = indexOf(n);
		if(i > -1){
			temp3 = (Connector) links.get(i);
			temp3.d = d;
			int j = n.indexOf(this);
			if(j > -1){
				temp3 = (Connector) n.links.get(j);
				temp3.d = d;
			}
		}
	}

	// Iterates thru neighbours and unlinks Connectors incomming to this - Node is
	// still linked to neighbours though

	public void disconnect(){
		for(int i = 0; i < links.size(); i++){
			c5 = (Connector) links.get(i);
			int index = c5.n.indexOf(this);
			if(index > -1){
				c5.n.links.remove(index);
			}
		}
	}

	// Calculates shortest link and kills all links around the Node in that radius
	// Used for making routes around objects account for the object's size
	// Uses actual distances rather than Connector settings

	public void radialDisconnect(){
		float radius = 0.0f;
		for(int j = 0; j < links.size(); j++){
			myLink = (Connector) links.get(j);
			if(straightLink(myLink.n)){
				radius = dist(myLink.n);
				break;
			}
		}
		for(int j = 0; j < links.size(); j++){
			myLink2 = (Connector) links.get(j);
			removeMe = new ArrayList<Node>();
			for(int k = 0; k < myLink2.n.links.size(); k++){
				myLinkLink = (Connector) myLink2.n.links.get(k);
				float midX = (myLink2.n.x + myLinkLink.n.x) * 0.5f;
				float midY = (myLink2.n.y + myLinkLink.n.y) * 0.5f;
				float midZ = (myLink2.n.z + myLinkLink.n.z) * 0.5f;
				temp4 = new Node(midX, midY, midZ);
				if(dist(temp4) <= radius){
					removeMe.add(myLinkLink.n);
				}
			}
			for(int k = 0; k < removeMe.size(); k++){
				temp5 = (Node) removeMe.get(k);
				int index = myLink2.n.indexOf(temp5);
				if(index > -1){
					myLink2.n.links.remove(index);
				}
			}
		}
	}

	// Checks if a Node's position differs along one dimension only

	public boolean straightLink(Node myLink){
		if(indexOf(myLink) < 0){
			return false;
		}
		int dimDelta = 0;
		if(x != myLink.x){
			dimDelta++;
		}
		if(y != myLink.y){
			dimDelta++;
		}
		if(z != myLink.z){
			dimDelta++;
		}
		if(dimDelta == 1){
			return true;
		}
		return false;
	}

	//
	// Location tools
	//

	// Euclidean distance measuring for accuracy

	public float dist(Node n){
		if(z == 0.0 && n.z == 0.0){
			return (float) Math.sqrt(((x - n.x) * (x - n.x))
					+ ((y - n.y) * (y - n.y)));
		}else{
			return (float) Math.sqrt(((x - n.x) * (x - n.x))
					+ ((y - n.y) * (y - n.y)) + ((z - n.z) * (z - n.z)));
		}
	}

	// Manhattan distance measuring for avoiding jagged paths

	public float manhattan(Node n){
		if(z == 0.0 && n.z == 0.0){
			return ((x - n.x) * (x - n.x)) + ((y - n.y) * (y - n.y))
					+ ((z - n.z) * (z - n.z));
		}else{
			return ((x - n.x) * (x - n.x)) + ((y - n.y) * (y - n.y));
		}
	}

	public float distanceTo(Node node) {
		return manhattan(node);
	}
}
