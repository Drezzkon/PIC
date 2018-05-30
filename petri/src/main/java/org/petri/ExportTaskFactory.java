package org.petri;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 * Constructor for PetriTasks
 * @author M. Gehrmann, M. Kirchner
 *
 */
public class ExportTaskFactory extends AbstractTaskFactory{
	private final CyNetwork petriNet;
	private final CyNode[] cyPlaceArray;
	private final CyNode[] cyTransitionArray;
	
	/**
	 * Constructor
	 * @param netMgr CyNetworkManager
	 * @param namingUtil CyNetworkNaming
	 * @param cnvm CyNetworkViewManager
	 * @param eventHelper EventHelper
	 * @param petriNet Petri Net to be filled with data
	 * @param petriUtils Utilities for Petri Net
	 */
	public ExportTaskFactory(final CyNetwork petriNet, final CyNode[] cyPlaceArray, final CyNode[] cyTransitionArray) {
		this.petriNet = petriNet;
		this.cyPlaceArray = cyPlaceArray;
		this.cyTransitionArray = cyTransitionArray;
	}
	

	/**
	 * 
	 */
	public TaskIterator createTaskIterator(){
		return new TaskIterator(new ExportTask(petriNet, cyPlaceArray, cyTransitionArray));
	}	
}
