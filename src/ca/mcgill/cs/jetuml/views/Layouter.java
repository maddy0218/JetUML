package ca.mcgill.cs.jetuml.views;

import static ca.mcgill.cs.jetuml.views.EdgePriority.priorityOf;
import static ca.mcgill.cs.jetuml.views.MergeStyle.getMergeStyle;
import static ca.mcgill.cs.jetuml.views.MergeStyle.SHARED_END;
import static ca.mcgill.cs.jetuml.views.MergeStyle.SHARED_START;
import static ca.mcgill.cs.jetuml.views.MergeStyle.NO_MERGE;
import static java.util.stream.Collectors.toList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import ca.mcgill.cs.jetuml.diagram.Diagram;
import ca.mcgill.cs.jetuml.diagram.Edge;
import ca.mcgill.cs.jetuml.diagram.Node;
import ca.mcgill.cs.jetuml.diagram.edges.AggregationEdge;
import ca.mcgill.cs.jetuml.geom.Direction;
import ca.mcgill.cs.jetuml.geom.EdgePath;
import ca.mcgill.cs.jetuml.geom.GeomUtils;
import ca.mcgill.cs.jetuml.geom.Line;
import ca.mcgill.cs.jetuml.geom.Point;
import ca.mcgill.cs.jetuml.geom.Rectangle;
import ca.mcgill.cs.jetuml.viewers.edges.EdgeStorage;
import ca.mcgill.cs.jetuml.viewers.edges.EdgeViewerRegistry;
import ca.mcgill.cs.jetuml.viewers.edges.NodeIndex;
import ca.mcgill.cs.jetuml.viewers.nodes.NodeViewerRegistry;
import javafx.scene.canvas.GraphicsContext;
/**
 * Plans the paths for edges based on the position of other edges and nodes 
 * to make the diagram more clean and readable.
 *
 */
public class Layouter 
{
	private final EdgeStorage aEdgeStorage;
	private final int MARGIN = 20;
	private final int SQUARESIZE = 10;
	private final int NODE_WIDTH = 100;
	private final int NODE_HEIGHT = 60;
	
	/**
	 * Plans edge paths using pEgdeStorage.
	 * @param pEdgeStorage the EdgeStorage
	 */
	public Layouter(EdgeStorage pEdgeStorage)
	{
		aEdgeStorage = pEdgeStorage;
	}
	
	/**
	 * Uses positional information of nodes and edges in storage to plan the trajectory 
	 * of edges in the diagram. 
	 * @param pDiagram the diagram of interest
	 * @param pEdgeStorage object which stores edge positions in the diagram
	 * @param pGraphics graphics context 
	 * @pre pDiagram!=null
	 */
	public void layout(Diagram pDiagram)
	{
		assert pDiagram !=null;
		layoutSharedEndEdges(pDiagram, EdgePriority.INHERITANCE);	
		layoutSharedEndEdges(pDiagram, EdgePriority.IMPLEMENTATION);
		layoutSharedStartEdges(pDiagram, EdgePriority.AGGREGATION);
		layoutSharedStartEdges(pDiagram, EdgePriority.COMPOSITION);
		layoutSharedEndEdges(pDiagram, EdgePriority.ASSOCIATION);
		layoutDependencyEdges(pDiagram);
		layoutSelfEdges(pDiagram);
	}
	

