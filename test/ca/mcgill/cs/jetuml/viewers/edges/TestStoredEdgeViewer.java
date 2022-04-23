package ca.mcgill.cs.jetuml.viewers.edges;

import ca.mcgill.cs.jetuml.geom.Point;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ca.mcgill.cs.jetuml.diagram.Diagram;
import ca.mcgill.cs.jetuml.diagram.DiagramType;
import ca.mcgill.cs.jetuml.diagram.Edge;
import ca.mcgill.cs.jetuml.diagram.edges.AggregationEdge;
import ca.mcgill.cs.jetuml.diagram.edges.AssociationEdge;
import ca.mcgill.cs.jetuml.diagram.edges.AssociationEdge.Directionality;
import ca.mcgill.cs.jetuml.diagram.edges.DependencyEdge;
import ca.mcgill.cs.jetuml.diagram.edges.GeneralizationEdge;
import ca.mcgill.cs.jetuml.diagram.nodes.ClassNode;
import ca.mcgill.cs.jetuml.geom.EdgePath;
import ca.mcgill.cs.jetuml.viewers.ArrowHead;
import ca.mcgill.cs.jetuml.viewers.ClassDiagramViewer;
import ca.mcgill.cs.jetuml.viewers.LineStyle;

/**
 * Tests the StoredEdgeViewer class.
 */
public class TestStoredEdgeViewer 
{
	private final Diagram aDiagram = new Diagram(DiagramType.CLASS);
	private static final StoredEdgeViewer aStoredEdgeViewer = new StoredEdgeViewer();
	private GeneralizationEdge aInheritanceEdge;
	private GeneralizationEdge aImplementationEdge;
	private AggregationEdge aAggregationEdge;
	private AggregationEdge aCompositionEdge;
	private AssociationEdge aAssociationEdge;
	private DependencyEdge aDependencyEdge;
	private ClassNode aNodeA;
	private ClassNode aNodeB;
	
	@BeforeEach
	public void setUp()
	{
		aInheritanceEdge = new GeneralizationEdge(GeneralizationEdge.Type.Inheritance);
		aImplementationEdge = new GeneralizationEdge(GeneralizationEdge.Type.Implementation);
		aAggregationEdge = new AggregationEdge(AggregationEdge.Type.Aggregation);
		aCompositionEdge = new AggregationEdge(AggregationEdge.Type.Composition);
		aAssociationEdge = new AssociationEdge();
		aDependencyEdge = new DependencyEdge();
		aNodeA = new ClassNode();
		aNodeB = new ClassNode();
		aDiagram.addRootNode(aNodeA);
		aDiagram.addRootNode(aNodeB);
		
	}

	
	@Test
	public void testGetLineStyle()
	{
		assertEquals(LineStyle.SOLID, getLineStyle(aInheritanceEdge));
		assertEquals(LineStyle.DOTTED, getLineStyle(aImplementationEdge));
		assertEquals(LineStyle.SOLID, getLineStyle(aAggregationEdge));
		assertEquals(LineStyle.SOLID, getLineStyle(aCompositionEdge));
		assertEquals(LineStyle.SOLID, getLineStyle(aAssociationEdge));
		assertEquals(LineStyle.DOTTED, getLineStyle(aDependencyEdge));
	}
	
	@Test
	public void testGetArrowStart_aggregation()
	{
		assertEquals(ArrowHead.DIAMOND, getArrowStart(aAggregationEdge));
		assertEquals(ArrowHead.BLACK_DIAMOND, getArrowStart(aCompositionEdge));
	}
	
	@Test
	public void testGetArrowStart_generalization()
	{
		assertEquals(ArrowHead.NONE, getArrowStart(aInheritanceEdge));
		assertEquals(ArrowHead.NONE, getArrowStart(aImplementationEdge));
	}
	
	@Test
	public void testGetArrowStart_association()
	{
		assertEquals(ArrowHead.NONE, getArrowStart(aAssociationEdge));
		aAssociationEdge.setDirectionality(Directionality.Unidirectional);
		assertEquals(ArrowHead.NONE, getArrowStart(aAssociationEdge));
		aAssociationEdge.setDirectionality(Directionality.Bidirectional);
		assertEquals(ArrowHead.V, getArrowStart(aAssociationEdge));
	}
	
