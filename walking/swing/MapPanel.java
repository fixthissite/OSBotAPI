package lemons.api.walking.swing;
//necessary imports
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import lemons.api.walking.FileManager;
import lemons.api.walking.map.Tile;


public class MapPanel extends JPanel {

    
	/**
	 * 
	 */
	private static final long serialVersionUID = -3670280190057975946L;
	private int x, y;
    private int width = 400, height = 400;
    BufferedImage img;
	private MapListener lst;
    private final RenderingHints textRenderHints = new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    private final RenderingHints imageRenderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    private final RenderingHints renderHints = new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    int startX, startY;
	private Graphics2D graphics;
	private FileManager fm;

    public void setListener(MapListener e) {
    	lst = e;
    }
    
    public Point toGameCoords(Point p) {
    	return new Point(1984 + (p.x / 2), 4223 - (p.y / 2));
    }

    public Point toPanelCoords(Point p) {
    	return new Point((p.x - 1984) * 2, (4223 - p.y) * 2);
    }
    
    public MapPanel(FileManager fm) {
    	x = 0;
    	y = 0;
    	this.fm = fm;
    	
        addMouseListener(new MouseAdapter() {
            private Point gamePoint;
			private Point tmpPoint;
			@Override
            public void mousePressed(MouseEvent me) {
                super.mousePressed(me);
                startX = me.getX();
                startY = me.getY();
            }
            @Override
            public void mouseClicked(MouseEvent me) {
            	tmpPoint = me.getPoint();
            	tmpPoint.x += x;
            	tmpPoint.y += y;
            	gamePoint = toGameCoords(tmpPoint);
            	if (lst != null)
            		lst.coordsChanged(gamePoint.x, gamePoint.y);
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent me) {
                super.mouseDragged(me);

                if (me.getX() != startX) {
                    x -= (me.getX() - startX);
                }
                if (me.getY() != startY) {
                    y -= (me.getY() - startY);
                }
                startX = me.getX();
                startY = me.getY();
                repaint();
            }
        });
    }
    
    public void centerOnGamePoint(Point p) {
    	centerOnPanelPoint(toPanelCoords(p));
    }
    
    public void centerOnGamePoint(Tile t) {
    	centerOnPanelPoint(toPanelCoords(new Point(t.getX(), t.getY())));
    }
    
    public void centerOnPanelPoint(Point p) {
        x = p.x - (getWidth() / 2);
        y = p.y - (getHeight() / 2);
        if (x < 0 || x > img().getWidth())
        	x = 2450 - (getWidth() / 2);
        if (y < 0 || y > img().getHeight())
        	y = 2000 - (getHeight() / 2);
        repaint();
    }

    private BufferedImage img() {
        if (img == null)
        	img = fm.getMapImage();
		return img;
	}

	@Override
    protected void paintComponent(Graphics grphcs) {
        super.paintComponent(grphcs);
        graphics = (Graphics2D) grphcs;

        //turn on some nice effects
        applyRenderHints(graphics);
       
        graphics.drawImage(img(), -x, -y, null);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }

    public void applyRenderHints(Graphics2D g2d) {
        g2d.setRenderingHints(textRenderHints);
        g2d.setRenderingHints(imageRenderHints);
        g2d.setRenderingHints(renderHints);
    }
}