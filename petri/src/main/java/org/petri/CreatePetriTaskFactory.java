package org.petri;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;

public class CreatePetriTaskFactory extends AbstractTaskFactory{
	private final CyNetworkManager netMgr;
	private final CyNetworkNaming namingUtil; 
	private final CyNetworkViewFactory cnvf;
	private final CyNetworkViewManager cnvm;
	private final CyEventHelper eventHelper;
	private final CyLayoutAlgorithmManager calm;
	private final SynchronousTaskManager<?> synctm;
	private final VisualMappingManager vmm;
	private final VisualMappingFunctionFactory vmffd;
	private final VisualMappingFunctionFactory vmffp;
	private final CyNetwork petriNet;
	
	public CreatePetriTaskFactory(final CyNetworkManager netMgr, final CyNetworkNaming namingUtil,
			final CyNetworkViewFactory cnvf, final CyNetworkViewManager cnvm,
			final CyEventHelper eventHelper, CyLayoutAlgorithmManager calm, SynchronousTaskManager<?> synctm,
			VisualMappingManager vmm, VisualMappingFunctionFactory vmffd, 
			VisualMappingFunctionFactory vmffp, final CyNetwork petriNet){
		this.netMgr = netMgr;
		this.namingUtil = namingUtil;
		this.cnvf = cnvf;
		this.cnvm = cnvm;
		this.eventHelper = eventHelper;
		this.calm = calm;
		this.synctm = synctm;
		this.vmm = vmm;
		this.vmffd = vmffd;
		this.vmffp = vmffp;
		this.petriNet = petriNet;
	}
	
	public TaskIterator createTaskIterator(){
		return new TaskIterator(new CreatePetriTask(netMgr, namingUtil, cnvf, cnvm, eventHelper, calm,
				synctm, vmm, vmffd, vmffp, petriNet));
	}

}
