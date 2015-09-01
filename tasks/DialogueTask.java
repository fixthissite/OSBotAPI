package lemons.api.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import lemons.api.script.entities.NPC;
import lemons.api.tasks.templates.AbstractTask;

import org.osbot.rs07.api.ui.RS2Widget;

public class DialogueTask extends AbstractTask {
	
	private String[] optionTexts = new String[] {
			"Select an Option"
	};

	private ArrayList<String> options = new ArrayList<String>();
	private boolean doOnce = false, doTalk = false;
	private boolean hasTalked;
	private Predicate<NPC> pred;
	
	public DialogueTask(Predicate<NPC> n) {
		pred = n;
	}

	@Override
	public void run() {
		if (!inDialogue()) {
			debug("Talking to NPC");
			// Need to talk to the NPC
			interact(getNpcs().closest(pred), "Talk-to");
			for (int i = 0; i < 100 && myPlayer().isMoving() && !inDialogue(); i++)
				sleep(80, 100);
			hasTalked = inDialogue();
			return;
		}
		
		if (canContinue()) {
			debug("Continuing with space...");
			sleep(1000, 2000);
			spaceToContinue();
			sleep();
			return;
		}
		
		if (!canChoose())
			return;
		
		debug("Choosing an option...");
		if (getOptions() == null || getOptions().size() == 0)
			return;
		
		// We got an option
		int i = 0;
		for (String option : getOptions()) {
			i++;
			for (String optionName : options) {
				if (!option.toLowerCase().contains(optionName.toLowerCase()))
					continue;
				
				getLogger().debug("Choosing option "+option);
				
				sleep(1000, 2000);
				if (chooseOption(i)) {
					sleep();
				}
				return;
			}
		}
	}
	
	public DialogueTask setInitDialogue(boolean b) {
		doTalk = true;
		return this;
	}
	
	private ArrayList<String> getOptions() {
		ArrayList<String> options = new ArrayList<String>();
		for (int i = 1; widget(i) != null; i++) {
			options.add(widget(i).getMessage());
		}
		return options;
	}
	
	private RS2Widget widget(int id) {
		List<RS2Widget> widgets = getWidgets().containingText(optionTexts);
		for (RS2Widget widget : widgets) {
			return getWidgets().get(widget.getRootId(), 0, id);
		}
		return null;
	}

	private boolean chooseOption(int optionIndex) {
		debug("Choosing index "+optionIndex);
		 getKeyboard().typeString(""+optionIndex);
		return true;
	}

	private void spaceToContinue() {
		getDialogues().clickContinue();
	}

	private boolean canChoose() {
		return getDialogues().isPendingOption();
	}

	private boolean canContinue() {
		return getDialogues().isPendingContinuation();
	}

	private boolean inDialogue() {
		return getDialogues().inDialogue();
	}

	private void sleep() {
		for (int x = 0; x < 50 && getWidgets().containingText("Please wait").size() > 0; x++)
			sleep(80, 100);
	}

	@Override
	public boolean isActive() {
		if (doTalk)
			return true;
		return (doOnce ? !hasTalked : false) || 
				(inDialogue() && isThisNPC())
				|| canContinue();
	}
	
	private boolean isThisNPC() {
		NPC n = getNpcs().closest(pred);
		return n != null && tile(n).dist() < 7;
	}

	public DialogueTask addOption(String optionName) {
		options.add(optionName);
		return this;
	}

}
