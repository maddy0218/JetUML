package ca.mcgill.cs.jetuml.views;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ca.mcgill.cs.jetuml.geom.Direction.fromLine;
import static ca.mcgill.cs.jetuml.views.EdgePriority.priorityOf;
import static java.util.Comparator.comparing;

import java.util.ArrayList;

import ca.mcgill.cs.jetuml.diagram.Diagram;
import ca.mcgill.cs.jetuml.diagram.Edge;
import ca.mcgill.cs.jetuml.diagram.Node;
import ca.mcgill.cs.jetuml.geom.Direction;
import ca.mcgill.cs.jetuml.geom.EdgePath;
import ca.mcgill.cs.jetuml.geom.GeomUtils;
import ca.mcgill.cs.jetuml.geom.Line;
import ca.mcgill.cs.jetuml.geom.Point;
import ca.mcgill.cs.jetuml.geom.Rectangle;
import ca.mcgill.cs.jetuml.viewers.edges.EdgeStorage;
import ca.mcgill.cs.jetuml.viewers.edges.EdgeViewerRegistry;
import ca.mcgill.cs.jetuml.viewers.nodes.NodeStorage;
import ca.mcgill.cs.jetuml.viewers.nodes.NodeViewerRegistry;
import javafx.scene.canvas.GraphicsContext;
import static java.util.stream.Collectors.toList;;
/**
 * Plans the paths for edges based on the position of other edges and nodes 
 * to make the diagram more clean and readable.
 *
 */
public class Layouter 
{
	private final EdgeStorage aEdgeStorage;
	private final int MARGIN = 20;
	
	/**
	 * Plans edge paths using pEgdeStorage.
	 * @param pEdgeStorage the EdgeStorage
	 */
	public Layouter(EdgeStorage pEdgeStorage)
	{
		aEdgeStorage = pEdgeStorage;
	}
	
	/**
	 * Uses position information about nodes and edges to plan the trajectory 
	 * of edges in the diagram. 
	 * @param pDiagram the diagram of interest
	 * @param pEdgeStorage object which stores edge positions in the diagram
	 * @param pGraphics graphics context 
	 * @pre pDiagram!=null
	 */
	public void layout(Diagram pDiagram, GraphicsContext pGraphics)
	{
		assert pDiagram !=null;
		layoutInheritanceEdges(pDiagram, pGraphics);		
	}
	

	/**
	 * Plans the trajectory for inheritance edges by merging incoming edges when they are close enough 
	 * and simplifying the paths for merged edges.
	 * @param pDiagram the diagram to layout
	 * @param pEdgeStorage the storage for EdgePaths for pDiagram
	 * @param pGraphics graphics context
	 * @pre pDiagram!=null && pGraphics!=null
	 */
	private void layoutInheritanceEdges(Diagram pDiagram, GraphicsContext pGraphics)
	{
		assert pDiagram!=null && pGraphics!=null;
		List<Edge> inheritanceEdges = pDiagram.edges().stream()
			.filter(edge -> EdgePriority.priorityOf(edge)==EdgePriority.INHERITANCE)
			.collect(toList());
		
		while (!inheritanceEdges.isEmpty())
		{
			Edge currentEdge = inheritanceEdges.get(0);
			List<Edge> edgesToMerge = getEdgesToMerge(currentEdge, inheritanceEdges);
			Direction edgeDirection = getAttatchedSide(currentEdge, currentEdge.getStart());
			//remove siblings from inheritanceEdges
			inheritanceEdges.removeAll(edgesToMerge);
			Point endPoint = getConnectionPoint(currentEdge.getEnd(), currentEdge);
			if (!edgesToMerge.isEmpty()) 
			{
				List<Point> edgeStartPoints = new ArrayList<>();
				for (Edge e : edgesToMerge)
				{
					edgeStartPoints.add(getConnectionPoint(e.getStart(), e));
				}
				buildAndStoreEdgePaths(edgeDirection, edgesToMerge);
			}
			else //there are no edges to merge with currentEdge
			{
				EdgeViewerRegistry.store(currentEdge, new EdgePath(currentEdge.getStart().position(), endPoint));
			}
		}
	}
	
