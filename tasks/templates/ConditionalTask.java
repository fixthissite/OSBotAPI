package lemons.api.tasks.templates;

import java.awt.Graphics2D;
import java.util.function.Supplier;

import lemons.api.script.TaskScript;

import org.osbot.rs07.api.ui.Message;

public class ConditionalTask extends AbstractTask {

	private final Supplier<Boolean> cond;
	public final AbstractTask task;

	public ConditionalTask(Supplier<Boolean> cond, AbstractTask task) {
		this.cond = cond;
		this.task = task;
	}
	
	@Override
	public void setDepth(int i) {
		super.setDepth(i);

		task.setDepth(getDepth());
	}
	
	@Override
	public String getStatus() {
		return task.getStatus();
	}
	
	@Override
	public String getTaskName() {
		return task.getTaskName();
	}
	
	@Override
	public void onMessage(Message message) {
		super.onMessage(message);
		
		if (task != null)
			task.onMessage(message);
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		task.onStart();
	}
	
	@Override
	public void setScript(TaskScript s) {
		super.setScript(s);
		
		task.setScript(s);
	}
	
	@Override
	public boolean isActive() {
		return cond.get();
	}
	
	@Override
	public void onTaskStart() {
		super.onTaskStart();
		
		task.onTaskStart();
	}
	
	@Override
	public void onTaskFinish() {
		super.onTaskFinish();
		
		task.onTaskFinish();
	}
	
	@Override
	public void run() {
		task.rrun();
	}

	@Override
	public int getChildCount() {
		return task.getChildCount();
	}
	
	@Override
	public void onPaint(Graphics2D g) {
		super.onPaint(g);
		
		task.onPaint(g);
	}
}