	@Test
	public void testGetArrowStart_dependency()
	{
		assertEquals(ArrowHead.NONE, getArrowStart(aDependencyEdge));
		aDependencyEdge.setDirectionality(DependencyEdge.Directionality.Unidirectional);
		assertEquals(ArrowHead.NONE, getArrowStart(aDependencyEdge));
		aDependencyEdge.setDirectionality(DependencyEdge.Directionality.Bidirectional);
		assertEquals(ArrowHead.V, getArrowStart(aDependencyEdge));
	}
	
	@Test
	public void testGetArrowEnd_aggregation()
	{
		assertEquals(ArrowHead.NONE, getArrowEnd(aAggregationEdge));
		assertEquals(ArrowHead.NONE, getArrowEnd(aCompositionEdge));
	}
	
	@Test
	public void testGetArrowEnd_generalization()
	{
		assertEquals(ArrowHead.TRIANGLE, getArrowEnd(aInheritanceEdge));
		assertEquals(ArrowHead.TRIANGLE, getArrowEnd(aImplementationEdge));
	}
	
	@Test
	public void testGetArrowEnd_association()
	{
		assertEquals(ArrowHead.NONE, getArrowEnd(aAssociationEdge));
		aAssociationEdge.setDirectionality(Directionality.Unidirectional);
		assertEquals(ArrowHead.V, getArrowEnd(aAssociationEdge));
		aAssociationEdge.setDirectionality(Directionality.Bidirectional);
		assertEquals(ArrowHead.V, getArrowEnd(aAssociationEdge));
	}
	
	@Test
	public void testGetArrowEnd_dependency()
	{
		assertEquals(ArrowHead.V, getArrowEnd(aDependencyEdge));
		aDependencyEdge.setDirectionality(DependencyEdge.Directionality.Bidirectional);
		assertEquals(ArrowHead.V, getArrowEnd(aDependencyEdge));
	}
	
	@Test
	public void testgetStartLabel()
	{
		aAggregationEdge.setStartLabel("test");
		aCompositionEdge.setStartLabel("test");
		aAssociationEdge.setStartLabel("test");
		assertEquals("", getStartLabel(aInheritanceEdge));
		assertEquals("", getStartLabel(aImplementationEdge));
		assertEquals("", getStartLabel(aDependencyEdge));
		assertEquals("test", getStartLabel(aAggregationEdge));
		assertEquals("test", getStartLabel(aCompositionEdge));
		assertEquals("test", getStartLabel(aAssociationEdge));
	}

	@Test
	public void testgetMiddleLabel()
	{
		aAggregationEdge.setMiddleLabel("test");
		aCompositionEdge.setMiddleLabel("test");
		aAssociationEdge.setMiddleLabel("test");
		aDependencyEdge.setMiddleLabel("test");
		assertEquals("", getMiddleLabel(aInheritanceEdge));
		assertEquals("", getMiddleLabel(aImplementationEdge));
		assertEquals("test", getMiddleLabel(aDependencyEdge));
		assertEquals("test", getMiddleLabel(aAggregationEdge));
		assertEquals("test", getMiddleLabel(aCompositionEdge));
		assertEquals("test", getMiddleLabel(aAssociationEdge));
	}
	
	@Test
	public void testgetEndLabel()
	{
		aAssociationEdge.setEndLabel("test");
		aAggregationEdge.setEndLabel("test");
		aCompositionEdge.setEndLabel("test");
		aAssociationEdge.setEndLabel("test");
		assertEquals("", getEndLabel(aInheritanceEdge));
		assertEquals("", getEndLabel(aImplementationEdge));
		assertEquals("", getEndLabel(aDependencyEdge));
		assertEquals("test", getEndLabel(aAggregationEdge));
		assertEquals("test", getEndLabel(aCompositionEdge));
		assertEquals("test", getEndLabel(aAssociationEdge));
	}

	
	@Test
	public void testContains()
	{
		aDependencyEdge.connect(aNodeB, aNodeA, aDiagram);
		aDiagram.addEdge(aDependencyEdge);
		store(aDependencyEdge, new EdgePath(new Point(0, 0), new Point(0, 100)));
		assertTrue(aStoredEdgeViewer.contains(aDependencyEdge, new Point(0, 50)));
		assertTrue(aStoredEdgeViewer.contains(aDependencyEdge, new Point(1, 1)));
		assertFalse(aStoredEdgeViewer.contains(aDependencyEdge, new Point(10, 50)));
	}

