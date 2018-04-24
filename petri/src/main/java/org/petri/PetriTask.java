package org.petri;

import java.awt.Color;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.app.CyAppAdapter;
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
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

/**
 * App for Cytoscape to support Petri Nets
 * @author M. Gehrmann, M. Kirchner
 *
 */
public class PetriTask extends AbstractTask {

	private final CyNetworkManager netMgr;
	private final CyNetworkNaming namingUtil;
	private final CyNetworkViewFactory cnvf;
	private final CyNetworkViewManager cnvm;
	@Tunable(description="Choose a file", params="input=true")
	public File inpFile; // Ask file for creating Petri Net
	private final CyEventHelper eventHelper;
	private final CyLayoutAlgorithmManager calm ;
	private final CyAppAdapter adapter;
	private final VisualMappingManager vmm;
	private final VisualMappingFunctionFactory vmffd;
	private final CyNetwork petriNet;
	
	/**
	 * Constructor
	 * @param netMgr
	 * @param namingUtil
	 * @param cnvf
	 * @param cnvm
	 * @param eventHelper
	 * @param calm
	 * @param adapter
	 * @param vmm
	 * @param vmffd
	 * @param petriNet
	 */
	public PetriTask(final CyNetworkManager netMgr, final CyNetworkNaming namingUtil,
			final CyNetworkViewFactory cnvf, final CyNetworkViewManager cnvm,
			final CyEventHelper eventHelper, CyLayoutAlgorithmManager calm, CyAppAdapter adapter,
			VisualMappingManager vmm, VisualMappingFunctionFactory vmffd, CyNetwork petriNet){
		this.netMgr = netMgr;
		this.namingUtil = namingUtil;
		this.cnvf = cnvf;
		this.cnvm = cnvm;
		this.eventHelper = eventHelper;
		this.calm = calm;
		this.adapter = adapter;
		this.vmm = vmm;
		this.vmffd = vmffd;
		this.petriNet = petriNet;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	/**
	 * Fill Petri Net with Nodes and Edges from an input file.
	 * Depending on format of file, a corresponding reading function
	 * will be called from org.petri.FileUtils.
	 * Afterwards apply default visual style and layout for Petri Nets.
	 */
	public void run(TaskMonitor monitor) throws Exception {
		petriNet.getRow(petriNet).set(CyNetwork.NAME,
			      namingUtil.getSuggestedNetworkTitle("Petri Net"));
		String fileName = inpFile.getName(); 	// Get extension of input file
		String extension = "";
		int dot = fileName.lastIndexOf('.');
		if (dot > 0) {
		    extension = fileName.substring(dot+1);
		    FileUtils fileUtils = new FileUtils(inpFile);
		    fileUtils.choose(extension, petriNet);	// Choose reading method based on extension
		}
		else {
			throw new Exception("Could not find extension"); // No extension could be found (no "." in fileName)
		}
		int places = 0; int transitions = 0;	// Number of places and transitions, used for array size
		for (CyNode n : petriNet.getNodeList()) {
			String ntype = (String) (petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("type", String.class));
			if (ntype.equals("Place")) {
				places++;	
			}
			else if (ntype.equals("Transition")) {
				transitions++;
			}
		}
		CyNode[] cyPlaceArray = new CyNode[places];
		CyNode[] cyTransitionArray = new CyNode[transitions];
		places = 0; transitions = 0;	// Reset for filling arrays
		for (CyNode n : petriNet.getNodeList()) {
			String ntype = (String) (petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("type", String.class));
			if (ntype.equals("Place")) {
				cyPlaceArray[places] = n;
				places++;
			}
			else if (ntype.equals("Transition")) {
				cyTransitionArray[transitions] = n;
				transitions++;
			}
		}
		
		eventHelper.flushPayloadEvents();
		CyNetworkView cnv = cnvf.createNetworkView(petriNet);		// Setting up view
		Set <View<CyNode>> nodeviews = new HashSet<View<CyNode>>();	// Used for layout
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
		CyEdge [] cyEdgeArray = new CyEdge[petriNet.getEdgeCount()];		//Generate views for edges
		petriNet.getEdgeList().toArray(cyEdgeArray);
		for (int i=0; i<petriNet.getEdgeCount(); i++) {
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
		shapeMap.putMapValue("Transition", NodeShapeVisualProperty.RECTANGLE); // Transitions are squares, places are circles
		shapeMap.putMapValue("Place", NodeShapeVisualProperty.ELLIPSE);
		vs.addVisualMappingFunction(shapeMap);
		
		for (View<CyNode> v : transitionviews) {	// Views for transitions, always white on startup
			v.setLockedValue(BasicVisualLexicon.NODE_WIDTH, 35.0);
			v.setLockedValue(BasicVisualLexicon.NODE_FILL_COLOR, Color.WHITE);
		}
		
		for (View<CyNode> v : placeviews) {			// Views for places, label always contains amount of tokens
		v.setLockedValue(BasicVisualLexicon.NODE_WIDTH, 35.0);
		v.setLockedValue(BasicVisualLexicon.NODE_FILL_COLOR, Color.RED);
		v.setLockedValue(BasicVisualLexicon.NODE_LABEL,
				petriNet.getDefaultNodeTable().getRow(v.getModel().getSUID()).get("name", String.class)+"\n"
				+ Integer.toString(petriNet.getDefaultNodeTable().getRow(v.getModel().getSUID()).get("tokens", Integer.class)));
		}
		
		CyLayoutAlgorithm def = calm.getDefaultLayout(); // Apply default layout
		TaskIterator itr = def.createTaskIterator(cnv, def.getDefaultLayoutContext(), nodeviews, null);
		adapter.getTaskManager().execute(itr);
		SynchronousTaskManager<?> synTaskMan = adapter.getCyServiceRegistrar().getService(SynchronousTaskManager.class);
		synTaskMan.execute(itr);
		cnv.updateView();

		boolean destroyNetwork = false;
		if (destroyNetwork) {
			netMgr.destroyNetwork(petriNet);
		}
	}
}
