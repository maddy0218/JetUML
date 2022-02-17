package ca.mcgill.cs.jetuml.viewers.edges;

import ca.mcgill.cs.jetuml.geom.Direction;
import ca.mcgill.cs.jetuml.geom.Line;
import ca.mcgill.cs.jetuml.geom.Point;

/**
 * Represents indexed positions on the faces of nodes where edges can attatch.
 * North and South-facing sides of nodes have indices in range -4 to +4.
 * East and West-facing sides of nodes have indices in range -2 to +2.
 */
public enum NodeIndex 
{
	MINUS_FOUR, MINUS_THREE, MINUS_TWO, MINUS_ONE, ZERO,
	PLUS_ONE, PLUS_TWO, PLUS_THREE, PLUS_FOUR;
	
	/**
	 * Indicates whether a given NodeIndex is negative
	 * @param pNodeIndex the index of interest
	 * @return true if pNodeIndex is -4, -3, -2 or -1.
	 */
	public static boolean isNegative(NodeIndex pNodeIndex)
	{
		return pNodeIndex.ordinal() < 4;
	}
	
	/**
	 * Indicates whether a given NodeIndex is positive
	 * @param pNodeIndex the index of interest
	 * @return true if pNodeIndex is +1, +2, +3, +4
	 */
	public static boolean isPositive(NodeIndex pNodeIndex)
	{
		return pNodeIndex.ordinal() > 4;
	}
	
	/**
	 * Indicates whether a given NodeIndex is position zero (center).
	 * @param pNodeIndex the index of interest
	 * @return true if pNodeIndex is ZERO.
	 */
	public static boolean isCenter(NodeIndex pNodeIndex)
	{
		return pNodeIndex == NodeIndex.ZERO;
	}
	
	/**
	 * Creates a point on the side of a node at a given NodeIndex position.
	 * @param pNodeFace a Line representing the side of pNode where the point is needed.
	 * @param pAttatchmentSide the side of the node of interest.
	 * @param pNodeIndex the indexed position on pNodeFace
	 * @return a point on pNodeFace at the pNodeIndex position.
	 */
	public static Point toPoint(Line pNodeFace, Direction pAttatchmentSide, NodeIndex pNodeIndex)
	{
		//determine the offset from the center point. 10 is the margin between indices
		int offset;
		
		offset = (pNodeIndex.ordinal()- 4) * 10;
		
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
