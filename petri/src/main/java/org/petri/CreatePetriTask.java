package org.petri;

import java.awt.Color;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CreatePetriTask extends AbstractTask {

	private final CyNetworkManager netMgr;
	private final CyNetworkNaming namingUtil;
	private final CyNetworkViewFactory cnvf;
	private final CyNetworkViewManager cnvm;
	@Tunable(description="Choose a file", params="input=true")
	public File xmlfile; // Ask file for creating Petri Net
	private final CyEventHelper eventHelper;
	private final CyLayoutAlgorithmManager calm ;
	private final SynchronousTaskManager<?> synctm;
	private final VisualMappingManager vmm;
	private final VisualMappingFunctionFactory vmffd;
	private final VisualMappingFunctionFactory vmffp;
	private final CyNetwork petriNet;
	private static final Logger LOGGER = Logger.getLogger(CreatePetriTask.class);
	
	public CreatePetriTask(final CyNetworkManager netMgr, final CyNetworkNaming namingUtil,
			final CyNetworkViewFactory cnvf, final CyNetworkViewManager cnvm,
			final CyEventHelper eventHelper, CyLayoutAlgorithmManager calm, SynchronousTaskManager<?> synctm,
			VisualMappingManager vmm, VisualMappingFunctionFactory vmffd,
			VisualMappingFunctionFactory vmffp, CyNetwork petriNet){
		this.netMgr = netMgr;
		this.namingUtil = namingUtil;
		this.cnvf = cnvf;
		this.cnvm = cnvm;
		this.eventHelper = eventHelper;
		this.calm = calm;
		this.synctm = synctm;
		this.vmm = vmm;
		this.vmffd = vmffd;
		this.vmffp = vmffp;
		this.petriNet = petriNet;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void run(TaskMonitor monitor) throws Exception {
		//Create Petri Net
		petriNet.getRow(petriNet).set(CyNetwork.NAME,
			      namingUtil.getSuggestedNetworkTitle("Petri Net"));
		//Reading from Document
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(xmlfile);
		LOGGER.info("Opened Petri Net XML. Now Generating Nodes for Places");
		//Generating Nodes for Places
		NodeList listOfPlaces = doc.getElementsByTagName("species");
		CyNode [] cyPlaceArray = new CyNode[listOfPlaces.getLength()];
		petriNet.getDefaultNodeTable().createColumn("id", String.class, true);
		petriNet.getDefaultNodeTable().createColumn("tokens", Integer.class, false);
		petriNet.getDefaultNodeTable().createColumn("initial tokens", Integer.class, true);
		petriNet.getDefaultNodeTable().createColumn("type", String.class, true);
		petriNet.getDefaultNodeTable().createColumn("fired", Integer.class, false);
		int maxAmount = 0;
		for (int i = 0; i<listOfPlaces.getLength(); i++) {
			cyPlaceArray[i] = petriNet.addNode();
			Element element = (Element) listOfPlaces.item(i);
			petriNet.getDefaultNodeTable().getRow(cyPlaceArray[i].getSUID()).set("id", element.getAttribute("id"));
			petriNet.getDefaultNodeTable().getRow(cyPlaceArray[i].getSUID()).set("tokens", Integer.parseInt(element.getAttribute("initialAmount")));
			petriNet.getDefaultNodeTable().getRow(cyPlaceArray[i].getSUID()).set("initial tokens", Integer.parseInt(element.getAttribute("initialAmount")));
			petriNet.getDefaultNodeTable().getRow(cyPlaceArray[i].getSUID()).set("type", "Place");
			if (element.getAttribute("name") != null) {
				petriNet.getDefaultNodeTable().getRow(cyPlaceArray[i].getSUID()).set("name", element.getAttribute("name"));
			}
			if (Integer.parseInt(element.getAttribute("initialAmount"))> maxAmount) {
				maxAmount = Integer.parseInt(element.getAttribute("initialAmount"));
			}
		}
	    eventHelper.flushPayloadEvents();
		LOGGER.info("Nodes for Places successfully generated. Now generating Nodes for Transitions and Edges");

		NodeList listOfTransitions = doc.getElementsByTagName("reaction");
		CyNode [] cyTransitionArray = new CyNode[listOfTransitions.getLength()];
		CyEdge [] cyEdgeArray = new CyEdge[listOfPlaces.getLength()*listOfTransitions.getLength()];
		int numOfEdges = 0;
		petriNet.getDefaultEdgeTable().createColumn("weight", Integer.class, true);
		for (int i = 0; i<listOfTransitions.getLength(); i++) {
			cyTransitionArray[i] = petriNet.addNode();
			Element element = (Element) listOfTransitions.item(i);
			String id =  element.getAttribute("id");
			petriNet.getDefaultNodeTable().getRow(cyTransitionArray[i].getSUID()).set("id", id);
			petriNet.getDefaultNodeTable().getRow(cyTransitionArray[i].getSUID()).set("type", "Transition");
			petriNet.getDefaultNodeTable().getRow(cyTransitionArray[i].getSUID()).set("fired", 0);
			if (element.getAttribute("name") != null) {
				petriNet.getDefaultNodeTable().getRow(cyTransitionArray[i].getSUID()).set("name", element.getAttribute("name"));
			}
			NodeList children = element.getChildNodes();
			LOGGER.info("Generating Edges for Transition");
			
			for (int index = 0; index<children.getLength(); index++) {
				LOGGER.info("Casting Child of Transition");
				String nname = children.item(index).getNodeName();
				if (nname.equals("listOfReactants")) {
					NodeList reactants = children.item(index).getChildNodes();
					for (int reactIndex = 0; reactIndex<reactants.getLength(); reactIndex++) {
						LOGGER.info("Casting reactant");
						if (reactants.item(reactIndex).getNodeType() == Node.ELEMENT_NODE) {
							Element reactant = (Element) reactants.item(reactIndex);
							for (int placeIndex = 0; placeIndex<listOfPlaces.getLength(); placeIndex++) {
								if (petriNet.getDefaultNodeTable().getRow(cyPlaceArray[placeIndex].getSUID()).get("id", String.class).equals(reactant.getAttribute("species"))) {
									cyEdgeArray[numOfEdges] = petriNet.addEdge(cyPlaceArray[placeIndex], cyTransitionArray[i], true);
									petriNet.getDefaultEdgeTable().getRow(cyEdgeArray[numOfEdges].getSUID()).set("weight", Integer.parseInt(reactant.getAttribute("stoichiometry")));
									petriNet.getDefaultEdgeTable().getRow(cyEdgeArray[numOfEdges].getSUID()).set("name", reactant.getAttribute("species")+"->"+id);
									numOfEdges++;
									break;
								}
							}
						}
					}
				}
				else if (nname.equals("listOfProducts")) {
					NodeList products = children.item(index).getChildNodes();
					for (int prodIndex = 0; prodIndex<products.getLength(); prodIndex++) {
						LOGGER.info("Casting product");
						if (products.item(prodIndex).getNodeType() == Node.ELEMENT_NODE) {
							Element product = (Element) products.item(prodIndex);
							for (int placeIndex = 0; placeIndex<listOfPlaces.getLength(); placeIndex++) {
								if (petriNet.getDefaultNodeTable().getRow(cyPlaceArray[placeIndex].getSUID()).get("id", String.class).equals(product.getAttribute("species"))) {
									cyEdgeArray[numOfEdges] = petriNet.addEdge(cyTransitionArray[i], cyPlaceArray[placeIndex], true);
									petriNet.getDefaultEdgeTable().getRow(cyEdgeArray[numOfEdges].getSUID()).set("weight", Integer.parseInt(product.getAttribute("stoichiometry")));
									petriNet.getDefaultEdgeTable().getRow(cyEdgeArray[numOfEdges].getSUID()).set("name", id+"->"+product.getAttribute("species"));
									numOfEdges++;
									break;
								}
							}	
						}
					}
				}
			}
		}
		eventHelper.flushPayloadEvents();
		CyNetworkView cnv = cnvf.createNetworkView(petriNet);
		Set <View<CyNode>> nodeviews = new HashSet<View<CyNode>>();
		Set <View<CyNode>> placeviews = new HashSet<View<CyNode>>();
		Set <View<CyNode>> transitionviews = new HashSet<View<CyNode>>();
		for (int i = 0; i<cyPlaceArray.length; i++) {
			View<CyNode> nodeview = cnv.getNodeView(cyPlaceArray[i]);
			placeviews.add(nodeview);
		}
		for (int i=0; i<cyTransitionArray.length; i++) {
			View<CyNode> nodeview = cnv.getNodeView(cyTransitionArray[i]);
			transitionviews.add(nodeview);
		}
		for (int i=0; i<numOfEdges; i++) {
			View<CyEdge> edgeview = cnv.getEdgeView(cyEdgeArray[i]);
			edgeview.setLockedValue(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.ARROW);
		}
		nodeviews.addAll(transitionviews);
		nodeviews.addAll(placeviews);
		cnvm.addNetworkView(cnv);
		VisualStyle vs = vmm.getVisualStyle(cnv);
		vs.setDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT, Color.BLACK);
		vs.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 1.0);
		DiscreteMapping shapeMap = (DiscreteMapping) vmffd.createVisualMappingFunction("type", String.class, BasicVisualLexicon.NODE_SHAPE);
		shapeMap.putMapValue("Transition", NodeShapeVisualProperty.RECTANGLE);
		shapeMap.putMapValue("Place", NodeShapeVisualProperty.ELLIPSE);
		vs.addVisualMappingFunction(shapeMap);
		PassthroughMapping<String, ?> nameMap = (PassthroughMapping<String, ?>)
				vmffp.createVisualMappingFunction("id", String.class, BasicVisualLexicon.NODE_LABEL);
		vs.addVisualMappingFunction(nameMap);
		for (View<CyNode> v : transitionviews) {
			v.setLockedValue(BasicVisualLexicon.NODE_WIDTH, 35.0);
			v.setLockedValue(BasicVisualLexicon.NODE_FILL_COLOR, Color.WHITE);
		}
		for (View<CyNode> v : placeviews) {
		v.setLockedValue(BasicVisualLexicon.NODE_WIDTH, 35.0);
		}
		CyLayoutAlgorithm def = calm.getDefaultLayout();
		TaskIterator itr = def.createTaskIterator(cnv, def.getDefaultLayoutContext(), nodeviews, null);
		synctm.execute(itr);
		cnv.updateView();
		boolean destroyNetwork = false;
		if (destroyNetwork) {
			netMgr.destroyNetwork(petriNet);
		}
	}
}
