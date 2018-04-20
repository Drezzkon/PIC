package org.petri;
	
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JPanel;

import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;

public class PetriPanel extends JPanel implements CytoPanelComponent {

	private static final long serialVersionUID = -6054408975485644227L;
	private JPanel jPanel;
	private CyNetwork petriNet;
	private PetriUtils petriUtils;
	private CreatePetriTaskFactory createPetriTaskFactory;

	public PetriPanel(final CyNetworkManager cyNetworkManagerServiceRef,
			final CyNetworkNaming cyNetworkNamingServiceRef,
			final CyNetworkFactory cyNetworkFactoryServiceRef,
			final CyNetworkViewFactory cyNetworkViewFactoryServiceRef,
			final CyNetworkViewManager cyNetworkViewManagerServiceRef, 
			final CyEventHelper eventHelperServiceRef,
			final CyLayoutAlgorithmManager cyLayoutAlgorithmManagerRef,
			final SynchronousTaskManager<?> synchronousTaskManagerRef,
			final VisualMappingManager visualMappingManagerRef,
			final VisualMappingFunctionFactory visualMappingFunctionFactoryRefd,
			final VisualMappingFunctionFactory visualMappingFunctionFactoryRefp,
			final CyAppAdapter adapter) {
		super();
		jPanel = new JPanel();
		jPanel.setBackground(Color.WHITE);
		jPanel.setLayout(new BorderLayout());
		JPanel top = new JPanel();
		top.setLayout(new GridLayout(0,1));
		top.add(new Label("Control Panel for Petri Net App"));
		Button loadBut = new Button("Load Petri Net");
		loadBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (petriNet != null) {
				cyNetworkManagerServiceRef.destroyNetwork(petriNet);
				}
				petriNet = cyNetworkFactoryServiceRef.createNetwork();
				cyNetworkManagerServiceRef.addNetwork(petriNet);
				createPetriTaskFactory = new CreatePetriTaskFactory(cyNetworkManagerServiceRef,
						cyNetworkNamingServiceRef,cyNetworkViewFactoryServiceRef,cyNetworkViewManagerServiceRef,
						eventHelperServiceRef,cyLayoutAlgorithmManagerRef, adapter, 
						visualMappingManagerRef,visualMappingFunctionFactoryRefd, 
						visualMappingFunctionFactoryRefp, petriNet);
				petriUtils = new PetriUtils(petriNet, cyNetworkViewManagerServiceRef);
				TaskIterator petri = createPetriTaskFactory.createTaskIterator();
				adapter.getTaskManager().execute(petri);
				SynchronousTaskManager<?> synTaskMan = adapter.getCyServiceRegistrar().getService(SynchronousTaskManager.class);
				synTaskMan.execute(petri);
			}
		});
		top.add(loadBut);
		Button resetBut = new Button("Reset Petri Net");
		resetBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				petriUtils.reset();
				TaskIterator itr = petriUtils.updateView();
				adapter.getTaskManager().execute(itr);
				SynchronousTaskManager<?> synTaskMan = adapter.getCyServiceRegistrar().getService(SynchronousTaskManager.class);
				synTaskMan.execute(itr);
			}
		});
		top.add(resetBut);
		top.add(new Label("How often do you want to fire?"));
		final TextField times = new TextField("1");
		top.add(times);
		jPanel.add(top, BorderLayout.PAGE_START);
		JPanel but = new JPanel();
		but.setLayout(new GridLayout(0,1));
		Button fireBut = new Button("Fire Petri Net");
		fireBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int length = 0;
				for (CyNode n : petriNet.getNodeList()) {
					String ntype = (String) (petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("type", String.class));
					if (ntype.equals("Transition")) {
						length++;				
					}
				}
				CyNode[] cyTransitionArray = new CyNode[length];
				length = 0;
				for (CyNode n : petriNet.getNodeList()) {
					String ntype = (String) (petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("type", String.class));
					if (ntype.equals("Transition")) {
						cyTransitionArray[length] = n;
						length++;
					}
				}
				for (CyNode n : cyTransitionArray) {
					petriNet.getDefaultNodeTable().getRow(n.getSUID()).set("fired", 0);
				}
				for (int i=0; i<Integer.parseInt(times.getText()); i++) {
					petriUtils.fire(cyTransitionArray);
				}
				TaskIterator itr = petriUtils.updateView();
				adapter.getTaskManager().execute(itr);
				SynchronousTaskManager<?> synTaskMan = adapter.getCyServiceRegistrar().getService(SynchronousTaskManager.class);
				synTaskMan.execute(itr);
			}
		});
		but.add(fireBut);
		jPanel.add(but, BorderLayout.PAGE_END);
		this.add(jPanel);
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public String getTitle() {
		return "PetriNet";
	}
	
}
