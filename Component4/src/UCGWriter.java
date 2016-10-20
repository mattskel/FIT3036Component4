/*
 * Generates the UCG for a tagged utterance
 * Takes as input a tagged utterance
 * Based on the tags sorts the input into landmarks/objects and relations
 * landmarks/objects are represented as nodes in the UCG
 * relations are represented as arcs
 */

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
	
    List<List<String>> objects = new ArrayList<List<String>>();		//Strings of all the objects and landmarks
	List<List<String>> arcs = new ArrayList<List<String>>();	// Strings of the preposition/specifier
	
	int numNodes;	// The total number of nodes in the graph
	boolean printMode = true;
	
	// Node features list
	String[] featuresList = {"called", "cg_role","definiteness","determiner"};
	
	public UCGWriter() {}
	
	public void SetPrintMode(boolean printModeIn) {printMode = printModeIn;}
	
	/*
	 * Sorts a tagged input string into objects and arcs
	 * Objects and landmarks are both treated as being the same
	 * The tags are extracted from from the words and stored without tags
	 * Input: "plate:O at:P table:L at:P..."
	 * Generated: [plate,table,...], [at,...]
	 */
	public void SortTaggedUtterance(String taggedUtterance) {
		
		// Stores words and their tags in a list
		// [The:B-O, chair:I-O, in:B-P, front:I-P,...]
		if (taggedUtterance == null){taggedUtterance = "thing:O";}
		List<String> utteranceList = new ArrayList<String>(Arrays.asList(taggedUtterance.split(" ")));
		
		int index = 0;
		int lastTag = 0; //1: O, 2:P/S, 3: L
		int currentArc = 0;
		List<String> tmpArcList = new ArrayList<String>();
		while(index < utteranceList.size()) {
			String tmpString = utteranceList.get(index);
			// Breaks up the word and the tag so they can be assessed individually, [chair, I-O]
			List<String> tmpList = new ArrayList<String>(Arrays.asList(tmpString.split(":")));
			String word = tmpList.get(0);
			String tag = tmpList.get(1);
			if (tag.charAt(tag.length() - 1) == 'O') {
				if (lastTag == 1){
					objects.get(objects.size() - 1).add(word);
				} else {
					lastTag = 1;
					objects.add(new ArrayList<String>());
					objects.get(objects.size() - 1).add(word);
				}
			} else if (tag.charAt(tag.length() - 1) == 'L') {
				if (lastTag == 3){
					objects.get(objects.size() - 1).add(word);
				} else {
					lastTag = 3;
					objects.add(new ArrayList<String>());
					objects.get(objects.size() - 1).add(word);
				}
			} else if (tag.charAt(tag.length() - 1) == 'P' || tag.charAt(tag.length() - 1) == 'S') {
				if (lastTag == 2){
					arcs.get(arcs.size() - 1).add(word);
				} else {
					lastTag = 2;
					arcs.add(new ArrayList<String>());
					arcs.get(arcs.size() - 1).add(word);
				}
			}
			index += 1;
		}
		
		
		
		//if there are no objects, create a thing
		
		if (objects.size() == 0){
			objects.add(new ArrayList<String>());
			objects.get(objects.size() - 1).add("thing");
		}
		
		numNodes = objects.size();	// Set the number of nodes
		
		//if list of arcs is not length of objects minus 1. Adjust the length
		
		while (arcs.size() < objects.size()-1){
			arcs.add(new ArrayList<String>());
			arcs.get(arcs.size() - 1).add("at"); //use at whenever there is no valid positional phrase
		}
		while (arcs.size() > objects.size()-1){
			arcs.remove(arcs.size()-1);
		}
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
			
			nodeList = objects.get(i);
			
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
		
		int numUCGs = (objects.size() > 1) ? objects.size() - 1 : 1; // number UCGs based on objects
		
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
			
			if (objects.size() == 1) {BuildXML(i);} //Ensures we build an xml for no landmarks
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
		if (printMode) {
			System.out.println("##################\nUCG_" + fileNumber);
			Print();
		}
		
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
	
	public void Run(String inputString) {
		System.out.println("UCGWriter.Run(inputString)");
		SortTaggedUtterance(inputString);
		InitialiseDocument();
		GenerateNodes();
		GenerateArcs();
	}
	
	public void Print() {
		NodeList nList = document.getElementsByTagName("node");
		for (int i = 0; i < nList.getLength(); i++) {
			Element node = (Element) nList.item(i);
			Element concept = (Element) node.getElementsByTagName("concept").item(0);
			Element feature = (Element) concept.getElementsByTagName("feature").item(0);
			NodeList arcList = node.getElementsByTagName("arc"); 
			if (i == 0) {System.out.println(feature.getAttribute("value"));}
			for (int j = 0; j < arcList.getLength(); j++) {
				if (i != 0 && j == 0) {System.out.println(feature.getAttribute("value"));}
				Element arc = (Element) arcList.item(j);
				Element arcConcept = (Element) arc.getElementsByTagName("concept").item(0);
				Element arcFeature = (Element) arcConcept.getElementsByTagName("feature").item(0);
				
				Element child = (Element) arc.getElementsByTagName("child").item(0);
				String childString = child.getAttribute("node");
				String nodeValue = childString.substring(childString.lastIndexOf("Node") + 4);
				Element childNode = (Element) nList.item(Integer.parseInt(nodeValue));
				Element childConcept = (Element) childNode.getElementsByTagName("concept").item(0);
				Element childFeature = (Element) childConcept.getElementsByTagName("feature").item(0);
				System.out.println(" | " + arcFeature.getAttribute("value") + " -> " + childFeature.getAttribute("value"));
			}
		}
	}

}
