package org.petri;
	
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
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
	private boolean firingMode; // Async = false, Sync = true
	private boolean random;

	/**
	 * Constructor
	 * @param cyNetworkManagerServiceRef CyNetworkManager
	 * @param cyNetworkNamingServiceRef CyNetworkNaming
	 * @param cyNetworkFactoryServiceRef CyNetworkFactory
	 * @param cyNetworkViewFactoryServiceRef CyNetworkViewFactory
	 * @param cyNetworkViewManagerServiceRef CyNetworkViewManager
	 * @param eventHelperServiceRef EventHelper
	 * @param cyLayoutAlgorithmManagerRef CyLayoutAlgorithmManager
	 * @param synchronousTaskManagerRef SynchronousTaskManager
	 * @param visualMappingManagerRef VisualMappingManager
	 * @param visualMappingFunctionFactoryRefd VisualMappingFunctionFactory for discrete mappings
	 * @param adapter CyAppAdapter
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
			final CyAppAdapter adapter) {
		super();
		jPanel = new JPanel();					// Main panel, later added to PetriPanel
		jPanel.setBackground(Color.WHITE);
		jPanel.setLayout(new BorderLayout());
		JPanel top = new JPanel();				// Upper Panel of jPanel
		top.setLayout(new GridLayout(0,1));
		top.add(new Label("Control Panel for Petri Net App"));
		final JComboBox<String> invarHolder = new JComboBox<String>();
		JButton createBut = new JButton("Create new Petri Net");
		createBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (petriNet != null) {						// Destroy previously loaded Petri Net, only one active at a time
					invarHolder.removeAllItems();
					cyNetworkManagerServiceRef.destroyNetwork(petriNet);
				}
				petriNet = cyNetworkFactoryServiceRef.createNetwork();	// New Network for Petri Net
				cyNetworkManagerServiceRef.addNetwork(petriNet);
				petriUtils = new PetriUtils(petriNet, cyNetworkViewManagerServiceRef,	// Used for updating views later on
						cyNetworkViewFactoryServiceRef, visualMappingManagerRef,
						cyLayoutAlgorithmManagerRef, adapter, visualMappingFunctionFactoryRefd); 
				petriUtils.initializeColumns();
				petriUtils.createVisualStyle();
			}
		});
		top.add(createBut);
		JButton placeBut = new JButton("Create new place");
		placeBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TaskIterator itr = petriUtils.createPlace();
				adapter.getTaskManager().execute(itr);
				SynchronousTaskManager<?> synTaskMan = adapter.getCyServiceRegistrar().getService(SynchronousTaskManager.class);
				synTaskMan.execute(itr);
			}
		});
		top.add(placeBut);
		JButton transBut = new JButton("Create new transition");
		transBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TaskIterator itr = petriUtils.createTransition();
				adapter.getTaskManager().execute(itr);
				SynchronousTaskManager<?> synTaskMan = adapter.getCyServiceRegistrar().getService(SynchronousTaskManager.class);
				synTaskMan.execute(itr);
			}
		});
		top.add(transBut);
		JButton edgeBut = new JButton("Create new edge");
		edgeBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TaskIterator itr = petriUtils.createEdge();
				adapter.getTaskManager().execute(itr);
				SynchronousTaskManager<?> synTaskMan = adapter.getCyServiceRegistrar().getService(SynchronousTaskManager.class);
				synTaskMan.execute(itr);
			}
		});
		top.add(edgeBut);
		JButton viewBut = new JButton("Update Views");
		viewBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TaskIterator itr = petriUtils.updateView();
				adapter.getTaskManager().execute(itr);
				SynchronousTaskManager<?> synTaskMan = adapter.getCyServiceRegistrar().getService(SynchronousTaskManager.class);
				synTaskMan.execute(itr);
			}
		});
		top.add(viewBut);
		JButton loadBut = new JButton("Load Petri Net");		// Button for loading new Petri Nets
		loadBut.addActionListener(new ActionListener() {	
			public void actionPerformed(ActionEvent e) {
				if (petriNet != null) {						// Destroy previously loaded Petri Net, only one active at a time
					invarHolder.removeAllItems();
					cyNetworkManagerServiceRef.destroyNetwork(petriNet);
				}
				petriNet = cyNetworkFactoryServiceRef.createNetwork();	// New Network for Petri Net
				cyNetworkManagerServiceRef.addNetwork(petriNet);
				petriUtils = new PetriUtils(petriNet, cyNetworkViewManagerServiceRef,	 // Used for updating views later on
						cyNetworkViewFactoryServiceRef, visualMappingManagerRef,
						cyLayoutAlgorithmManagerRef, adapter, visualMappingFunctionFactoryRefd);
				PetriTaskFactory = new PetriTaskFactory(cyNetworkManagerServiceRef,	// Fill Petri Net with nodes and apply default views/layout
						cyNetworkNamingServiceRef,cyNetworkViewManagerServiceRef,
						eventHelperServiceRef,petriNet, petriUtils);
				TaskIterator petri = PetriTaskFactory.createTaskIterator();
				adapter.getTaskManager().execute(petri);
				SynchronousTaskManager<?> synTaskMan = adapter.getCyServiceRegistrar().getService(SynchronousTaskManager.class);
				synTaskMan.execute(petri);
			}
		});
		top.add(loadBut);
		JButton veriBut = new JButton("Verify PetriNet");
		veriBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				petriUtils.verifyNet();
			}
		});
		top.add(veriBut);
		JButton resetBut = new JButton("Reset Petri Net");	// Button for resetting tokens and fired
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
		invarHolder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (invarHolder.getItemCount() == 0) { // Do not update on resetting the container
					return;
				}
				Integer[] invar = petriUtils.invars.get(invarHolder.getSelectedIndex());
				CyNetworkView [] cnvs = new CyNetworkView[1];
				cyNetworkViewManagerServiceRef.getNetworkViews(petriNet).toArray(cnvs);
				CyNetworkView cnv = cnvs[0];
				for (View <CyEdge> edgeview : cnv.getEdgeViews()){ // Clear locked edge colours from previously selected invariant
					edgeview.clearValueLock(BasicVisualLexicon.EDGE_PAINT);
				}
				// Gather all transitions belonging to invariant and paint their edges
				for (int i=1; i<=invar.length; i++) {
					if(invar[invar.length-i] == 1) {
						CyNode trans = null;
						for (CyNode n : petriNet.getNodeList()) {
							if (petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("internal id", String.class).equals("t"+Integer.toString(i-1))) {
								trans = n;
								break;
							}
						}
						Iterable<CyEdge>edges = petriNet.getAdjacentEdgeIterable(trans, CyEdge.Type.DIRECTED);
						for (CyEdge edge :edges) {
							View<CyEdge> edgeview = cnv.getEdgeView(edge);
							edgeview.setLockedValue(BasicVisualLexicon.EDGE_PAINT, Color.CYAN);
						}
					}
				}
			}
		});
		JButton invarBut = new JButton("Calculate T-Invariants");	// Button for calculating invariants
		invarBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFrame f = new JFrame("Warning");
				int choice = JOptionPane.showConfirmDialog(f, "Do you really want to calculate invariants?");
				if (choice != JOptionPane.YES_OPTION) {
					return;
				}
				invarHolder.removeAllItems();
				CyNode[] cyTransitionArray = petriUtils.getTransitions();
				CyNode[] cyPlaceArray = petriUtils.getPlaces();
				ArrayList<Integer[]> invars = petriUtils.invar(cyTransitionArray, cyPlaceArray);
				petriUtils.invars = invars;
				// Transform invariants for visual representation and add them to container
				for (int index=0; index<invars.size(); index++) {
					Integer[] invar = invars.get(index);
					String empty = "";
					Integer current = 0;
					for (int i=1; i<=invar.length; i++) {
						if (invar[invar.length-i] == 1) {
							String name = "";
							for (CyNode n : petriNet.getNodeList()) {
								if (petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("internal id", String.class).equals("t"+current.toString())) {
									name = petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("name", String.class);
									break;
								}
							}
							empty += name + ", ";
						}
						current++;
					}
					invarHolder.addItem(empty.substring(0, empty.lastIndexOf(",")));
				}
				TaskIterator itr = petriUtils.updateView();
				adapter.getTaskManager().execute(itr);
				SynchronousTaskManager<?> synTaskMan = adapter.getCyServiceRegistrar().getService(SynchronousTaskManager.class);
				synTaskMan.execute(itr);
				petriUtils.is_CTI();
			}
		});
		top.add(invarBut);
		top.add(new Label("How often do you want to fire?"));
		final TextField times = new TextField("1");			// Used to determine how often to fire on button click
		top.add(times);
		JButton fireBut = new JButton("Fire Petri Net"); 		// Button for firing the Petri Net
		fireBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				CyNode[] cyTransitionArray = petriUtils.getTransitions();
				for (int i=0; i<Integer.parseInt(times.getText()); i++) {				// Fire Petri Net x times
					petriUtils.fire(cyTransitionArray, firingMode, random);
				}
				TaskIterator itr = petriUtils.updateView();
				adapter.getTaskManager().execute(itr);
				SynchronousTaskManager<?> synTaskMan = adapter.getCyServiceRegistrar().getService(SynchronousTaskManager.class);
				synTaskMan.execute(itr);
			}
		});
		top.add(fireBut);
		top.add(invarHolder);
		jPanel.add(top, BorderLayout.PAGE_START);
		
		JPanel bot = new JPanel();					// Lower panel of jPanel
		bot.setLayout(new GridLayout(0,2));
		JRadioButton radSync = new JRadioButton("Synchronous firing");
		firingMode = false;
		radSync.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				firingMode = true;
			}
		});
		JRadioButton radAsync = new JRadioButton("Asynchronous firing");
		radAsync.setSelected(true);
		radAsync.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				firingMode = false;
			}
		});
		random = false;
		JCheckBox rndSel = new JCheckBox("Randomize firing order");
		rndSel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				random = !random;
			}
		});
		ButtonGroup frOpt = new ButtonGroup();
		frOpt.add(radSync);
		frOpt.add(radAsync);
		bot.add(radSync);
		bot.add(radAsync);
		bot.add(rndSel);
		jPanel.add(bot, BorderLayout.PAGE_END);
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
