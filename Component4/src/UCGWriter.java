
import java.util.*;
import javax.xml.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import java.io.*;

public class UCGWriter {
	
	Document document;
	DocumentBuilderFactory docFactory;
	DocumentBuilder docBuilder;
	DOMSource source;
	
	List<Document> documentList = new ArrayList<Document>();
	
	Element rootElement;
//	Element feature;
	
	TransformerFactory transformerFactory;
    Transformer transformer;
	
    
	List<String> object = new ArrayList<String>();	// Strings of the object
	
	List<List<String>> landmarks = new ArrayList<List<String>>();	// Strings of the landmarks
	List<List<String>> arcs = new ArrayList<List<String>>();	// Strings of the preposition/specifier
	
	int numNodes;	// The total number of nodes in the graph
	
	// Node features list
	String[] featuresList = {"called", "cg_role","definiteness","determiner"};
	
	public UCGWriter() {}
	
	// Input a tagged utterance
	// Sorts into objects, landmarks and arcs (prepositions and specifiers)
	// Stores words in lists
	public void SortTaggedUtterance(String taggedUtterance) {
		
		// Stores words and their tags in a list
		// [The:B-O, chair:I-O, in:B-P, front:I-P,...]
		List<String> utteranceList = new ArrayList<String>(Arrays.asList(taggedUtterance.split(" ")));
		
		int index = 0;
		int currentArc = 0;
		List<String> tmpArcList = new ArrayList<String>();
		while(index < utteranceList.size()) {
			String tmpString = utteranceList.get(index);
			// Breaks up the word and the tag so they can be assessed individually, [chair, I-O]
			List<String> tmpList = new ArrayList<String>(Arrays.asList(tmpString.split(":")));
			String word = tmpList.get(0);
			String tag = tmpList.get(1);
			if (tag.charAt(2) == 'O') {
				object.add(word);
			} else if (tag.charAt(2) == 'L') {
				if (tag.charAt(0) == 'B') {
					List<String> newLandmarkList = new ArrayList<String>();
					newLandmarkList.add(word);
					landmarks.add(newLandmarkList);
				} else {
					landmarks.get(landmarks.size() - 1).add(word);
				}
			} else if (tag.charAt(2) == 'P' || tag.charAt(2) == 'S') {
				if (tag.equals("B-P")) {
					List<String> newArcList = new ArrayList<String>();
					newArcList.add(word);
					arcs.add(newArcList);
				} else {
					arcs.get(arcs.size() - 1).add(word);
				}
			}
			index += 1;
		}
		numNodes = 1 + landmarks.size();	// Set the number of nodes
	}
	
	public void InitialiseDocument() {
		docFactory = DocumentBuilderFactory.newInstance();
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		document = docBuilder.newDocument();
		
		// Create the root element
		rootElement = document.createElement("CG");
		document.appendChild(rootElement);
	}
	
	/*
	 * The nodes of the UCG will be made up of an object and landmarks
	 * These are generated first with no arcs in the order they are spoken
	 */
	public void GenerateNodes() {
		
		for (int i = 0; i < numNodes; i++) {
			List<String> nodeList = new ArrayList<String>();
			if (i == 0) {
				nodeList = object;
			} else {
				nodeList = landmarks.get(i - 1);
			}
			
			Element node = document.createElement("node");
			rootElement.appendChild(node);
			
			node.setAttribute("label", "Node" + i);
			
			Element concept = document.createElement("concept");
			concept.setAttribute("kind", "base");
			node.appendChild(concept);
			
			// Add the node features
			for (int j = 0; j < 4; j++) {
				Element feature = document.createElement("feature");
				feature.setAttribute("key", featuresList[j]);
				switch(j) {
				case 0:	// called
					feature.setAttribute("value", nodeList.get(nodeList.size() - 1));
					break;
				case 1:	// cg_role
					feature.setAttribute("value","node");
					break;
				case 2:	// definiteness
					if (nodeList.get(0).equals("a")) {
						feature.setAttribute("value", "indefinite");
					} else {
						feature.setAttribute("value", "definite");
					}
					break;
				case 3:	//determiner
					feature.setAttribute("value", "definite");
					break;
				default:
					break;
				}
				concept.appendChild(feature);
			}
		}
	}
	
	/*
	 * Generate the arcs in the UCG using the prepositions and specifiers
	 * The method only supports at most two arcs (two landmarks)
	 * The arc between the object and the first spoken landmark will always be the same
	 * The second arc can be from the object to landmark2 or landmark1 to landmark2
	 * After the second arc is added the UCG is built
	 */
	public void GenerateArcs() {
		
		// Clean the directory before writing
		CleanDirectory();
		
		int numUCGs = landmarks.size(); // number UCGs based on landmarks
		
		NodeList nList = document.getElementsByTagName("node");
		
		for (int i = 0; i < numUCGs; i++) {
			for (int j = 0; j < arcs.size(); j++) {
				// Create the arc element
				Element arc = document.createElement("arc");
				arc.setAttribute("label", "Arc" + j);
				// Create the concept and add to the arc
				Element concept = document.createElement("concept");
				concept.setAttribute("kind", "base");
				// Add the concept to the arc
				arc.appendChild(concept);
				for (int k = 0; k < 2; k++) {
					Element feature = document.createElement("feature");
					feature.setAttribute("key",featuresList[k]);
					switch (k) {
					case 0:
						List<String> arcValueList = arcs.get(j);
						String arcValueString = arcValueList.get(0);
						for (int l = 1; l < arcValueList.size(); l++) {
							arcValueString += "_" + arcValueList.get(l);
						}
						feature.setAttribute("value", arcValueString);
						break;
					case 1:
						feature.setAttribute("value","node");
						break;
					default:
						break;
					}
					concept.appendChild(feature);
				}
				Element child = document.createElement("child");
				Element childNode = (Element) nList.item(j+1); // the landmark the arc points to
				child.setAttribute("node", childNode.getAttribute("label"));
				arc.appendChild(child);
				
				// The first arc is always appended to the object
				// Only need to append it once hence i == 0
				if (i == 0) {
					Element parentNode = (Element) nList.item(0);
					parentNode.appendChild(arc);
//					BuildXML(i);
				}
				// Sets the parent node for the arc
				// Uses i to determine if it is the object or the first landmark
				// This will be called for each iteration of i, hence numUCGs
				// This only works for at most two landmarks
				// Because this arc is only temporary, build the UCG here and then reomve the arc
				if (j == arcs.size() - 1) {
					Element parentNode = (Element) nList.item(i);
					parentNode.appendChild(arc);
					BuildXML(i);
					parentNode.removeChild(arc);
				}
			}
//			BuildXML(i);
		}
	}
	
	/*
	 * Cleans the UCG directory
	 * Removes any existing UCGs in the file
	 */
	public void CleanDirectory() {
		File directory = new File("UCG");
		File[] directoryFileList = directory.listFiles();
		for (File f : directoryFileList) {
			if (f.isFile() && f.exists()) {
				f.delete();
			}
		}
	}
	
	/*
	 * Writes the XML to a file
	 */
	public void BuildXML (int fileNumber) {
		
		transformerFactory = TransformerFactory.newInstance();
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		StreamResult result = new StreamResult(new File("UCG/UCG_" + fileNumber + ".xml"));
		try {
			transformer.transform(new DOMSource(document), result);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
