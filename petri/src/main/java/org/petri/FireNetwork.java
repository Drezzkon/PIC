package org.petri;

import java.util.ArrayList;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;

public class FireNetwork {

	private CyNetwork petriNet;

	public FireNetwork(CyNetwork petriNet) {
		this.petriNet = petriNet;
	}

	public void fire() {
		int length = 0;
		for (CyNode n : petriNet.getNodeList()) {
			String ntype = (String) (petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("type", String.class));
			if (ntype.equals("Transition")) {
				length++;				
			}
		}
		CyNode[] cyTransitionArray = new CyNode[length];
		length = 0;
		for (CyNode n : petriNet.getNodeList()) {
			String ntype = (String) (petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("type", String.class));
			if (ntype.equals("Transition")) {
				cyTransitionArray[length] = n;
				length++;
			}
		}
		ArrayList<CyNode> fireableTransitions = new ArrayList<CyNode>();
		for (int i=0; i<cyTransitionArray.length; i++){
			Iterable<CyEdge>incomingEdges = petriNet.getAdjacentEdgeIterable(cyTransitionArray[i], CyEdge.Type.INCOMING);
			boolean fireable = true;
			for (CyEdge incomingEdge: incomingEdges){
				if (petriNet.getDefaultNodeTable().getRow(incomingEdge.getSource().getSUID()).get("amount", Integer.class) < petriNet.getDefaultEdgeTable().getRow(incomingEdge.getSUID()).get("weight", Integer.class)){
					fireable = false;
					break;
				}
			}
			if (fireable) {
				fireableTransitions.add(cyTransitionArray[i]);
			}
		}
		for (int i = 0; i<fireableTransitions.size(); i++){
			Iterable<CyEdge>incomingEdges = petriNet.getAdjacentEdgeIterable(fireableTransitions.get(i), CyEdge.Type.INCOMING);
			for (CyEdge incomingEdge: incomingEdges){
				Integer newAmount = petriNet.getDefaultNodeTable().getRow(incomingEdge.getSource().getSUID()).get("amount", Integer.class)
						- petriNet.getDefaultEdgeTable().getRow(incomingEdge.getSUID()).get("weight", Integer.class);
				petriNet.getDefaultNodeTable().getRow(incomingEdge.getSource().getSUID()).set("amount", newAmount);
			}
			Iterable<CyEdge>outgoingEdges = petriNet.getAdjacentEdgeIterable(fireableTransitions.get(i),  CyEdge.Type.OUTGOING);
			for (CyEdge outgoingEdge: outgoingEdges){
				Integer newAmount = petriNet.getDefaultNodeTable().getRow(outgoingEdge.getTarget().getSUID()).get("amount", Integer.class)
						+ petriNet.getDefaultEdgeTable().getRow(outgoingEdge.getSUID()).get("weight", Integer.class);
				petriNet.getDefaultNodeTable().getRow(outgoingEdge.getTarget().getSUID()).set("amount", newAmount);
			}
		}
		for (int i = 0; i<cyTransitionArray.length; i++){
			if (fireableTransitions.contains(cyTransitionArray[i])){
				petriNet.getDefaultNodeTable().getRow(cyTransitionArray[i].getSUID()).set("fired", 1);
			}
			else {
				petriNet.getDefaultNodeTable().getRow(cyTransitionArray[i].getSUID()).set("fired", 0);
			}
		}
	}
}
