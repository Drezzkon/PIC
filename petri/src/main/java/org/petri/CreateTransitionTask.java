package org.petri;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class CreateTransitionTask extends AbstractTask {
	
	private CyNetwork petriNet;
	private int id;
	@Tunable(description="Name of new Transition")
	public String name;

	public CreateTransitionTask(CyNetwork petriNet, int id) {
		this.petriNet = petriNet;
		this.id = id;
	}

	public void run(TaskMonitor taskMonitor) throws Exception {
		if (name == null) {
			//TODO ERROR MESSAGES HERE
			return;
		}
		CyNode transition = petriNet.addNode();
		petriNet.getDefaultNodeTable().getRow(transition.getSUID()).set("internal id", "t"+id);
		petriNet.getDefaultNodeTable().getRow(transition.getSUID()).set("type", "Transition");
		petriNet.getDefaultNodeTable().getRow(transition.getSUID()).set("name", name);
		petriNet.getDefaultNodeTable().getRow(transition.getSUID()).set("fired", 0);
	}
}
