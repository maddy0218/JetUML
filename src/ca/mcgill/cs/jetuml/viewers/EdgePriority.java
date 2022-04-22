package ca.mcgill.cs.jetuml.viewers;
import ca.mcgill.cs.jetuml.diagram.Edge;
import ca.mcgill.cs.jetuml.diagram.edges.AggregationEdge;
import ca.mcgill.cs.jetuml.diagram.edges.AssociationEdge;
import ca.mcgill.cs.jetuml.diagram.edges.DependencyEdge;
import ca.mcgill.cs.jetuml.diagram.edges.GeneralizationEdge;

/**
 * Represents the priority level for edges so that Layouter can lay out edges in a hierarchical order. 
 * Arranged in decreasing order of priority (INHERITANCE has the highest priority)
 */
public enum EdgePriority 
{
	INHERITANCE, IMPLEMENTATION, AGGREGATION, COMPOSITION,
	ASSOCIATION, DEPENDENCY, SELF_EDGE, OTHER;
	
	/**
	 * Gets the EdgePriority of pEdge.
	 * @param pEdge the edge of interest
	 * @return the EdgePriority associated with pEdge
	 * @pre pEdge!=null
	 */
	public static EdgePriority priorityOf(Edge pEdge)
	{
		assert pEdge != null;
		if (pEdge.getStart()!= null && pEdge.getEnd() != null && pEdge.getStart().equals(pEdge.getEnd()))
		{
			return EdgePriority.SELF_EDGE;
		}
		else if (pEdge instanceof GeneralizationEdge)
		{
			if (((GeneralizationEdge) pEdge).getType() == GeneralizationEdge.Type.Inheritance) 
			{
				return EdgePriority.INHERITANCE;
			}
			else
			{
				return EdgePriority.IMPLEMENTATION;
			}
		}
		else if (pEdge instanceof AggregationEdge)
		{
			if (((AggregationEdge) pEdge).getType() == AggregationEdge.Type.Aggregation)
			{
				return EdgePriority.AGGREGATION;
			}
			else
			{
				return EdgePriority.COMPOSITION;
			}
		}
		else if (pEdge instanceof AssociationEdge)
		{
			return EdgePriority.ASSOCIATION;
		}
		else if (pEdge instanceof DependencyEdge)
		{
			return EdgePriority.DEPENDENCY;
		}
		else
		{
			return EdgePriority.OTHER;
		}
	}
	
	/**
	 * Returns whether pPriority describes a segmented edge.
	 * Since Layouter plans the paths of self-edges separately, self-edges are not segmented by this method. 
	 * @param pPriority the EdgePriority level of interest
	 * @return true if the pPriority is the priority for a segmented edge, false otherwise
	 * @pre pPriority!=null;
	 */
	public static boolean isSegmented(EdgePriority pPriority)
	{
		assert pPriority != null;
		if (pPriority == EdgePriority.INHERITANCE || pPriority == EdgePriority.IMPLEMENTATION)
		{
			return true;
		}
		else 
		{
			return pPriority == EdgePriority.AGGREGATION || pPriority == EdgePriority.COMPOSITION || 
					pPriority == EdgePriority.ASSOCIATION;    	
		}
	}
	
	/**
	 * Returns whether pEdge is segmented.
	 * @param pEdge the edge of interest
	 * @return true if pEdge is segmented, false otherwise.
	 */
	public static boolean isSegmented(Edge pEdge)
	{
		return isSegmented(priorityOf(pEdge));
	}
	
	/**
	 * Returns whether pEdge is a  class diagram Edge which can be stored in EdgeStorage by Layouter. 
	 * @param pEdge the edge of interest
	 * @return true if pEdge should be stored in EdgeStorage, false otherwise. 
	 */
	public static boolean isStoredEdge(Edge pEdge)
	{
		return priorityOf(pEdge)!= OTHER;
	}
}
