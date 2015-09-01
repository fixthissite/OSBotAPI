package lemons.api.painters;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import lemons.api.script.TaskScript;
import lemons.api.utils.PriceGrabber;
import lemons.api.utils.Timer;

import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.Skill;
import org.osbot.rs07.api.ui.Tab;

public class GenericPainter extends Painter {
	
	private Timer timer = new Timer(1000);
	
	private LinkedHashMap<Skill, Integer> startXp = new LinkedHashMap<Skill, Integer>();
	private LinkedHashMap<Skill, Boolean> enabledSkills = new LinkedHashMap<Skill, Boolean>();
	private LinkedHashMap<Skill, Integer[]> startSkills = new LinkedHashMap<Skill, Integer[]>();
	private LinkedHashMap<Skill, Object[]> cachedSkills = new LinkedHashMap<Skill, Object[]>();
	private LinkedHashMap<String, Integer[]> storeExpenses = new LinkedHashMap<String, Integer[]>();
	private LinkedHashMap<Integer, ArrayList<Integer>> storeItems = new LinkedHashMap<Integer, ArrayList<Integer>>();
	
	private LinkedHashMap<String, String[]> cachedItems = new LinkedHashMap<String, String[]>();

	private NumberFormat df = NumberFormat.getInstance();
	
    private int[] xpLevels = { 0, 83, 174, 276, 388, 512, 650, 801, 969, 1154,
        1358, 1584, 1833, 2107, 2411, 2746, 3115, 3523, 3973, 4470, 5018,
        5624, 6291, 7028, 7842, 8740, 9730, 10824, 12031, 13363, 14833,
        16456, 18247, 20224, 22406, 24815, 27473, 30408, 33648, 37224,
        41171, 45529, 50339, 55649, 61512, 67983, 75127, 83014, 91721,
        101333, 111945, 123660, 136594, 150872, 166636, 184040, 203254,
        224466, 247886, 273742, 302288, 333804, 368599, 407015, 449428,
        496254, 547953, 605032, 668051, 737627, 814445, 899257, 992895,
        1096278, 1210421, 1336443, 1475581, 1629200, 1798808, 1986068,
        2192818, 2421087, 2673114, 2951373, 3258594, 3597792, 3972294,
        4385776, 4842295, 5346332, 5902831, 6517253, 7195629, 7944614,
        8771558, 9684577, 10692629, 11805606, 13034431, 200000001 };

	private int ttlhours, ttlminutes, ttlseconds, skillIter, countItems,
		tmpProfitGross, tmpProfitItems, curYOffset, iter, tmpProfitNet;

	private String tmpSkill, tmpString, profitItems = "--", profitGross = "--", profitNet = "--",
			profitItemsHr = "--", profitGrossHr = "--", profitNetHr = "--", progressTimeString = "",
			result, ttl;
	
	private StringBuffer sb = new StringBuffer();

	private double progressTime, xpPerSec, tmpValue;

	private Color lastColor, curColor;
	private Font lastFont;
	private long timeStart, ttlSec;
	private String[] data;

	private ArrayList<Integer> cachedProfit;

	private Integer[] cachedExpense;

	private int count;

	private boolean hasUpdatedExp = false;

	private boolean debug = false;

	public String itemVerb = "Made";
	
	public boolean hideStuff = false;
	
	public GenericPainter(TaskScript s) {
		super(s);
		
		for (Skill skill : getSkillArray()) {
			disableSkill(skill);
		}
		
		start();
		
		addButton(new Rectangle(530, 3, 16, 16), g -> {
			g.setColor(new Color(0, 0, 0));
			g.fillRect(0, 0, 15, 15);
			g.setColor(new Color(255, 255, 255));
			g.drawRect(1, 1, 14, 14);
			g.drawLine(1, 8, 15, 8);
			if (hideStuff)
				g.drawLine(8, 1, 8, 15);
		}, e -> {
			hideStuff = !hideStuff;
		});
	}
	
	public void start() {
		timeStart = System.currentTimeMillis();
	}
	
	public GenericPainter enableSkill(Skill skill) {
		enabledSkills.put(skill, true);
		return this;
	}
	
	public void disableSkill(Skill skill) {
		enabledSkills.put(skill, false);
	}
	
