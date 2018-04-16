package org.petri;
	
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JPanel;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;

public class PetriPanel extends JPanel implements CytoPanelComponent {

	private static final long serialVersionUID = -6054408975485644227L;

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
