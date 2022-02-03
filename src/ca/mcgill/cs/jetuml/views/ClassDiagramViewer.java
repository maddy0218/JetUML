package ca.mcgill.cs.jetuml.views;

import ca.mcgill.cs.jetuml.diagram.Diagram;
import ca.mcgill.cs.jetuml.diagram.Edge;
import ca.mcgill.cs.jetuml.geom.Conversions;
import ca.mcgill.cs.jetuml.geom.Rectangle;
import ca.mcgill.cs.jetuml.viewers.edges.EdgeStorage;
import ca.mcgill.cs.jetuml.viewers.edges.EdgeViewerRegistry;
import ca.mcgill.cs.jetuml.viewers.edges.InheritanceEdgeViewer;
import ca.mcgill.cs.jetuml.viewers.nodes.NodeViewerRegistry;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

/**
 * A specialized viewer for Class diagrams which will apply use Layouter 
 * to improve edge layout.
 */
public class ClassDiagramViewer extends DiagramViewer
{
	private final EdgeStorage aEdgeStorage = new EdgeStorage();
	private final InheritanceEdgeViewer aInheritanceEdgeViewer = new InheritanceEdgeViewer();
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
		layouter.layout(pDiagram, pGraphics, aEdgeStorage);
		
		//draw edges using plan from EdgeStorage
		for (Edge edge : pDiagram.edges())
		{
			if (EdgePriority.getEdgePriority(edge) == EdgePriority.INHERITANCE)
			{
				aInheritanceEdgeViewer.drawFromStorage(edge, pGraphics, aEdgeStorage);
			}
			else
			{
				EdgeViewerRegistry.draw(edge, pGraphics);
			}
		}
		
		NodeViewerRegistry.deactivateAndClearNodeStorages();

	}
}
	
	
	