	public void updateItemProfit(Item item) {
		String name = item.getName();
		cachedProfit = storeItems.get(item.getId());
		cachedExpense = storeExpenses.get(name);
		count = 0;
		if (cachedProfit != null)
			for (int i : cachedProfit) {
				count += i;
			}
		int value = PriceGrabber.lookUp(item.getId());
		if (cachedProfit != null && cachedExpense != null) {
			cachedItems.put(name, new String[] {
					formatValue(count),
					formatValue(-cachedExpense[0]),
					formatValue((count * value) - cachedExpense[1]) });
		} else if (cachedProfit != null) {
			cachedItems.put(name, new String[] {
					formatValue(count),
					formatValue(0),
					formatValue((count * value)) });
		} else {
			cachedItems.put(name, new String[] {
					formatValue(0),
					formatValue(-cachedExpense[0]),
					formatValue(-cachedExpense[1]) });
		}
	}
	
	public void onPaint(Graphics2D g) {
		doAccounting();
		if (!hasUpdatedExp && getSkills().getStatic(Skill.HITPOINTS) > 8) {
			for (Skill skill : getSkillArray()) {
				startXp.put(skill, getSkills().getExperience(skill));
			}
			hasUpdatedExp = true;
		} else if (!hasUpdatedExp) {
			// Wait for us to get EXP
			return;
		}
		
		if (!timer.isRunning()) {
			timer.reset();
			//timeLeft = (Main.logoutTimer != null ? formatTimeStamp((int) Main.logoutTimer.getRemaining() / 1000) : "--:--:--");
				
			updateintCache();
			updateProfit();
			
			progressTimeString = formatTimeStamp((long) progressTime / 1000);
		}
		
		lastColor = g.getColor();
		lastFont = g.getFont();
		
		skillIter = 0;
		g.setFont(new Font("", Font.PLAIN, 12));
		
		if (debug ) {
			curColor = Color.WHITE;
			drawString(g, "Animation: "+myPlayer().getAnimation(), 20, 200);
		}

		for (Skill skill : getSkillArray()) {
			if (!cachedSkills.containsKey(skill) || !enabledSkills.get(skill)
					|| ((String) cachedSkills.get(skill)[2]).equalsIgnoreCase("0"))
				continue;
			
			curYOffset = 18 * skillIter;
			
			// Create level bar for each skill
			g.setColor(new Color(0, 0, 0, 220));
			g.fillRect(7, 345 + curYOffset, 490, 18);
			g.setColor(new Color(114, 79, 45, 255));
			g.fillRect(7, 347 + curYOffset, (int) cachedSkills.get(skill)[4], 14);
			
			// Create 
			g.setFont(new Font("", Font.PLAIN, 12));
			
			curColor = Color.WHITE;
			drawString(g, getName(skill)+" Level: ", 20, 358 + curYOffset);
			drawString(g, "|", 204, 358 + curYOffset);
			drawString(g, "XP/hr:", 215, 358 + curYOffset);
			drawString(g, "|", 361, 358 + curYOffset);
			drawString(g, "TTL: ", 381, 358 + curYOffset);
			
			g.setFont(new Font("", Font.BOLD, 12));
			
			curColor = Color.ORANGE;
			drawString(g, ""+((String) cachedSkills.get(skill)[0])+" (+"+((String) cachedSkills.get(skill)[1])+")", 150, 358 + curYOffset);
			drawString(g, ""+((String) cachedSkills.get(skill)[2])+"/hr", 256, 358 + curYOffset);
			drawString(g, (String) cachedSkills.get(skill)[3], 420, 358 + curYOffset);
			
			skillIter++;
		}
		
			g.setFont(new Font("", Font.PLAIN, 12));
		if (!hideStuff) {
			if (cachedItems.size() > 0 || storeExpenses.size() > 0) {
				iter = 1;
				drawItemRow("Item", "Gain", "Loss", "Profit", 0, g);
				
				if (cachedItems.size() > 0) {
					curColor = Color.ORANGE;
					
					curColor = Color.GREEN;
					
					for (String key : cachedItems.keySet()) {
						data = cachedItems.get(key);
						drawItemRow(key, data[0], data[1], data[2], iter++, g);
					}
				}
			}
		}
		
		iter = 0;
		
		curColor = Color.WHITE;
		drawString(g, "Net Profit:", 10, 314);
		drawString(g, "Gross Profit:", 10, 330);
		drawString(g, "Items "+itemVerb+": ", 10, 298);
		drawString(g, "Run Time:", 300, 330);
		
		curColor = Color.ORANGE;
		drawString(g, profitNet, 110, 314);
		drawString(g, profitGross, 110, 330);
		drawString(g, profitItems, 110, 298);
		
		drawString(g, profitNetHr+"/hr", 200, 314);
		drawString(g, profitGrossHr+"/hr", 200, 330);
		drawString(g, profitItemsHr+"/hr", 200, 298);
		
		drawString(g, progressTimeString, 400, 330);
		
		g.setColor(lastColor);
		g.setFont(lastFont);
	}

