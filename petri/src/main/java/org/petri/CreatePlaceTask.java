package org.petri;

import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class CreatePlaceTask extends AbstractTask {
	
	private CyNetwork petriNet;
	@Tunable(description="Name of new Place", groups="Name")
	public String name;
	@Tunable(description="Initial amount of Tokens", groups="Tokens")
	public String tokens;

	public CreatePlaceTask(CyNetwork petriNet) {
		this.petriNet = petriNet;
	}

	public void run(TaskMonitor taskMonitor) throws Exception {
		if (name.equals("") || tokens.equals("")) {
			JFrame f = new JFrame("Error during place creation");
			String msg = "Missing input values";
			JOptionPane.showMessageDialog(f, msg);
			return;
		}
		boolean invalidTokens;
	    Scanner sc = new Scanner(tokens.trim());
	    if(!sc.hasNextInt(10)) {
	    	invalidTokens = true;
	    }
	    else {
	    sc.nextInt(10);
	    invalidTokens = sc.hasNext();
	    }
	    sc.close();
		if (invalidTokens || Integer.parseInt(tokens) < 0) {
			JFrame f = new JFrame("Error during place creation");
			String msg = "Invalid token amount";
			JOptionPane.showMessageDialog(f, msg);
			return;
		}
		int length = 0;
		for (CyNode n : petriNet.getNodeList()) {	// Get length of array
			String ntype = (String) (petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("type", String.class));
			if (ntype.equals("Place")) {
				length++;				
			}
		}
		CyNode place = petriNet.addNode();
		petriNet.getDefaultNodeTable().getRow(place.getSUID()).set("internal id", "p"+Integer.toString(length));
		petriNet.getDefaultNodeTable().getRow(place.getSUID()).set("type", "Place");
		petriNet.getDefaultNodeTable().getRow(place.getSUID()).set("name", name);
		petriNet.getDefaultNodeTable().getRow(place.getSUID()).set("initial tokens", Integer.parseInt(tokens));
		petriNet.getDefaultNodeTable().getRow(place.getSUID()).set("tokens", Integer.parseInt(tokens));
	}
}