	/**
	 * Plans the EdgePaths for segmented edges which share a common end point when merged. 
	 * (For use with Inheritance, Implementation, and Association Edges)
	 * @param pDiagram the diagram to layout
	 * @param pSegmentedEdgePriority the edge priority level 
	 * @pre pDiagram!=null && pSegmentedEdgePriority!=null
	 * @pre getMergeStyle(pEdgePriority) == SHARED_END
	 */
	private void layoutSharedEndEdges(Diagram pDiagram, EdgePriority pEdgePriority)
	{
		assert pDiagram!=null && pEdgePriority!=null;
		assert getMergeStyle(pEdgePriority) == SHARED_END;
		List<Edge> edgesToProcess = pDiagram.edges().stream()
				.filter(edge -> priorityOf(edge) == pEdgePriority)
				.collect(Collectors.toList());
		while (!edgesToProcess.isEmpty())
		{
			Edge currentEdge = edgesToProcess.get(0);
			List<Edge> edgesToMerge = getEdgesToMergeEnd(currentEdge, edgesToProcess);
			Direction edgeDirection = getAttachedSide(currentEdge, currentEdge.getStart());
			edgesToProcess.removeAll(edgesToMerge);
			if (!edgesToMerge.isEmpty()) 
			{
				storeSharedEndEdges(edgeDirection, edgesToMerge);
			}
		}
	}
	/**
	 * Plans the EdgePaths for segmented edges which share a common start point when merged. 
	 * (Aggregation and Composition edges)
	 * @param pDiagram the diagram of interest
	 * @param pEdgePriority the edge priority level 
	 * @pre pDiagram!=null && pEdgePriority!=null
	 * @pre MergeStyle.getMergeStyle(pEdgePriority) == MergeStyle.SHARED_START
	 */
	private void layoutSharedStartEdges(Diagram pDiagram, EdgePriority pEdgePriority)
	{
		assert pDiagram != null && pEdgePriority != null;
		assert getMergeStyle(pEdgePriority) == SHARED_START;
		List<Edge> edgesToProcess = pDiagram.edges().stream()
				.filter(edge -> priorityOf(edge) == pEdgePriority)
				.collect(Collectors.toList());
		while (!edgesToProcess.isEmpty())
		{
			Edge currentEdge = edgesToProcess.get(0);
			List<Edge> edgesToMerge = getEdgesToMergeStart(currentEdge, edgesToProcess);
			edgesToProcess.removeAll(edgesToMerge);
			Direction edgeDirection = getAttachedSide(currentEdge, currentEdge.getStart());
			if (!edgesToMerge.isEmpty()) 
			{
				storeSharedStartEdges(edgeDirection, edgesToMerge);
			}
		}
	}
	/**
	 * Plans the EdgePaths for Dependency Edges.
	 * @param pDiagram the diagram of interest
	 */
	private void layoutDependencyEdges(Diagram pDiagram)
	{
		assert pDiagram!=null;
		for (Edge edge : pDiagram.edges())
		{
			if (priorityOf(edge)==EdgePriority.DEPENDENCY)
			{
				Direction attachedEndSide = getAttachedSide(edge, edge.getEnd());
				Point startPoint = getConnectionPoint(edge.getStart(), edge, attachedEndSide.mirrored());
				Point endPoint = getConnectionPoint(edge.getEnd(), edge, attachedEndSide);
				aEdgeStorage.store(edge, new EdgePath(startPoint, endPoint));
			}
		}	
	}
	
	public void layoutSelfEdges(Diagram pDiagram)
	{
		assert pDiagram !=null;
		List<Edge> selfEdges = pDiagram.edges().stream()
			.filter(edge -> priorityOf(edge) == EdgePriority.SELF_EDGE)
			.collect(Collectors.toList());
		for (Edge edge : selfEdges)
		{
			Rectangle nodeBounds = NodeViewerRegistry.getBounds(edge.getEnd());
			Direction verticalSide = leastOccupiedVerticalSide(edge.getEnd());
			Direction horizontalSide = leastOccupiedHorizontalSide(edge.getEnd());
			NodeIndex startIndex = NodeIndex.PLUS_THREE;
			
			Point start = NodeIndex.toPoint(getNodeFace(nodeBounds, horizontalSide), verticalSide, NodeIndex.MINUS_THREE);
			
			
		}
		
	}
	
	private int numberOfEdgesOnNodeFace(Node pNode, Direction pSide)
	{
		return (int) aEdgeStorage.edgesConnectedTo(pNode).stream()
			.filter(edge -> getAttachedSide(edge, pNode) == pSide)
			.count();
	}
	
	
	private Direction leastOccupiedVerticalSide(Node pNode)
	{
		int westEdges = numberOfEdgesOnNodeFace(pNode, Direction.WEST);
		int eastEdges = numberOfEdgesOnNodeFace(pNode, Direction.EAST);
		if (westEdges < eastEdges )
		{
			return Direction.WEST;
		}
		else
		{
			return Direction.EAST;
		}
	}
	
	private Direction leastOccupiedHorizontalSide(Node pNode)
	{
		int northEdges = numberOfEdgesOnNodeFace(pNode, Direction.NORTH);
		int southEdges = numberOfEdgesOnNodeFace(pNode, Direction.SOUTH);
		if (northEdges <= southEdges )
		{
			return Direction.NORTH;
		}
		else
		{
			return Direction.SOUTH;
		}
	}
	
