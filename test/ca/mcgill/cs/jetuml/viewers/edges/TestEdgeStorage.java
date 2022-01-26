package ca.mcgill.cs.jetuml.viewers.edges;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;



import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.mcgill.cs.jetuml.diagram.Edge;
import ca.mcgill.cs.jetuml.diagram.edges.AggregationEdge;
import ca.mcgill.cs.jetuml.diagram.edges.DependencyEdge;
import ca.mcgill.cs.jetuml.geom.EdgePath;
import ca.mcgill.cs.jetuml.geom.Point;

/**
 * Tests the edge storage.
 */
public class TestEdgeStorage 
{
	private EdgeStorage aEdgeStorage;
	private Edge edge1 = new AggregationEdge();
	private Edge edge2 = new DependencyEdge();
	private final EdgePath path1 = new EdgePath(new Point(2,2), new Point(10, 10));
	
	@BeforeEach
	private void setUp()
	{
		aEdgeStorage = new EdgeStorage();
	}
	
	@Test
	public void testContains()
	{
		aEdgeStorage.store(edge1, path1);
		assertTrue(aEdgeStorage.contains(edge1));
		assertFalse(aEdgeStorage.contains(edge2));
	}
	
	@Test
	public void testRemove()
	{
		aEdgeStorage.store(edge1, path1);
		aEdgeStorage.remove(edge1);
		assertFalse(aEdgeStorage.contains(edge1));
	}
	
	@Test
	public void testGetEdgePath_isPresent()
	{
		aEdgeStorage.store(edge1, path1);
		assertTrue(aEdgeStorage.getEdgePath(edge1).isPresent());
		assertEquals(aEdgeStorage.getEdgePath(edge1), Optional.of(path1));
	}
	
	@Test
	public void testGetEdgePath_notPresent()
	{
		assertEquals(Optional.empty(), aEdgeStorage.getEdgePath(edge1));
	}
}
