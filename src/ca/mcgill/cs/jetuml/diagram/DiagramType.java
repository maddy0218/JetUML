/*******************************************************************************
 * JetUML - A desktop application for fast UML diagramming.
 *
 * Copyright (C) 2016, 2019 by the contributors of the JetUML project.
 *
 * See: https://github.com/prmr/JetUML
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package ca.mcgill.cs.jetuml.diagram;

import static ca.mcgill.cs.jetuml.application.ApplicationResources.RESOURCES;

import ca.mcgill.cs.jetuml.diagram.builder.ClassDiagramBuilder;
import ca.mcgill.cs.jetuml.diagram.builder.DiagramBuilder;
import ca.mcgill.cs.jetuml.diagram.builder.ObjectDiagramBuilder;
import ca.mcgill.cs.jetuml.diagram.builder.SequenceDiagramBuilder;
import ca.mcgill.cs.jetuml.diagram.builder.StateDiagramBuilder;
import ca.mcgill.cs.jetuml.diagram.builder.UseCaseDiagramBuilder;
import ca.mcgill.cs.jetuml.diagram.edges.AggregationEdge;
import ca.mcgill.cs.jetuml.diagram.edges.AssociationEdge;
import ca.mcgill.cs.jetuml.diagram.edges.CallEdge;
import ca.mcgill.cs.jetuml.diagram.edges.DependencyEdge;
import ca.mcgill.cs.jetuml.diagram.edges.GeneralizationEdge;
import ca.mcgill.cs.jetuml.diagram.edges.NoteEdge;
import ca.mcgill.cs.jetuml.diagram.edges.ObjectCollaborationEdge;
import ca.mcgill.cs.jetuml.diagram.edges.ObjectReferenceEdge;
import ca.mcgill.cs.jetuml.diagram.edges.ReturnEdge;
import ca.mcgill.cs.jetuml.diagram.edges.StateTransitionEdge;
import ca.mcgill.cs.jetuml.diagram.edges.UseCaseAssociationEdge;
import ca.mcgill.cs.jetuml.diagram.edges.UseCaseDependencyEdge;
import ca.mcgill.cs.jetuml.diagram.edges.UseCaseGeneralizationEdge;
import ca.mcgill.cs.jetuml.diagram.nodes.ActorNode;
import ca.mcgill.cs.jetuml.diagram.nodes.ClassNode;
import ca.mcgill.cs.jetuml.diagram.nodes.FieldNode;
import ca.mcgill.cs.jetuml.diagram.nodes.FinalStateNode;
import ca.mcgill.cs.jetuml.diagram.nodes.ImplicitParameterNode;
import ca.mcgill.cs.jetuml.diagram.nodes.InitialStateNode;
import ca.mcgill.cs.jetuml.diagram.nodes.InterfaceNode;
import ca.mcgill.cs.jetuml.diagram.nodes.NoteNode;
import ca.mcgill.cs.jetuml.diagram.nodes.ObjectNode;
import ca.mcgill.cs.jetuml.diagram.nodes.PackageNode;
import ca.mcgill.cs.jetuml.diagram.nodes.StateNode;
import ca.mcgill.cs.jetuml.diagram.nodes.UseCaseNode;
import ca.mcgill.cs.jetuml.views.DiagramViewer;
import ca.mcgill.cs.jetuml.views.SequenceDiagramViewer;

/**
 * The different types of UML diagrams supported by 
 * this application.
 */
public enum DiagramType
{
	CLASS(
			ClassDiagram.class, 
			ClassDiagramBuilder.class, 
			new DiagramViewer(), 
			RESOURCES.getString("classdiagram.file.extension"),
			RESOURCES.getString("classdiagram.file.name"),
			new Node [] { new ClassNode(), new InterfaceNode(), new PackageNode(), new NoteNode()},
			new Edge[] {
					new DependencyEdge(), 
					new GeneralizationEdge(), 
					new GeneralizationEdge(GeneralizationEdge.Type.Implementation),
					new AssociationEdge(),
					new AggregationEdge(),
					new AggregationEdge(AggregationEdge.Type.Composition),
					new NoteEdge()}), 
	
	SEQUENCE(
			SequenceDiagram.class, 
			SequenceDiagramBuilder.class, 
			new SequenceDiagramViewer(),
			RESOURCES.getString("sequencediagram.file.extension"),
			RESOURCES.getString("sequencediagram.file.name"),
			new Node[]{new ImplicitParameterNode(), new NoteNode()},
			new Edge[]{new CallEdge(), new ReturnEdge(), new NoteEdge()}), 
	
