package lemons.api.walking.map;

import lemons.api.script.TaskScript;
import lemons.api.tasks.banking.BankLocation;
import lemons.api.tasks.banking.BankTask;

import org.osbot.rs07.api.map.Area;

public class ResourceZone extends AbstractZone {

	private BankTask bank;
	private BankLocation bankLoc;
	
	public ResourceZone(TaskScript s) {
		super(s);
	}
	
	public ResourceZone(TaskScript s, Area p) {
		super(s, p);
	}
	
	public ResourceZone setBank(BankTask bank, BankLocation bankLoc) {
		this.bank = bank;
		this.bankLoc = bankLoc;
		return this;
	}

	public void updateBanking() {
		if (bank == null)
			return;
		bank.setBankLocation(bankLoc);
	}
	
}
