package ca.mcgill.cs.jetuml.views;

import ca.mcgill.cs.jetuml.diagram.Diagram;
import ca.mcgill.cs.jetuml.diagram.Edge;
import ca.mcgill.cs.jetuml.viewers.edges.EdgeStorage;
import ca.mcgill.cs.jetuml.viewers.edges.EdgeViewerRegistry;
import ca.mcgill.cs.jetuml.viewers.nodes.NodeViewerRegistry;
import javafx.scene.canvas.GraphicsContext;

/**
 * A specialized viewer for Class diagrams which will apply use Layouter 
 * to improve edge layout.
 */
public class ClassDiagramViewer extends DiagramViewer
{
	private final EdgeStorage aEdgeStorage = new EdgeStorage();
	
	/**
	 * To-do: draws pDiagram onto pGraphics based on planned edge layout.
	 */
	@Override
	public void draw(Diagram pDiagram, GraphicsContext pGraphics)
	{
		assert pDiagram != null && pGraphics != null;
		//draw and store nodes 
		NodeViewerRegistry.activateNodeStorages();
		pDiagram.rootNodes().forEach(node -> drawNode(node, pGraphics));
		
		//plan edge paths
		Layouter layouter = new Layouter();
		layouter.layout(pDiagram, aEdgeStorage, pGraphics);
		
		//draw edges using plan from EdgeStorage
		pDiagram.edges().forEach(edge -> drawFromStorage(edge, pGraphics));
		
		//pDiagram.edges().forEach(edge -> EdgeViewerRegistry.draw(edge, pGraphics));
		NodeViewerRegistry.deactivateAndClearNodeStorages();

	}
	
	/*
	 * 
	 */
	private void drawFromStorage(Edge pEdge, GraphicsContext pGraphics) {
		
	}
	
}
