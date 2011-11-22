package org.apache.maven.diagrams.prefuse;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;


import prefuse.render.Renderer;
import prefuse.visual.VisualItem;

public class MyRenderer implements Renderer {

	public boolean locatePoint(Point2D arg0, VisualItem arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public void render(Graphics2D arg0, VisualItem arg1) {
		// TODO Auto-generated method stub

	}

	public void setBounds(VisualItem arg0) {
		arg0.setBounds(0, 0, 10, 10);
	}

}