	/**
	 * Builds EdgePaths for each edge to be merged, and stores the edgePath in storage.
	 * @param pDirection the direction of the edges (the face of the start node on which edges are connected)
	 * @param pEdgesToMerge a list of edges to be merged
	 * @pre pDirection!=null && pDirection.isCardinal()
	 * @pre pEdgesToMerge!=null && pEdgesToMerge.size() > 0
	 */
	private void storeSharedEndEdges(Direction pDirection, List<Edge> pEdgesToMerge)
	{
		assert pDirection!=null && pDirection.isCardinal();
		assert pEdgesToMerge!=null && pEdgesToMerge.size() > 0;
		
		//Merged edges will share a common end point
		Point endPoint = getConnectionPoint(pEdgesToMerge.get(0).getEnd(), pEdgesToMerge.get(0), pDirection.mirrored());
		//get the individual start point for each edge
		List<Point> edgeStartPoints = new ArrayList<>();
		for (Edge e : pEdgesToMerge)
		{
			edgeStartPoints.add(getConnectionPoint(e.getStart(), e, pDirection));
		}
		//The edge segment bend will occur half-way between the end point and the closest start point
		//Unless layout adjustments are needed
		Point closestStartPoint = getClosestPoint(edgeStartPoints, pDirection);
		Node endNode = pEdgesToMerge.get(0).getEnd();
		int midLineCoordinate;
		if (pDirection == Direction.NORTH || pDirection == Direction.SOUTH)
		{
			midLineCoordinate = getHorizontalMidline(closestStartPoint, endPoint, pDirection, endNode, pEdgesToMerge.get(0));
		}
		else
		{
			midLineCoordinate = getVerticalMidline(closestStartPoint, endPoint, pDirection, endNode, pEdgesToMerge.get(0));
		}
		for (Edge edge : pEdgesToMerge)
		{
			Point start = edgeStartPoints.get(pEdgesToMerge.indexOf(edge));
			EdgePath path = buildSegmentedEdgePath(pDirection, start, midLineCoordinate, endPoint);
			aEdgeStorage.store(edge, path);
		}
	}
	
	/**
	 * Plans the EdgePaths for Aggregation and Composition Edges.
	 * @param pDirection the direction of the edges (the face of the start node on which edges are connected)
	 * @param pEdgesToMerge a list of edges which should be merged
	 * @pre pDirection!=null && pDirection.isCardinal();
	 * @pre pEdgesToMerge!=null && pEdgesToMerge.size()>0;
	 */
	private void storeSharedStartEdges(Direction pDirection, List<Edge> pEdgesToMerge)
	{
		assert pDirection!=null && pDirection.isCardinal();
		assert pEdgesToMerge != null && pEdgesToMerge.size() > 0;
		
		//Get the shared start point for all pEdgesToMerge
		Point startPoint = getConnectionPoint(pEdgesToMerge.get(0).getStart(), pEdgesToMerge.get(0), pDirection);
		//Get the individual end points for each edge
		List<Point> edgeEndPoints = new ArrayList<>();
		for (Edge e : pEdgesToMerge)
		{
			edgeEndPoints.add(getConnectionPoint(e.getEnd(), e, pDirection.mirrored()));
		}
		Point closestEndPoint = getClosestPoint(edgeEndPoints, pDirection.mirrored());
		Node startNode = pEdgesToMerge.get(0).getStart();
		int midLineCoordinate;
		if (pDirection == Direction.NORTH || pDirection == Direction.SOUTH)
		{
			midLineCoordinate = getHorizontalMidline(closestEndPoint, startPoint, pDirection, startNode, pEdgesToMerge.get(0));
		}
		else
		{
			midLineCoordinate = getVerticalMidline(closestEndPoint, startPoint, pDirection, startNode, pEdgesToMerge.get(0));
		}
		for (Edge edge : pEdgesToMerge)
		{
			Point endPoint = edgeEndPoints.get(pEdgesToMerge.indexOf(edge));
			EdgePath path = buildSegmentedEdgePath(pDirection, startPoint, midLineCoordinate, endPoint);
			aEdgeStorage.store(edge, path);
		}
	}
	
	/**
	 * Creates a segmented EdgePath using pStart and pEnd as start and end points (respectively)
	 * and pMidLine as an X or Y coordinate representing the middle segment. 
	 * @param pEdgeDirection the direction describing the trajectory of pEdge
	 * @param pStart the start point of the edge path
	 * @param pMidLine integer representing an X or Y coordinate or the middle segment
	 * @param pEnd the end point of pEdge
	 * @return an EdgePath
	 */
	private EdgePath buildSegmentedEdgePath(Direction pEdgeDirection, Point pStart, int pMidLine, Point pEnd)
	{
		Point startToMidLine;
		Point midLineToEnd;
		if (pEdgeDirection == Direction.NORTH || pEdgeDirection == Direction.SOUTH)
		{
			//Then the mid-point coordinate is a Y-coordinate
		
			startToMidLine = new Point(pStart.getX(), pMidLine);
			midLineToEnd = new Point(pEnd.getX(), pMidLine);
		}
		else //East or West
		{	//Then the mid-point coordinate is a X-coordinate
			startToMidLine = new Point(pMidLine, pStart.getY());
			midLineToEnd = new Point(pMidLine, pEnd.getY());
		}
		return new EdgePath(pStart, startToMidLine, midLineToEnd, pEnd);
	}
	