	private Skill[] getSkillArray() {
		return new Skill[] { Skill.AGILITY, Skill.ATTACK, Skill.HITPOINTS,
				Skill.CONSTRUCTION, Skill.COOKING, Skill.CRAFTING,
				Skill.DEFENCE, Skill.FARMING, Skill.FIREMAKING,
				Skill.FISHING, Skill.FLETCHING, Skill.HERBLORE,
				Skill.HUNTER, Skill.MAGIC, Skill.MINING, Skill.PRAYER,
				Skill.RANGED, Skill.RUNECRAFTING, Skill.SLAYER,
				Skill.SMITHING, Skill.STRENGTH, Skill.THIEVING,
				Skill.WOODCUTTING };
	}
	
	private void updateintCache() {
		progressTime = timeStart > 0 ? (System.currentTimeMillis() - timeStart) : 0;
		
		for (Skill skill : getSkillArray()) {
			if (!startSkills.containsKey(skill)) {
				startSkills.put(skill, new Integer[] {
						getSkills().getExperience(skill),
						getSkills().getStatic(skill)
				});
			}
			
			if (!enabledSkills.get(skill))
				continue;
			
			cachedSkills.put(skill, new Object[] {
					""+getSkills().getStatic(skill),
					""+totalLvl(skill),
					formatValue(perHour(totalXp(skill))),
					timeToLevel(skill),
					7 + (int)(487 * xpPercent(getSkills().getExperience(skill), getSkills().getStatic(skill)))
			});
			
			getSkills().getStatic(skill);
		}
	}
	
	private static String toProperCase(String s) {
    	return s.substring(0, 1).toUpperCase() +
    			s.substring(1).toLowerCase();
    }

	/**
     * This will return the string formated to
     * thousands, millions and billions.
     *
     * @param value
     * @return String
     */
	private String formatValue(double value) {
		result = Double.toString(value);
		
		tmpValue = Math.abs(value);

		if (tmpValue < 1000)
			result = df.format(value);
		else if (tmpValue >= 1000 && tmpValue < 1000000)
			result = df.format((value / 1000)) + "K";
		else if (tmpValue >= 1000000 && tmpValue < 1000000000)
			result = df.format((value / 1000000)) + "M";
		else if (tmpValue >= 1000000000)
			result = df.format((value / 1000000000)) + "B";

		return result;
	}

	/**
	 * This will return the the value per hour.
	 * 
	 * @param value
	 * @return String
	 */
	private double perHour(double value) {
		return (3600000.0 / progressTime) * value;
	}

	/**
	 * Turn the XP into a percentage.
	 * 
	 * @param xp
	 * @param level
	 * @return double
	 */
	private double xpPercent(int xp, int level) {
		if (level <= 0)
			return 0.0;
		return ((double) (xp - xpLevels[level - 1])
				/ (double) (xpLevels[level] - xpLevels[level - 1]));
	}
	
	private String formatTimeStamp(long seconds) {
		if (seconds <= 0)
			return "--:--:--";
		
		ttl = null;
		sb.delete(0, sb.length());
		
		ttlhours = (int) Math.floor(seconds / 3600);
		ttlminutes = (int) Math.floor((seconds % 3600) / 60);
		ttlseconds = (int) Math.floor(seconds % 60);
		
		if (ttlhours == Integer.MAX_VALUE)
			return "--:--:--";
		
		if (ttlhours < 10)
			sb.append("0");

		sb.append(ttlhours);
		sb.append(":");

		if (ttlminutes < 10)
			sb.append("0");

		sb.append(ttlminutes);
		sb.append(":");

		if (ttlseconds < 10)
			sb.append("0");

		sb.append(ttlseconds);
		ttl = sb.toString();
		
		return ttl;
	}

