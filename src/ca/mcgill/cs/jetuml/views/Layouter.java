package ca.mcgill.cs.jetuml.views;

import ca.mcgill.cs.jetuml.diagram.Diagram;
import ca.mcgill.cs.jetuml.viewers.edges.EdgeStorage;
import ca.mcgill.cs.jetuml.viewers.nodes.NodeStorage;
import javafx.scene.canvas.GraphicsContext;

/**
 * Plans the paths for edges based on the position of other edges and nodes 
 * to make the diagram more clean and readable.
 *
 */
public class Layouter 
{
	
	/**
	 * Uses positon information about nodes and edges to plan the trajectory 
	 * of edges in the diagram. 
	 * @param pDiagram the diagram of interest
	 * @param pEdgeStorage object which stores edge positions in the diagram
	 * @param pGraphics 
	 */
	public void layout(Diagram pDiagram, EdgeStorage pEdgeStorage, GraphicsContext pGraphics)
	{
		EdgeStorage edgeStorage = new EdgeStorage();
		//iterate over each edge, starting with those of highest priority
		
		//Establish the connection points for the edge
		
		//get edges default path using its EdgeViewer
		
		//avoid crossing nodes
		
		//fix path overlaying
		
		//minimize edge crossings
		
		//adjust edges to be paralell to grid lines
	}
	
	
}
