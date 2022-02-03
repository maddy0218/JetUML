package ca.mcgill.cs.jetuml.views;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


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
	
	/**
	 * Uses position information about nodes and edges to plan the trajectory 
	 * of edges in the diagram. 
	 * @param pDiagram the diagram of interest
	 * @param pEdgeStorage object which stores edge positions in the diagram
	 * @param pGraphics 
	 */
	public void layout(Diagram pDiagram, GraphicsContext pGraphics, EdgeStorage pEdgeStorage)
	{
		layoutInheritanceEdges(pDiagram, pGraphics, pEdgeStorage);
		
	}
	

	/**
	 * Plans the trajectory for inheritance edges by merging incoming edges when they are close enough 
	 * and simplifying the paths for merged edges
	 * @param pDiagram the diagram to layout
	 * @param pEdgeStorage the storage for EdgePaths for pDiagram
	 * @param pGraphics 
	 */
	private void layoutInheritanceEdges(Diagram pDiagram, GraphicsContext pGraphics, EdgeStorage pEdgeStorage)
	{
		//get all inheritanceEdges
		List<Edge> inheritanceEdges = pDiagram.edges().stream()
			.filter(edge -> EdgePriority.priorityOf(edge)==EdgePriority.INHERITANCE)
			.collect(toList());
		
		while (!inheritanceEdges.isEmpty())
		{
			Edge currentEdge = inheritanceEdges.get(0);
			List<Edge> edgesToMerge = getEdgesToMerge(currentEdge, inheritanceEdges);
			Direction edgeDirection = getCardinalDirection(Direction.fromLine(currentEdge.getStart().position(), currentEdge.getEnd().position()));
			//TO-DO: decide which sibling edges are close enough to merge (merge them all for now)
			
			//remove siblings from inheritanceEdges
			inheritanceEdges.removeAll(edgesToMerge);
			Point endPoint = getConnectionPoint(currentEdge.getEnd(), currentEdge, pDiagram);
			
			if (!edgesToMerge.isEmpty()) {
			
				List<Point> edgeStartPoints = new ArrayList<>();
				for (Edge e : edgesToMerge)
				{
					edgeStartPoints.add(getConnectionPoint(e.getStart(), e, pDiagram));
				}
				if (edgeDirection==Direction.NORTH)
				{
					//Then the closest start point will have the smallest Y-coordinate
					Point closestStartPoint = edgeStartPoints.stream()
									.min((p1, p2)->Integer.compare(p1.getY(), p2.getY())).orElseGet(null);
					
					int midY = endPoint.getY() + ((closestStartPoint.getY() - endPoint.getY())/2);
					Point midYToEnd = new Point(endPoint.getX(), midY);
					// Use mid-Y to build and store connected paths for each edge
					for (Edge edge : edgesToMerge)
					{
						Point start = edgeStartPoints.get(edgesToMerge.indexOf(edge));
						EdgePath path = new EdgePath(start, new Point(start.getX(), midY), midYToEnd, endPoint);
						pEdgeStorage.store(edge, path);
					}
				}
				else if (edgeDirection==Direction.SOUTH)
				{
					//Then the closest start point will have the largest Y-coordinate
					Point closestStartPoint = edgeStartPoints.stream()
									.max((p1, p2) -> Integer.compare(p1.getY(), p2.getY())).orElseGet(null);
					
					int midY = ((endPoint.getY() - closestStartPoint.getY())/2) + closestStartPoint.getY();
					Point midYToEnd = new Point(endPoint.getX(), midY);
					// Use mid-Y to build and store connected paths for each edge
					for (Edge edge : edgesToMerge)
					{
						Point start = edgeStartPoints.get(edgesToMerge.indexOf(edge));
						EdgePath path = new EdgePath(start, new Point(start.getX(), midY), midYToEnd, endPoint);
						pEdgeStorage.store(edge, path);
					}
				}
				else if (edgeDirection == Direction.EAST)
				{
					//then look for the point with the largest X-coordinate
					Point closestStartPoint = edgeStartPoints.stream()
							.max((p1, p2)-> Integer.compare(p1.getX() , p2.getX())).orElseGet(null);
					
					int midX = closestStartPoint.getX() + ((endPoint.getX() - closestStartPoint.getX())/2);
					Point midXToEnd = new Point(midX, endPoint.getY());
					// Use mid-X to build and store connected paths for each edge
					for (Edge edge : edgesToMerge)
					{
						Point start = edgeStartPoints.get(edgesToMerge.indexOf(edge));
						EdgePath path = new EdgePath(start, new Point(midX, start.getY()), midXToEnd, endPoint);
						pEdgeStorage.store(edge, path);
					}
				}
				else //Direction is West
				{
					//then look for the point with the smallest X-coordinate
					Point closestChild = edgeStartPoints.stream()
							.min((p1, p2)-> Integer.compare(p1.getX(), p2.getX())).orElseGet(null);
					
					int midX = endPoint.getX() + ((closestChild.getX() - endPoint.getX())/2);
					Point midXToEnd = new Point(midX, endPoint.getY());
					
					// Use mid-X to build and store connected paths for each edge
					for (Edge edge : edgesToMerge)
					{
						Point start = edgeStartPoints.get(edgesToMerge.indexOf(edge));
						EdgePath path = new EdgePath(start, new Point(midX, start.getY()), midXToEnd, endPoint);
					}
				}
			}
			else //there are no edges to merge with currentEdge
			{
				EdgeViewerRegistry.store(currentEdge, new EdgePath(currentEdge.getStart().position(), endPoint));
			}
		}
	}
	
	
	
	/**
	 * Returns any edges of the same priority type as pEdge who share the same end node as pEdge.
	 * @param pEdge the edge of interest
	 * @param pDiagramEdges a list of edges from the diagram (could be all edges or a subset of edges).
	 * @return list of edges to merge, including pEdge
	 */
	private List<Edge> getEdgesToMerge(Edge pEdge, List<Edge> pDiagramEdges)
	{
		return pDiagramEdges.stream()
				.filter(edge -> edge.getEnd()==pEdge.getEnd())
				.filter(edge -> EdgePriority.priorityOf(edge)== EdgePriority.priorityOf(pEdge))
				.collect(toList());
	}
	
	/**
	 * This will need to be updated when Layouter is implemented for other edge types.
	 * For now, this method returns a point at index 0 on the face of the node
	 * 
	 * Uses information about edges from EdgeStorage to get 
	 * the point on which pEdge connects to pNode, if it does connect.
	 * @param pNode the node of interest
	 * @param pEdge the edge of interest
	 * @return the Point where pEdge connects to pNode if they do connect, empty empty otherwise
	 */
	private Point getConnectionPoint(Node pNode, Edge pEdge, Diagram pDiagram)
	{
		Rectangle nodeBounds = NodeViewerRegistry.getBounds(pNode);	
		Direction sideOfNode;
		if (pEdge.getStart()==pNode)//pEdge is outgoing from pNode
		{
			sideOfNode = Direction.fromLine(pEdge.getStart().position(), pEdge.getEnd().position());
		}
		else //pEdge is incoming on pNode
		{
			sideOfNode = Direction.fromLine(pEdge.getEnd().position(), pEdge.getStart().position());
		}
		//Return the midpoint of that side of the rectangle
		return GeomUtils.intersectRectangle(nodeBounds, sideOfNode);	
	}
	
	/**
	 * Gets the closest cardinal direction to pDirection
	 * @param pDirection the direction of interest
	 * @return the Direction (either NORTH, SOUTH, EAST, or WEST) which is closest to pDirection
	 */
	private Direction getCardinalDirection(Direction pDirection)
	{
		if (pDirection.asAngle() < 45 || pDirection.asAngle() > 315) 
		{ 
			return Direction.NORTH; 
		}
		else if (pDirection.asAngle() < 135) 
		{ 
			return Direction.EAST; 
		}
		else if (pDirection.asAngle() < 225) 
		{ 
			return Direction.SOUTH; 
		}
		else 
		{ 
			return Direction.WEST; 
		}
	}
	
}