	/**
	 * Get the time to level.
	 * 
	 * @param skill
	 * @param runTime
	 * @return String
	 */
	private String timeToLevel(Skill skill) {
		if (totalXp(skill) == 0 || progressTime == 0)
			return "--:--:--";
		
		xpPerSec = (((double) totalXp(skill)) * 1000.0) / ((double) progressTime);

		ttlSec = (long) (remainingXp(skill) / xpPerSec);

		return formatTimeStamp(ttlSec);
	}
	
	private int totalXp(Skill skill) {
		return getSkills().getExperience(skill) - (int) startSkills.get(skill)[0];
	}
	
	private int totalLvl(Skill skill) {
		return getSkills().getStatic(skill) - (int) startSkills.get(skill)[1];
	}

	private double remainingXp(Skill skill) {
		return getSkills().getExperienceForLevel(getSkills().getStatic(skill) + 1) - 
				getSkills().getExperience(skill);
	}
	
	public void drawString(Graphics2D g, String s, int x, int y) {
		g.setColor(Color.BLACK);
		g.drawString(s, x + 1, y + 1);
		g.setColor(curColor);
		g.drawString(s, x, y);
	}


	private void drawItemRow(String name, String gained, String loss, String value, int i, Graphics2D g) {
		if (i != 0) {
			g.setColor(new Color(0, 0, 0, i % 2 == 0 ? 180 : 130));
			g.fillRect(176, 8 + (i * 16), 340, 16);
		} else {
			g.setColor(new Color(0, 0, 0, 200));
			g.fillRect(176, 4, 340, 20);
		}
		
		drawString(g, name, 181, 20 + (i * 16));
		drawString(g, gained, 330, 20 + (i * 16));
		drawString(g, loss, 390, 20 + (i * 16));
		drawString(g, value, 450, 20 + (i * 16));
	}
	
    private String getName(Skill skill) {
    	tmpSkill = getSkillName(skill);
    	tmpString = "";
    	for (String part : tmpSkill.split("_")){
    		tmpString = tmpString + toProperCase(part) + " ";
    	}
    	return tmpString.substring(0, tmpString.length() - 1).equalsIgnoreCase("Constitution") ?
    			"Health" : tmpString.substring(0, tmpString.length() - 1);
    }

    private String getSkillName(Skill skill) {
    	if (Skill.AGILITY == skill) return "AGILITY";
		if (Skill.ATTACK == skill) return "ATTACK";
		if (Skill.HITPOINTS == skill) return "CONSTITUTION";
		if (Skill.CONSTRUCTION == skill) return "CONSTRUCTION";
		if (Skill.COOKING == skill) return "COOKING";
		if (Skill.CRAFTING == skill) return "CRAFTING";
		if (Skill.DEFENCE == skill) return "DEFENSE";
		if (Skill.FARMING == skill) return "FARMING";
		if (Skill.FIREMAKING == skill) return "FIREMAKING";
		if (Skill.FISHING == skill) return "FISHING";
		if (Skill.FLETCHING == skill) return "FLETCHING";
		if (Skill.HERBLORE == skill) return "HERBLORE";
		if (Skill.HUNTER == skill) return "HUNTER";
		if (Skill.MAGIC == skill) return "MAGIC";
		if (Skill.MINING == skill) return "MINING";
		if (Skill.PRAYER == skill) return "PRAYER";
		if (Skill.RANGED == skill) return "RANGE";
		if (Skill.RUNECRAFTING == skill) return "RUNECRAFTING";
		if (Skill.SLAYER == skill) return "SLAYER";
		if (Skill.SMITHING == skill) return "SMITHING";
		if (Skill.STRENGTH == skill) return "STRENGTH";
		if (Skill.THIEVING == skill) return "THIEVING";
		if (Skill.WOODCUTTING == skill) return "WOODCUTTING";
		return null;
	}
	
