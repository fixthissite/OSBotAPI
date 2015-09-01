package lemons.api.script;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.Supplier;

import lemons.api.Bank;
import lemons.api.Logger;
import lemons.api.Monitor;
import lemons.api.painters.Painter;
import lemons.api.script.entities.GroundItems;
import lemons.api.script.entities.NPCS;
import lemons.api.script.entities.Objects;
import lemons.api.script.entities.Players;
import lemons.api.script.interaction.Interact;
import lemons.api.tasks.templates.AbstractTask;
import lemons.api.tasks.templates.ComplexTask;
import lemons.api.tasks.templates.ConditionalTask;
import lemons.api.utils.Timer;
import lemons.api.walking.Walker;

import org.osbot.rs07.antiban.AntiBan.Behavior;
import org.osbot.rs07.antiban.AntiBan.BehaviorType;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.api.ui.Message.MessageType;
import org.osbot.rs07.input.mouse.BotMouseListener;
import org.osbot.rs07.script.Script;

public abstract class TaskScript extends Script {

	private ComplexTask tasks;
	private Logger log;
	private Walker web;
	private long lastTime = 0;
	private final long MIN_LOOP_TIME = 500;
	
	SimpleDateFormat file_sdf = new SimpleDateFormat("YYYY-MM-dd_HH-mm-ss");
	private Interact interact;
	private Objects objects2;
	private lemons.api.script.entities.GroundItems grounditems2;
	private Players players2;
	private NPCS npcs2;
	private Bank bank2;
	private ArrayList<Thread> threads = new ArrayList<Thread>();
	private Timer timer;
	private boolean running = true;

	public ComplexTask getTasks() {
		return tasks;
	}
	
	public void addTask(AbstractTask t) {
		addTask(t, false);
	}
	
	public void addTask(AbstractTask t, boolean forceCheck) {
		addTask(null, t, forceCheck);
	}
	
	public void addTask(Supplier<Boolean> s, AbstractTask t) {
		addTask(s, t, false);
	}
	
	public void addTask(Supplier<Boolean> s, AbstractTask t, boolean forceCheck) {
		if (tasks == null) {
			tasks = new ComplexTask();
			tasks.setScript(this);
			tasks.onStart();
		}
		if (s == null)
			tasks.addTask(t, forceCheck);
		else
			tasks.addTask(new ConditionalTask(s, t), forceCheck);
	}
	
	public void addPainter(Painter p) {
		if (tasks == null) {
			tasks = new ComplexTask();
			tasks.setScript(this);
			tasks.onStart();
		}
		tasks.addPainter(p);
	}
	
	@Override
	public void onPaint(Graphics2D g) {
		super.onPaint(g);
		if (tasks != null)
			tasks.onPaint(g);
	}
	
	@Override
	public final int onLoop() {
		timer.reset();
		// Look through the widgets
		if (lastTime > 0 && lastTime < System.currentTimeMillis() + MIN_LOOP_TIME) {
			zzz((int) (lastTime + MIN_LOOP_TIME - System.currentTimeMillis()));
		}
		lastTime = System.currentTimeMillis();
		try {
			if (tasks != null) {
				getLogger().debug("Task activity tree:");
				tasks.activ();
				tasks.run();
			}
		} catch (Exception e) {
			log.exception(e);
		}
		return 0;
	}
	
