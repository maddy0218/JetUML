package ca.mcgill.cs.jetuml.viewers.edges;

import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import ca.mcgill.cs.jetuml.diagram.Edge;
import ca.mcgill.cs.jetuml.diagram.Node;
import ca.mcgill.cs.jetuml.geom.Direction;
import ca.mcgill.cs.jetuml.geom.EdgePath;
import ca.mcgill.cs.jetuml.geom.Point;
import ca.mcgill.cs.jetuml.views.EdgePriority;

/**
 * Stores the bounds of edges as a list of rectangles which bound the edges.
 */
public class EdgeStorage
{
	private Map<Edge, EdgePath> aEdgePaths = new IdentityHashMap<>();
 	
 	/**
 	 * Adds pEdge and its associated EdgePath into storage.
 	 * If pEdge is already in storage, then its EdgePath is updated.
 	 * @param pEdge the edge to store
 	 * @pre pEdge!=null
 	 * @pre pEdgePath!=null
 	 */
 	public void store(Edge pEdge, EdgePath pEdgePath)
 	{
 		assert pEdge!=null && pEdgePath!=null;
 		aEdgePaths.put(pEdge, pEdgePath);
 	}
 	
 	/**
 	 * Removes an edge from storage. Does nothing if edge is not in storage in the first place.
 	 * @param pEdge the edge of interest
 	 * @Pre pEdge!=null
 	 */
 	public void remove(Edge pEdge)
 	{
 		aEdgePaths.remove(pEdge);
 	}
 	
 	/**
 	 * Returns an edge's EdgePath from storage if it is in storage.
 	 * @param pEdge the edge of interest
 	 * @return the EdgePath for pEdge from storage 
 	 * @pre pEdge!=null
 	 * @pre this.contains(pEdge)
 	 */
 	public EdgePath getEdgePath(Edge pEdge)
 	{
 		assert pEdge!=null;
 		assert this.contains(pEdge);
 		return aEdgePaths.get(pEdge);
 		
 	}
 	
 	/**
 	 * Returns whether the edge is in storage.
 	 * @param pEdge the edge of interest
 	 * @return true if pEdge is in storage, false otherwise
 	 * @pre pEdge!=null
 	 */
 	public boolean contains(Edge pEdge)
 	{
 		assert pEdge!=null;
 		return aEdgePaths.containsKey(pEdge);
 	}

 	/**
	 * @param pNode The node of interest
	 * @return All the edges connected to pNode
	 * @pre pNode != null
	 */
	public List<Edge> edgesConnectedTo(Node pNode)
	{
		assert pNode != null;
		List<Edge> result = new ArrayList<>();
		for( Edge edge : aEdgePaths.keySet() )
		{
			if( edge.getStart() == pNode || edge.getEnd() == pNode )
			{
				result.add(edge);
			}
		}
		return result;
	}
	
	/**
	 * Returns whether there is an edge whose start of end point is pConnectionPoint.
	 * @param pConnectionPoint a Point in the diagram
	 * @return false if pPoint is a connection point occupied by an edge in storage, true otherwise
	 * @pre pConnectionPoint !=null;
	 */
	public boolean connectionPointIsAvailable(Point pConnectionPoint)
	{
		assert pConnectionPoint !=null;
		for( EdgePath path : aEdgePaths.values() )
		{
			if (path.getStartPoint().equals(pConnectionPoint) || path.getEndPoint().equals(pConnectionPoint))
			{
				return false;
			}
		}
		return true;
	}
	
 	
}
