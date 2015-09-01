package lemons.api.painters;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;

import lemons.api.script.TaskScript;
import lemons.api.walking.LocalPath.Node;
import lemons.api.walking.map.Tile;

public class DebugPainter extends Painter {

	public DebugPainter(TaskScript s) {
		super(s);
		setFPS(60);
	}
	
	private boolean enableMouseTrail = true,
			enableMouseCursor = true,
			enableMemoryBar = true;
	
	@Override
	public void onPaint(Graphics2D g) {
		try {
			int myX = myPlayer().getLocalX(),
					myY = myPlayer().getLocalY();
			if (getWalker().tileData != null) {
				for (int x = 0; x < 104; x++) {
					for (int y = 103; y >= 0; y--) {
						g.setColor(Color.GRAY);
						if (x == myX && y == myY) {
							g.setColor(Color.WHITE);
						} else if (getWalker().tileData[x][y] == 1) {
							g.setColor(Color.GREEN);
						} else if (getWalker().tileData[x][y] == 2) {
							g.setColor(Color.RED);
						}
						g.drawRect(10+(x*2), 218-((y+1)*2), 1, 1);
					}
				}
			}
		} catch (Exception e) {
			getLogger().exception(e);
		}
		
		if (enableMouseTrail)
			paintMouseTrail(g);
		if (enableMouseCursor)
			paintMouseCursor(g);
		paintMemory(g);
		
		if (!isInGame())
			return;
		
		// Position
		g.setColor(Color.CYAN);
		Font f = g.getFont();
		g.setFont(new Font("", Font.BOLD, 10));
		String str = myLocation().toString();
		g.drawString(str, 762 - g.getFontMetrics().stringWidth(str), 154);
		str = "Yaw: "+getCamera().getYawAngle()+" Pitch: "+getCamera().getPitchAngle();
		g.drawString(str, 762 - g.getFontMetrics().stringWidth(str), 164);
		g.setFont(f);
		
		getInteract().clearAreas();
		
		if (getInteract().getProcessedArea() != null) {
			g.setColor(new Color(255, 0, 0));
			g.draw(getInteract().getProcessedArea());
		}
		
		if (getInteract().getPlayerArea() != null) {
			g.setColor(new Color(0, 255, 0));
			g.draw(getInteract().getPlayerArea());
		}
		
		if (getInteract().getNpcArea() != null) {
			g.setColor(new Color(0, 255, 255));
			g.draw(getInteract().getNpcArea());
		}
		
		if (getInteract().getRawArea() != null) {
			g.setColor(new Color(0, 255, 255));
			g.draw(getInteract().getRawArea());
		}
	}
	
	private ArrayList<Point> points = new ArrayList<Point>();
	
	private void paintMouseTrail(Graphics2D g) {
		points.add(getMouse().getPosition());
		if (points.size() > 120)
			points.remove(0);
		Point lp = null;
		g.setColor(new Color(0, 0, 0, 150));
		for (int i = 1; i >= 0; i--) {
			for (Point p : points) {
				if (lp != null) {
					if (lp.x != p.x || lp.y != p.y)
						g.drawLine(p.x + i, p.y + i, lp.x + i, lp.y + i);
				}
				lp = p;
			}
			lp = null;
			g.setColor(new Color(255, 255, 255, 150));
		}
	}

	private void paintMouseCursor(Graphics2D g) {
		Point p = getMouse().getPosition();
		g.setColor(Color.red);
		g.drawLine(p.x - 2, p.y - 2, p.x + 2, p.y + 2);
		g.drawLine(p.x + 2, p.y - 2, p.x - 2, p.y + 2);
	}
	
	private Runtime runtime = Runtime.getRuntime();

	private final int MEMORY_WIDTH = 766,
				MEMORY_HEIGHT = 2,
				MEMORY_X = 0,
				MEMORY_Y = 0,
				MEMORY_TEXT_X = 760,
				MEMORY_TEXT_Y = 13;
	
	private void paintMemory(Graphics2D g) {
        double maxMemory = runtime.maxMemory();
        double totalMemory = runtime.totalMemory();
        double freeMemory = runtime.freeMemory();
        String str3 = humanReadableByteCount((long) (totalMemory - freeMemory), false),
        		str2 = humanReadableByteCount((long) totalMemory, false),
        		str1 = humanReadableByteCount((long) maxMemory, false);
        Font f = g.getFont();
        g.setFont(new Font("", Font.BOLD, 10));
        g.setColor(new Color(0, 138, 255));
        if (enableMemoryBar)
        	g.fillRect(MEMORY_X, MEMORY_Y, MEMORY_WIDTH, MEMORY_HEIGHT);
        g.drawString(str1, MEMORY_TEXT_X - g.getFontMetrics().stringWidth(str1), MEMORY_TEXT_Y + 18);
        g.setColor(new Color(255, 168, 0));
        if (enableMemoryBar)
        	g.fillRect(MEMORY_X, MEMORY_Y, (int) ((totalMemory / maxMemory) * MEMORY_WIDTH), MEMORY_HEIGHT);
        g.drawString(str2, MEMORY_TEXT_X - g.getFontMetrics().stringWidth(str2), MEMORY_TEXT_Y + 9);
        g.setColor(new Color(255, 0, 0));
        if (enableMemoryBar)
        	g.fillRect(MEMORY_X, MEMORY_Y, (int) (((totalMemory - freeMemory) / maxMemory) * MEMORY_WIDTH), MEMORY_HEIGHT );
        g.drawString(str3, MEMORY_TEXT_X - g.getFontMetrics().stringWidth(str3), MEMORY_TEXT_Y);
        g.setFont(f);
	}

	public static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	public DebugPainter enableMouseTrail(boolean b) {
		enableMouseTrail = b;
		return this;
	}
	
	public DebugPainter enableMouseCursor(boolean b) {
		enableMouseCursor = b;
		return this;
	}
	
	public DebugPainter enableMemoryBar(boolean b) {
		enableMemoryBar = b;
		return this;
	}

}