	/**
	 * Gets the Y-coordinate of the horizontal middle segment of pEdge.
	 * @param pStart the start point for pEdge
	 * @param pEnd the end point for pEdge
	 * @param pDirection the trajectory of pEdge, either North or South
	 * @param pEndNode the end node of pEdge
	 * @param pEdge the segmented edge of interest
	 * @return an integer representing a Y-coordinate of the middle segment of pEdge
	 */
	private int getHorizontalMidline(Point pStart, Point pEnd, Direction pDirection, Node pEndNode, Edge pEdge)
	{
		List<Edge> storedEdgesOnNodeFace = aEdgeStorage.edgesConnectedTo(pEndNode).stream()
				.filter(edge -> getAttachedSide(edge, pEndNode) == pDirection.mirrored())
				.filter(edge -> EdgePriority.isSegmented(edge))
				.filter(edge -> getIndexSign(pEndNode, edge, pDirection) == getIndexSign(pEndNode, pEdge, pDirection))
				.collect(Collectors.toList());
		//return the Y-coordinate between pStart and pEnd if there are no other incoming edges in the way of pEdge
		if (storedEdgesOnNodeFace.isEmpty())
		{
			return pEnd.getY() + ((pStart.getY() - pEnd.getY())/2);
		}
		else
		{ //return the Y-coordinate one grid square closer to pNode than the closest segment currently in storage
			Edge closest = storedEdgesOnNodeFace.stream()
				.min(Comparator.comparing(edge -> verticalDistanceToNode(pEndNode, edge, pDirection))).orElseGet(null);
			if (pDirection == Direction.NORTH)
			{
				return aEdgeStorage.getEdgePath(closest).getPointByIndex(1).getY() - SQUARESIZE;
			}
			else
			{
				return aEdgeStorage.getEdgePath(closest).getPointByIndex(1).getY() + SQUARESIZE;
			}
		}
	}
	
	/**
	 * Gets the X-coordinate of the middle segment for pEdge.
	 * @param pStart the start point of pEdge
	 * @param pEnd the end point of pEdge
	 * @param pDirection the cardinal trajectory of pEdge
	 * @param pEndNode the node on which pEdge converges
	 * @param pEdge the edge of interest
	 * @return an integer representing an X-coordinate of the middle segment of pEdge
	 * @pre pStart !=null && pEnd!=null
	 * @pre pDirection == Direction.EAST || pDriection == Direction.West
	 */
	private int getVerticalMidline(Point pStart, Point pEnd, Direction pDirection, Node pEndNode, Edge pEdge)
	{
		assert pStart !=null && pEnd!=null;
		assert pDirection == Direction.EAST || pDirection == Direction.WEST;
		List<Edge> storedEdgesOnNodeFace = aEdgeStorage.edgesConnectedTo(pEndNode).stream()
				.filter(edge -> getAttachedSide(edge, pEndNode) == pDirection.mirrored())
				.filter(edge -> EdgePriority.isSegmented(edge))
				.filter(edge -> getIndexSign(pEndNode, edge, pDirection) == getIndexSign(pEndNode, pEdge, pDirection))
				.collect(Collectors.toList());
		//return the X-coordinate between pStart and pEnd if there are no other incoming edges in the way of pEdge
		if (storedEdgesOnNodeFace.isEmpty())
		{
			return pEnd.getX() + ((pStart.getX() - pEnd.getX())/2);
		}
		else //return the X-coordinate one grid square closer to pNode than the closest segment currently in storage
		{
			Edge closest = storedEdgesOnNodeFace.stream()
				.min(Comparator.comparing(edge -> horizontalDistanceToNode(pEndNode, edge, pDirection))).orElseGet(null);
			if (pDirection == Direction.WEST)
			{
				return aEdgeStorage.getEdgePath(closest).getPointByIndex(1).getX() - SQUARESIZE;
			}
			else
			{
				return aEdgeStorage.getEdgePath(closest).getPointByIndex(1).getX() + SQUARESIZE;
			}
			
		}
	}
	
