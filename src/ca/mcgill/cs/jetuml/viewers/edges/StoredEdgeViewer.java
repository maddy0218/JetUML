package ca.mcgill.cs.jetuml.viewers.edges;

import java.util.Optional;
import java.util.function.Function;

import ca.mcgill.cs.jetuml.diagram.Edge;
import ca.mcgill.cs.jetuml.diagram.edges.AggregationEdge;
import ca.mcgill.cs.jetuml.diagram.edges.GeneralizationEdge;
import ca.mcgill.cs.jetuml.diagram.edges.GeneralizationEdge.Type;
import ca.mcgill.cs.jetuml.diagram.edges.SingleLabelEdge;
import ca.mcgill.cs.jetuml.diagram.edges.ThreeLabelEdge;
import ca.mcgill.cs.jetuml.geom.Conversions;
import ca.mcgill.cs.jetuml.geom.Dimension;
import ca.mcgill.cs.jetuml.geom.EdgePath;
import ca.mcgill.cs.jetuml.geom.Point;
import ca.mcgill.cs.jetuml.geom.Rectangle;
import ca.mcgill.cs.jetuml.views.ArrowHead;
import ca.mcgill.cs.jetuml.views.EdgePriority;
import ca.mcgill.cs.jetuml.views.LineStyle;
import ca.mcgill.cs.jetuml.views.StringViewer;
import ca.mcgill.cs.jetuml.views.ToolGraphics;
import ca.mcgill.cs.jetuml.views.StringViewer.Alignment;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

/**
 *Renders the path of edges using EdgeStorage.
 */
public class StoredEdgeViewer
{
	private static final StringViewer TOP_CENTERED_STRING_VIEWER = StringViewer.get(Alignment.TOP_CENTER);
	private static final StringViewer BOTTOM_CENTERED_STRING_VIEWER = StringViewer.get(Alignment.BOTTOM_CENTER);
	private static final StringViewer LEFT_JUSTIFIED_STRING_VIEWER = StringViewer.get(Alignment.TOP_LEFT);
	final int singleCharWidth =  LEFT_JUSTIFIED_STRING_VIEWER.getDimension(" ").width();
	final int singleCharHeight =  LEFT_JUSTIFIED_STRING_VIEWER.getDimension(" ").height();
	private static final int MAX_LENGTH_FOR_NORMAL_FONT = 15;
	private static final int DEGREES_180=180;
	
	/**
	 * Gets the line style for pEdge.
	 * @param pEdge the edge of interest
	 * @return the LineStyle of pEdge
	 * @pre pEdge !=null
	 */
	private static LineStyle getLineStyle(Edge pEdge)
	{
		assert pEdge !=null;
		if(EdgePriority.priorityOf(pEdge)==EdgePriority.IMPLEMENTATION)
		{
			return LineStyle.DOTTED;
		}
		else
		{
			return LineStyle.SOLID;
		}
	}
	
	/**
	 * Gets the start arrow for pEdge.
	 * @param pEdge the edge of interest
	 * @return the start arrow for pEdge
	 * @pre pEdge !=null
	 */
	private static ArrowHead getArrowStart(Edge pEdge)
	{
		assert pEdge !=null;
		if (EdgePriority.priorityOf(pEdge)==EdgePriority.AGGREGATION)
		{
			return ArrowHead.DIAMOND;
		}
		else if (EdgePriority.priorityOf(pEdge)==EdgePriority.COMPOSITION)
		{
			return ArrowHead.BLACK_DIAMOND;
		}
		else
		{
			return ArrowHead.NONE;
		}
	}
	
	/**
	 * Gets the end arrow for pEdge.
	 * @param pEdge the edge of interest
	 * @return the end arrow for pEdge
	 * @pre pEdge !=null
	 */
	private static ArrowHead getArrowEnd(Edge pEdge)
	{
		
		assert pEdge !=null;
		if (EdgePriority.priorityOf(pEdge)==EdgePriority.IMPLEMENTATION ||
			 EdgePriority.priorityOf(pEdge)==EdgePriority.INHERITANCE )
		{
			return ArrowHead.TRIANGLE;
		}
		else if (EdgePriority.priorityOf(pEdge)==EdgePriority.DEPENDENCY )
		{
			return ArrowHead.V;
		}
		else {
			return ArrowHead.NONE;
		}
	}
	

