package ca.mcgill.cs.jetuml.geom;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Represents the path of an edge on a diagram as a series of points. 
 * Non-segmented paths consist of 2 points (the start and end points).
 */
public class EdgePath implements Iterable<Point>
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
	public int hashCode() {
		return Objects.hash(aPoints);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		EdgePath other = (EdgePath) obj;
		return Objects.equals(aPoints, other.aPoints);
	}
	
	/**
	 * Returns the number of points in the path.
	 * @return an integer representing the size of the EdgePath
	 */
	public int size()
	{
		return aPoints.size();
	}

	@Override
	public Iterator<Point> iterator() {
		return aPoints.iterator();
	}
	
	
}