	/**
	 * Gets the vertical distance between the North side of pEndNode and the horizontal middle segment of pEdge.
	 * @param pEndNode the end node of interest. 
	 * @param pEdge the segmented edge of interest. 
	 * @param pEdgeDirection the trajectory of pEdge
	 * @return the absolute value of the distance between the North side of pEndNode and the middle segment of pEdge
	 * @pre pEdgeDirection == Direction.NORTH || PEdgeDirection == Direction.SOUTH	
	 * @pre pEdge.getEnd() == pEndNode
	 * @pre pEdge is segmented
	 */
	private int verticalDistanceToNode(Node pEndNode, Edge pEdge, Direction pEdgeDirection) 
	{
		assert pEdgeDirection == Direction.NORTH || pEdgeDirection == Direction.SOUTH;	
		assert pEdge.getEnd() == pEndNode;
		assert EdgePriority.isSegmented(priorityOf(pEdge));
		return Math.abs(aEdgeStorage.getEdgePath(pEdge).getPointByIndex(1).getY() - pEndNode.position().getY());
	}
	
	/**
	 * Gets the horizontal distance between the West side of pNode and the vertical middle segment of pEdge.
	 * @param pEndNode the node of interest. The end node for pEdge. 
	 * @param pEdge the segmented edge of interest.
	 * @param pEdgeDirection the trajectory of pEdge
	 * @return the absolute value of the distance between the West side of pNode and the middle segment of pEdge
	 * @pre pEdgeDirection == Direction.EAST || PEdgeDirection == Direction.WEST	
	 * @pre pEdge.getEnd() == pEndNode
	 * @pre pEdge is segmented
	 */
	private int horizontalDistanceToNode(Node pEndNode, Edge pEdge, Direction pEdgeDirection) 
	{
		assert pEdgeDirection == Direction.EAST || pEdgeDirection == Direction.WEST;	
		assert pEdge.getEnd() == pEndNode;
		assert EdgePriority.isSegmented(priorityOf(pEdge));
		return Math.abs(aEdgeStorage.getEdgePath(pEdge).getPointByIndex(1).getX() - pEndNode.position().getX());
	}
	
	/**
	 * Gets the edge start point point closest to the edge's end points given a list of edge start points. 
	 * @param pEdgeStartPoints the list of edge start points
	 * @param pDirection the direction the edge is traveling (the side of the start node where the edge is outgoing)
	 * @return the Point closest to an edge's end point
	 * @pre pEdgeStartPoints!=null && pEdgeStartPoints.size()>0
	 * @pre pDirection.iscardinal()
	 */
	private Point getClosestPoint(List<Point> pEdgeStartPoints, Direction pDirection) 
	{
		assert  pEdgeStartPoints!=null && pEdgeStartPoints.size()>0;
		assert pDirection!=null && pDirection.isCardinal();
		if (pDirection == Direction.NORTH)
		{//Then the closest start point will have the smallest Y-coordinate
			return pEdgeStartPoints.stream()
							.min((p1, p2)->Integer.compare(p1.getY(), p2.getY())).orElseGet(null);
		}
		else if (pDirection == Direction.SOUTH) 
		{//Then the closest start point will have the largest Y-coordinate
			return pEdgeStartPoints.stream()
						.max((p1, p2) -> Integer.compare(p1.getY(), p2.getY())).orElseGet(null);
		}
		else if (pDirection == Direction.EAST)
		{//Then the closest start point will have the largest X-coordinate
			return pEdgeStartPoints.stream()
				.max((p1, p2)-> Integer.compare(p1.getX() , p2.getX())).orElseGet(null);
		}
		else
		{ // direction is West, so look for the point with the smallest X-coordinate
			return pEdgeStartPoints.stream()
					.min((p1, p2)-> Integer.compare(p1.getX(), p2.getX())).orElseGet(null);
		}
		
	}

	
	/**
	 * Gets the edges which should share a common end point with pEdge.
	 * @param pEdge the edge of interest
	 * @param pEdges a list of edges in the diagram
	 * @return a list containing pEdge and all the edges which should merge with pEdge.
	 * @pre MergeStyle.getMergeStyle == MergeStyle.SHARED_END
	 */
	private List<Edge> getEdgesToMergeEnd(Edge pEdge, List<Edge> pEdges)
	{
		assert MergeStyle.getMergeStyle(pEdge) == MergeStyle.SHARED_END;
		return pEdges.stream()
				.filter(edge -> edge.getEnd() == pEdge.getEnd())
				.filter(edge -> priorityOf(edge) == priorityOf(pEdge))
				.filter(edge -> noOtherEdgesBetween(edge, pEdge, pEdge.getEnd()))
				.filter(edge -> getAttachedSide(edge, edge.getEnd()) == getAttachedSide(pEdge, pEdge.getEnd())) 
				.collect(Collectors.toList());
	}
	
