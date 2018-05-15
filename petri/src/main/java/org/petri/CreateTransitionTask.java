package org.petri;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class CreateTransitionTask extends AbstractTask {
	
	private CyNetwork petriNet;
	@Tunable(description="Name of new Transition")
	public String name;

	public CreateTransitionTask(CyNetwork petriNet) {
		this.petriNet = petriNet;
	}

	public void run(TaskMonitor taskMonitor) throws Exception {
		if (name.equals("")) {
			JFrame f = new JFrame("Error during transition creation");
			String msg = "Missing input values";
			JOptionPane.showMessageDialog(f, msg);
			return;
		}
		int length = 0;
		for (CyNode n : petriNet.getNodeList()) {	// Get length of array
			String ntype = (String) (petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("type", String.class));
			if (ntype.equals("Transition")) {
				length++;				
			}
		}
		CyNode transition = petriNet.addNode();
		petriNet.getDefaultNodeTable().getRow(transition.getSUID()).set("internal id", "t"+Integer.toString(length));
		petriNet.getDefaultNodeTable().getRow(transition.getSUID()).set("type", "Transition");
		petriNet.getDefaultNodeTable().getRow(transition.getSUID()).set("name", name);
		petriNet.getDefaultNodeTable().getRow(transition.getSUID()).set("fired", 0);
	}
}
