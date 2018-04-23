package org.petri;

import java.io.File;
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
			if (element.getAttribute("name") != null) {
				petriNet.getDefaultNodeTable().getRow(cyPlaceArray[i].getSUID()).set("name", element.getAttribute("name"));
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

	public void readAPNN(CyNetwork petriNet) throws Exception {
		// TODO Auto-generated method stub
	}

	public void readDAT(CyNetwork petriNet) throws Exception {
		// TODO Auto-generated method stub
	}

	public void readRL(CyNetwork petriNet) throws Exception {
		// TODO Auto-generated method stub
	}

	public void choose(String ext, CyNetwork petriNet) throws Exception {
		if (ext.equals("xml")) {
			readXML(petriNet);
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
