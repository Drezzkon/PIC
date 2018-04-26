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

/**
 * Panel for using the app. Is added as an additional
 * tab in the control panel of Cytoscape and controls
 * both loading input files and firing/resetting the network. 
 * @author M. Gehrmann, M. Kirchner
 *
 */
public class PetriPanel extends JPanel implements CytoPanelComponent {

	private static final long serialVersionUID = -6054408975485644227L;
	private JPanel jPanel;
	private CyNetwork petriNet;
	private PetriUtils petriUtils;
	private PetriTaskFactory PetriTaskFactory;

	/**
	 * Constructor
	 * @param cyNetworkManagerServiceRef
	 * @param cyNetworkNamingServiceRef
	 * @param cyNetworkFactoryServiceRef
	 * @param cyNetworkViewFactoryServiceRef
	 * @param cyNetworkViewManagerServiceRef
	 * @param eventHelperServiceRef
	 * @param cyLayoutAlgorithmManagerRef
	 * @param synchronousTaskManagerRef
	 * @param visualMappingManagerRef
	 * @param visualMappingFunctionFactoryRefd
	 * @param visualMappingFunctionFactoryRefp
	 * @param adapter
	 */
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
		jPanel = new JPanel();					// Main panel, later added to PetriPanel
		jPanel.setBackground(Color.WHITE);
		jPanel.setLayout(new BorderLayout());
		JPanel top = new JPanel();				// Upper Panel of jPanel
		top.setLayout(new GridLayout(0,1));
		top.add(new Label("Control Panel for Petri Net App"));
		Button loadBut = new Button("Load Petri Net");		// Button for loading new Petri Nets
		loadBut.addActionListener(new ActionListener() {	
			public void actionPerformed(ActionEvent e) {
				if (petriNet != null) {						// Destroy previously loaded Petri Net, only one active at a time
				cyNetworkManagerServiceRef.destroyNetwork(petriNet);
				}
				petriNet = cyNetworkFactoryServiceRef.createNetwork();	// New Network for Petri Net
				cyNetworkManagerServiceRef.addNetwork(petriNet);
				petriUtils = new PetriUtils(petriNet, cyNetworkViewManagerServiceRef); // Used for updating views later on
				PetriTaskFactory = new PetriTaskFactory(cyNetworkManagerServiceRef,	// Fill Petri Net with nodes and apply default views/layout
						cyNetworkNamingServiceRef,cyNetworkViewFactoryServiceRef,cyNetworkViewManagerServiceRef,
						eventHelperServiceRef,cyLayoutAlgorithmManagerRef, adapter, 
						visualMappingManagerRef,visualMappingFunctionFactoryRefd, 
						visualMappingFunctionFactoryRefp, petriNet, petriUtils);
				TaskIterator petri = PetriTaskFactory.createTaskIterator();
				adapter.getTaskManager().execute(petri);
				SynchronousTaskManager<?> synTaskMan = adapter.getCyServiceRegistrar().getService(SynchronousTaskManager.class);
				synTaskMan.execute(petri);
			}
		});
		top.add(loadBut);
		Button resetBut = new Button("Reset Petri Net");	// Button for resetting tokens and fired
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
		final TextField times = new TextField("1");			// Used to determine how often to fire on button click
		top.add(times);
		jPanel.add(top, BorderLayout.PAGE_START);
		JPanel but = new JPanel();					// Lower panel of jPanel
		but.setLayout(new GridLayout(0,1));
		Button fireBut = new Button("Fire Petri Net"); 		// Button for firing the Petri Net
		fireBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CyNode[] cyTransitionArray = petriUtils.getTransitions();
				for (int i=0; i<Integer.parseInt(times.getText()); i++) {				// Fire Petri Net x times
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
		return CytoPanelName.WEST;	// Add to Control Panel
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
