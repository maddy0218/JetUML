package ca.mcgill.cs.jetuml.viewers.edges;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import ca.mcgill.cs.jetuml.geom.EdgePath;
import ca.mcgill.cs.jetuml.geom.Point;

public class TestEdgePath {
	private static final Point pointA = new Point(1,1);
	private static final Point pointB = new Point(1,6);
	private static final Point pointC = new Point(6,6);
	private static final EdgePath edgePath1 = new EdgePath(pointA, pointB);
	private static final EdgePath edgePath2 = new EdgePath(pointA, pointB, pointC);
	private static final EdgePath edgePath3 = new EdgePath(pointC, pointB, pointA);
	
	@Test
	public void testGetStartPoint()
	{
		assertEquals(edgePath1.getStartPoint(), pointA);
		assertEquals(edgePath2.getStartPoint(), pointA);
		assertEquals(edgePath3.getStartPoint(), pointC);
	}
	
	@Test
	public void testGetEndPoint()
	{
		assertEquals(edgePath1.getEndPoint(), pointB);
		assertEquals(edgePath2.getEndPoint(), pointC);
		assertEquals(edgePath3.getEndPoint(), pointA);
	}
	
	
	@Test
	public void testEquals()
	{
		assertTrue(edgePath1.equals(edgePath1));
		assertFalse(edgePath1.equals(null));
		assertFalse(edgePath1.equals(edgePath3));
		assertTrue(edgePath1.equals(new EdgePath(pointA, pointB)));
	}

}
