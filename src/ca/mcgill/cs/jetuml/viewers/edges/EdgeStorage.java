package ca.mcgill.cs.jetuml.viewers.edges;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import ca.mcgill.cs.jetuml.diagram.Edge;
import ca.mcgill.cs.jetuml.geom.EdgePath;

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
 	 */
 	public Optional<EdgePath> getEdgePath(Edge pEdge)
 	{
 		assert pEdge!=null;
 		if (aEdgePaths.containsKey(pEdge)) 
 		{
 			return Optional.of(aEdgePaths.get(pEdge));
 		}
 		else
 		{
 			return Optional.empty();
 		}
 		
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
 	
}
