package uk.co.boeing.leap;

import com.leapmotion.leap.*;

public class LeapTest {

	static Controller leap;
	static int windowWidth = 800;
	static int windowHeight = 800;
	
	public static void main(String[] args) {
		
		//size(windowWidth, windowHeight, P2D);
		leap = new Controller();
		//cursor = createShape(ELLIPSE, 0, 0, 40, 40);
		//cursor.stroke(cursorNormalColor);
		//cursor.fill(colorWithAlpha(cursorNormalColor, 16));
	
		while (true) {
			Frame frame = leap.frame();
	        Pointable pointable = frame.pointables().frontmost();
	        float distance = pointable.touchDistance();
	        
	        Finger finger = frame.fingers().frontmost();
	        Vector stabilizedPosition = finger.stabilizedTipPosition();
	
	        InteractionBox iBox = leap.frame().interactionBox();
	        Vector normalizedPosition = iBox.normalizePoint(stabilizedPosition);
	        float x = normalizedPosition.getX() * windowWidth;
	        float y = windowHeight - normalizedPosition.getY() * windowHeight;
	        
	        System.out.println(distance + " " + x + " " + y);
		}
	}

}
