package org.petri;

import java.util.ArrayList;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskIterator;

public class PetriUtils {

	private CyNetwork petriNet;
	private CyNetworkViewManager cnvm;
	
	public PetriUtils(CyNetwork petriNet, CyNetworkViewManager cnvm) {
		this.petriNet = petriNet;
		this.cnvm = cnvm;
	}

	public void fire(CyNode[] cyTransitionArray) {
		ArrayList<CyNode> fireableTransitions = new ArrayList<CyNode>();
		for (int i=0; i<cyTransitionArray.length; i++){
			Iterable<CyEdge>incomingEdges = petriNet.getAdjacentEdgeIterable(cyTransitionArray[i], CyEdge.Type.INCOMING);
			boolean fireable = true;
			for (CyEdge incomingEdge: incomingEdges){
				if (petriNet.getDefaultNodeTable().getRow(incomingEdge.getSource().getSUID()).get("tokens", Integer.class) < petriNet.getDefaultEdgeTable().getRow(incomingEdge.getSUID()).get("weight", Integer.class)){
					fireable = false;
					break;
				}
			}
			if (fireable) {
				fireableTransitions.add(cyTransitionArray[i]);
				Iterable<CyEdge>incomingEdges1 = petriNet.getAdjacentEdgeIterable(cyTransitionArray[i], CyEdge.Type.INCOMING);
				for (CyEdge incomingEdge: incomingEdges1){
					Integer newAmount = petriNet.getDefaultNodeTable().getRow(incomingEdge.getSource().getSUID()).get("tokens", Integer.class)
							- petriNet.getDefaultEdgeTable().getRow(incomingEdge.getSUID()).get("weight", Integer.class);
					petriNet.getDefaultNodeTable().getRow(incomingEdge.getSource().getSUID()).set("tokens", newAmount);
				}
			}
		}
		for (int i = 0; i<fireableTransitions.size(); i++){
			Iterable<CyEdge>outgoingEdges = petriNet.getAdjacentEdgeIterable(fireableTransitions.get(i),  CyEdge.Type.OUTGOING);
			for (CyEdge outgoingEdge: outgoingEdges){
				Integer newAmount = petriNet.getDefaultNodeTable().getRow(outgoingEdge.getTarget().getSUID()).get("tokens", Integer.class)
						+ petriNet.getDefaultEdgeTable().getRow(outgoingEdge.getSUID()).get("weight", Integer.class);
				petriNet.getDefaultNodeTable().getRow(outgoingEdge.getTarget().getSUID()).set("tokens", newAmount);
			}
		}
		for (int i = 0; i<cyTransitionArray.length; i++){
			if (fireableTransitions.contains(cyTransitionArray[i])){
				int sofar = petriNet.getDefaultNodeTable().getRow(cyTransitionArray[i].getSUID()).get("fired", Integer.class);
				petriNet.getDefaultNodeTable().getRow(cyTransitionArray[i].getSUID()).set("fired", sofar+1);
			}
		}
	}
	
	public void reset() {
		int length = 0;
		for (CyNode n : petriNet.getNodeList()) {
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
		for (CyNode n : cyPlaceArray) {
			petriNet.getDefaultNodeTable().getRow(n.getSUID()).set("tokens", petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("initial tokens", Integer.class));
		}
	}

	public TaskIterator updateView() {
		return new TaskIterator(new ViewUpdaterTask(petriNet, cnvm));
	}
}
