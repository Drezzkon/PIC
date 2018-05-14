package org.petri;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class CreateEdgeTask extends AbstractTask {
	
	private CyNetwork petriNet;
	@Tunable(description="Internal ID of Source Node", groups="Nodes")
	public String sourceID;
	@Tunable(description="Internal ID of Target Node", groups="Nodes")
	public String targetID;
	@Tunable(description="Weight of Edge", groups="Weight")
	public String weight;
	private int id;
	
	
	public CreateEdgeTask(CyNetwork petriNet, int id) {
		this.petriNet = petriNet;
		this.id = id;
	}

	public void run(TaskMonitor taskMonitor) throws Exception {
		if (sourceID.equals("") || targetID.equals("") || weight.equals("")) {
			//TODO ERROR MESSAGES HERE FOR EMPTY TUNABLES
			return;
		}
		// TODO CHECK FOR NON-INT AND NEGATIVE WEIGHTS
		CyNode source = null;
		CyNode target = null;
		for (CyNode n : petriNet.getNodeList()) {
			if (petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("internal id", String.class).equals(sourceID)) {
				source = n;
			}
			if (petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("internal id", String.class).equals(targetID)) {
				target = n;
			}
		}
		if (source == null || target == null) {
			// TODO ERROR MESSAGE HERE FOR WRONG ID
			return;
		}
		if (petriNet.getDefaultNodeTable().getRow(source.getSUID()).get("type", String.class).equals(
				petriNet.getDefaultNodeTable().getRow(target.getSUID()).get("type", String.class))) {
			//TODO  ERROR MESSAGE HERE FOR SAME TYPE
			return;
		}
		CyEdge edge = petriNet.addEdge(source, target, true);
		petriNet.getDefaultEdgeTable().getRow(edge.getSUID()).set("internal id", "e"+Integer.toString(id));
		petriNet.getDefaultEdgeTable().getRow(edge.getSUID()).set("weight", Integer.parseInt(weight));
		String sourcename = petriNet.getDefaultNodeTable().getRow(source.getSUID()).get("name", String.class);
		String targetname = petriNet.getDefaultNodeTable().getRow(target.getSUID()).get("name", String.class);
		petriNet.getDefaultEdgeTable().getRow(edge.getSUID()).set("name", sourcename+"->"+targetname);		
	}
}
