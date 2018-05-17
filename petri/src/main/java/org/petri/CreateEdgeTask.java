package org.petri;

import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class CreateEdgeTask extends AbstractTask {
	
	private CyNetwork petriNet;
	private CyNetworkViewManager cnvm;
	@Tunable(description="Internal ID of Source Node", groups="Nodes")
	public String sourceID;
	@Tunable(description="Internal ID of Target Node", groups="Nodes")
	public String targetID;
	@Tunable(description="Weight of Edge", groups="Weight")
	public String weight;
	
	
	public CreateEdgeTask(CyNetwork petriNet, CyNetworkViewManager cnvm) {
		this.petriNet = petriNet;
		this.cnvm = cnvm;
	}

	public void run(TaskMonitor taskMonitor) throws Exception {
		if (sourceID.equals("") || targetID.equals("") || weight.equals("")) {
			JFrame f = new JFrame("Error during edge creation");
			String msg = "Missing input values";
			JOptionPane.showMessageDialog(f, msg);
			return;
		}
		boolean invalidWeight;
	    Scanner sc = new Scanner(weight.trim());
	    if(!sc.hasNextInt(10)) {
	    	invalidWeight = true;
	    }
	    else {
	    sc.nextInt(10);
	    invalidWeight = sc.hasNext();
	    }
	    sc.close();
		if (invalidWeight || Integer.parseInt(weight) < 1) {
			JFrame f = new JFrame("Error during edge creation");
			String msg = "Invalid weight";
			JOptionPane.showMessageDialog(f, msg);
			return;
		}
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
			JFrame f = new JFrame("Error during edge creation");
			String msg = "Source or Target not found";
			JOptionPane.showMessageDialog(f, msg);
			return;
		}
		if (petriNet.getDefaultNodeTable().getRow(source.getSUID()).get("type", String.class).equals(
				petriNet.getDefaultNodeTable().getRow(target.getSUID()).get("type", String.class))) {
			JFrame f = new JFrame("Error during edge creation");
			String msg = "Source and Target have same type";
			JOptionPane.showMessageDialog(f, msg);
			return;
		}
		CyEdge edge = petriNet.addEdge(source, target, true);
		petriNet.getDefaultEdgeTable().getRow(edge.getSUID()).set("internal id", "e"+Integer.toString(petriNet.getEdgeCount()-1));
		petriNet.getDefaultEdgeTable().getRow(edge.getSUID()).set("weight", Integer.parseInt(weight));
		String sourcename = petriNet.getDefaultNodeTable().getRow(source.getSUID()).get("name", String.class);
		String targetname = petriNet.getDefaultNodeTable().getRow(target.getSUID()).get("name", String.class);
		petriNet.getDefaultEdgeTable().getRow(edge.getSUID()).set("name", sourcename+"->"+targetname);
		CyNetworkView [] cnvs = new CyNetworkView[1];
		cnvm.getNetworkViews(petriNet).toArray(cnvs);
		CyNetworkView cnv = cnvs[0];
		cnv.updateView();
		View<CyEdge> view = cnv.getEdgeView(edge);
		view.setLockedValue(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.ARROW);
	}
}
