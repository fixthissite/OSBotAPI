package lemons.api.tasks;

public enum TaskPriority {
	/**
	 * EQUAL sorts based on the amount of items in your inventory.
	 */
	EQUAL,
	/**
	 * LEVEL sorts by the required level to mine the rock.
	 */
	LEVEL,
	/**
	 * VALUE sorts by the value of the rock.
	 */
	VALUE,
	/**
	 * ORDER sorts by the order the tasks were received in
	 */
	ORDER
}