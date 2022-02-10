package ca.mcgill.cs.jetuml.viewers.edges;

import ca.mcgill.cs.jetuml.geom.Direction;
import ca.mcgill.cs.jetuml.geom.Line;
import ca.mcgill.cs.jetuml.geom.Point;

/*
 * 
 */
public enum Index  
{
	CENTER, ONE, TWO, THREE, FOUR;
	
	
	public static Point toPoint(Line pNodeFace, Direction pAttatchmentSide, Index pIndex, IndexSign pSign)
	{
		//determine the offset from the center point. 10 is the margin between indices
		int offset = pIndex.ordinal() * 10;
		
		if (pSign == IndexSign.NEGATIVE)
		{
			offset = offset * -1;
		}
		//Determine center point and add the offset to the center point
		Point center;
		if (pAttatchmentSide == Direction.NORTH || pAttatchmentSide == Direction.SOUTH)
		{
			center = new Point(((pNodeFace.getX2()-pNodeFace.getX1())/2) + pNodeFace.getX1(), pNodeFace.getY1());
			return new Point(center.getX() + offset, center.getY());
		}
		else 
		{
			center = new Point(pNodeFace.getX1(), ((pNodeFace.getY2()-pNodeFace.getY1())/2) + pNodeFace.getY1());
			return new Point(center.getX(), center.getY() + offset);
		}
	}
	
	
	
	
		
		
		
		
	
	
}