	/**
	 * Builds EdgePaths for each edge to be merged, and stores the edgePath in storage.
	 * @param pDirection the direction of the edges to be merged
	 * @param pEdgesToMerge a list of edges to be merged
	 * @pre pDirection!=null && pDirection.isCardinal()
	 * @pre pEdgesToMerge!=null && pEdgesToMerge.size()>0
	 */
	private void buildAndStoreEdgePaths(Direction pDirection, List<Edge> pEdgesToMerge)
	{
		assert pDirection!=null && pDirection.isCardinal();
		assert pEdgesToMerge!=null && pEdgesToMerge.size()>0;
		//get end point and start point(s)
		Point endPoint = getConnectionPoint(pEdgesToMerge.get(0).getEnd(), pEdgesToMerge.get(0));
		List<Point> edgeStartPoints = new ArrayList<>();
		for (Edge e : pEdgesToMerge)
		{
			edgeStartPoints.add(getConnectionPoint(e.getStart(), e));
		}
		Point closestStartPoint = getClosestStartPoint(edgeStartPoints, pDirection);
		int midLine = getMidPoint(closestStartPoint, endPoint, pDirection);
		for (Edge edge : pEdgesToMerge)
		{
			Point start = edgeStartPoints.get(pEdgesToMerge.indexOf(edge));
			Point startToMidLine;
			Point midLineToEnd;
			if (pDirection == Direction.NORTH || pDirection == Direction.SOUTH)
			{
				startToMidLine = new Point(start.getX(), midLine);
				midLineToEnd = new Point(endPoint.getX(), midLine);
			}
			else //East or West
			{
				startToMidLine = new Point(midLine, start.getY());
				midLineToEnd = new Point(midLine, endPoint.getY());
			}
			EdgePath path = new EdgePath(start, startToMidLine, midLineToEnd, endPoint);
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
	private int getMidPoint(Point pStart, Point pEnd, Direction pDirection) {
		assert pStart!=null && pEnd!=null;
		assert pDirection!=null && pDirection.isCardinal();
		if (pDirection == Direction.NORTH)
		{
			return pEnd.getY() + ((pStart.getY() - pEnd.getY())/2);
		}
		else if (pDirection == Direction.SOUTH)
		{
			return ((pEnd.getY() - pStart.getY())/2) + pStart.getY();
		}
		else if (pDirection == Direction.EAST)
		{
			return pStart.getX() + ((pEnd.getX() - pStart.getX())/2);
		}
		else //West
		{
			return pEnd.getX() + ((pStart.getX() - pEnd.getX())/2);
		}
			
	}

	/**
	 * Gets the edge start point point closest to the edge's end points given a list of edge start points. 
	 * @param pEdgeStartPoints the list of edge start points
	 * @param pDirection the direction the edge is traveling (the side of the start node where the edge is outgoing)
	 * @return the Point closest to an edge's end point
	 * @pre pEdgeStartPoints!=null && pEdgeStartPoints.size()>0
	 * @pre pDirection.iscardinal()
	 */
	private Point getClosestStartPoint(List<Point> pEdgeStartPoints, Direction pDirection) 
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
		{//then look for the point with the largest X-coordinate
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
	 * Returns any edges of the same priority type as pEdge who share the same end node as pEdge.
	 * @param pEdge the edge of interest
	 * @param pDiagramEdges a list of edges from the diagram (could be all edges or a subset of edges).
	 * @return list of edges to merge, including pEdge
	 * @pre pEdge!=null
	 * @pre pDiagramEdges!=null
	 */
	private List<Edge> getEdgesToMerge(Edge pEdge, List<Edge> pDiagramEdges)
	{
		assert pEdge != null && pDiagramEdges !=null;
		return pDiagramEdges.stream()
				.filter(edge -> edge.getEnd()==pEdge.getEnd())
				.filter(edge -> EdgePriority.priorityOf(edge)== priorityOf(pEdge))
				.filter( edge -> getAttatchedSide(edge, edge.getEnd()) == getAttatchedSide(pEdge, pEdge.getEnd()))
				.collect(toList());
	}
	
	/**
	 * This will need to be updated when Layouter is implemented for other edge types.
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
		Direction attatchmentSide = getAttatchedSide(pEdge, pNode); //which face of the node pEdge is incoming onto
		Line faceOfNode = getNodeFace(nodeBounds, attatchmentSide);
		//get the other edges which are connected to the same face of pNode
		List<Edge> edgesOnNodeFace = getEdgesOnNodeFace(pNode, attatchmentSide);
		//iterate over these edge connection points, making changes when necessary
		
		
		//for now: inheritance edges get index 0 connection point
		if (attatchmentSide == Direction.NORTH || attatchmentSide == Direction.SOUTH)
		{
			return new Point(((faceOfNode.getX2()-faceOfNode.getX1())/2) + faceOfNode.getX1(), faceOfNode.getY1());
		}
		else 
		{
			return new Point(faceOfNode.getX1(), ((faceOfNode.getY2()-faceOfNode.getY1())/2) + faceOfNode.getY1());
		}
	}
	
	/**
	 * Gets all edges which are connected (incoming or outgoing) on the pFace side of pNode.
	 * @param pNode the node of interest
	 * @param pFace the Direction representing which face of pNode is of interest (NORTH, SOUTH, EAST, OR WEST)
	 * @param pEdgeStorage storage for edges
	 * @return a list of edges incoming on the pFace side of pNode
	 * @pre pNode !=null
	 * @pre pFace.isCardinal (North, South, East, or West)
	 */
	private List<Edge> getEdgesOnNodeFace(Node pNode, Direction pFace)
	{
		assert pNode != null;
		assert pFace.isCardinal();
		List<Edge> result = new ArrayList<>();
		Rectangle faceOfNode = getNodeFace(NodeViewerRegistry.getBounds(pNode), pFace).spanning();
		for (Edge edge : aEdgeStorage.edgesConnectedTo(pNode))
		{
			//if the edges start or end point are on this face of the rectangle
			EdgePath edgePath = aEdgeStorage.getEdgePath(edge);
			if (faceOfNode.contains(edgePath.getStartPoint()) || faceOfNode.contains(edgePath.getEndPoint()))
			{
				result.add(edge);
			}
		}
		return result;		
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
		else //pFace == Direction.EAST
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
	private Direction getAttatchedSide(Edge pEdge, Node pNode)
	{
		assert pEdge!=null && pNode !=null;
		assert pEdge.getStart()==pNode || pEdge.getEnd()==pNode;
		Rectangle startNodeBounds = NodeViewerRegistry.getBounds(pEdge.getStart());
		Rectangle endNodeBounds = NodeViewerRegistry.getBounds(pEdge.getEnd());
		
		if (endNodeBounds.getMaxY() < startNodeBounds.getY() - MARGIN)
		{
			return isOutgoingEdge(pEdge, pNode)?Direction.NORTH:Direction.SOUTH;
		}
		else if (startNodeBounds.getMaxY() < endNodeBounds.getY()-MARGIN)
		{
			return isOutgoingEdge(pEdge, pNode)?Direction.SOUTH:Direction.NORTH;
		}
		else if (endNodeBounds.getCenter().getX() < startNodeBounds.getCenter().getX())
		{
			return isOutgoingEdge(pEdge, pNode)?Direction.WEST:Direction.EAST;
		}
		else
		{
			return isOutgoingEdge(pEdge, pNode)?Direction.EAST:Direction.WEST;
		}
	}
}
