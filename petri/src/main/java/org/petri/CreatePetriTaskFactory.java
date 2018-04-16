package org.petri;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkFactory;
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
	private final CyNetworkFactory cnf;
	private final CyNetworkNaming namingUtil; 
	private final CyNetworkViewFactory cnvf;
	private final CyNetworkViewManager cnvm;
	private final CyEventHelper eventHelper;
	private final CyLayoutAlgorithmManager calm;
	private final SynchronousTaskManager<?> synctm;
	private final VisualMappingManager vmm;
	private final VisualMappingFunctionFactory vmffc;
	private final VisualMappingFunctionFactory vmffd;
	private final VisualMappingFunctionFactory vmffp;
	
	public CreatePetriTaskFactory(final CyNetworkManager netMgr, final CyNetworkNaming namingUtil,
			final CyNetworkFactory cnf, final CyNetworkViewFactory cnvf, final CyNetworkViewManager cnvm,
			final CyEventHelper eventHelper, CyLayoutAlgorithmManager calm, SynchronousTaskManager<?> synctm,
			VisualMappingManager vmm, VisualMappingFunctionFactory vmffc, VisualMappingFunctionFactory vmffd, 
			VisualMappingFunctionFactory vmffp){
		this.netMgr = netMgr;
		this.namingUtil = namingUtil;
		this.cnf = cnf;
		this.cnvf = cnvf;
		this.cnvm = cnvm;
		this.eventHelper = eventHelper;
		this.calm = calm;
		this.synctm = synctm;
		this.vmm = vmm;
		this.vmffc = vmffc;
		this.vmffd = vmffd;
		this.vmffp = vmffp;
	}
	
	public TaskIterator createTaskIterator(){
		return new TaskIterator(new CreatePetriTask(netMgr, namingUtil, cnf, cnvf, cnvm, eventHelper, calm,
				synctm, vmm, vmffc, vmffd, vmffp));
	}

}