	/**
	 * Uses stored information about the layout of pEdge to draw it.
	 * @param pEdge the edge to be drawn
	 * @param pGraphics the graphics context
	 * @pre pEdge !=null && pEdgeStorage != null
	 */
	public void drawFromStorage(Edge pEdge, GraphicsContext pGraphics, EdgeStorage pEdgeStorage) 
	{
		assert pEdge !=null && pEdgeStorage != null;
		//get the EdgePath of pEdge and convert it into a Point2D[] array
		Point2D[] points = Conversions.toPoint2DArray(pEdgeStorage.getEdgePath(pEdge));
		ToolGraphics.strokeSharpPath(pGraphics, getSegmentPath(pEdge, pEdgeStorage), getLineStyle(pEdge));
		getArrowStart(pEdge).view().draw(pGraphics, Conversions.toPoint(points[1]), Conversions.toPoint(points[0]));
		getArrowEnd(pEdge).view().draw(pGraphics, Conversions.toPoint(points[points.length - 2]), 
				Conversions.toPoint(points[points.length - 1]));
		drawString(pGraphics, points[1], points[0], getArrowStart(pEdge), getStartLabel(pEdge), false, isStepUp(pEdge));
		drawString(pGraphics, points[points.length / 2 - 1], points[points.length / 2], null, getMiddleLabel(pEdge), true, isStepUp(pEdge));
		drawString(pGraphics, points[points.length - 2], points[points.length - 1], 
				getArrowEnd(pEdge), getEndLabel(pEdge), false, isStepUp(pEdge));
	}
	
