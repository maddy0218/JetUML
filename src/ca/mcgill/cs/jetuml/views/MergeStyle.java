package ca.mcgill.cs.jetuml.views;

import ca.mcgill.cs.jetuml.diagram.Edge;
import ca.mcgill.cs.jetuml.diagram.edges.AggregationEdge;
import ca.mcgill.cs.jetuml.diagram.edges.AssociationEdge;
import ca.mcgill.cs.jetuml.diagram.edges.GeneralizationEdge;

/**
 * Represents the way in which edges merge.
 */
public enum MergeStyle 
{
	SHARED_END, SHARED_START, NO_MERGE;
	
	/**
	 * Gets the merge style of pEdge.
	 * @param pEdge the edge of interest
	 * @return the MegeStyle of pEgde
	 * @pre pEdge!=null;
	 */
	public static MergeStyle getMergeStyle(EdgePriority pEdgePriority)
	{
		assert pEdgePriority != null;
		if (pEdgePriority == EdgePriority.INHERITANCE || pEdgePriority == EdgePriority.IMPLEMENTATION ||
				pEdgePriority == EdgePriority.ASSOCIATION)
		{
			return SHARED_END;
		}
		else if (pEdgePriority == EdgePriority.AGGREGATION || pEdgePriority == EdgePriority.COMPOSITION)
		{
			return SHARED_START;
		}
		else
		{
			return NO_MERGE;
		}
			
	}
	
	/**
	 * Gets the merge style of pEdgePriority.
	 * @param pEdgePriority the edge priority of interest
	 * @return the MegeStyel of pEgde
	 * @pre pEdge!=null;
	 */
	public static MergeStyle getMergeStyle(Edge pEdge)
	{
		assert pEdge != null;
		if (pEdge instanceof GeneralizationEdge || pEdge instanceof AssociationEdge)
		{
			return SHARED_END;
		}
		else if (pEdge instanceof AggregationEdge)
		{
			return SHARED_START;
		}
		else
		{
			return NO_MERGE;
		}
			
	}
}


