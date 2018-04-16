package org.petri;

import java.awt.Button;
import java.awt.Label;
import java.awt.TextField;
import java.util.Properties;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);
		CyNetworkNaming cyNetworkNamingServiceRef = getService(bc,CyNetworkNaming.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc,CyNetworkFactory.class);
		CyNetworkViewFactory cyNetworkViewFactoryServiceRef = getService(bc, CyNetworkViewFactory.class);
		CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc,CyNetworkViewManager.class);
		CyEventHelper eventHelperServiceRef = getService(bc, CyEventHelper.class);
		CyLayoutAlgorithmManager cyLayoutAlgorithmManagerRef = getService(bc, CyLayoutAlgorithmManager.class);
		SynchronousTaskManager<?> synchronousTaskManagerRef = getService(bc, SynchronousTaskManager.class);
		VisualMappingManager visualMappingManagerRef = getService(bc, VisualMappingManager.class);
		VisualMappingFunctionFactory visualMappingFunctionFactoryRefc = getService(bc, VisualMappingFunctionFactory.class,
				"(mapping.type=continuous)");
		VisualMappingFunctionFactory visualMappingFunctionFactoryRefd = getService(bc, VisualMappingFunctionFactory.class,
				"(mapping.type=discrete)");
		VisualMappingFunctionFactory visualMappingFunctionFactoryRefp = getService(bc, VisualMappingFunctionFactory.class,
				"(mapping.type=passthrough)");
		//Just Petri Things
		PetriPanel petriPanel = new PetriPanel();
		
		Label lb = new Label();
		lb.setText("Control Panel for Petri Net App");
		petriPanel.add(lb);
		Label lb2 = new Label();
		lb2.setText("How often do you want to fire?");
		f1.add(lb2);
		TextField tf = new TextField();
		tf.setSize(300, 10);
		f1.add(tf);
		Button trans = new Button();
		trans.setLabel("Fire Petri Net");
		f1.add(trans);
		petriPanel.add(f1);

		
		CreatePetriTaskFactory createPetriTaskFactory = new CreatePetriTaskFactory(cyNetworkManagerServiceRef,
				cyNetworkNamingServiceRef,cyNetworkFactoryServiceRef,cyNetworkViewFactoryServiceRef,
				cyNetworkViewManagerServiceRef, eventHelperServiceRef,cyLayoutAlgorithmManagerRef,
				synchronousTaskManagerRef, visualMappingManagerRef, visualMappingFunctionFactoryRefc,
				visualMappingFunctionFactoryRefd, visualMappingFunctionFactoryRefp);

		Properties petriTaskFactoryProps = new Properties();
		petriTaskFactoryProps.setProperty("preferredMenu","Apps.Petri");
		petriTaskFactoryProps.setProperty("title","Create Petri Net");
		registerService(bc,createPetriTaskFactory,TaskFactory.class, petriTaskFactoryProps);
		registerService(bc, petriPanel, CytoPanelComponent.class, new Properties());
	}
}
