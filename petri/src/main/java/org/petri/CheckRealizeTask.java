package org.petri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JComboBox;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

/**
 * Task to check whether one/all??? invariant(s) is/are realizable
 * @author M. Gehrmann
 *
 */
public class CheckRealizeTask extends AbstractTask {
	@Tunable(description="Yes to check all invariants, No to check only currently selected invariant")
	public boolean all;
	private CyNetwork petriNet;
	private JComboBox<String> invarHolder;

	/**
	 * Constructor
	 * @param petriNet Petri net currently being worked on
	 * @param invarHolder Container for invariants
	 */
	public CheckRealizeTask(CyNetwork petriNet, JComboBox<String> invarHolder) {
		this.petriNet = petriNet;
		this.invarHolder = invarHolder;
	}
	
	public void run(TaskMonitor taskMonitor) throws Exception {
		/*
		 * Alright, lets start to rant a little about what to do here. Basically, we have two main options.
		 * 
		 * 		1. Check whether the currently selected invariant is realizable
		 * 		2. Check for all invariants, whether they are realizable
		 * 
		 * Second option would, of course, take a lot less time, as we only have to check for one invariant.
		 * Also, we are only supposed to check it for a given marking. Would also be possible to change
		 * this up and check for all possible markings that are relevant to the transition. Relevance might
		 * be very interesting to compute. We might even be able to make use of some tricks Marius' might use
		 * for his computation of invariants, after all we are interested in inferring knowledge about invariants
		 * from only fractions of their data, which might also help in calculating them in the first place.
		 * But, as this is something more for the future, we will leave it at that for now.
		 * Generally, checking for ONE invariant has to be implemented, with checking for all just being a
		 * different initialization of this function, but essentially the same thing, just done repeatedly.
		 * As such, we will start with implementing this for ONE invariant. Should build a switch tunable
		 * so user can decide whether he wants one or all invariants checked.
		 */
		// First, we have to gather the invariant from the container. Do this in for loop to get all invariants
		String invar = (String) invarHolder.getSelectedItem();
		// Extract transitions from string by name
		String[] transitionArray = invar.split(", ");
		ArrayList<CyNode> transitions = new ArrayList<CyNode>();
		HashMap<String, String> times = new HashMap<String, String>(); // Use this to store how often transitions are fired
		String amt;
		HashSet<CyNode> places = new HashSet<CyNode>();
		HashMap<String, Integer> tokens = new HashMap<String, Integer>();
		for (String trans : transitionArray) {
			amt = "1"; // Default
			if (trans.contains(" ")) {
				amt = trans.split(" ")[0];
				trans = trans.split(" ")[1];
			}
			times.put(trans, amt);
			for (CyNode n : petriNet.getNodeList()) {
				if (petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("name", String.class).equals(trans)) {
					transitions.add(n);
					// Gather all places influenced by the invariant
					// Need to get the marking and map it
					for (CyEdge e : petriNet.getAdjacentEdgeIterable(n, CyEdge.Type.INCOMING)) {
						places.add(e.getSource());
						tokens.put(petriNet.getDefaultNodeTable().getRow(e.getSource().getSUID()).get("name", String.class),
								petriNet.getDefaultNodeTable().getRow(e.getSource().getSUID()).get("tokens",Integer.class));
					}
					for (CyEdge e : petriNet.getAdjacentEdgeIterable(n, CyEdge.Type.OUTGOING)) {
						places.add(e.getTarget());
						tokens.put(petriNet.getDefaultNodeTable().getRow(e.getTarget().getSUID()).get("name", String.class),
								petriNet.getDefaultNodeTable().getRow(e.getTarget().getSUID()).get("tokens",Integer.class));
					}
					break;
				}
			}
			// Now we have all transitions in an ArrayList and the marking mapped to tokens
			// Anyway, I need to generate ALL permutations next, not just ONE. Should probably do this by Branch and Bound?
			// Would probably require a separate function then, should implement that in PetriUtils, I guess?
		}
	}
}