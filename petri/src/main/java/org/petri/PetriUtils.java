package org.petri;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
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
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;

/**
 * Utilites for managing Petri Nets, like updating views and firing
 * @author M. Gehrmann, M. Kirchner
 *
 */
public class PetriUtils {

	private CyNetwork petriNet;
	private CyNetworkViewManager cnvm;
	private CyNetworkViewFactory cnvf;
	private VisualMappingManager vmm;
	private CyLayoutAlgorithmManager clam;
	private CyAppAdapter adapter;
	private VisualMappingFunctionFactory vmffd;
	
	/**
	 * Constructor
	 * @param petriNet
	 * @param cnvm
	 */
	public PetriUtils(CyNetwork petriNet, CyNetworkViewManager cnvm, CyNetworkViewFactory cnvf, VisualMappingManager vmm,
			CyLayoutAlgorithmManager clam, CyAppAdapter adapter, VisualMappingFunctionFactory vmffd) {
		this.petriNet = petriNet;
		this.cnvm = cnvm;
		this.cnvf = cnvf;
		this.vmm = vmm;
		this.clam = clam;
		this.adapter = adapter;
		this.vmffd = vmffd;
	}

	/**
	 * Getter for all transitions among the nodes of petriNet
	 * @return CyNode[] containing all transitions
	 */
	public CyNode[] getTransitions() {
		int length = 0;
		for (CyNode n : petriNet.getNodeList()) {	// Get length of array
			String ntype = (String) (petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("type", String.class));
			if (ntype.equals("Transition")) {
				length++;				
			}
		}
		CyNode[] cyTransitionArray = new CyNode[length];
		length = 0;
		for (CyNode n : petriNet.getNodeList()) {	// Insert transitions into array
			String ntype = (String) (petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("type", String.class));
			if (ntype.equals("Transition")) {
				cyTransitionArray[length] = n;
				length++;
			}
		}
		return cyTransitionArray;
	}

	/**
	 * Getter for all places among the nodes of petriNet
	 * @return CyNode[] containing all places
	 */
	public CyNode[] getPlaces() {
		int length = 0;
		for (CyNode n : petriNet.getNodeList()) {	// Get length of array
			String ntype = (String) (petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("type", String.class));
			if (ntype.equals("Place")) {
				length++;				
			}
		}
		CyNode[] cyPlaceArray = new CyNode[length];
		length = 0;
		for (CyNode n : petriNet.getNodeList()) {
			String ntype = (String) (petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("type", String.class));
			if (ntype.equals("Place")) {
				cyPlaceArray[length] = n;
				length++;
			}
		}
		return cyPlaceArray;
	}

	/**
	 * Initializes Columns when creating a new Petri Net
	 */
	public void initializeColumns() {
		petriNet.getDefaultNodeTable().createColumn("id", String.class, true);
		petriNet.getDefaultNodeTable().createColumn("tokens", Integer.class, false);
		petriNet.getDefaultNodeTable().createColumn("initial tokens", Integer.class, true);
		petriNet.getDefaultNodeTable().createColumn("type", String.class, true);
		petriNet.getDefaultNodeTable().createColumn("fired", Integer.class, false);
		petriNet.getDefaultEdgeTable().createColumn("weight", Integer.class, true);
	}
	
