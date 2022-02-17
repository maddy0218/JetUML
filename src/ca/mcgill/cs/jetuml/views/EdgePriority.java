package ca.mcgill.cs.jetuml.views;

import ca.mcgill.cs.jetuml.diagram.Edge;
import ca.mcgill.cs.jetuml.diagram.edges.AggregationEdge;
import ca.mcgill.cs.jetuml.diagram.edges.AssociationEdge;
import ca.mcgill.cs.jetuml.diagram.edges.DependencyEdge;
import ca.mcgill.cs.jetuml.diagram.edges.GeneralizationEdge;
import ca.mcgill.cs.jetuml.diagram.edges.GeneralizationEdge.Type;

/**
 * Represents the priority level for edges to be followed by Layouter when 
 * planning the layout of edges. 
 * Arranged in decreasing order (INHERITANCE has the highest priority)
 */
public enum EdgePriority {
	INHERITANCE, IMPLEMENTATION, AGGREGATION, COMPOSITION,
	ASSOCIATION, DEPENDENCY, SELFCALL, OTHER;
	
	/**
	 * Gets the priority of an edge
	 * @param pEdge the edde of interest
	 * @return the EdgePriority associated with pEdge
	 * @pre pEdge!=null
	 */
	public static EdgePriority priorityOf(Edge pEdge)
	{
		assert pEdge !=null;
		if (pEdge.getStart()==pEdge.getEnd())
		{
			return EdgePriority.SELFCALL;
		}
		else if (pEdge instanceof GeneralizationEdge)
		{
			if (((GeneralizationEdge) pEdge).getType()==GeneralizationEdge.Type.Inheritance) 
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
			if (((AggregationEdge) pEdge).getType()==AggregationEdge.Type.Aggregation)
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
	 * Returns whether pPriority is for that of a segmented edge
	 * @param pPriority the edgePriority level of interest
	 * @return true if the pPriority is the priority for a segmented edge
	 * @pre pPriority!=nill;
	 */
	public static boolean isSegmented(EdgePriority pPriority)
	{
		assert pPriority !=null;
		return  pPriority == EdgePriority.INHERITANCE || 
				pPriority == EdgePriority.IMPLEMENTATION || 
				pPriority == EdgePriority.AGGREGATION || 
			    pPriority == EdgePriority.COMPOSITION || 
			    pPriority == EdgePriority.ASSOCIATION;
				
	}
}
