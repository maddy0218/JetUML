package ca.mcgill.cs.jetuml.views;

import static ca.mcgill.cs.jetuml.views.EdgePriority.priorityOf;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
import javafx.scene.canvas.GraphicsContext;;
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
		layoutSegmentedEdges(pDiagram, EdgePriority.INHERITANCE);	
		layoutSegmentedEdges(pDiagram, EdgePriority.IMPLEMENTATION);
		layoutAggregationEdges(pDiagram, EdgePriority.AGGREGATION);
		layoutAggregationEdges(pDiagram, EdgePriority.COMPOSITION);
		layoutSegmentedEdges(pDiagram, EdgePriority.ASSOCIATION);
		layoutStraightLineEdges(pDiagram);
		//Layout self-call edges
	}
	

	/**
	 * Plans the EdgePaths for segmented edges.
	 * @param pDiagram the diagram to layout
	 * @param pSegmentedEdgePriority the edge priority level 
	 * @pre pDiagram!=null && pSegmentedEdgePriority!=null
	 * @pre Edg
	 */
	private void layoutSegmentedEdges(Diagram pDiagram, EdgePriority pEdgePriority)
	{
		assert pDiagram!=null && pEdgePriority!=null;
		assert pEdgePriority == EdgePriority.INHERITANCE || pEdgePriority == EdgePriority.IMPLEMENTATION ||
				pEdgePriority == EdgePriority.ASSOCIATION;
		List<Edge> edgesToProcess = new ArrayList<>();
		for (Edge e : pDiagram.edges())
		{
			if (priorityOf(e) == pEdgePriority)
			{
				edgesToProcess.add(e);
			}
		}
		while (!edgesToProcess.isEmpty())
		{
			Edge currentEdge = edgesToProcess.get(0);
			List<Edge> edgesToMerge = getEdgesToMerge(currentEdge, edgesToProcess);
			Direction edgeDirection = getAttachedSide(currentEdge, currentEdge.getStart());
			edgesToProcess.removeAll(edgesToMerge);
			if (!edgesToMerge.isEmpty()) 
			{
				buildAndStoreEdgePaths(edgeDirection, edgesToMerge);
			}
			else 
			{
				Point endPoint = getConnectionPoint(currentEdge.getEnd(), currentEdge);
				EdgeViewerRegistry.store(currentEdge, new EdgePath(currentEdge.getStart().position(), endPoint));
			}
		}
	}
	/**
	 * Plans the EdgePaths for Aggregation and Composition edges.
	 * @param pDiagram the diagram of interest
	 * @param pEdgePriority the edge priority level 
	 * @pre pDiagram!=null && pEdgePriority!=null
	 * @pre pEdgePriority == EdgePriority.AGGREGATION || pEdgePriority == EdgePriority.COMPOSITION
	 */
	private void layoutAggregationEdges(Diagram pDiagram, EdgePriority pEdgePriority)
	{
		assert pDiagram!=null && pEdgePriority!=null;
		assert pEdgePriority == EdgePriority.AGGREGATION || pEdgePriority == EdgePriority.COMPOSITION;
		List<Edge> edgesToProcess = new ArrayList<>();
		for (Edge e : pDiagram.edges())
		{
			if (priorityOf(e) == pEdgePriority)
			{
				edgesToProcess.add(e);
			}
		}
		while (!edgesToProcess.isEmpty())
		{
			Edge currentEdge = edgesToProcess.get(0);
			List<Edge> edgesToMerge = getEdgesToMerge(currentEdge, edgesToProcess);
			edgesToProcess.removeAll(edgesToMerge);
			Direction edgeDirection = getAttachedSide(currentEdge, currentEdge.getStart());
			if (!edgesToMerge.isEmpty()) 
			{
				buildAndStoreAggregationEdgePaths(edgeDirection, edgesToMerge);
			}
			else //there are no edges to merge with currentEdge
			{
				Point startPoint = getConnectionPoint(currentEdge.getStart(), currentEdge);
				EdgeViewerRegistry.store(currentEdge, new EdgePath(startPoint, currentEdge.getEnd().position()));
			}
		}
	}
	/**
	 * Plans the EdgePaths for Dependency Edges
	 * @param pDiagram the diagram of interest
	 */
	private void layoutStraightLineEdges(Diagram pDiagram)
	{
		assert pDiagram!=null;
		for (Edge edge : pDiagram.edges())
		{
			if (priorityOf(edge)==EdgePriority.DEPENDENCY)
			{
				Point startPoint = getConnectionPoint(edge.getStart(), edge);
				Point endPoint = getConnectionPoint(edge.getEnd(), edge);
				aEdgeStorage.store(edge, new EdgePath(startPoint, endPoint));
			}
		}
			
	}
	
	/**
	 * Builds EdgePaths for each edge to be merged, and stores the edgePath in storage.
	 * @param pDirection the direction of the edges (the face of the start node on which edges are connected)
	 * @param pEdgesToMerge a list of edges to be merged
	 * @pre pDirection!=null && pDirection.isCardinal()
	 * @pre pEdgesToMerge!=null && pEdgesToMerge.size() > 0
	 */
	private void buildAndStoreEdgePaths(Direction pDirection, List<Edge> pEdgesToMerge)
	{
		assert pDirection!=null && pDirection.isCardinal();
		assert pEdgesToMerge!=null && pEdgesToMerge.size()>0;
		//Merged edges share a common end point
		Point endPoint = getConnectionPoint(pEdgesToMerge.get(0).getEnd(), pEdgesToMerge.get(0));
		//get the start point for each edge
		List<Point> edgeStartPoints = new ArrayList<>();
		for (Edge e : pEdgesToMerge)
		{
			edgeStartPoints.add(getConnectionPoint(e.getStart(), e));
		}
		//The edge segment bend will occur half-way between the end point and the closest start point
		//Unless layout adjustments are needed
		Point closestStartPoint = getClosestPoint(edgeStartPoints, pDirection);
		int midLine = getMidPoint(closestStartPoint, endPoint, pDirection, pEdgesToMerge.get(0).getEnd());
		for (Edge edge : pEdgesToMerge)
		{
			Point start = edgeStartPoints.get(pEdgesToMerge.indexOf(edge));
			Point startToMidLine;
			Point midLineToEnd;
			if (pDirection == Direction.NORTH || pDirection == Direction.SOUTH)
			{
				//Then the mid-point coordinate is a Y-coordinate
				startToMidLine = new Point(start.getX(), midLine);
				midLineToEnd = new Point(endPoint.getX(), midLine);
			}
			else //East or West
			{	//Then the mid-point coordinate is a X-coordinate
				startToMidLine = new Point(midLine, start.getY());
				midLineToEnd = new Point(midLine, endPoint.getY());
			}
			EdgePath path = new EdgePath(start, startToMidLine, midLineToEnd, endPoint);
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
	private void buildAndStoreAggregationEdgePaths(Direction pDirection, List<Edge> pEdgesToMerge)
	{
		assert pDirection!=null && pDirection.isCardinal();
		assert pEdgesToMerge!=null && pEdgesToMerge.size()>0;
		//get end point and start point(s)
		Point startPoint = getConnectionPoint(pEdgesToMerge.get(0).getStart(), pEdgesToMerge.get(0));
		List<Point> endPoints = new ArrayList<>();
		for (Edge e : pEdgesToMerge)
		{
			endPoints.add(getConnectionPoint(e.getEnd(), e));
		}
		Point closestEndPoint = getClosestPoint(endPoints, pDirection.mirrored());
		int midLine = getMidPoint(closestEndPoint, startPoint, pDirection.mirrored(), pEdgesToMerge.get(0).getStart());
		for (Edge edge : pEdgesToMerge)
		{
			Point startToMidLine;
			Point midLineToEnd;
			Point endPoint = endPoints.get(pEdgesToMerge.indexOf(edge));
			if (pDirection == Direction.NORTH || pDirection == Direction.SOUTH)
			{
				startToMidLine = new Point(startPoint.getX(), midLine);
				midLineToEnd = new Point(endPoint.getX(), midLine);
			}
			else //East or West
			{
				startToMidLine = new Point(midLine, startPoint.getY());
				midLineToEnd = new Point(midLine, endPoint.getY());
			}
			EdgePath path = new EdgePath(startPoint, startToMidLine, midLineToEnd, endPoint);
			aEdgeStorage.store(edge, path);
		}
		
		
	}
	
	
	/**
	 * Gets the integer representing a coordinate halfway between pStart and pEnd, on the X-axis or Y-Axis.
	 * @param pStart the start point of interest
	 * @param pEnd the end point of interest
	 * @param pDirection the direction from the start to the end
	 * @return an integer representing either an X-coordinate or Y-coordinate in between pStart and pEnd
	 * @pre pStart!=null && pEnd!=null
	 * @pre pDirection!=null && pDirection.isCardinal();
	 */
	private int getMidPoint(Point pStart, Point pEnd, Direction pDirection, Node pEndNode) 
	{
		assert pStart!=null && pEnd!=null;
		assert pDirection!=null && pDirection.isCardinal();
		int midPoint;
		Line midLine;
		if (pDirection == Direction.NORTH || pDirection == Direction.SOUTH)
		{
			midPoint = pEnd.getY() + ((pStart.getY() - pEnd.getY())/2);
			midLine = new Line(new Point(pStart.getX(), midPoint), new Point(pEnd.getX(), midPoint));
			while (!segmentIsAvailable(midLine, pDirection, pEndNode))
			{
				if (pDirection == Direction.NORTH)
				{
					midPoint -= SQUARESIZE;
				}
				else
				{
					midPoint += SQUARESIZE;
				}
				midLine = new Line(new Point(pStart.getX(), midPoint), new Point(pEnd.getX(), midPoint));
			}
		}
		else
		{
			midPoint = ((pEnd.getX() - pStart.getX())/2) + pStart.getX();
			midLine = new Line(new Point(midPoint, pStart.getY()), new Point(midPoint, pEnd.getY()));
			while (!segmentIsAvailable(midLine, pDirection, pEndNode))
			{
				if (pDirection == Direction.EAST)
				{
					midPoint += SQUARESIZE;
				}
				else
				{
					midPoint -= SQUARESIZE;
				}
				
				midLine = new Line(new Point(pStart.getX(), midPoint), new Point(pEnd.getX(), midPoint));
			}
		}
		return midPoint;
	}
	
	
	/**
	 * Returns whether there are edges in storage whose segments are too close in proximity to pSegment.
	 * @param pSegment a line segment of interest
	 * @param pDirection the direction of the segment 
	 * @param pEndNode the end node of interest
	 * @return true if there are no stored edges in the way of pSegment, false otherwise
	 * @pre pSegment !=null && pSegment!=null
	 * @pre pDirection.isCardinal()
	 */
	public boolean segmentIsAvailable(Line pSegment, Direction pDirection, Node pEndNode)
	{
		assert pSegment !=null && pSegment!=null;
		assert pDirection.isCardinal();
		for (Edge edge : aEdgeStorage.edgesConnectedTo(pEndNode))
		{
			if (getAttachedSide(edge, pEndNode).mirrored() == pDirection &&
				EdgePriority.isSegmented(priorityOf(edge)))
			{
				//get rectangles which enclose both pSegment and the stored edge segment
				Point storedPoint1 = aEdgeStorage.getEdgePath(edge).getPointByIndex(1);
				Point storedPoint2 = aEdgeStorage.getEdgePath(edge).getPointByIndex(2);
				Rectangle newSegmentBounds = createSegmentBounds(pSegment, pDirection);
				Rectangle storedSegmentBounds = createSegmentBounds(new Line(storedPoint1, storedPoint2), pDirection);
				//ensure these rectangles do not overlap with the segments
				if (newSegmentBounds.contains(storedPoint1) || newSegmentBounds.contains(storedPoint2))
				{
					return false;
				}
				else if (storedSegmentBounds.contains(pSegment.getPoint1()) || storedSegmentBounds.contains(pSegment.getPoint2()))
				{
					return false;
				}
				
			}
		}
		return true;
	}
	
	/**
	 * Creates a rectangle boundary around pSegment with a margin of 9 on the sides 
	 * Parallel to the line, and a perpendicular margin of 0. 
	 * @param pSegment the line segment of interest
	 * @param pDirection the direction of pSegment
	 * @return a rectangle enclosing pSegment with a margin of 9 on the long sides. 
	 * @pre pSegment != null
	 * @pre pDirection!=null && pDirection.isCardinal()
	 */
	private Rectangle createSegmentBounds(Line pSegment, Direction pDirection)
	{
		assert pSegment != null;
		assert pDirection!=null && pDirection.isCardinal();
		int width;
		int height;
		int topLeftX;
		int topLeftY;
		if (pDirection==Direction.NORTH || pDirection == Direction.SOUTH)
		{
			width = Math.abs(pSegment.getX2()-pSegment.getX1());
			height = (SQUARESIZE * 2) - 2;
			if (pSegment.getX1() < pSegment.getX2())
			{
				topLeftX = pSegment.getX1();
				topLeftY = pSegment.getY1() - SQUARESIZE + 1;
			}
			else
			{
				topLeftX = pSegment.getPoint2().getX();
				topLeftY = pSegment.getY2() - SQUARESIZE + 1;
			}
			
		}
		else
		{
			width = (SQUARESIZE * 2) - 2;
			height = Math.abs(pSegment.getY2()-pSegment.getY1());
			if (pSegment.getY1() < pSegment.getY2())
			{
				topLeftX = pSegment.getX1() - SQUARESIZE + 1;
				topLeftY = pSegment.getY1();
			}
			else
			{
				topLeftX = pSegment.getX2() - SQUARESIZE + 1;
				topLeftY = pSegment.getY2();
			}
		}
		return new Rectangle(topLeftX, topLeftY, width, height);
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
	 * Returns any edges of the same priority type as pEdge which share the same end node as pEdge.
	 * @param pEdge the edge of interest
	 * @param pDiagramEdges a list of edges from the diagram (could be all edges or a subset of edges).
	 * @return list of edges to merge, including pEdge
	 * @pre pEdge!=null
	 * @pre pDiagramEdges!=null
	 */
	private List<Edge> getEdgesToMerge(Edge pEdge, List<Edge> pDiagramEdges)
	{
		assert pEdge != null && pDiagramEdges !=null;
		List<Edge> result = new ArrayList<>();
		if (pEdge instanceof AggregationEdge)
		{
			pDiagramEdges.stream()
				.filter(edge -> edge.getStart() == pEdge.getStart())
				.filter(edge -> priorityOf(edge) == priorityOf(pEdge))
//				.filter(edge -> getAttachedSide(edge, edge.getStart() == getAttachedSide(pEdge, pEdge.getStart()))) // TODO add missing conditions
				.collect(Collectors.toList());
			
			
			for (Edge e : pDiagramEdges)
			{
				if (e.getStart() == pEdge.getStart() &&
					priorityOf(e) == priorityOf(pEdge) &&
					getAttachedSide(e, e.getStart()) == getAttachedSide(pEdge, pEdge.getStart()) &&
					noOtherEdgesBetween(e, pEdge, pEdge.getStart()) &&
					noConflictingLabels(e, pEdge))
				{
					result.add(e);
				}
			}
		}
		else //Edge is not an Aggregation or Composition edge
		{
			for (Edge e : pDiagramEdges)
			{
				if (e.getEnd() == pEdge.getEnd() &&
					priorityOf(e) == priorityOf(pEdge) &&
					getAttachedSide(e, e.getEnd())==getAttachedSide(pEdge, pEdge.getEnd()) &&
					noOtherEdgesBetween(e, pEdge, pEdge.getEnd()))
				{
					result.add(e);
				}	
			}
		}
		return result;
		
	}
	
	/**
	 * Checks whether the start labels of pEdge1 and pEdge2 conflict. 
	 * @param pEdge1 an edge of interest
	 * @param pEdge2 another edge of interest
	 * @return false if the edges are both aggregation edges with different non-empty start labels. True otherwise. 
	 * @pre pEdge1 !=null && pEdge2 !=null
	 */
	private boolean noConflictingLabels(Edge pEdge1, Edge pEdge2)
	{
		assert pEdge1 !=null && pEdge2 !=null;
		if (pEdge1 instanceof AggregationEdge && pEdge2 instanceof AggregationEdge)
		{
			AggregationEdge aggregationEdge1 = (AggregationEdge) pEdge1;
			AggregationEdge aggregationEdge2 = (AggregationEdge) pEdge2;
			String label1 = aggregationEdge1.getStartLabel();
			String label2 = aggregationEdge2.getStartLabel();
			//If either are empty strings then there is no conflict
			if ("".equals(label1) || "".equals(label2))
			{
				return true;
			}
			//if the labels are the same string then there is no conflict
			else 
			{
				return label1.equals(label2);
			}
			
		}
		else
		{//other edge types will not have conflicting edge labels since they don't have start labels
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
		List<Edge> edgesOnSide = new ArrayList<>();
		for (Edge e : aEdgeStorage.edgesConnectedTo(pNode))
		{
			if (getAttachedSide(e, pNode) == side)
			{
				edgesOnSide.add(e);
			}
		}
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
	 * the point on which pEdge connects to pNode, if it does connect.
	 * @param pNode the node of interest
	 * @param pEdge the edge of interest
	 * @return the Point where pEdge connects to pNode if they do connect, empty empty otherwise
	 * @pre pEdge!=null
	 * @pre pNode !=null
	 */
	private Point getConnectionPoint(Node pNode, Edge pEdge)
	{
		assert pNode!=null && pEdge!=null;
		Rectangle nodeBounds = NodeViewerRegistry.getBounds(pNode);
		Node otherNode = getOtherNode(pEdge, pNode);
		Direction attachmentSide = getAttachedSide(pEdge, pNode); //which face of the node pEdge is incoming onto
		Line faceOfNode = getNodeFace(nodeBounds, attachmentSide);
		int maxIndex = 4;
		//East and West sides of nodes have fewer connection points: -2 to +2
		if (attachmentSide == Direction.EAST || attachmentSide == Direction.WEST)
		{
			maxIndex = 2; 
		}
		int indexSign = getIndexSign(pNode, otherNode, attachmentSide.mirrored());
		for (int offset = 0; offset <= maxIndex; offset++) 
		{
			int ordinal = 4 + (indexSign * offset);
			NodeIndex bestIndex = NodeIndex.values()[ordinal];
			Point bestPoint = NodeIndex.toPoint(faceOfNode, attachmentSide, bestIndex);
			if (aEdgeStorage.connectionPointIsAvailable(bestPoint))
			{
				return bestPoint;
			}
		}
		int maxOrdinal = maxIndex * indexSign;
		return NodeIndex.toPoint(faceOfNode, attachmentSide, NodeIndex.values()[maxOrdinal]);
	}
	
	
	
	/**
	 * Returns the node on which pEdge is connected which is not pNode.
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
	 * Uses the relative positions of pStartNode and pEndNode to get the index sign (either positive or negative)
	 * of the index position where an edge from pStartNode would connect on pEndNode.
	 * @param pStartNode the start node 
	 * @param pEndNode the node where an edge from pStart connects
	 * @param pEdgeDirection the cardinal direction describing the trajectory of an edge from pStartNode to pEndNode
	 * @return and IndexSign (either POSITIVE or NEGATIVE). Returns IndexSign.NEGATIVE if the connection point is
	 * 	West or North of the center point. Returns IndexSign.POSITIVE if the connection point is equal to, SOUTH, or EAST 
	 * 	of the center point. 
	 */
	private int getIndexSign(Node pStartNode, Node pEndNode, Direction pEdgeDirection)
	{
		Rectangle nodeBounds = NodeViewerRegistry.getBounds(pStartNode);
		Rectangle otherBounds = NodeViewerRegistry.getBounds(pEndNode);
		if (pEdgeDirection == Direction.NORTH || pEdgeDirection == Direction.SOUTH)
		{
			//then compare x-coordinates
			if (nodeBounds.getCenter().getX() > otherBounds.getCenter().getX())
			{
				return -1;
			}
			else
			{
				return 1;
			}
		}
		else //Direction is East or West so compare Y-values
		{
			//then compare x-coordinates
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
	 * Gets a line representing the side of pNode. 
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
		Point start;
		Point end;
		if (pFace == Direction.SOUTH)
		{
			start = bottomLeft;
			end = bottomRight;
		}
		else if (pFace == Direction.NORTH)
		{
			start = topLeft;
			end = topRight;
		}
		else if (pFace == Direction.WEST)
		{
			start = topLeft;
			end = bottomLeft;
		}
		else 
		{
			start = topRight;
			end = bottomRight;
		}
		return new Line(start, end);
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
	 * Gets the side of pNode on which pEdge should be attached, depending on 
	 * their relative positions. 
	 * @param pEdge the edge of interest
	 * @param pNode the node of interest
	 * @return the Direction representing the side of pNode where pEdge should 
	 * @pre pEdge !=null
	 * @pre pNode!=null
	 * @pre pEdge is attached to pNode
	 */
	private Direction getAttachedSide(Edge pEdge, Node pNode)
	{
		assert pEdge!=null && pNode !=null;
		assert pEdge.getStart()==pNode || pEdge.getEnd()==pNode;
		Rectangle startNodeBounds = NodeViewerRegistry.getBounds(pEdge.getStart());
		Rectangle endNodeBounds = NodeViewerRegistry.getBounds(pEdge.getEnd());
		Direction outgoingSide;
		if (pEdge instanceof AggregationEdge)
		{ //then attach to East/West sides unless pEdge is directly above or below pNode
			if (endNodeBounds.getMaxX() < startNodeBounds.getX() - MARGIN)
			{
				outgoingSide = Direction.WEST;
			}
			else if (startNodeBounds.getMaxX() < endNodeBounds.getX()-MARGIN)
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
		}
		else // Any other edge type:	
		{ //Attach to North/South sides unless edge is directly beside pNode
			if (endNodeBounds.getMaxY() < startNodeBounds.getY() - MARGIN)
			{
				outgoingSide = Direction.NORTH;
			}
			else if (startNodeBounds.getMaxY() < endNodeBounds.getY()-MARGIN)
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
