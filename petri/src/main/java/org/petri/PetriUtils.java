package org.petri;

import java.util.ArrayList;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskIterator;

/**
 * Utilites for managing Petri Nets, like updating views and firing
 * @author M. Gehrmann, M. Kirchner
 *
 */
public class PetriUtils {

	private CyNetwork petriNet;
	private CyNetworkViewManager cnvm;
	
	/**
	 * Constructor
	 * @param petriNet
	 * @param cnvm
	 */
	public PetriUtils(CyNetwork petriNet, CyNetworkViewManager cnvm) {
		this.petriNet = petriNet;
		this.cnvm = cnvm;
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
	 * Fire Petri Net. Goes through all transitions and checks which of them can fired, then does so for those.
	 * @param cyTransitionArray - Should actually just implement a getter for this and use that instead
	 */
	public void fire(CyNode[] cyTransitionArray) {
		for (CyNode n : cyTransitionArray) {									
			petriNet.getDefaultNodeTable().getRow(n.getSUID()).set("fired", 0);	// Reset which transitions were fired
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