	/**
	 * Create Visual Style for new Petri Net
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void createVisualStyle() {
		CyNode[] cyPlaceArray = getPlaces();
		CyNode[] cyTransitionArray = getTransitions();
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
		
		CyLayoutAlgorithm def = clam.getDefaultLayout(); // Apply default layout
		TaskIterator itr = def.createTaskIterator(cnv, def.getDefaultLayoutContext(), nodeviews, null);
		adapter.getTaskManager().execute(itr);
		SynchronousTaskManager<?> synTaskMan = adapter.getCyServiceRegistrar().getService(SynchronousTaskManager.class);
		synTaskMan.execute(itr);
		vs.apply(cnv);
		cnv.updateView();
	}
	
	/**
	 * Verify correctness of Petri Net
	 */
	public void verifyNet() {
		CyNode cyNodeArray[] = new CyNode[petriNet.getNodeCount()];
		CyEdge cyEdgeArray[] = new CyEdge[petriNet.getEdgeCount()];
		petriNet.getEdgeList().toArray(cyEdgeArray);
		petriNet.getNodeList().toArray(cyNodeArray);
		ArrayList<String> errors = new ArrayList<String>();
		for (CyNode n : cyNodeArray) {
			if (petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("type", String.class) == null) {
				errors.add(petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("name", String.class) + ": missing type");
			}
			else if (petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("type", String.class).equals("Place")) {
				if (petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("tokens", Integer.class) == null) {
					errors.add(petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("name", String.class) + ": missing tokens");
				}
				if (petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("initial tokens", Integer.class) == null) {
					errors.add(petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("name", String.class) + ": missing initial tokens");				
				}
				if (petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("fired", Integer.class) != null) {
					errors.add(petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("name", String.class) +
							": fired should not be defined for place");
				}
			}
			else if (petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("type", String.class).equals("Transition")) {
				if (petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("fired", Integer.class) == null) {
					errors.add(petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("name", String.class) + ": missing fired");
				}
				if (petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("tokens", Integer.class) != null) {
					errors.add(petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("name", String.class) +
							": tokens should not be defined for transition");
				}
				if (petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("initial tokens", Integer.class) != null) {
					errors.add(petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("name", String.class) +
							": initial tokens should not be defined for transition");
				}
			}
		}
		for (CyEdge e : cyEdgeArray) {
			if (petriNet.getDefaultEdgeTable().getRow(e.getSUID()).get("weight", Integer.class) == null ||
					petriNet.getDefaultEdgeTable().getRow(e.getSUID()).get("weight", Integer.class) < 1) {
				errors.add(petriNet.getDefaultEdgeTable().getRow(e.getSUID()).get("name", String.class) + ": missing or negative weight");
			}
			if (petriNet.getDefaultNodeTable().getRow(e.getSource().getSUID()).get("type", String.class) == null ||
					petriNet.getDefaultNodeTable().getRow(e.getTarget().getSUID()).get("type", String.class) == null) {
				continue;
			}
			else if (petriNet.getDefaultNodeTable().getRow(e.getSource().getSUID()).get("type", String.class).equals("Place")){
				if (petriNet.getDefaultNodeTable().getRow(e.getTarget().getSUID()).get("type", String.class).equals("Place")){
					errors.add(petriNet.getDefaultEdgeTable().getRow(e.getSUID()).get("name", String.class) + ": connects two places");
				}
			}
			else if (petriNet.getDefaultNodeTable().getRow(e.getSource().getSUID()).get("type", String.class).equals("Transition")) {
				if (petriNet.getDefaultNodeTable().getRow(e.getTarget().getSUID()).get("type", String.class).equals("Transition")) {
					errors.add(petriNet.getDefaultEdgeTable().getRow(e.getSUID()).get("name", String.class) + ": connects two transitions");
				}
			}
		}
		JFrame f = new JFrame("Errors during verification");
		// TODO FORMAT ERROR MESSAGE
		String msg = errors.toString().replaceAll(",", System.lineSeparator());
		msg = msg.replace("[", "").replace("]", "");
		JOptionPane.showMessageDialog(f, msg);
	}
	
