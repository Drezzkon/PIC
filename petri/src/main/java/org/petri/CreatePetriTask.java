package org.petri;

import java.awt.Color;
import java.awt.Paint;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
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
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.event.CyEventHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CreatePetriTask extends AbstractTask {

	private final CyNetworkManager netMgr;
	private final CyNetworkFactory cnf;
	private final CyNetworkNaming namingUtil;
	private final CyNetworkViewFactory cnvf;
	private final CyNetworkViewManager cnvm;
	@Tunable(description="Choose a file", params="input=true")
	public File xmlfile; // Ask file for creating Petri Net
	private final CyEventHelper eventHelper;
	private final CyLayoutAlgorithmManager calm ;
	private final SynchronousTaskManager<?> synctm;
	private final VisualMappingManager vmm;
	private final VisualMappingFunctionFactory vmffc;
	private final VisualMappingFunctionFactory vmffd;
	private final VisualMappingFunctionFactory vmffp;
	private static final Logger LOGGER = Logger.getLogger(CreatePetriTask.class);
	
	public CreatePetriTask(final CyNetworkManager netMgr, final CyNetworkNaming namingUtil,
			final CyNetworkFactory cnf, final CyNetworkViewFactory cnvf, final CyNetworkViewManager cnvm,
			final CyEventHelper eventHelper, CyLayoutAlgorithmManager calm, SynchronousTaskManager<?> synctm,
			VisualMappingManager vmm, VisualMappingFunctionFactory vmffc, VisualMappingFunctionFactory vmffd,
			VisualMappingFunctionFactory vmffp){
		this.netMgr = netMgr;
		this.cnf = cnf;
		this.namingUtil = namingUtil;
		this.cnvf = cnvf;
		this.cnvm = cnvm;
		this.eventHelper = eventHelper;
		this.calm = calm;
		this.synctm = synctm;
		this.vmm = vmm;
		this.vmffc = vmffc;
		this.vmffd = vmffd;
		this.vmffp = vmffp;		
	}
	public void run(TaskMonitor monitor) throws Exception {
		//Create Petri Net
	    CyNetwork petriNet = cnf.createNetwork();
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
		petriNet.getDefaultNodeTable().createColumn("amount", Integer.class, false);
		petriNet.getDefaultNodeTable().createColumn("initial amount", Integer.class, true);
		petriNet.getDefaultNodeTable().createColumn("type", String.class, true);
		int maxAmount = 0;
		for (int i = 0; i<listOfPlaces.getLength(); i++) {
			cyPlaceArray[i] = petriNet.addNode();
			Element element = (Element) listOfPlaces.item(i);
			petriNet.getDefaultNodeTable().getRow(cyPlaceArray[i].getSUID()).set("name", element.getAttribute("id"));
			petriNet.getDefaultNodeTable().getRow(cyPlaceArray[i].getSUID()).set("amount", Integer.parseInt(element.getAttribute("initialAmount")));
			petriNet.getDefaultNodeTable().getRow(cyPlaceArray[i].getSUID()).set("initial amount", Integer.parseInt(element.getAttribute("initialAmount")));
			petriNet.getDefaultNodeTable().getRow(cyPlaceArray[i].getSUID()).set("type", "Place");
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
			petriNet.getDefaultNodeTable().getRow(cyTransitionArray[i].getSUID()).set("name", element.getAttribute("id"));
			petriNet.getDefaultNodeTable().getRow(cyTransitionArray[i].getSUID()).set("amount", -1);
			petriNet.getDefaultNodeTable().getRow(cyTransitionArray[i].getSUID()).set("type", "Transition");
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
								if (petriNet.getDefaultNodeTable().getRow(cyPlaceArray[placeIndex].getSUID()).get("name", String.class).equals(reactant.getAttribute("species"))) {
									cyEdgeArray[numOfEdges] = petriNet.addEdge(cyPlaceArray[placeIndex], cyTransitionArray[i], true);
									petriNet.getDefaultEdgeTable().getRow(cyEdgeArray[numOfEdges].getSUID()).set("weight", Integer.parseInt(reactant.getAttribute("stoichiometry")));
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
								if (petriNet.getDefaultNodeTable().getRow(cyPlaceArray[placeIndex].getSUID()).get("name", String.class).equals(product.getAttribute("species"))) {
									cyEdgeArray[numOfEdges] = petriNet.addEdge(cyTransitionArray[i], cyPlaceArray[placeIndex], true);
									petriNet.getDefaultEdgeTable().getRow(cyEdgeArray[numOfEdges].getSUID()).set("weight", Integer.parseInt(product.getAttribute("stoichiometry")));
									numOfEdges++;
									break;
								}
							}	
						}
					}
				}
			}
		}
		netMgr.addNetwork(petriNet);
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
		ContinuousMapping colorMap = (ContinuousMapping) vmffc.createVisualMappingFunction("amount", Integer.class,
				BasicVisualLexicon.NODE_FILL_COLOR);
		BoundaryRangeValues<Paint> brv1 = new BoundaryRangeValues<Paint>(new Color(255,255,255), new Color(255,191,191), new Color(255,127,127));
		BoundaryRangeValues<Paint> brv2 = new BoundaryRangeValues<Paint>(new Color(255,127,127), new Color(255,63,63), new Color(255,0,0));
		colorMap.addPoint(1, brv1);
		colorMap.addPoint(maxAmount-maxAmount/2, brv2);
		vs.addVisualMappingFunction(colorMap);
		vs.setDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT, Color.BLACK);
		vs.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 1.0);
		DiscreteMapping shapeMap = (DiscreteMapping) vmffd.createVisualMappingFunction("type", String.class, BasicVisualLexicon.NODE_SHAPE);
		shapeMap.putMapValue("Transition", NodeShapeVisualProperty.RECTANGLE);
		shapeMap.putMapValue("Place", NodeShapeVisualProperty.TRIANGLE);
		vs.addVisualMappingFunction(shapeMap);
		PassthroughMapping nameMap = (PassthroughMapping) vmffp.createVisualMappingFunction("name", String.class, BasicVisualLexicon.NODE_LABEL);
		vs.addVisualMappingFunction(nameMap);
		for (View<CyNode> v : transitionviews) {
			v.setLockedValue(BasicVisualLexicon.NODE_FILL_COLOR, Color.WHITE);
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