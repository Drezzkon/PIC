package org.petri;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 * Constructor for PetriTasks
 * @author M. Gehrmann, M. Kirchner
 *
 */
public class CreatePlaceTaskFactory extends AbstractTaskFactory{
	private final CyNetworkViewManager cnvm;
	private final CyNetwork petriNet;
	
	/**
	 * Constructor
	 * @param netMgr CyNetworkManager
	 * @param namingUtil CyNetworkNaming
	 * @param cnvm CyNetworkViewManager
	 * @param eventHelper EventHelper
	 * @param petriNet Petri Net to be filled with data
	 * @param petriUtils Utilities for Petri Net
	 */
	public CreatePlaceTaskFactory(final CyNetworkViewManager cnvm,final CyNetwork petriNet) {
		this.cnvm = cnvm;
		this.petriNet = petriNet;
	}
	

	/**
	 * 
	 */
	public TaskIterator createTaskIterator(){
		return new TaskIterator(new CreatePlaceTask(petriNet, cnvm));
	}	
}
