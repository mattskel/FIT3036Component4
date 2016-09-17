
import java.util.*;
import javax.xml.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.*;
import java.io.*;

public class UCGWriter {
	
	Document document;
	DocumentBuilderFactory docFactory;
	DocumentBuilder docBuilder;
	DOMSource source;
	
	Element rootElement;
	
	TransformerFactory transformerFactory;
    Transformer transformer;
	
	List<String> object = new ArrayList<String>();
	List<List<String>> landmarks = new ArrayList<List<String>>();
	List<List<String>> arcs = new ArrayList<List<String>>();
	
	// Node features list
	String[] featuresList = {"called", "cg_role","definiteness","determiner"};
	
	public UCGWriter() {}
	
	// Input a tagged utterance
	// Interprets as a UCG
	public void SortTaggedUtterance(String taggedUtterance) {
		
		// Sorts all the words from the tagged utterance
		// stores words in lists
		List<String> utteranceList = new ArrayList<String>(Arrays.asList(taggedUtterance.split(" ")));
		
		int index = 0;
		while(index < utteranceList.size()) {
			String tmpString = utteranceList.get(index);
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
		source = new DOMSource(document);
		
		// Create the root element
		rootElement = document.createElement("CG");
		document.appendChild(rootElement);
	}
	
	public void GenerateNodes() {
		
		for (int i = 0; i < landmarks.size() + 1; i++) {
			List<String> nodeList = new ArrayList<String>();
			if (i == 0) {
				nodeList = object;
			} else {
				nodeList = landmarks.get(i - 1);
			}
			
			Element node = document.createElement("node");
			rootElement.appendChild(node);
			
			node.setAttribute("label", "Node" + i);
		}
	}

}