	@Override
	public void onExit() {
		running  = false;
		try {
			tasks.onExit();
			super.onExit();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onStart() {
		try {
			super.onStart();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Painter mouse listener
		getBot().addMouseListener(new BotMouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
				for (Painter p : tasks.getPainters())
					p.mouseReleased(e);
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				for (Painter p : tasks.getPainters())
					p.mousePressed(e);
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				for (Painter p : tasks.getPainters())
					p.mouseExited(e);
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				for (Painter p : tasks.getPainters())
					p.mouseEntered(e);
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				for (Painter p : tasks.getPainters())
					p.mouseClicked(e);
			}
			
			@Override
			public boolean blockInput(Point arg0) {
				for (Painter p : tasks.getPainters())
					if (p.blockInput(arg0))
						return true;
				return false;
			}
		});
		timer = new Timer(60000);
		log = new Logger(this);
		web = new Walker(this);
		bank2 = new Bank(this);
		objects2 = new Objects(this);
		grounditems2 = new GroundItems(this);
		npcs2 = new NPCS(this);
		players2 = new Players(this);
		web.loadObstacles();
		interact = new Interact(this);
		getAntiBan().unregisterAllBehaviors();
		for (Behavior b : getAntiBan().getBehavior(BehaviorType.OTHER))
			getAntiBan().registerBehavior(b);
		addMonitor(bank2);
		addMonitor(new Monitor() {
			
			private ArrayList<Message> lastMessages;

			@Override
			public int sleep() {
				return 500;
			}
			
			@Override
			public void look() {
				ArrayList<Message> messages = new ArrayList<Message>();
				for (int i = 0; getWidgets().get(162, 43, i) != null; i += 2) {
					String name = getWidgets().get(162, 43, i).getMessage();
					String message = getWidgets().get(162, 43, i + 1).getMessage();
					if (!message.isEmpty())
						messages.add(new Message(MessageType.PLAYER.ordinal(), name, message));
					else
						messages.add(new Message(MessageType.GAME.ordinal(), null, name));
				}
				
				if (messages.size() == 0)
					return;
				
				if (lastMessages == null) {
					lastMessages = messages;
				}
				
				// Check each message for changes
				for (int i = 0; i < lastMessages.size(); i++) {
					Message m = messages.get(i);
					Message l = lastMessages.get(i);
					
					if (!l.getMessage().equals(m.getMessage())) {
						// Different message in this index, continue until we find the index
						int index = 0;
						for (int a = i; a < messages.size(); a++) {
							if (l.getMessage().equals(messages.get(a).getMessage()))
								break;
							index++;
						}
						
						// Loop through and throw the new messages
						for (int a = 0; a < index; a++) {
							try {
								tasks.onMessage(messages.get(i));
							} catch (Exception e) {
								getLogger().exception(e);
							}
						}
						lastMessages = messages;
						return;
					}
				}
				
				lastMessages = messages;
			}
		});
	}
	
	protected void addMonitor(final Monitor monitor) {
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
				try {
					while (running) {
						try {
							long last = System.currentTimeMillis();
							try {
								monitor.look();
							} catch (Exception e) {
								getLogger().exception(e);
							}
							zzz((int) Math.max(monitor.sleep() - (System.currentTimeMillis() - last), 0));
						} catch (Exception e) {
							getLogger().exception(e);
						}
					}
					getLogger().debug("Monitor "+monitor.getClass().getName()+" has ended.");
				} catch (Exception e) {
					getLogger().exception(e);
				}
			}
		};
		Thread t = new Thread(r);
		t.start();
		threads.add(t);
	}
	
	public TaskScript getScript() {
		return this;
	}

	public Objects getObjects2() {
		return objects2;
	}
	
	public Bank getBank2() {
		return bank2;
	}
	
	public GroundItems getGroundItems2() {
		return grounditems2;
	}
	
	public NPCS getNpcs2() {
		return npcs2;
	}
	
	public Players getPlayers2() {
		return players2;
	}

	public Walker getWalker() {
		return web;
	}
	
	public Logger getLogger() {
		return log;
	}
	
	public Interact getInteract() {
		return interact;
	}

	public void zzz(int a, int b) {
		try {
			sleep(random(a, b));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void zzz(int a) {
		try {
			sleep(a);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	// Random functions
	public long random(long a, long b)  {
		long max = Math.max(a, b),
			min = Math.min(a, b);
		
		return min + ((new Random()).nextLong() * (max - min));
	}
	public double random(double a, double b)  {
		double max = Math.max(a, b),
			min = Math.min(a, b);
		
		return min + ((new Random()).nextDouble() * (max - min));
	}
	public float random(float a, float b)  {
		float max = Math.max(a, b),
			min = Math.min(a, b);
		
		return min + ((new Random()).nextFloat() * (max - min));
	}

}
