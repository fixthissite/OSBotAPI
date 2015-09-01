package lemons.api.tasks.templates;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

import lemons.api.painters.Painter;
import lemons.api.script.TaskScript;

import org.osbot.rs07.api.ui.Message;

/**
 * An extension of the AbstractTask, the ComplexTask deals with any Task
 * having a list of requirements to complete. This will be most Tasks that
 * are beyond a single action.
 *
 */
public class ComplexTask extends AbstractTask {

	private ArrayList<AbstractTask> tasks = new ArrayList<AbstractTask>();
	private HashMap<AbstractTask, Boolean> taskCheck = new HashMap<AbstractTask, Boolean>(),
					taskStick = new HashMap<AbstractTask, Boolean>();
	private AbstractTask task;
	private boolean init = false;
	private boolean stickyTasks = false;
	private AbstractTask tmptask;
	
	public ComplexTask setSticky(boolean b) {
		stickyTasks = b;
		return this;
	}
	
	@Override
	public void setScript(TaskScript s) {
		super.setScript(s);
	}
	public ComplexTask addTask(AbstractTask task) {
		return addTask(task, false);
	}
	
	public ComplexTask addTask(AbstractTask task, boolean forceCheck) {
		return addTask(null, task, forceCheck);
	}
	
	public ComplexTask addTask(Supplier<Boolean> s, AbstractTask task) {
		return addTask(s, task, false);
	}
	
	public ComplexTask addTask(Supplier<Boolean> s, AbstractTask task, boolean forceCheck) {
		if (!init) {
			getLogger().error("Cannot add tasks outside the onInit function ("+getTaskName()+")!");
			return this;
		}
		if (task == null) {
			getLogger().debug("addTask was passed a null task ("+getTaskName()+")");
			return this;
		}
		if (s != null)
			task = new ConditionalTask(s, task);
		task.setDepth(getDepth() + 1);
		if (stickyTasks)
			taskCheck.put(task, forceCheck);
		else
			taskCheck.put(task, true);
		if (!stickyTasks)
			taskStick.put(task, forceCheck);
		else
			taskStick.put(task, false);
		if (getScript() != null) {
			getLogger().debug(task.depthString()+task.getTaskName()+" initialized");
			task.setScript(getScript());
			task.onStart();
		} else {
			getLogger().debug(task.depthString()+task.getTaskName()+" added");
		}
		tasks.add(task);
		return this;
	}
	
	@Override
	public void setDepth(int i) {
		super.setDepth(i);
		
		for (AbstractTask t : tasks) {
			t.setDepth(i + 1);
		}
	}
	
	@Override
	public String getStatus() {
		return task != null ? task.getStatus() : "Idle";
	}
	
	@Override
	public int getChildCount() {
		return tasks.size();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		init = true;
	}
	
	@Override
	public void onExit() {
		super.onExit();
		
		for (AbstractTask t : tasks) {
			t.onExit();
		}
	}
	
	@Override
	public void onTaskStart() {
		super.onTaskStart();
		if (task != null) {
			task.onTaskStart();
			task = null;
		}
	}
	
	@Override
	public void onTaskFinish() {
		super.onTaskFinish();
		if (task != null) {
			task.onTaskFinish();
			task = null;
		}
	}
	
	public final void run() {
		boolean newTask = (task == null);
		if (task == null && tmptask != null) {
			task = tmptask;
			newTask = true;
			tmptask = null;
		} else {
			if (task == null || !taskStick.get(task))
				for (AbstractTask ctask : tasks) {
					boolean mightRun = taskCheck.get(ctask) || (!stickyTasks ? true : task == null);
					if (mightRun && ctask.isActive()) {
						newTask = task == null || ctask.hashCode() != task.hashCode();
						task = ctask;
						break;
					}
				}
		}
		
		if (task == null) {
			return;
		}
		
		if (newTask) {
			debug("New task, triggering onTaskStart");
			task.onTaskStart();
		}
		
		if (task.isActive()) {
			if (!(task instanceof ComplexTask))
				debug("Running task "+task.getTaskName());
			task.rrun();
		} else {
			debug("End of task, triggering onTaskEnd");
			task.onTaskFinish();
			task = null;
		}
	}
	
	public void activ() {
		if (!getLogger().isDebug())
			return;
		for (AbstractTask ctask : tasks) {
			if (ctask.isActive()) {
				debug(ctask.depthString()+ctask.getTaskName()+" is active ("+ctask.getChildCount()+")");
				if (ctask instanceof ComplexTask)
					((ComplexTask) ctask).activ();
				if (tmptask == null)
					tmptask = ctask;
				return;
			}
			debug(ctask.depthString()+ctask.getTaskName()+" is idle ("+ctask.getChildCount()+")");
			if (ctask instanceof ComplexTask)
				((ComplexTask) ctask).activ();
		}
	}
	
	public final boolean isActive() {
		for (AbstractTask ctask : tasks) {
			if (ctask.isActive())
				return true;
		}
		return false;
	}
	
	@Override
	public void onPaint(Graphics2D g) {
		super.onPaint(g);
		
		if (task != null)
			task.onPaint(g);
		
		for (Painter p : painters)
			try {
				p.paint(g);
			} catch (Exception e) {
				exception(e);
			}
	}
	
	private ArrayList<Painter> painters = new ArrayList<Painter>();
	
	public void addPainter(Painter p) {
		painters.add(p);
	}
	
	public ArrayList<Painter> getPainters() {
		return painters;
	}
	
	// Custom events for Tasks
	
	@Override
	public void onMessage(Message message) {
		super.onMessage(message);
		
		
		for (AbstractTask t : tasks) {
			t.onMessage(message);
		}
		
		for (Painter p : painters) {
			p.onMessage(message);
		}
	}

}
