package org.petri;
	
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.TextField;

import javax.swing.Icon;
import javax.swing.JPanel;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;

public class PetriPanel extends JPanel implements CytoPanelComponent {

	private static final long serialVersionUID = -6054408975485644227L;
	private JPanel jPanel;

	/*public PetriPanel() {
		super();
		this.add(new Label("Control Panel for Petri Net App"));
		this.add(new Label("Which Network do you want to fire?"));
		this.add(new TextField());
		this.add(new Label("How often do you want to fire?"));
		this.add(new TextField());
		this.add(new Button("Fire Petri Net"));
	}*/

	@Override
	public Component getComponent() {
		jPanel = new JPanel();
		jPanel.setBackground(Color.WHITE);
		jPanel.setLayout(new BorderLayout());
		JPanel top = new JPanel();
		top.setLayout(new GridLayout(0,1));
		top.add(new Label("Control Panel for Petri Net App"));
		top.add(new Label("Which Network do you want to fire?"));
		top.add(new TextField());
		top.add(new Label("How often do you want to fire?"));
		top.add(new TextField());
		jPanel.add(top, BorderLayout.PAGE_START);
		JPanel but = new JPanel();
		but.setLayout(new GridLayout(0,1));
		but.add(new Button("Fire Petri Net"));
		jPanel.add(but, BorderLayout.CENTER);
		return jPanel;
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
