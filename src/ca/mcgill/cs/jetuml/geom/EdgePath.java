package ca.mcgill.cs.jetuml.geom;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Represents the path of an edge on a diagram as a series of points. 
 * Non-segmented paths consist of 2 points (the start and end points).
 */
public class EdgePath
{
	private List<Point> aPoints;
	
	/**
	 * @param pPoints the points of an edge (Start, possible segment connections, and end)
	 * @Pre pPoints.length >= 2
	 */
	public EdgePath(Point...pPoints)
	{
		assert pPoints.length >= 2;
		aPoints = Arrays.asList(pPoints);
	}
	
	/**
	 * Constructor using lines as arguments.
	 * @param pLines the line segment(s) which compose the path.
	 * @Pre pLines.length() > 0
	 */
	public EdgePath(Line...pLines)
	{
		assert pLines.length > 0;
		aPoints.add(pLines[0].getPoint1());
		for (Line line : pLines)
		{
			aPoints.add(line.getPoint2());
		}
	}
	

	/**
	 * Gets the starting point for the path.
	 * @return the Point where the edge starts.
	 */
	public Point getStartPoint()
	{
		return aPoints.get(0);
	}
	
	/**
	 * Gets the end point of the edge.
	 * @return the Point where the edge ends.
	 */
	public Point getEndPoint()
	{
		return aPoints.get(aPoints.size()-1);
	}

	@Override
	public int hashCode() 
	{
		return Objects.hash(aPoints);
	}

	@Override
	public boolean equals(Object pObj) {
		if (this ==  pObj)
		{
			return true;
		}
		if ( pObj == null)
		{
			return false;
		}
		if (getClass() !=  pObj.getClass())
		{
			return false;
		}
		EdgePath other = (EdgePath)  pObj;
		return Objects.equals(aPoints, other.aPoints);
	}
	
	
}