	/**
	 * Gets the edges which should share a common start point with pEdge.
	 * @param pEdge the edge of interest
	 * @param pEdges a list of edges in the diagram
	 * @return a list containing pEdge and all the edges which should merge with pEdge.
	 * @pre MergeStyle.getMergeStyle == MergeStyle.SHARED_START
	 */
	private List<Edge> getEdgesToMergeStart(Edge pEdge, List<Edge> pEdges)
	{
		assert MergeStyle.getMergeStyle(pEdge) == MergeStyle.SHARED_START;
		return pEdges.stream()
			.filter(edge -> edge.getStart() == pEdge.getStart())
			.filter(edge -> priorityOf(edge) == priorityOf(pEdge))
			.filter(edge -> getAttachedSide(edge, edge.getStart()) == getAttachedSide(pEdge, pEdge.getStart()))
			.filter(edge -> noConflictingLabels(edge, pEdge))
			.collect(Collectors.toList());
	}
	
	/**
	 * Checks whether the start labels of pEdge1 and pEdge2 conflict. 
	 * @param pEdge1 an edge of interest
	 * @param pEdge2 another edge of interest
	 * @return false if the edges are both aggregation edges with different start labels. True otherwise. 
	 * @pre pEdge1 !=null && pEdge2 !=null
	 */
	private boolean noConflictingLabels(Edge pEdge1, Edge pEdge2)
	{
		assert pEdge1 !=null && pEdge2 !=null;
		if (pEdge1 instanceof AggregationEdge && pEdge2 instanceof AggregationEdge)
		{
			AggregationEdge aggregationEdge1 = (AggregationEdge) pEdge1;
			AggregationEdge aggregationEdge2 = (AggregationEdge) pEdge2;
			return aggregationEdge1.getStartLabel().equals(aggregationEdge2.getStartLabel());
		}
		else
		{
			return true;
		}
	}
	
	/**
	 * Returns whether there are any edges connected to pNode in between pEdge1 and pEdge2.
	 * @param pEdge1 an edge of interest
	 * @param pEdge2 another edge of interest
	 * @param pNode the node on which pEdge1 and pEdge2 are attached
	 * @return true if there are no stored edges on pNode in between pEdge1 and pEdge2, false otherwise
	 * @pre pEdge1 and pEdge2 are connected to pNode
	 * @pre pNode !=null
	 */
	private boolean noOtherEdgesBetween(Edge pEdge1, Edge pEdge2, Node pNode)
	{
		assert pEdge1.getStart() == pNode && pEdge2.getStart() == pNode ||
				pEdge1.getEnd() == pNode && pEdge2.getEnd() == pNode;
		if (pEdge1 == pEdge2)
		{
			return true;
		}
		//get all edges connected to the same side of pNode as pEdge1 and pEdge2
		Direction side = getAttachedSide(pEdge1, pNode);
		List<Edge> edgesOnSide = aEdgeStorage.edgesConnectedTo(pNode).stream()
					.filter(edge -> getAttachedSide(edge, pNode) == side)
					.collect(Collectors.toList());
		if (edgesOnSide.isEmpty())
		{
			return true;
		}
		else
		{
			//check if the center points of both nodes attached to pEdge1 and pEdge2 
			//are on the same side of pNode's center point
			Point centerPoint1 = NodeViewerRegistry.getBounds(getOtherNode(pEdge1, pNode)).getCenter();
			Point centerPoint2 = NodeViewerRegistry.getBounds(getOtherNode(pEdge2, pNode)).getCenter();
			Point nodeCenter = NodeViewerRegistry.getBounds(pNode).getCenter();
			if (side == Direction.NORTH || side == Direction.SOUTH)
			{
				return centerPoint1.getX() <= nodeCenter.getX() && centerPoint2.getX() <= nodeCenter.getX() ||
						centerPoint1.getX() >= nodeCenter.getX() && centerPoint2.getX() >= nodeCenter.getX();
			}
			else
			{
				return centerPoint1.getY() <= nodeCenter.getY() && centerPoint2.getY() <= nodeCenter.getY() ||
						centerPoint1.getY() >= nodeCenter.getY() && centerPoint2.getY() >= nodeCenter.getY();
			}
		}
	}
	
