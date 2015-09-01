package lemons.api.tasks.banking;

public class WithdrawRequirement {

	public final String name;
	public final int max, min;

	public WithdrawRequirement(String name, int max, int min) {
		this.name = name;
		this.max = max;
		this.min = min;
	}

}
