package org.ofbiz.designer.util;

import java.awt.*;

public class ArrowComponent extends LineComponent { 
	
	private Line rightProng;
	private Line leftProng;
	
	private static float PRONG_RADIUS = 5;
	
	private static int PRONG_LENGTH = 10;
	
	private static int PRONG_ANGLE = 20;
	
	public ArrowComponent() {
		updateProngs();
	}
	
	public ArrowComponent(Point head, Point tail) {
		super(head, tail);
		updateProngs();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawLine(rightProng.getHeadX(),rightProng.getHeadY(),rightProng.getTailX(),rightProng.getTailY());
		g.drawLine(leftProng.getHeadX(),leftProng.getHeadY(),leftProng.getTailX(),leftProng.getTailY());
	}
	
	protected void updateProngs() {
		rightProng = new Line(getAbstractLine().getHeadX()-getX(),
							  getAbstractLine().getHeadY()-getY(),
							  getAbstractLine().getTailX()-getX(),
							  getAbstractLine().getTailY()-getY()
							 );
		leftProng = new Line(getAbstractLine().getHeadX()-getX(),
							 getAbstractLine().getHeadY()-getY(),
							 getAbstractLine().getTailX()-getX(),
							 getAbstractLine().getTailY()-getY()
							 );

		
		rightProng.setLengthFromHead(PRONG_LENGTH);
		leftProng.setLengthFromHead(PRONG_LENGTH);
		
		rightProng.rotateAroundHead(PRONG_ANGLE);
		leftProng.rotateAroundHead(360-PRONG_ANGLE);
	}
	
	public boolean contains(int x, int y) {
		return (super.contains(x,y) ||
				rightProng.containingRect(PRONG_RADIUS).contains(x,y) ||
				leftProng.containingRect(PRONG_RADIUS).contains(x,y));
	}

	public void setBoundsByAbstractLine(Line newLine) {
		super.setBoundsByAbstractLine(newLine);
		updateProngs();
	}
	
	public void setBounds(Rectangle bounds){
		super.setBounds(bounds);
		updateProngs();
	}
}