	/**
	 * Gets the segment path for pEdge.
	 * @param pEdge the edge of interest
	 * @param pEdgeStorage the edge storage
	 * @return the Path for pEdge according to pEdgeStorage
	 * @pre pEdge !=null && pEdgeStorage != null
	 */
	private Path getSegmentPath(Edge pEdge, EdgeStorage pEdgeStorage)
	{
		assert pEdge !=null && pEdgeStorage != null;
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
	
	/**
	 * Returns whether an edge is segmented and is a step up. 
	 * @param pEdge
	 * @return
	 */
	private boolean isStepUp(Edge pEdge) 
	{
		Point point1 = EdgeViewerRegistry.getConnectionPoints(pEdge).getPoint1();
		Point point2 = EdgeViewerRegistry.getConnectionPoints(pEdge).getPoint2();
		return point1.getX() < point2.getX() && point1.getY() > point2.getY() || 
				point1.getX() > point2.getX() && point1.getY() < point2.getY();
	}
	

	/**
	 * Draws a string.
	 * @param pGraphics the graphics context
	 * @param pEndPoint1 an endpoint of the segment along which to draw the string
	 * @param pEndPoint2 the other endpoint of the segment along which to draw the string
	 * @param pString the string to draw 
	 * @param pCenter true if the string should be centered along the segment
	 */
	private void drawString(GraphicsContext pGraphics, Point2D pEndPoint1, Point2D pEndPoint2, 
			ArrowHead pArrowHead, String pString, boolean pCenter, boolean pIsStepUp)
	{
		if (pString == null || pString.length() == 0)
		{
			return;
		}
		String label = wrapLabel(pString, pEndPoint1, pEndPoint2);
		Rectangle bounds = getStringBounds(pEndPoint1, pEndPoint2, pArrowHead, label, pCenter, pIsStepUp);
		if(pCenter) 
		{
			if ( pEndPoint2.getY() >= pEndPoint1.getY() )
			{
				TOP_CENTERED_STRING_VIEWER.draw(label, pGraphics, bounds);
			}
			else
			{
				BOTTOM_CENTERED_STRING_VIEWER.draw(label, pGraphics, bounds);
			}
		}
		else
		{
			LEFT_JUSTIFIED_STRING_VIEWER.draw(label, pGraphics, bounds);
		}
	}
	
	private String wrapLabel(String pString, Point2D pEndPoint1, Point2D pEndPoint2) 
	{
		int distanceInX = (int)Math.abs(pEndPoint1.getX() - pEndPoint2.getX());
		int distanceInY = (int)Math.abs(pEndPoint1.getY() - pEndPoint2.getY());
		int lineLength = MAX_LENGTH_FOR_NORMAL_FONT;
		double distanceInXPerChar = distanceInX / singleCharWidth;
		double distanceInYPerChar = distanceInY / singleCharHeight;
		if (distanceInX > 0)
		{
			double angleInDegrees = Math.toDegrees(Math.atan(distanceInYPerChar/distanceInXPerChar));
			lineLength = Math.max(MAX_LENGTH_FOR_NORMAL_FONT, (int)((distanceInX / 4) * (1 - angleInDegrees / DEGREES_180)));
		}
		return LEFT_JUSTIFIED_STRING_VIEWER.wrapString(pString, lineLength);
		
	}
	
	/*
	 * Computes the extent of a string that is drawn along a line segment.
	 * @param p an endpoint of the segment along which to draw the string
	 * @param q the other endpoint of the segment along which to draw the string
	 * @param s the string to draw
	 * @param center true if the string should be centered along the segment
	 * @return the rectangle enclosing the string
	 */
	private static Rectangle getStringBounds(Point2D pEndPoint1, Point2D pEndPoint2, 
			ArrowHead pArrow, String pString, boolean pCenter, boolean pIsStepUp)
	{
		if (pString == null || pString.isEmpty())
		{
			return new Rectangle((int)Math.round(pEndPoint2.getX()), 
					(int)Math.round(pEndPoint2.getY()), 0, 0);
		}
		
		Dimension textDimensions = TOP_CENTERED_STRING_VIEWER.getDimension(pString);
		Rectangle stringDimensions = new Rectangle(0, 0, textDimensions.width(), textDimensions.height());
		Point2D a = getAttachmentPoint(pEndPoint1, pEndPoint2, pArrow, stringDimensions, pCenter, pIsStepUp);
		return new Rectangle((int)Math.round(a.getX()), (int)Math.round(a.getY()),
				Math.round(stringDimensions.getWidth()), Math.round(stringDimensions.getHeight()));
	}
	
	
	/**
	 * Computes the attachment point for drawing a string.
	 * @param pEndPoint1 an endpoint of the segment along which to draw the string
	 * @param pEndPoint2 the other endpoint of the segment along which to draw the string
	 * @param b the bounds of the string to draw
	 * @param pCenter true if the string should be centered along the segment
	 * @return the point at which to draw the string
	 */
	private static Point2D getAttachmentPoint(Point2D pEndPoint1, Point2D pEndPoint2, 
			ArrowHead pArrow, Rectangle pDimension, boolean pCenter, boolean pIsStepUp)
	{    
		final int gap = 3;
		double xoff = gap;
		double yoff = -gap - pDimension.getHeight();
		Point2D attach = pEndPoint2;
		if (pCenter)
		{
			if (pEndPoint1.getX() > pEndPoint2.getX()) 
			{ 
				return getAttachmentPoint(pEndPoint2, pEndPoint1, pArrow, pDimension, pCenter, pIsStepUp); 
			}
			attach = new Point2D((pEndPoint1.getX() + pEndPoint2.getX()) / 2, 
					(pEndPoint1.getY() + pEndPoint2.getY()) / 2);
			if (pEndPoint1.getX() == pEndPoint2.getX() && pIsStepUp)
			{
				yoff = gap;
			}
			else if (pEndPoint1.getX() == pEndPoint2.getX() && !pIsStepUp)
			{
				yoff =  -gap-pDimension.getHeight();
			}
			else if (pEndPoint1.getY() == pEndPoint2.getY())
			{
				if (pDimension.getWidth() > Math.abs(pEndPoint1.getX() - pEndPoint2.getX()))
				{
					attach = new Point2D(pEndPoint2.getX() + (pDimension.getWidth() / 2) + gap, 
							(pEndPoint1.getY() + pEndPoint2.getY()) / 2);
				}
				xoff = -pDimension.getWidth() / 2;
			}
		}
		else 
		{
			if(pEndPoint1.getX() < pEndPoint2.getX())
			{
				xoff = -gap - pDimension.getWidth();
			}
			if(pEndPoint1.getY() > pEndPoint2.getY())
			{
				yoff = gap;
			}
			if(pArrow != null && pArrow != ArrowHead.NONE)
			{
				Bounds arrowBounds = pArrow.view().getPath(
						Conversions.toPoint(pEndPoint1), 
						Conversions.toPoint(pEndPoint2)).getBoundsInLocal();
				if(pEndPoint1.getY() == pEndPoint2.getY())
				{
					yoff -= arrowBounds.getHeight() / 2;
				}
				else if(pEndPoint1.getX() == pEndPoint2.getX())
				{
					xoff += arrowBounds.getWidth() / 2;
				}
			}
		}
		return new Point2D(attach.getX() + xoff, attach.getY() + yoff);
	}
	
	/**
	 * Gets the start label for pEdge.
	 * @param pEdge the edge of interest
	 * @return the string start label for pEdge
	 * @pre pEdge != null
	 */
	private String getStartLabel(Edge pEdge)
	{
		assert pEdge !=null;
		if (pEdge instanceof ThreeLabelEdge)
		{
			ThreeLabelEdge threeLabelEdge = (ThreeLabelEdge) pEdge;
			return threeLabelEdge.getStartLabel();
		}
		else
		{
			return "";
		}
	}
	
	/**
	 * Gets the middle label for pEdge.
	 * @param pEdge the edge of interest
	 * @return the String middle label for pEdge
	 * @pre pEdge != null
	 */
	private String getMiddleLabel(Edge pEdge)
	{
		assert pEdge !=null;
		if (pEdge instanceof ThreeLabelEdge)
		{
			ThreeLabelEdge threeLabelEdge = (ThreeLabelEdge) pEdge;
			return threeLabelEdge.getMiddleLabel();
		}
		else if (pEdge instanceof SingleLabelEdge)
		{
			SingleLabelEdge singleLabelEdge = (SingleLabelEdge) pEdge;
			return singleLabelEdge.getMiddleLabel();
		}
		else
		{
			return "";
		}
	}
	
	/**
	 * Gets the end label for pEdge.
	 * @param pEdge the edge of interest
	 * @return the String end label for pEdge
	 * @pre pEdge != null
	 */
	private String getEndLabel(Edge pEdge)
	{
		assert pEdge !=null;
		if (pEdge instanceof ThreeLabelEdge)
		{
			ThreeLabelEdge threeLabelEdge = (ThreeLabelEdge) pEdge;
			return threeLabelEdge.getEndLabel();
		}
		else
		{
			return "";
		}
	}

	

}

	



