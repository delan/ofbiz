package org.ofbiz.designer.util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;


public class LineComponent extends JComponent {

    protected Line abstractLine;

    private static float THE_RADIUS = 10;

    private static int OFFSET_FROM_BORDER = 10;

    private static final int FROM_HEAD = 1;
    private static final int FROM_TAIL = -1;

    private Color theColor;

    private static final int TOP_LEFT = 0;
    private static final int TOP_RIGHT = 1;
    private static final int BOTTOM_LEFT = 2;
    private static final int BOTTOM_RIGHT = 3;

    private int tailCorner;

    public LineComponent() {
        setBoundsByAbstractLine(new Line(new Point(0,0),new Point(0,0)));
    }

    public LineComponent(Point head, Point tail) {
        setBoundsByAbstractLine(new Line(head, tail));
    }

    public void setColor(Color aColor) {
        theColor = aColor;
    }

    public Color getColor() {
        return theColor;
    }

    protected void updateBounds() {
        Rectangle lineBounds = abstractLine.getBounds();

        /*
        setLocation(lineBounds.x-OFFSET_FROM_BORDER,
                    lineBounds.y-OFFSET_FROM_BORDER);
        setSize(lineBounds.width+(OFFSET_FROM_BORDER*2), lineBounds.height+(OFFSET_FROM_BORDER*2));
        */
        super.setBounds(lineBounds.x-OFFSET_FROM_BORDER,
                        lineBounds.y-OFFSET_FROM_BORDER,
                        lineBounds.width+(OFFSET_FROM_BORDER*2), lineBounds.height+(OFFSET_FROM_BORDER*2));
    }


    public boolean contains(int x, int y) {
        int absX = x+getX();
        int absY = y+getY();
        Polygon containRect = abstractLine.containingRect(THE_RADIUS);

        return containRect.contains(absX,absY);
    }


    public void paintComponent(Graphics g) {
        g.setColor(theColor);

        g.drawLine(abstractLine.getHeadX()-getX(),
                   abstractLine.getHeadY()-getY(),
                   abstractLine.getTailX()-getX(),
                   abstractLine.getTailY()-getY());
    }

    protected void setBoundsByAbstractLine(Line newLine) {
        abstractLine = newLine;
        tailCorner = computeTailCorner(newLine);
        updateBounds();
    }

    protected Line getAbstractLine() {
        return abstractLine;
    }

    public int computeTailCorner(Line aLine) {
        if(aLine.getTailX()>=aLine.getHeadX()) {
            if(aLine.getTailY()>=aLine.getHeadY()) {
                return BOTTOM_RIGHT;
            } else {
                return TOP_RIGHT;
            }
        } else {
            if(aLine.getTailY()>=aLine.getHeadY()) {
                return BOTTOM_LEFT;
            } else {
                return TOP_LEFT;
            }
        }
    }

    public void setLineOutsideBox(Rectangle headBounds, Rectangle tailBounds) {


        Point newHead = new Point();
        newHead.x = (int)headBounds.getCenterX();
        newHead.y = (int)headBounds.getCenterY();

        Point newTail = new Point();
        newTail.x = (int)tailBounds.getCenterX();
        newTail.y = (int)tailBounds.getCenterY();

        Line newLine = new Line(newTail, newHead);

        float lineAngle = newLine.getSlopeAngle();

        float slopeRatio;

        if((newHead.x-newTail.x)==0) {
            slopeRatio =0;
        } else {
            slopeRatio = ((float)(newTail.y-newHead.y))/((float)(newTail.x-newHead.x));
        }

        Point deltaHead = getDelta(headBounds, lineAngle, slopeRatio, FROM_HEAD);
        Point deltaTail = getDelta(tailBounds, lineAngle, slopeRatio, FROM_TAIL);

        newHead.x += Math.rint(deltaHead.x);
        newHead.y += Math.rint(deltaHead.y);

        newTail.x += Math.rint(deltaTail.x);
        newTail.y += Math.rint(deltaTail.y);

        newLine = new Line(newHead,newTail);
        setBoundsByAbstractLine(newLine);
    }


    private Point getDelta(Rectangle bounds, float lineAngle, float slopeRatio, int direction) {

        Line diagonal = new Line(0,0,bounds.width,bounds.height);

        float diag1 = diagonal.getSlopeAngle();

        float diag2 = 180-(2*diag1);

        double deltaX, deltaY;

        if((lineAngle >= diag1) && (lineAngle < (diag1+diag2))) {
            deltaY = -(bounds.getHeight()/2)*direction;
            if(slopeRatio == 0) deltaX = 0;
            else deltaX = (deltaY/slopeRatio);
        }

        else if((lineAngle > (diag1+diag2)) && (lineAngle <= ((3*diag1)+diag2))) {
            deltaX = (bounds.getWidth()/2)*direction;
            deltaY = (deltaX*slopeRatio);
        }

        else if((lineAngle >= ((3*diag1)+diag2)) && (lineAngle < ((3*diag1)+(2*diag2)))) {
            deltaY = (bounds.getHeight()/2)*direction;
            if(slopeRatio == 0) deltaX = 0;
            else deltaX = (deltaY/slopeRatio);
        }

        else {
            deltaX = -(bounds.getWidth()/2)*direction;
            deltaY = (deltaX*slopeRatio);
        }
        return new Point((int)Math.rint(deltaX), (int)Math.rint(deltaY));
    }

    public void reverse() {
        setBoundsByAbstractLine(new Line(abstractLine.getHeadPosition(),abstractLine.getTailPosition()));
    }

    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        if(tailCorner == BOTTOM_RIGHT) {
            setAbstractLine(new Line(x+width-OFFSET_FROM_BORDER,y+height-OFFSET_FROM_BORDER,
                                     x+OFFSET_FROM_BORDER,y+OFFSET_FROM_BORDER));
        } else if(tailCorner == BOTTOM_LEFT) {
            setAbstractLine(new Line(x+OFFSET_FROM_BORDER,y+height-OFFSET_FROM_BORDER,
                                     x+width-OFFSET_FROM_BORDER,y+OFFSET_FROM_BORDER));
        } else if(tailCorner == TOP_RIGHT) {
            setAbstractLine(new Line(x+width-OFFSET_FROM_BORDER,y+OFFSET_FROM_BORDER,
                                     x+OFFSET_FROM_BORDER,y+height-OFFSET_FROM_BORDER));
        } else {
            setAbstractLine(new Line(x+OFFSET_FROM_BORDER,y+OFFSET_FROM_BORDER,
                                     x+width-OFFSET_FROM_BORDER,y+height-OFFSET_FROM_BORDER));
        }
    }

    private void setAbstractLine(Line newLine) {
        abstractLine = newLine;
        tailCorner = computeTailCorner(newLine);
    }
}
