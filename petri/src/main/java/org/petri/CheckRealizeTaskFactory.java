package org.petri;

import javax.swing.JComboBox;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 * Factory for CheckRealizeTasks
 * @author M. Gehrmann
 * 
 */
public class CheckRealizeTaskFactory extends AbstractTaskFactory{
	private final CyNetwork petriNet;
	private final JComboBox<String> invarHolder;
	
	/**
	 * Constructor
	 * @param petriNet Petri net currently being worked on
	 * @param invarHolder Container for invariants
	 */
	public CheckRealizeTaskFactory(final CyNetwork petriNet, JComboBox<String> invarHolder) {
		this.petriNet = petriNet;
		this.invarHolder = invarHolder;
	}
	
	public TaskIterator createTaskIterator(){
		return new TaskIterator(new CheckRealizeTask(petriNet, invarHolder));
	}	
}
