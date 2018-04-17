package org.petri;

import java.util.Properties;

import org.cytoscape.app.CyAppAdapter;
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
import org.osgi.framework.BundleContext;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		CyAppAdapter adapter = getService(bc, CyAppAdapter.class);
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

		PetriPanel petriPanel = new PetriPanel(cyNetworkManagerServiceRef,
				cyNetworkNamingServiceRef,cyNetworkFactoryServiceRef,cyNetworkViewFactoryServiceRef,
				cyNetworkViewManagerServiceRef, eventHelperServiceRef,cyLayoutAlgorithmManagerRef,
				synchronousTaskManagerRef, visualMappingManagerRef, visualMappingFunctionFactoryRefc,
				visualMappingFunctionFactoryRefd, visualMappingFunctionFactoryRefp, adapter);
		registerService(bc, petriPanel, CytoPanelComponent.class, new Properties());
	}
}