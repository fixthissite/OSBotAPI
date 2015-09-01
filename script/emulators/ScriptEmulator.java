package lemons.api.script.emulators;

import java.util.Random;

import lemons.api.script.TaskScript;

import org.osbot.rs07.Bot;
import org.osbot.rs07.antiban.AntiBan;
import org.osbot.rs07.api.*;
import org.osbot.rs07.api.map.Position;
import org.osbot.rs07.api.util.ExperienceTracker;
import org.osbot.rs07.event.Event;
import org.osbot.rs07.script.Script;

/**
 * Attempts to mimic all the functionality available in a Script, along with
 * additional API stuff.
 */
public class ScriptEmulator<S extends Script> {
	
	private S script;

	public ScriptEmulator() {}

	public ScriptEmulator(S script) {
		setScript(script);
	}
	
	public void setScript(S script) {
		this.script = script;
	}
	
	public S getScript() {
		return script;
	}
	
	protected int random(int a, int b)  {
		int max = Math.max(a, b),
			min = Math.min(a, b);
		
		if (max == min)
			return max;
		
		return min + ((new Random()).nextInt(max - min));
	}
	
	// Sleep functions
	protected void sleep(int a) {
		try {
			TaskScript.sleep(a);
		} catch (InterruptedException e) { }
	}
	protected void sleep(int a, int b) {
		try {
			TaskScript.sleep(random(a, b));
		} catch (InterruptedException e) { }
	}
	
	@Deprecated
	protected void log(String s) {
		getScript().log(s);
	}
	
	protected void stop() {
		getScript().stop();
	}
	
	protected void execute(Event e) {
		getScript().execute(e);
	}
	
	// Give access to MethodProvider stuffs
	
	protected Position myPosition() {
		return script.myPosition();
	}

	protected AntiBan getAntiBan() {
		return script.getAntiBan();
	}

	protected String getAuthor() {
		return script.getAuthor();
	}

	public Bot getBot() {
		return script.getBot();
	}

	protected Camera getCamera() {
		return script.getCamera();
	}

	protected Chatbox getChatbox() {
		return script.getChatbox();
	}

	public Client getClient() {
		return script.getClient();
	}

	protected ColorPicker getColorPicker() {
		return script.getColorPicker();
	}

	protected Combat getCombat() {
		return script.getCombat();
	}

	protected Configs getConfigs() {
		return script.getConfigs();
	}

	protected DepositBox getDepositBox() {
		return script.getDepositBox();
	}

	protected Dialogues getDialogues() {
		return script.getDialogues();
	}

	protected DoorHandler getDoorHandler() {
		return script.getDoorHandler();
	}

	protected Equipment getEquipment() {
		return script.getEquipment();
	}

	protected ExperienceTracker getExperienceTracker() {
		return script.getExperienceTracker();
	}

	protected GrandExchange getGrandExchange() {
		return script.getGrandExchange();
	}

	protected HintArrow getHintArrow() {
		return script.getHintArrow();
	}

	protected Inventory getInventory() {
		return script.getInventory();
	}

	protected Keyboard getKeyboard() {
		return script.getKeyboard();
	}

	protected LocalWalker getLocalWalker() {
		return script.getLocalWalker();
	}

	protected LogoutTab getLogoutTab() {
		return script.getLogoutTab();
	}

	protected Magic getMagic() {
		return script.getMagic();
	}

	protected Map getMap() {
		return script.getMap();
	}

	protected Menu getMenuAPI() {
		return script.getMenuAPI();
	}

	protected Mouse getMouse() {
		return script.getMouse();
	}

	protected PollBooth getPollBooth() {
		return script.getPollBooth();
	}

	protected Prayer getPrayer() {
		return script.getPrayer();
	}

	protected Quests getQuests() {
		return script.getQuests();
	}

	protected Settings getSettings() {
		return script.getSettings();
	}

	protected Skills getSkills() {
		return script.getSkills();
	}

	protected Store getStore() {
		return script.getStore();
	}

	protected Tabs getTabs() {
		return script.getTabs();
	}

	protected Trade getTrade() {
		return script.getTrade();
	}

	protected double getVersion() {
		return script.getVersion();
	}

	protected Widgets getWidgets() {
		return script.getWidgets();
	}

	protected Worlds getWorlds() {
		return script.getWorlds();
	}

}
