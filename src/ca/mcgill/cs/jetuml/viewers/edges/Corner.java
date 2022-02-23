package ca.mcgill.cs.jetuml.viewers.edges;

import ca.mcgill.cs.jetuml.geom.Direction;

public enum Corner 
{
	TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT;
	
	public Corner getCorner(Direction pVertical, Direction pHorizontal)
	{
		if (pVertical == Direction.NORTH)
		{
			if (pHorizontal == Direction.WEST)
			{
				return TOP_LEFT;
			}
			else
			{
				return TOP_RIGHT;
			}
		}
		else
		{
			if (pHorizontal == Direction.WEST)
			{
				return 	BOTTOM_LEFT;
			}
			else
			{
				return BOTTOM_RIGHT;
			}
		}
	}
}