	/**
	 * Uses information about edges from EdgeStorage to get 
	 * the point on which pEdge connects to pNode.
	 * @param pNode the node of interest
	 * @param pEdge the edge of interest
	 * @return the Point where pEdge connects to pNode
	 * @pre pEdge!=null
	 * @pre pNode !=null
	 * @pre pEdge.getStart() == pNode || pEdge.getEnd() == pNode	
	 */
	private Point getConnectionPoint(Node pNode, Edge pEdge, Direction pAttachmentSide)
	{
		assert pNode!=null && pEdge!=null;
		assert pEdge.getStart() == pNode || pEdge.getEnd() == pNode;		
		Rectangle nodeBounds = NodeViewerRegistry.getBounds(pNode);
		
		Line faceOfNode = getNodeFace(nodeBounds, pAttachmentSide);
		int maxIndex = 4; //North and South node sides have connection points: -4 to +4
		//East and West node sides have connection points: -2 to +2
		if (pAttachmentSide == Direction.EAST || pAttachmentSide == Direction.WEST)
		{
			maxIndex = 2; 
		}
		int indexSign = getIndexSign(pNode, pEdge, pAttachmentSide.mirrored());
		for (int offset = 0; offset <= maxIndex; offset++) 
		{
			int ordinal = 4 + (indexSign * offset);
			NodeIndex bestIndex = NodeIndex.values()[ordinal];
			Point bestPoint = NodeIndex.toPoint(faceOfNode, pAttachmentSide, bestIndex);
			if (aEdgeStorage.connectionPointIsAvailable(bestPoint))
			{
				return bestPoint;
			}
		}
		int maxOrdinal = maxIndex * indexSign;
		return NodeIndex.toPoint(faceOfNode, pAttachmentSide, NodeIndex.values()[maxOrdinal]);
	}
	
	/**
	 * Returns the node connected to pEdge which is not pNode.
	 * @param pEdge the edge of interest
	 * @param pNode a node attached to pEdge
	 * @return the other node attached to pEgde
	 * @pre pEdge!=null
	 * @pre pNode!=null
	 * @pre pEdge.getStart() == pNode || pEdge.getEnd() == pNode
	 */
	private Node getOtherNode(Edge pEdge, Node pNode)
	{
		assert pEdge!=null;
		assert pNode!=null;
		assert pEdge.getStart() == pNode || pEdge.getEnd() == pNode;
		if (pEdge.getStart() == pNode)
		{
			return pEdge.getEnd();
		}
		else
		{
			return pEdge.getStart();
		}
	}
	
	/**
	 * Uses the relative positions of pNode and pEdge to get the index sign (-1 or 1) 
	 * of the index position where pEdge would connect to pNode.
	 * @param pNode the node of interest
	 * @param pEdge the edge of interest
	 * @param pEdgeDirection  the direction representing the trajectory of pEdge
	 * @return -1 if pNode is West or North of pEdge's other node. Returns +1 otherwise. 
	 * @pre pEdgeDirection.isCardinal()
	 * @pre pEdge.getStart()==pNode || pEdge.getEnd()==pNode;
	 */
	private int getIndexSign(Node pNode, Edge pEdge, Direction pEdgeDirection)
	{
		assert pEdgeDirection.isCardinal();
		assert pEdge.getStart()==pNode || pEdge.getEnd()==pNode;
		Rectangle nodeBounds = NodeViewerRegistry.getBounds(pNode);
		Rectangle otherBounds = NodeViewerRegistry.getBounds(getOtherNode(pEdge, pNode));
		if (pEdgeDirection == Direction.NORTH || pEdgeDirection == Direction.SOUTH)
		{
			if (nodeBounds.getCenter().getX() > otherBounds.getCenter().getX())
			{
				return -1;
			}
			else
			{
				return 1;
			}
		}
		else 
		{
			if (nodeBounds.getCenter().getY() > otherBounds.getCenter().getY())
			{
				return -1;
			}
			else
			{
				return 1;
			}
		}
	}
	
	/**
	 * Gets a line representing the pFace side of pNode. 
	 * @param pNode the node of interest
	 * @param pFace the desired side of pNode
	 * @return a line spanning the pFace side of pNode
	 * @pre pNodeBounds !=null
	 * @pre pFace.isCardinal()
	 */
	private Line getNodeFace(Rectangle pNodeBounds, Direction pFace)
	{
		assert pNodeBounds !=null && pFace.isCardinal();
		Point topLeft = pNodeBounds.getOrigin();
		Point topRight = new Point(pNodeBounds.getMaxX(), pNodeBounds.getY());
		Point bottomLeft = new Point(pNodeBounds.getX(), pNodeBounds.getMaxY());
		Point bottomRight = new Point(pNodeBounds.getMaxX(), pNodeBounds.getMaxY());
		if (pFace == Direction.SOUTH)
		{
			return new Line(bottomLeft, bottomRight);
		}
		else if (pFace == Direction.NORTH)
		{
			return new Line(topLeft, topRight);
		}
		else if (pFace == Direction.WEST)
		{
			return new Line(topLeft, bottomLeft);
		}
		else 
		{
			return new Line(topRight, bottomRight);
		}
	}
	