	/**
	 * Fire Petri Net. Goes through all transitions and checks which of them can fired, then does so for those.
	 * @param cyTransitionArray - Array of transition type nodes
	 * @param random - randomize firing order
	 * @param firingMode - synchronous (true) or asynchronous (false) firing
	 */
	public void fire(CyNode[] cyTransitionArray, boolean firingMode, boolean random) {
		for (CyNode n : cyTransitionArray) {									
			petriNet.getDefaultNodeTable().getRow(n.getSUID()).set("fired", 0);	// Reset which transitions were fired
		}
		if (random) {
			List <CyNode> transitions = Arrays.asList(cyTransitionArray);
			Collections.shuffle(transitions);
			transitions.toArray(cyTransitionArray);
		}
		ArrayList<CyNode> fireableTransitions = new ArrayList<CyNode>();
		for (int i=0; i<cyTransitionArray.length; i++){
			Iterable<CyEdge>incomingEdges = petriNet.getAdjacentEdgeIterable(cyTransitionArray[i], CyEdge.Type.INCOMING); // Incoming Edges for a Transition
			boolean fireable = true;
			for (CyEdge incomingEdge: incomingEdges){
				if (petriNet.getDefaultNodeTable().getRow(incomingEdge.getSource().getSUID()).get("tokens", Integer.class) < petriNet.getDefaultEdgeTable().getRow(incomingEdge.getSUID()).get("weight", Integer.class)){
					fireable = false; // Transition can't be fired, if the source of an incoming edge does not have enough tokens
					break;
				}
			}
			if (fireable) {
				fireableTransitions.add(cyTransitionArray[i]);
				Iterable<CyEdge>incomingEdges1 = petriNet.getAdjacentEdgeIterable(cyTransitionArray[i], CyEdge.Type.INCOMING);
				// Remove tokens from sources of incoming before continuing for the next transition. Prevents negative token amounts
				for (CyEdge incomingEdge: incomingEdges1){	
					Integer newAmount = petriNet.getDefaultNodeTable().getRow(incomingEdge.getSource().getSUID()).get("tokens", Integer.class)
							- petriNet.getDefaultEdgeTable().getRow(incomingEdge.getSUID()).get("weight", Integer.class);
					petriNet.getDefaultNodeTable().getRow(incomingEdge.getSource().getSUID()).set("tokens", newAmount);
				}
				if (!firingMode) {
					break;
				}
			}
		}
		for (int i = 0; i<fireableTransitions.size(); i++){
			Iterable<CyEdge>outgoingEdges = petriNet.getAdjacentEdgeIterable(fireableTransitions.get(i),  CyEdge.Type.OUTGOING);
			// Add tokens to targets of outgoing edges
			for (CyEdge outgoingEdge: outgoingEdges){
				Integer newAmount = petriNet.getDefaultNodeTable().getRow(outgoingEdge.getTarget().getSUID()).get("tokens", Integer.class)
						+ petriNet.getDefaultEdgeTable().getRow(outgoingEdge.getSUID()).get("weight", Integer.class);
				petriNet.getDefaultNodeTable().getRow(outgoingEdge.getTarget().getSUID()).set("tokens", newAmount);
			}
		}
		for (int i = 0; i<cyTransitionArray.length; i++){
			if (fireableTransitions.contains(cyTransitionArray[i])){	// Updates, which transitions have fired
				//int sofar = petriNet.getDefaultNodeTable().getRow(cyTransitionArray[i].getSUID()).get("fired", Integer.class);
				petriNet.getDefaultNodeTable().getRow(cyTransitionArray[i].getSUID()).set("fired", 1);
			}
		}
	}
	
	/**
	 * Resets state of Petri Net to the beginning. Should probably just define a getter for cyPlaceArray.
	 */
	public void reset() {
		CyNode[] cyTransitionArray = getTransitions();
		for (CyNode n : cyTransitionArray) {									
			petriNet.getDefaultNodeTable().getRow(n.getSUID()).set("fired", 0);	// Reset how often transitions were fired
		}
		CyNode[] cyPlaceArray = getPlaces();
		for (CyNode n : cyPlaceArray) {
			petriNet.getDefaultNodeTable().getRow(n.getSUID()).set("tokens", petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("initial tokens", Integer.class));
		}
	}

	/**
	 * @return TaskIterator for a ViewUpdaterTask
	 * Similar to AbstractTaskFactory.CreateTaskIterator
	 */
	public TaskIterator updateView() {
		return new TaskIterator(new ViewUpdaterTask(petriNet, cnvm));
	}
}
