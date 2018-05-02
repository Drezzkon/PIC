package org.petri;

import java.util.HashMap;
import java.util.Map;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class UpdateAnnotationTask extends AbstractTask {
	private CyNetwork petriNet;
	private AnnotationFactory<TextAnnotation> annFac;
	private AnnotationManager annMan;
	private CyNetworkViewManager cnvm;
	
	public UpdateAnnotationTask(CyNetwork petriNet, AnnotationFactory<TextAnnotation> annFac,
			AnnotationManager annMan, CyNetworkViewManager cnvm) {
		this.petriNet = petriNet;
		this.annFac = annFac;
		this.annMan = annMan;
		this.cnvm = cnvm;
	}

	@Override
	public void run(TaskMonitor monitor) throws Exception {
		CyNetworkView [] cnvs = new CyNetworkView[1];
		cnvm.getNetworkViews(petriNet).toArray(cnvs);
		CyNetworkView cnv = cnvs[0];
		if (annMan.getAnnotations(cnv) != null) {
			for (Annotation a : annMan.getAnnotations(cnv)) {
				a.removeAnnotation();
			}
		}
		for (View<CyNode> v : cnv.getNodeViews()) {
			if (petriNet.getDefaultNodeTable().getRow(v.getModel().getSUID()).get("type", String.class).equals("Place")) {
				Map <String, String> argMap = new HashMap<String, String>();
				argMap.put(Annotation.CANVAS, Annotation.FOREGROUND);
				argMap.put(Annotation.X, Double.toString(v.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION)));
				argMap.put(Annotation.Y, Double.toString(v.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION)-10.0));
				argMap.put(Annotation.NAME, petriNet.getDefaultNodeTable().getRow(v.getModel().getSUID()).get("name", String.class));
				argMap.put(Annotation.ZOOM, "1.0");
				TextAnnotation ann = annFac.createAnnotation(TextAnnotation.class, cnv, argMap);
				ann.setText(Integer.toString(petriNet.getDefaultNodeTable().getRow(v.getModel().getSUID()).get("tokens", Integer.class)));
				annMan.addAnnotation(ann);		
			}
		}
	}
}
