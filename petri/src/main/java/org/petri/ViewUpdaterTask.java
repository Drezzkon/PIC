package org.petri;

import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class ViewUpdaterTask extends AbstractTask {
	
	private CyNetwork petriNet;
	private CyNetworkView cnv;
	private VisualMappingManager vmm;
	private VisualMappingFunctionFactory vmffd;
	private CyLayoutAlgorithmManager clam;
	private CyAppAdapter adapter;
	private CyNetworkViewManager cnvm;
	private CyNetworkViewFactory cnvf;
	
	public ViewUpdaterTask(CyNetwork petriNet, VisualMappingManager vmm, VisualMappingFunctionFactory vmffd,
			CyLayoutAlgorithmManager clam, CyAppAdapter adapter, CyNetworkViewManager cnvm, CyNetworkViewFactory cnvf) {
		this.petriNet = petriNet;
		this.cnvm = cnvm;
		this.cnvf = cnvf;
		this.vmm = vmm;
		this.vmffd = vmffd;
		this.clam = clam;
		this.adapter = adapter;
	}

	public void run(TaskMonitor taskMonitor) {
		CyNetworkView [] cnvs = new CyNetworkView[1];
		cnvm.getNetworkViews(petriNet).toArray(cnvs);
		cnv = cnvs[0];
		Set <View<CyNode>> placeviews = new HashSet<View<CyNode>>();
		Set <View<CyNode>> transitionviews = new HashSet<View<CyNode>>();
		for (View<CyNode> v : cnv.getNodeViews()) {
			String ntype = petriNet.getDefaultNodeTable().getRow(v.getModel().getSUID()).get("type", String.class);
			if (ntype.equals("Transition")) {
				transitionviews.add(v);
			}
			else {
				placeviews.add(v);
			}
		}
		for (View<CyNode> v : transitionviews) {
			v.setLockedValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.RECTANGLE);
			if (petriNet.getDefaultNodeTable().getRow(v.getModel().getSUID()).get("fired", Integer.class) == 1) {
				v.setLockedValue(BasicVisualLexicon.NODE_FILL_COLOR, Color.GREEN);
			}
			else {
				v.setLockedValue(BasicVisualLexicon.NODE_FILL_COLOR, Color.WHITE);
			}
		}
		for (View<CyNode> v : placeviews) {
		v.setLockedValue(BasicVisualLexicon.NODE_LABEL,
				petriNet.getDefaultNodeTable().getRow(v.getModel().getSUID()).get("name", String.class)+"\n"
				+ Integer.toString(petriNet.getDefaultNodeTable().getRow(v.getModel().getSUID()).get("tokens", Integer.class)));
		}
		cnv.updateView();
	}
}

