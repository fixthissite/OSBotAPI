package lemons.api.painters;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.input.mouse.BotMouseListener;

import lemons.api.script.TaskScript;
import lemons.api.script.emulators.TaskScriptEmulator;
import lemons.api.utils.Timer;

public abstract class Painter extends TaskScriptEmulator<TaskScript> implements BotMouseListener {
	
	public Painter(TaskScript s) {
		super(s);
	}

	private int fps = 1;
	private Image offscreen = new BufferedImage(764, 502, BufferedImage.TYPE_INT_ARGB);
	private Timer timer;
	private HashMap<Rectangle, Consumer<Graphics2D>> buttons = new HashMap<Rectangle, Consumer<Graphics2D>>();
	private HashMap<Rectangle, Consumer<MouseEvent>> clicks = new HashMap<Rectangle, Consumer<MouseEvent>>();
	
	public abstract void onPaint(Graphics2D g);
	
	public void onMessage(Message m) {
		
	}
	
	public final void setFPS(int fps) {
		this.fps = fps;
	}
	
	public final void paint(Graphics2D g) {
		try {
			if (!isInGame())
				return;
			for (Rectangle r : buttons.keySet()) {
				BufferedImage img = new BufferedImage(r.width, r.height,  BufferedImage.TYPE_INT_ARGB);

				Graphics2D g2 = (Graphics2D) img.getGraphics();
				
				g2.setColor(new Color(0, 0, 0, 0));
				g2.fillRect(0, 0, r.width, r.height);
				
				// Color defaults to white.
				g2.setColor(new Color(255, 255, 255, 255));
				// Render their stuff
				buttons.get(r).accept(g2);
				
				g.drawImage(img, r.x, r.y, null);
			}
			if (timer == null || fps == 60 || !timer.isRunning()) {
				// Double buffer ho!
				Graphics2D tmpG2d = (Graphics2D) offscreen.getGraphics();
				tmpG2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
				tmpG2d.setBackground(new Color(255,255,255,0));
				tmpG2d.clearRect(0, 0, 764, 502);
				tmpG2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
				
				// This function is your onPaint() stuff
				onPaint((Graphics2D) offscreen.getGraphics());
				
				if (timer != null)
					timer.reset();
				else
					timer = new Timer(1000 / fps);
			}
			
			g.drawImage(offscreen, 0, 0, null);
		} catch (Exception e) {
			exception(e);
		}
	}
	
	
	public void addButton(Rectangle r, Consumer<Graphics2D> image, Consumer<MouseEvent> onClick) {
		buttons.put(r, image);
		clicks.put(r, onClick);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		for (Rectangle r : clicks.keySet()) {
			debug("Mouse is clicked at "+e.getX()+","+e.getY());
			if (r.contains(e.getPoint())) {
				clicks.get(r).accept(e);
				return;
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}

	@Override
	public boolean blockInput(Point arg0) {
		for (Rectangle r : clicks.keySet()) {
			if (r.contains(arg0)) {
				debug("Blocking input for button!");
				return true;
			}
		}
		return false;
	};
}