	private void updateProfit() {
		tmpProfitGross = 0;
		tmpProfitItems = 0;
		
		for (int key : storeItems.keySet()) {
			countItems = 0;
			
			for (Integer i : storeItems.get(key)) {
				countItems += i;
				tmpProfitItems += i;
			}
			
			tmpProfitGross += PriceGrabber.lookUp(key) * countItems;
		}
		
		tmpProfitNet = tmpProfitGross;
		
		for (String key : storeExpenses.keySet()) {
			tmpProfitNet -= storeExpenses.get(key)[1];
		}

		profitGrossHr = ""+formatValue(perHour(tmpProfitGross));
		profitItemsHr = ""+formatValue(perHour(tmpProfitItems));
		profitNetHr = ""+formatValue(perHour(tmpProfitNet));
		profitGross = formatValue(tmpProfitGross);
		profitItems = formatValue(tmpProfitItems);
		profitNet = formatValue(tmpProfitNet);
	}

	private Item[] newItems, items;

	private Timer timer2;

	private boolean banking;

	private boolean muteItems = false;
	
	public void doAccounting() {
		if (getBank().isOpen() || muteItems)
			banking = true;
		
		// Check our time, and return if were not accounting
		if (timer2 != null && timer2.isRunning())
			return;
		
		if (timer2 == null)
			timer2 = new Timer(1000);
		else
			timer2.reset();
		
		if (!isInGame())
			return;
		
		if (!banking)
			compareItems();
		
		banking = false;
		
		items = getInventory().getItems();
	}
	
	private long getAmount(String item, Item[] items) {
		long i = 0;
		for (Item it : items) {
			if (item != null && it != null && item.equals(it.getName()))
				i += it.getAmount();
		}
		return i;
	}

	private void compareItems() {
		newItems = getInventory().getItems();
		if (items == null) {
			items = newItems;
		}
		for (int i = 0; i < items.length; i++) {
			if (items != null && items.length > i
					&& newItems != null
					&& items[i] != null
					&& getAmount(items[i].getName(), items) == getAmount(items[i].getName(), newItems))
				continue;
							
			if (items[i] == null && newItems.length > i && newItems[i] != null)
				addItem(newItems[i], newItems[i].getAmount());
			else if (items[i] != null && newItems[i] == null)
				addExpense(items[i], items[i].getAmount());
			else if (items[i] != null && newItems[i] != null) {
				if (!items[i].getName().equals(newItems[i].getName())) {
					// Potions
					if (!items[i].getName().replaceAll(" *\\([0-9]\\) *", "")
							.equals(newItems[i].getName().replaceAll(" *\\([0-9]\\) *", ""))) {
						// Item got switched?
						addExpense(items[i], items[i].getAmount());
						addItem(newItems[i], newItems[i].getAmount());
					} else {
						debug("Items are the same thing but changed doses, TODO!");
						//TODO
					}
				} else if (items[i].getAmount() < newItems[i].getAmount()) {
					addItem(items[i], newItems[i].getAmount() - items[i].getAmount());
				} else if (items[i].getAmount() > newItems[i].getAmount()) {
					addExpense(newItems[i], items[i].getAmount() - newItems[i].getAmount());
				}
			}
		}
	}

	public void haveBanked() {
		items = getInventory().getItems();
	}

	private void addItem(Item item, int qty) {
		String name = item.getName();
		
		if (item.getName() == null || item.getName().equalsIgnoreCase("null")) {
			getTabs().open(Tab.INVENTORY);
			sleep(500, 1000);
		}
		
		
		if (name == null) {
			// // Log.info("[Paint] Cannot add an item with a null name!");
			return;
		}
		if (!storeItems.containsKey(item.getId())) {
			storeItems.put(item.getId(), new ArrayList<Integer>());
		}
		
		storeItems.get(item.getId()).add(qty);
		
		countItems = 0;
		
		for (Integer i : storeItems.get(item.getId())) {
			countItems += i;
		}
		
		if (countItems == 0)
			return;
		
		updateItemProfit(item);
	}

	private void addExpense(Item item, int amount) {
		if (amount == 0)
			return;
		
		String name = item.getName();
		
		if (!storeExpenses.containsKey(name)) {
			storeExpenses.put(name, new Integer[]{0, 0});
		}
		
		cachedExpense = storeExpenses.get(name);
		
		// // Log.info("[Expense] "+name+" x"+amount+" = "+cost);
		int cost = PriceGrabber.lookUp(item.getId());
		storeExpenses.put(name, new Integer[] { cachedExpense[0] + amount, cachedExpense[1] + cost});
		
		updateItemProfit(item);
	}

	public void muteItems() {
		muteItems = true;
	}

	public void unmuteItems() {
		muteItems = false;
		items = getInventory().getItems();
		
	}
	
}