	/**
	 * Returns whether pEdge is an outgoing edge from pNode.
	 * @param pEdge the edge of interest
	 * @param pNode the node of interest
	 * @return true if pEdge is outgoing from pNode, false otherwise
	 * @pre pNode!=null
	 * @pre pEdge!=null
	 */
	private boolean isOutgoingEdge(Edge pEdge, Node pNode)
	{
		assert pEdge!=null && pNode!=null;
		return pEdge.getStart() == pNode;
	}
	
	/**
	 * Gets the side of pNode that pEdge should be attached to.
	 * @param pEdge the edge of interest
	 * @param pNode the node of interest
	 * @return the cardinal side of pNode where pEdge should be attached. 
	 */
	private Direction getAttachedSide(Edge pEdge, Node pNode)
	{
		if (MergeStyle.getMergeStyle(pEdge)==MergeStyle.SHARED_START)
		{
			return attachedSidePreferringEastWest(pEdge, pNode);
		}
		else
		{
			return attachedSidePreferringNorthSouth(pEdge, pNode);
		}
	}
	
	/**
	 * Gets the cardinal side of pNode on which pEdge will be attached.
	 * Edges should connect to the East or West sides of pNode
	 * unless they are directly above/below pNode. 
	 * @param pEdge the edge of interest
	 * @param pNode the node of interest
	 * @return the cardinal side of pNode that pEdge should be attached to
	 */
	private Direction attachedSidePreferringEastWest(Edge pEdge, Node pNode)
	{
		assert pEdge!= null && pNode != null;
		assert pEdge.getStart() == pNode || pEdge.getEnd() == pNode;
		Rectangle startNodeBounds = NodeViewerRegistry.getBounds(pEdge.getStart());
		Rectangle endNodeBounds = NodeViewerRegistry.getBounds(pEdge.getEnd());
		Direction outgoingSide;
		if (endNodeBounds.getX() + NODE_WIDTH < startNodeBounds.getX() - MARGIN)
		{
			outgoingSide = Direction.WEST;
		}
		else if (startNodeBounds.getX() + NODE_WIDTH < endNodeBounds.getX() - MARGIN)
		{
			outgoingSide = Direction.EAST;
		}
		else if (endNodeBounds.getCenter().getY() < startNodeBounds.getCenter().getY())
		{
			outgoingSide = Direction.NORTH;
		}
		else
		{
			outgoingSide = Direction.SOUTH;
		}
		//Edges incoming on pNode will have the opposite direction
		if (isOutgoingEdge(pEdge, pNode))
		{
			return outgoingSide;
		}
		else
		{
			return outgoingSide.mirrored();
		}
	}
	
	/**
	 * Gets the side of pNode on which pEdge should be attached. 
	 * Edges should be attached to the North or South sides of pNode unless the are directly to the right or left of pNode. 
	 * @param pEdge the edge of interest
	 * @param pNode the node of interest
	 * @return the cardinal side of pNode that pEdge should be attached to
	 */
	private Direction attachedSidePreferringNorthSouth(Edge pEdge, Node pNode)
	{
		assert pEdge!= null && pNode != null;
		assert pEdge.getStart() == pNode || pEdge.getEnd() == pNode;
		Rectangle startNodeBounds = NodeViewerRegistry.getBounds(pEdge.getStart());
		Rectangle endNodeBounds = NodeViewerRegistry.getBounds(pEdge.getEnd());
		Direction outgoingSide;
		if (endNodeBounds.getMaxY() < startNodeBounds.getY() )
		{
			outgoingSide = Direction.NORTH;
		}
		else if (startNodeBounds.getMaxY() < endNodeBounds.getY())
		{
			outgoingSide = Direction.SOUTH;
		}
		else if (endNodeBounds.getCenter().getX() < startNodeBounds.getCenter().getX())
		{
			outgoingSide = Direction.WEST;
		}
		else
		{
			outgoingSide = Direction.EAST;
		}
		
		if (isOutgoingEdge(pEdge, pNode))
		{
			return outgoingSide;
		}
		else
		{
			return outgoingSide.mirrored();
		}
	}
	
}
