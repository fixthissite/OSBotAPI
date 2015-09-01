package lemons.api.tasks;

import lemons.api.tasks.templates.AbstractTask;

public class CollectionBoxTask extends AbstractTask {

	@Override
	public void run() {
		getWidgets().get(402, 2).getChildWidget(11).interact();
	}

	@Override
	public boolean isActive() {
		return getWidgets().get(402, 2) != null &&
				getWidgets().get(402, 2).isVisible() &&
				getWidgets().get(402, 2).getChildWidget(11) != null &&
				getWidgets().get(402, 2).getChildWidget(11).isVisible();
	}

}
