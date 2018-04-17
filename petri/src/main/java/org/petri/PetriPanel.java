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
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;

public class PetriPanel extends JPanel implements CytoPanelComponent {

	private static final long serialVersionUID = -6054408975485644227L;
	private JPanel jPanel;

	public PetriPanel(final CyNetworkManager cyNetworkManagerServiceRef,
			final CyNetworkNaming cyNetworkNamingServiceRef,
			final CyNetworkFactory cyNetworkFactoryServiceRef,
			final CyNetworkViewFactory cyNetworkViewFactoryServiceRef,
			final CyNetworkViewManager cyNetworkViewManagerServiceRef, 
			final CyEventHelper eventHelperServiceRef,
			final CyLayoutAlgorithmManager cyLayoutAlgorithmManagerRef,
			final SynchronousTaskManager<?> synchronousTaskManagerRef,
			final VisualMappingManager visualMappingManagerRef,
			final VisualMappingFunctionFactory visualMappingFunctionFactoryRefc,
			final VisualMappingFunctionFactory visualMappingFunctionFactoryRefd,
			final VisualMappingFunctionFactory visualMappingFunctionFactoryRefp,
			final CyAppAdapter adapter) {
		super();
		final CyNetwork petriNet = cyNetworkFactoryServiceRef.createNetwork();
		final CreatePetriTaskFactory createPetriTaskFactory = new CreatePetriTaskFactory(cyNetworkManagerServiceRef,
				cyNetworkNamingServiceRef,cyNetworkViewFactoryServiceRef,cyNetworkViewManagerServiceRef,
				eventHelperServiceRef,cyLayoutAlgorithmManagerRef,synchronousTaskManagerRef, 
				visualMappingManagerRef, visualMappingFunctionFactoryRefc,visualMappingFunctionFactoryRefd, 
				visualMappingFunctionFactoryRefp, petriNet);
		final FireNetwork fn = new FireNetwork(petriNet);
		jPanel = new JPanel();
		jPanel.setBackground(Color.WHITE);
		jPanel.setLayout(new BorderLayout());
		JPanel top = new JPanel();
		top.setLayout(new GridLayout(0,1));
		top.add(new Label("Control Panel for Petri Net App"));
		Button loadBut = new Button("Load Petri Net");
		loadBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TaskIterator petri = createPetriTaskFactory.createTaskIterator();
				adapter.getTaskManager().execute(petri);
				SynchronousTaskManager<?> synTaskMan = adapter.getCyServiceRegistrar().getService(SynchronousTaskManager.class);
				synTaskMan.execute(petri);
			}
		});
		top.add(loadBut);
		top.add(new Label("Which Network do you want to fire?"));
		top.add(new TextField());
		top.add(new Label("How often do you want to fire?"));
		top.add(new TextField());
		jPanel.add(top, BorderLayout.PAGE_START);
		JPanel but = new JPanel();
		but.setLayout(new GridLayout(0,1));
		Button fireBut = new Button("Fire Petri Net");
		fireBut.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fn.fire();
				CyNetworkView [] cnv = new CyNetworkView[1];
				cyNetworkViewManagerServiceRef.getNetworkViews(petriNet).toArray(cnv);
				cnv[0].updateView();
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
