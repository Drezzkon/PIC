package org.petri;

import java.awt.Color;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class CreatePlaceTask extends AbstractTask {
	
	private CyNetwork petriNet;
	private CyNetworkViewManager cnvm;
	@Tunable(description="Name of new Place", groups="Name")
	public String name;
	@Tunable(description="Initial amount of Tokens", groups="Tokens")
	public String tokens;

	public CreatePlaceTask(CyNetwork petriNet, CyNetworkViewManager cnvm) {
		this.petriNet = petriNet;
		this.cnvm = cnvm;
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
		CyNetworkView [] cnvs = new CyNetworkView[1];
		cnvm.getNetworkViews(petriNet).toArray(cnvs);
		CyNetworkView cnv = cnvs[0];
		cnv.updateView();
		View <CyNode> view = cnv.getNodeView(place);
		view.setLockedValue(BasicVisualLexicon.NODE_FILL_COLOR, Color.RED);
		view.setLockedValue(BasicVisualLexicon.NODE_WIDTH, 35.0);
		view.setLockedValue(BasicVisualLexicon.NODE_LABEL,
				petriNet.getDefaultNodeTable().getRow(view.getModel().getSUID()).get("name", String.class)+"\n"
				+ Integer.toString(petriNet.getDefaultNodeTable().getRow(view.getModel().getSUID()).get("tokens", Integer.class)));
	}
}