	STATE(
			StateDiagram.class, 
			StateDiagramBuilder.class, 
			new DiagramViewer(),
			RESOURCES.getString("statediagram.file.extension"),
			RESOURCES.getString("statediagram.file.name"),
			new Node[]{new StateNode(), new InitialStateNode(), new FinalStateNode(), new NoteNode()},
			new Edge[]{new StateTransitionEdge(), new NoteEdge()}), 
	
	OBJECT(
			ObjectDiagram.class, 
			ObjectDiagramBuilder.class, 
			new DiagramViewer(),
			RESOURCES.getString("objectdiagram.file.extension"),
			RESOURCES.getString("objectdiagram.file.name"),
			new Node[] {new ObjectNode(), new FieldNode(), new NoteNode()},
			new Edge[] {new ObjectReferenceEdge(), new ObjectCollaborationEdge(), new NoteEdge() }), 
	
	USECASE(
			UseCaseDiagram.class, 
			UseCaseDiagramBuilder.class, 
			new DiagramViewer(),
			RESOURCES.getString("usecasediagram.file.extension"),
			RESOURCES.getString("usecasediagram.file.name"),
			new Node[]{new ActorNode(), new UseCaseNode(), new NoteNode()},
			new Edge[]{ new UseCaseAssociationEdge(),
					new UseCaseDependencyEdge(UseCaseDependencyEdge.Type.Extend),
					new UseCaseDependencyEdge(UseCaseDependencyEdge.Type.Include),
					new UseCaseGeneralizationEdge(),
					new NoteEdge()});
	
	private final Class<?> aClass;
	private final Class<?> aBuilderClass;
	private final DiagramViewer aViewer;
	private final String aFileExtension;
	private final String aDescription;
	private final Node[] aNodePrototypes;
	private final Edge[] aEdgePrototypes;
	
	DiagramType(Class<?> pClass, Class<?> pBuilderClass, DiagramViewer pViewer, 
			String pFileExtension, String pDescription, Node[] pNodePrototypes, Edge[] pEdgePrototypes)
	{
		aClass = pClass;
		aBuilderClass = pBuilderClass;
		aViewer = pViewer;
		aFileExtension = pFileExtension;
		aDescription = pDescription;
		aNodePrototypes = pNodePrototypes;
		aEdgePrototypes = pEdgePrototypes;
	}
	
	/**
	 * @return The file extension for this type of diagram.
	 */
	public String getFileExtension()
	{
		return aFileExtension;
	}
	
	/**
	 * @return A short description of the diagram type.
	 */
	public String getDescription()
	{
		return aDescription;
	}
	
	/**
	 * Gets the node types of a particular diagram type.
	 * @return An array of node prototypes
	 */   
	public Node[] getNodePrototypes()
	{
		return aNodePrototypes;
	}
	
	/**
	 * Gets the edge types of a particular diagram type.
	 * @return an array of edge prototypes
	 */ 
	public Edge[] getEdgePrototypes()
	{
		return aEdgePrototypes;
	}
	
	/**
	 * @param pDiagram The diagram whose type we want to check.
	 * @return The type of pDiagram.
	 * @pre pDiagram != null
	 */
	public static DiagramType typeOf(Diagram pDiagram)
	{
		assert pDiagram != null;
		for( DiagramType type : values())
		{
			if( pDiagram.getClass() == type.aClass )
			{
				return type;
			}
		}
		assert false;
		return null;
	}
	
	/**
	 * @return A new instance of the diagram type that corresponds to this value.
	 */
	public Diagram newInstance()
	{
		try
		{
			return (Diagram) aClass.getDeclaredConstructor().newInstance();
		}
		catch(ReflectiveOperationException exception)
		{
			assert false;
			return null;
		}
	}
	
	/**
	 * @param pDiagram The diagram for which we want to build a builder.
	 * @return A new instance of a builder for this diagram type.
	 * @pre pDiagram != null
	 */
	public static DiagramBuilder newBuilderInstanceFor(Diagram pDiagram)
	{
		assert pDiagram != null;
		try
		{
			return (DiagramBuilder) typeOf(pDiagram).aBuilderClass.getDeclaredConstructor(Diagram.class).newInstance(pDiagram);
		}
		catch(ReflectiveOperationException exception)
		{
			assert false;
			return null;
		}
	}
	
	/**
	 * @return The name of the handler, which is the simple name of the corresponding
	 * class in all lower case.
	 */
	public String getName()
	{
		return aClass.getSimpleName().toLowerCase();
	}
	
	/**
	 * @param pDiagram The diagram for which we want a viewer.
	 * @return The DiagramViewer instance registered for this type of diagram.
	 * @pre pDiagram != null;
	 */
	public static DiagramViewer viewerFor(Diagram pDiagram) 
	{
		assert pDiagram != null;
		return typeOf(pDiagram).aViewer;
	}
}
