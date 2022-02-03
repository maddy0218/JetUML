package ca.mcgill.cs.jetuml.viewers.edges;

import java.util.Optional;
import java.util.function.Function;

import ca.mcgill.cs.jetuml.diagram.Edge;
import ca.mcgill.cs.jetuml.diagram.edges.GeneralizationEdge;
import ca.mcgill.cs.jetuml.diagram.edges.GeneralizationEdge.Type;
import ca.mcgill.cs.jetuml.geom.Conversions;
import ca.mcgill.cs.jetuml.geom.Dimension;
import ca.mcgill.cs.jetuml.geom.EdgePath;
import ca.mcgill.cs.jetuml.geom.Point;
import ca.mcgill.cs.jetuml.geom.Rectangle;
import ca.mcgill.cs.jetuml.views.ArrowHead;
import ca.mcgill.cs.jetuml.views.LineStyle;
import ca.mcgill.cs.jetuml.views.StringViewer;
import ca.mcgill.cs.jetuml.views.ToolGraphics;
import ca.mcgill.cs.jetuml.views.StringViewer.Alignment;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

/**
 *Renders the path of an inheritance egde
 */
public class InheritanceEdgeViewer
{
	private static final StringViewer TOP_CENTERED_STRING_VIEWER = StringViewer.get(Alignment.TOP_CENTER);
	private static final StringViewer BOTTOM_CENTERED_STRING_VIEWER = StringViewer.get(Alignment.BOTTOM_CENTER);
	private static final StringViewer LEFT_JUSTIFIED_STRING_VIEWER = StringViewer.get(Alignment.TOP_LEFT);
	private Function<Edge, LineStyle> aLineStyleExtractor = e -> getLineStyle((GeneralizationEdge)e);
	private Function<Edge, ArrowHead> aArrowStartExtractor = e -> ArrowHead.NONE;
	private Function<Edge, ArrowHead> aArrowEndExtractor = e -> ArrowHead.TRIANGLE;
	private Function<Edge, String> aStartLabelExtractor = e -> "";
	private Function<Edge, String> aMiddleLabelExtractor = e -> "";
	private Function<Edge, String> aEndLabelExtractor = e -> "";
	
	
	
	
	
	
	
	/**
	 * @return The line style for this edge.
	 */
	private static LineStyle getLineStyle(GeneralizationEdge pEdge)
	{
		if( pEdge.getType() == Type.Implementation )
		{
			return LineStyle.DOTTED;
		}
		else
		{
			return LineStyle.SOLID;
		}
	}
	

	
	
	
	/**
	 * Uses stored information about the layout of pEdge to draw it.
	 * @param pEdge
	 * @param pGraphics
	 */
	public void drawFromStorage(Edge pEdge, GraphicsContext pGraphics, EdgeStorage pEdgeStorage) {
		//get the EdgePath of pEdge and convert it into a Point2D[] array
		Point2D[] points = Conversions.toPoint2DArray(pEdgeStorage.getEdgePath(pEdge));
		
		ToolGraphics.strokeSharpPath(pGraphics, getSegmentPath(pEdge, pEdgeStorage), aLineStyleExtractor.apply(pEdge));
		aArrowStartExtractor.apply(pEdge).view().draw(pGraphics, 
				Conversions.toPoint(points[1]), 
				Conversions.toPoint(points[0]));
		
		aArrowEndExtractor.apply(pEdge).view().draw(pGraphics, 
				Conversions.toPoint(points[points.length - 2]), 
				Conversions.toPoint(points[points.length - 1]));
		drawString(pGraphics, points[1], points[0], aArrowStartExtractor.apply(pEdge), 
				aStartLabelExtractor.apply(pEdge), false, isStepUp(pEdge));
		drawString(pGraphics, points[points.length / 2 - 1], points[points.length / 2], null, 
				aMiddleLabelExtractor.apply(pEdge), true, isStepUp(pEdge));
		drawString(pGraphics, points[points.length - 2], points[points.length - 1], 
				aArrowEndExtractor.apply(pEdge), aEndLabelExtractor.apply(pEdge), false, isStepUp(pEdge));
	}
	
	
	
	private Path getSegmentPath(Edge pEdge, EdgeStorage pEdgeStorage)
	{
		Point2D[] points = Conversions.toPoint2DArray(pEdgeStorage.getEdgePath(pEdge));
		Path path = new Path();
		Point2D p = points[points.length - 1];
		MoveTo moveTo = new MoveTo((float) p.getX(), (float) p.getY());
		path.getElements().add(moveTo);
		for(int i = points.length - 2; i >= 0; i--)
		{
			p = points[i];
			LineTo lineTo = new LineTo((float) p.getX(), (float) p.getY());
			path.getElements().add(lineTo);
		}
		return path;
	}
	
	private boolean isStepUp(Edge pEdge) 
	{
		Point point1 = EdgeViewerRegistry.getConnectionPoints(pEdge).getPoint1();
		Point point2 = EdgeViewerRegistry.getConnectionPoints(pEdge).getPoint2();
		return point1.getX() < point2.getX() && point1.getY() > point2.getY() || 
				point1.getX() > point2.getX() && point1.getY() < point2.getY();
	}
	

	private void drawString(GraphicsContext pGraphics, Point2D pEndPoint1, Point2D pEndPoint2, 
			ArrowHead pArrowHead, String pString, boolean pCenter, boolean pIsStepUp)
	{
		//do nothing for now
	}
	
	
	
	
}

	



