package org.petri;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 * Constructor for PetriTasks
 * @author M. Gehrmann, M. Kirchner
 *
 */
public class PetriTaskFactory extends AbstractTaskFactory{
	private final CyNetworkManager netMgr;
	private final CyNetworkNaming namingUtil; 
	private final CyNetworkViewManager cnvm;
	private final CyEventHelper eventHelper;
	private final CyNetwork petriNet;
	private final PetriUtils petriUtils;
	
	/**
	 * Constructor
	 * @param netMgr CyNetworkManager
	 * @param namingUtil CyNetworkNaming
	 * @param cnvm CyNetworkViewManager
	 * @param eventHelper EventHelper
	 * @param petriNet Petri Net to be filled with data
	 * @param petriUtils Utilities for Petri Net
	 */
	public PetriTaskFactory(final CyNetworkManager netMgr, final CyNetworkNaming namingUtil,
			final CyNetworkViewManager cnvm, final CyEventHelper eventHelper, final CyNetwork petriNet,
			final PetriUtils petriUtils){
		this.netMgr = netMgr;
		this.namingUtil = namingUtil;
		this.cnvm = cnvm;
		this.eventHelper = eventHelper;
		this.petriNet = petriNet;
		this.petriUtils = petriUtils;
	}
	

	/**
	 * 
	 */
	public TaskIterator createTaskIterator(){
		return new TaskIterator(new PetriTask(netMgr, namingUtil, cnvm, eventHelper, petriNet, petriUtils));
	}	
}
