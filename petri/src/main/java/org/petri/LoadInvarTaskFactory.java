package org.petri;

import javax.swing.JComboBox;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 * Factory for CreateNetworkTasks
 * @author M. Gehrmann, M. Kirchner
 *
 */
public class LoadInvarTaskFactory extends AbstractTaskFactory{	
	private JComboBox<String> invarHolder;

	/**
	 * Constructor
	 * @param invarHolder Container for invariants
	 */
	public LoadInvarTaskFactory(JComboBox<String> invarHolder){
		this.invarHolder = invarHolder;
	}
	

	/**
	 * 
	 */
	public TaskIterator createTaskIterator(){
		return new TaskIterator(new LoadInvarTask(invarHolder));
	}	
}
