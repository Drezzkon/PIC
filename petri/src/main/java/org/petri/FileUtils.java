package org.petri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class FileUtils {
	File inpFile;

	public FileUtils(File inpFile) {
		this.inpFile = inpFile;
	}

	public void readXML(CyNetwork petriNet) throws Exception {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(inpFile);
		//Generating Nodes for Places
		NodeList listOfPlaces = doc.getElementsByTagName("species");
		CyNode [] cyPlaceArray = new CyNode[listOfPlaces.getLength()];
		petriNet.getDefaultNodeTable().createColumn("id", String.class, true);
		petriNet.getDefaultNodeTable().createColumn("tokens", Integer.class, false);
		petriNet.getDefaultNodeTable().createColumn("initial tokens", Integer.class, true);
		petriNet.getDefaultNodeTable().createColumn("type", String.class, true);
		petriNet.getDefaultNodeTable().createColumn("fired", Integer.class, false);
		int maxAmount = 0;
		for (int i = 0; i<listOfPlaces.getLength(); i++) {
			cyPlaceArray[i] = petriNet.addNode();
			Element element = (Element) listOfPlaces.item(i);
			petriNet.getDefaultNodeTable().getRow(cyPlaceArray[i].getSUID()).set("id", element.getAttribute("id"));
			petriNet.getDefaultNodeTable().getRow(cyPlaceArray[i].getSUID()).set("tokens", Integer.parseInt(element.getAttribute("initialAmount")));
			petriNet.getDefaultNodeTable().getRow(cyPlaceArray[i].getSUID()).set("initial tokens", Integer.parseInt(element.getAttribute("initialAmount")));
			petriNet.getDefaultNodeTable().getRow(cyPlaceArray[i].getSUID()).set("type", "Place");
			if (element.getAttribute("name") != "") {
				petriNet.getDefaultNodeTable().getRow(cyPlaceArray[i].getSUID()).set("name", element.getAttribute("name"));
			}
			else {
				petriNet.getDefaultNodeTable().getRow(cyPlaceArray[i].getSUID()).set("name", element.getAttribute("id"));
			}
			if (Integer.parseInt(element.getAttribute("initialAmount"))> maxAmount) {
				maxAmount = Integer.parseInt(element.getAttribute("initialAmount"));
			}
		}
		NodeList listOfTransitions = doc.getElementsByTagName("reaction");
		CyNode [] cyTransitionArray = new CyNode[listOfTransitions.getLength()];
		CyEdge [] cyEdgeArray = new CyEdge[listOfPlaces.getLength()*listOfTransitions.getLength()];
		int numOfEdges = 0;
		petriNet.getDefaultEdgeTable().createColumn("weight", Integer.class, true);
		for (int i = 0; i<listOfTransitions.getLength(); i++) {
			cyTransitionArray[i] = petriNet.addNode();
			Element element = (Element) listOfTransitions.item(i);
			String id =  element.getAttribute("id");
			petriNet.getDefaultNodeTable().getRow(cyTransitionArray[i].getSUID()).set("id", id);
			petriNet.getDefaultNodeTable().getRow(cyTransitionArray[i].getSUID()).set("type", "Transition");
			petriNet.getDefaultNodeTable().getRow(cyTransitionArray[i].getSUID()).set("fired", 0);
			if (element.getAttribute("name") != null) {
				petriNet.getDefaultNodeTable().getRow(cyTransitionArray[i].getSUID()).set("name", element.getAttribute("name"));
			}
			NodeList children = element.getChildNodes();
			
			for (int index = 0; index<children.getLength(); index++) {
				String nname = children.item(index).getNodeName();
				if (nname.equals("listOfReactants")) {
					NodeList reactants = children.item(index).getChildNodes();
					for (int reactIndex = 0; reactIndex<reactants.getLength(); reactIndex++) {
						if (reactants.item(reactIndex).getNodeType() == Node.ELEMENT_NODE) {
							Element reactant = (Element) reactants.item(reactIndex);
							for (int placeIndex = 0; placeIndex<listOfPlaces.getLength(); placeIndex++) {
								if (petriNet.getDefaultNodeTable().getRow(cyPlaceArray[placeIndex].getSUID()).get("id", String.class).equals(reactant.getAttribute("species"))) {
									cyEdgeArray[numOfEdges] = petriNet.addEdge(cyPlaceArray[placeIndex], cyTransitionArray[i], true);
									petriNet.getDefaultEdgeTable().getRow(cyEdgeArray[numOfEdges].getSUID()).set("weight", Integer.parseInt(reactant.getAttribute("stoichiometry")));
									petriNet.getDefaultEdgeTable().getRow(cyEdgeArray[numOfEdges].getSUID()).set("name", reactant.getAttribute("species")+"->"+id);
									numOfEdges++;
									break;
								}
							}
						}
					}
				}
				else if (nname.equals("listOfProducts")) {
					NodeList products = children.item(index).getChildNodes();
					for (int prodIndex = 0; prodIndex<products.getLength(); prodIndex++) {
						if (products.item(prodIndex).getNodeType() == Node.ELEMENT_NODE) {
							Element product = (Element) products.item(prodIndex);
							for (int placeIndex = 0; placeIndex<listOfPlaces.getLength(); placeIndex++) {
								if (petriNet.getDefaultNodeTable().getRow(cyPlaceArray[placeIndex].getSUID()).get("id", String.class).equals(product.getAttribute("species"))) {
									cyEdgeArray[numOfEdges] = petriNet.addEdge(cyTransitionArray[i], cyPlaceArray[placeIndex], true);
									petriNet.getDefaultEdgeTable().getRow(cyEdgeArray[numOfEdges].getSUID()).set("weight", Integer.parseInt(product.getAttribute("stoichiometry")));
									petriNet.getDefaultEdgeTable().getRow(cyEdgeArray[numOfEdges].getSUID()).set("name", id+"->"+product.getAttribute("species"));
									numOfEdges++;
									break;
								}
							}	
						}
					}
				}
			}
		}
	}

	public void readPNT(CyNetwork petriNet) throws Exception {
		// TODO Auto-generated method stub
	}
	
	public void readAPNN(CyNetwork petriNet) throws Exception {
		// TODO Auto-generated method stub
	}

	public void readDAT(CyNetwork petriNet) throws Exception {
		// TODO Look up how this looks if it has initial tokens and weights -> change accordingly
		BufferedReader br = new BufferedReader(new FileReader(inpFile));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			
			while (line != null){
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			String everything = sb.toString();
			String splitString[] = everything.split("\\r?\\n");
			petriNet.getDefaultNodeTable().createColumn("id", String.class, true);
			petriNet.getDefaultNodeTable().createColumn("tokens", Integer.class, false);
			petriNet.getDefaultNodeTable().createColumn("initial tokens", Integer.class, true);
			petriNet.getDefaultNodeTable().createColumn("type", String.class, true);
			petriNet.getDefaultNodeTable().createColumn("fired", Integer.class, false);
			petriNet.getDefaultEdgeTable().createColumn("weight", Integer.class, true);
			ArrayList<String> transitions = new ArrayList<String>();
			ArrayList<String> places = new ArrayList<String>();
			for (int i = 0; i<splitString.length; i++){
				Integer x = i + 1;
				if (splitString[i].equals("-ENZIRREV")){
					transitions.addAll(Arrays.asList(splitString[x].split("\\s+")));
					CyNode [] cyTransitionArray = new CyNode[transitions.size()];
					for (int y = 0; y < transitions.size(); y++){
						cyTransitionArray[y] = petriNet.addNode();
						petriNet.getDefaultNodeTable().getRow(cyTransitionArray[y].getSUID()).set("id", transitions.get(y));
						petriNet.getDefaultNodeTable().getRow(cyTransitionArray[y].getSUID()).set("name", transitions.get(y));
						petriNet.getDefaultNodeTable().getRow(cyTransitionArray[y].getSUID()).set("type", "Transition");
						petriNet.getDefaultNodeTable().getRow(cyTransitionArray[y].getSUID()).set("fired", 0);
					}
				}
				else if (splitString[i].equals("-METINT")){
					places.addAll(Arrays.asList(splitString[x].split("\\s+")));
					CyNode [] cyPlaceArray = new CyNode[places.size()];
					for (int y = 0; y < places.size(); y ++){
						cyPlaceArray[y] = petriNet.addNode();
						petriNet.getDefaultNodeTable().getRow(cyPlaceArray[y].getSUID()).set("id", places.get(y));
						petriNet.getDefaultNodeTable().getRow(cyPlaceArray[y].getSUID()).set("name", places.get(y));
						petriNet.getDefaultNodeTable().getRow(cyPlaceArray[y].getSUID()).set("tokens", 0);
						petriNet.getDefaultNodeTable().getRow(cyPlaceArray[y].getSUID()).set("initial tokens", 0);
						petriNet.getDefaultNodeTable().getRow(cyPlaceArray[y].getSUID()).set("type", "Place");
					}
				}
				else if (splitString[i].equals("-CAT")){
					Integer numOfEdges = 0;
					CyEdge [] cyEdgeArray = new CyEdge[transitions.size()*places.size()];
					for (int y = x; y < splitString.length; y++){
						String lineSplit[] = splitString[y].split("\\s+");
						String currentTransition = lineSplit[0];
						CyNode trans = null;
						for (CyNode t : petriNet.getNodeList()) {
							if (petriNet.getDefaultNodeTable().getRow(t.getSUID()).get("id", String.class).equals(currentTransition)) {
								trans = t;
								break;
							}
						}
						String currentPosition =  "left";
						for (int z = 0; z < lineSplit.length; z++){
							if (currentPosition.equals("middle") && !lineSplit[z].equals(":") && !lineSplit[z].equals("=")
									&& !lineSplit[z].equals(".") && !lineSplit[z].equals("+")){	
								CyNode place = null;
								for (CyNode p : petriNet.getNodeList()) {
									if (petriNet.getDefaultNodeTable().getRow(p.getSUID()).get("id", String.class).equals(lineSplit[z])) {
										place = p;
										break;
									}
								}
								cyEdgeArray[numOfEdges] = petriNet.addEdge(place, trans, true);
								petriNet.getDefaultEdgeTable().getRow(cyEdgeArray[numOfEdges].getSUID()).set("weight", 0);
								petriNet.getDefaultEdgeTable().getRow(cyEdgeArray[numOfEdges].getSUID()).set("name", lineSplit[z]+"->"+currentTransition);
								numOfEdges++;
							}
							else if (currentPosition.equals("right") && !lineSplit[z].equals(":") && !lineSplit[z].equals("=")
									&& !lineSplit[z].equals(".") && !lineSplit[z].equals("+")){
								CyNode place = null;
								for (CyNode p : petriNet.getNodeList()) {
									if (petriNet.getDefaultNodeTable().getRow(p.getSUID()).get("id", String.class).equals(lineSplit[z])) {
										place = p;
										break;
									}
								}
								cyEdgeArray[numOfEdges] = petriNet.addEdge(trans, place, true);
								petriNet.getDefaultEdgeTable().getRow(cyEdgeArray[numOfEdges].getSUID()).set("weight", 0);
								petriNet.getDefaultEdgeTable().getRow(cyEdgeArray[numOfEdges].getSUID()).set("name", currentTransition+"->"+lineSplit[z]);
								numOfEdges++;
							}
							if (lineSplit[z].equals(":")){
								currentPosition = "middle";
							}
							else if (lineSplit[z].equals("=")){
								currentPosition = "right";
							}
						}
					}
				}
			}
		} finally {
			br.close();
		}
	}

	public void readRL(CyNetwork petriNet) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(inpFile));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			
			while (line != null){
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			String everything = sb.toString();
			String splitString[] = everything.split("\\r?\\n");
			petriNet.getDefaultNodeTable().createColumn("id", String.class, true);
			petriNet.getDefaultNodeTable().createColumn("tokens", Integer.class, false);
			petriNet.getDefaultNodeTable().createColumn("initial tokens", Integer.class, true);
			petriNet.getDefaultNodeTable().createColumn("type", String.class, true);
			petriNet.getDefaultNodeTable().createColumn("fired", Integer.class, false);
			petriNet.getDefaultEdgeTable().createColumn("weight", Integer.class, true);
			ArrayList<CyNode> transitions = new ArrayList<CyNode>();
			ArrayList<CyEdge> edges = new ArrayList<CyEdge>();
			for (String currLine : splitString) {
				String lineSplit[] = currLine.split("\\s+");
				String name = lineSplit[0].substring(0, lineSplit[0].length()-2);
				String dir = "in";
				transitions.add(petriNet.addNode());
				petriNet.getDefaultNodeTable().getRow(transitions.get(transitions.size()-1).getSUID()).set("id", name);
				petriNet.getDefaultNodeTable().getRow(transitions.get(transitions.size()-1).getSUID()).set("name", name);
				petriNet.getDefaultNodeTable().getRow(transitions.get(transitions.size()-1).getSUID()).set("type", "Transition");
				petriNet.getDefaultNodeTable().getRow(transitions.get(transitions.size()-1).getSUID()).set("fired", 0);
				for (int i=1; i<lineSplit.length; i++ ) {
					if (lineSplit[i].equals("->")) {
						dir = "out";
						continue;
					}
					else if (lineSplit[i].equals("+")) {
						continue;
					}
					else {
						CyNode place = null;
						for (CyNode n : petriNet.getNodeList()) {
							if (petriNet.getDefaultNodeTable().getRow(n.getSUID()).get("id", String.class).equals(lineSplit[i])) {
								place = n;
								break;
							}
						}
						if (place == null) {
							place = petriNet.addNode();
							petriNet.getDefaultNodeTable().getRow(place.getSUID()).set("id", lineSplit[i]);
							petriNet.getDefaultNodeTable().getRow(place.getSUID()).set("name", lineSplit[i]);
							petriNet.getDefaultNodeTable().getRow(place.getSUID()).set("initial tokens", 0);
							petriNet.getDefaultNodeTable().getRow(place.getSUID()).set("tokens", 0);
							petriNet.getDefaultNodeTable().getRow(place.getSUID()).set("type", "Place");
						}
						if (dir.equals("in")) {
							edges.add(petriNet.addEdge(place, transitions.get(transitions.size()-1), true));
							petriNet.getDefaultEdgeTable().getRow(edges.get(edges.size()-1).getSUID()).set("weight", 1);
							petriNet.getDefaultEdgeTable().getRow(edges.get(edges.size()-1).getSUID()).set("name", lineSplit[i]+"->"+lineSplit[0]);
						}
						else if (dir.equals("out")) {
							edges.add(petriNet.addEdge(transitions.get(transitions.size()-1), place, true));
							petriNet.getDefaultEdgeTable().getRow(edges.get(edges.size()-1).getSUID()).set("weight", 1);
							petriNet.getDefaultEdgeTable().getRow(edges.get(edges.size()-1).getSUID()).set("name", lineSplit[0]+"->"+lineSplit[i]);
						}
					}
				}
			}
		}
		finally {
			br.close();
		}
	}

	public void choose(String ext, CyNetwork petriNet) throws Exception {
		if (ext.equals("xml")) {
			readXML(petriNet);
		}
		else if (ext.equals("pnt")) {
			readPNT(petriNet);
		}
		else if (ext.equals("apnn")) {
			readAPNN(petriNet);
		}
		else if (ext.equals("dat")) {
			readDAT(petriNet);
		}
		else if (ext.equals("txt")) {
			readRL(petriNet);
		}
		else {
			throw new Exception("Wrong extension!");
		}
	}


}