	@Test
	public void testGetConnectionPoints()
	{
		aDependencyEdge.connect(aNodeB, aNodeA, aDiagram);
		aDiagram.addEdge(aDependencyEdge);
		store(aDependencyEdge, new EdgePath(new Point(0, 0), new Point(0, 100)));
		assertEquals(new Point(0, 0), aStoredEdgeViewer.getConnectionPoints(aDependencyEdge).getPoint1());
		assertEquals(new Point(0, 100), aStoredEdgeViewer.getConnectionPoints(aDependencyEdge).getPoint2());
	}
	
	@Test
	public void testGetStoredEdgePath()
	{
		aDependencyEdge.connect(aNodeB, aNodeA, aDiagram);
		aDiagram.addEdge(aDependencyEdge);
		store(aDependencyEdge, new EdgePath(new Point(0, 0), new Point(0, 100)));
		assertEquals(new EdgePath(new Point(0, 0), new Point(0, 100)), getStoredEdgePath(aDependencyEdge));
	}
	
	
	
	
	
	/// Private reflexive helper methods:
	
	private static LineStyle getLineStyle(Edge pEdge)
	{
		try
		{
			Method method = StoredEdgeViewer.class.getDeclaredMethod("getLineStyle", Edge.class);
			method.setAccessible(true);
			return (LineStyle) method.invoke(aStoredEdgeViewer, pEdge);
		}
		catch(ReflectiveOperationException e)
		{
			fail();
			return null;
		}
	}
	
	private static ArrowHead getArrowStart(Edge pEdge)
	{
		try
		{
			Method method = StoredEdgeViewer.class.getDeclaredMethod("getArrowStart", Edge.class);
			method.setAccessible(true);
			return (ArrowHead) method.invoke(aStoredEdgeViewer, pEdge);
		}
		catch(ReflectiveOperationException e)
		{
			fail();
			return null;
		}
	}

	private static ArrowHead getArrowEnd(Edge pEdge)
	{
		try
		{
			Method method = StoredEdgeViewer.class.getDeclaredMethod("getArrowEnd", Edge.class);
			method.setAccessible(true);
			return (ArrowHead) method.invoke(aStoredEdgeViewer, pEdge);
		}
		catch(ReflectiveOperationException e)
		{
			fail();
			return null;
		}
	}
	
	private String getStartLabel(Edge pEdge)
	{
		try
		{
			Method method = StoredEdgeViewer.class.getDeclaredMethod("getStartLabel", Edge.class);
			method.setAccessible(true);
			return (String) method.invoke(aStoredEdgeViewer, pEdge);
		}
		catch(ReflectiveOperationException e)
		{
			fail();
			return null;
		}
	}
	
	private String getMiddleLabel(Edge pEdge)
	{
		try
		{
			Method method = StoredEdgeViewer.class.getDeclaredMethod("getMiddleLabel", Edge.class);
			method.setAccessible(true);
			return (String) method.invoke(aStoredEdgeViewer, pEdge);
		}
		catch(ReflectiveOperationException e)
		{
			fail();
			return null;
		}
	}
	
	private String getEndLabel(Edge pEdge)
	{
		try
		{
			Method method = StoredEdgeViewer.class.getDeclaredMethod("getEndLabel", Edge.class);
			method.setAccessible(true);
			return (String) method.invoke(aStoredEdgeViewer, pEdge);
		}
		catch(ReflectiveOperationException e)
		{
			fail();
			return null;
		}
	}
	
	private EdgePath getStoredEdgePath(Edge pEdge)
	{
		try
		{
			Method method = StoredEdgeViewer.class.getDeclaredMethod("getStoredEdgePath", Edge.class);
			method.setAccessible(true);
			return (EdgePath) method.invoke(aStoredEdgeViewer, pEdge);
		}
		catch(ReflectiveOperationException e)
		{
			fail();
			return null;
		}
	}
	
	
	private void store(Edge pEdge, EdgePath pEdgePath)
	{
		ClassDiagramViewer viewer = (ClassDiagramViewer) DiagramType.viewerFor(aDiagram);
		viewer.store(pEdge, pEdgePath);
	}
	
}
