package org.petri;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class CreatePlaceTask extends AbstractTask {
	
	private CyNetwork petriNet;
	private int id;
	@Tunable(description="Name of new Place", groups="Name")
	public String name;
	@Tunable(description="Initial amount of Tokens", groups="Tokens")
	public String tokens;

	public CreatePlaceTask(CyNetwork petriNet, int id) {
		this.petriNet = petriNet;
		this.id = id;
	}

	public void run(TaskMonitor taskMonitor) throws Exception {
		if (name.equals("") || tokens.equals("")) {
			//TODO ERROR MESSAGES HERE FOR EMPTY TUNABLES
			return;
		}
		// TODO ERROR MESSAGES AND CHECKS FOR NON INT OR NEGATIVE TOKENS
		CyNode place = petriNet.addNode();
		petriNet.getDefaultNodeTable().getRow(place.getSUID()).set("internal id", "p"+id);
		petriNet.getDefaultNodeTable().getRow(place.getSUID()).set("type", "Place");
		petriNet.getDefaultNodeTable().getRow(place.getSUID()).set("name", name);
		petriNet.getDefaultNodeTable().getRow(place.getSUID()).set("initial tokens", Integer.parseInt(tokens));
		petriNet.getDefaultNodeTable().getRow(place.getSUID()).set("tokens", Integer.parseInt(tokens));
	}
}