package org.petri;

import java.awt.Color;
import java.awt.Paint;
import java.util.ArrayList;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;

public class PetriUtils {

	private CyNetwork petriNet;
	private VisualMappingFunctionFactory vmffc;
	private VisualMappingManager vmm;

	public PetriUtils(CyNetwork petriNet, VisualMappingFunctionFactory vmffc, VisualMappingManager vmm) {
		this.petriNet = petriNet;
		this.vmffc = vmffc;
		this.vmm = vmm;
	}

	public void fire(CyNode[] cyTransitionArray) {
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
				Iterable<CyEdge>incomingEdges1 = petriNet.getAdjacentEdgeIterable(cyTransitionArray[i], CyEdge.Type.INCOMING);
				for (CyEdge incomingEdge: incomingEdges1){
					Integer newAmount = petriNet.getDefaultNodeTable().getRow(incomingEdge.getSource().getSUID()).get("amount", Integer.class)
							- petriNet.getDefaultEdgeTable().getRow(incomingEdge.getSUID()).get("weight", Integer.class);
					petriNet.getDefaultNodeTable().getRow(incomingEdge.getSource().getSUID()).set("amount", newAmount);
				}
			}
		}
		for (int i = 0; i<fireableTransitions.size(); i++){
			Iterable<CyEdge>outgoingEdges = petriNet.getAdjacentEdgeIterable(fireableTransitions.get(i),  CyEdge.Type.OUTGOING);
			for (CyEdge outgoingEdge: outgoingEdges){
				Integer newAmount = petriNet.getDefaultNodeTable().getRow(outgoingEdge.getTarget().getSUID()).get("amount", Integer.class)
						+ petriNet.getDefaultEdgeTable().getRow(outgoingEdge.getSUID()).get("weight", Integer.class);
				petriNet.getDefaultNodeTable().getRow(outgoingEdge.getTarget().getSUID()).set("amount", newAmount);
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
			petriNet.getDefaultNodeTable().getRow(n.getSUID()).set("amount", petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("initial amount", Integer.class));
		}
	}

	public void mapFired(int fired, CyNetworkView cnv) {
		VisualStyle vs = vmm.getVisualStyle(cnv);
		ContinuousMapping<Integer, Paint> amountMap = (ContinuousMapping<Integer, Paint>)
				vmffc.createVisualMappingFunction("amount", Integer.class,BasicVisualLexicon.NODE_FILL_COLOR);
		BoundaryRangeValues<Paint> brv1 = new BoundaryRangeValues<Paint>(new Color(255,255,255), 
				new Color(255,191,191), new Color(255,127,127));
		BoundaryRangeValues<Paint> brv2 = new BoundaryRangeValues<Paint>(new Color(255,127,127),
				new Color(255,63,63), new Color(255,0,0));
		amountMap.addPoint(1, brv1);
		int maxAmount = 0;
		for (CyNode n : petriNet.getNodeList()) {
			Integer temp = petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("amount", Integer.class);
			if (temp != null) {
				if (temp > maxAmount) {
					maxAmount = temp;
				}
			}
		}
		amountMap.addPoint(maxAmount-maxAmount/2, brv2);
		vs.addVisualMappingFunction(amountMap);
		ContinuousMapping<Integer, Paint> firedMap = (ContinuousMapping<Integer, Paint>)
				vmffc.createVisualMappingFunction("amount", Integer.class,BasicVisualLexicon.NODE_FILL_COLOR);
		BoundaryRangeValues<Paint> brv3 = new BoundaryRangeValues<Paint>(new Color(255,255,255), 
				new Color(191,255,191), new Color(127,255,127));
		BoundaryRangeValues<Paint> brv4 = new BoundaryRangeValues<Paint>(new Color(127,255,127),
				new Color(63,255,63), new Color(0,255,0));
		firedMap.addPoint(1, brv3);
		firedMap.addPoint(fired-fired/2, brv4);
		vs.addVisualMappingFunction(firedMap);
	}
}
