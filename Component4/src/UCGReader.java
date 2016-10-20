/*
 * Given a file name reads the UCG from a folder of UCGS
 * Can be queried about the UCG from the main class
 */

import java.io.*;
import java.util.*;
import javax.xml.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class UCGReader {
	
	Document document;
	
	public UCGReader() {}
	
	// Opens a .xml from the folder
	// For now input file name
	// Later maybe just input an int and generate the filename from that
	public void RetriveUCG(String fileName) {
		System.out.println("UCGReader retrieving " + fileName);
		try {
			File file = new File("UCG/" + fileName);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			document = db.parse(file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Document GetUCGDocument() { return document; }
	
	// Returns the features of all the concept nodes in the UCG
	public List<List<String>> GetNodeFeatures() {
		
		List<List<String>> featuresList = new ArrayList<List<String>>();
		NodeList nList = document.getElementsByTagName("node");
		
		for (int i = 0; i < nList.getLength(); i++) {
			Element node = (Element) nList.item(i);
			
			NodeList conceptList = node.getElementsByTagName("concept");
			Element concept = (Element) conceptList.item(0);	// We know there is only one concept
			NodeList featureList = concept.getElementsByTagName("feature");
			
			List<String> tmpFeaturesList = new ArrayList<String>();
			
			for (int j = 0; j < featureList.getLength(); j++) {
				Element feature = (Element) featureList.item(j);
				tmpFeaturesList.add(feature.getAttribute("value"));
			}
			featuresList.add(tmpFeaturesList);
		}
		
		return featuresList;
	}
	
	// For each node in the doc returns the label
	public List<String> GetNodeLabels() {
		List<String> labelsList = new ArrayList<String>();
		NodeList nList = document.getElementsByTagName("node");
		for (int i = 0; i < nList.getLength(); i++) {
			Element node = (Element) nList.item(i);
			labelsList.add(node.getAttribute("label"));
		}
		return labelsList;
	}
	
	// GetArcLabels
	// Returns the label given to each of the arcs
	public List<String> GetArcLabels() {
		List<String> arcLabelList = new ArrayList<String>();
		NodeList nList = document.getElementsByTagName("node");
		for (int i = 0; i < nList.getLength(); i++) {
			Element node = (Element) nList.item(i);
			NodeList arcList = node.getElementsByTagName("arc");
			for (int j = 0; j < arcList.getLength(); j++) {
				Element arc = (Element) arcList.item(j);
				arcLabelList.add(arc.getAttribute("label"));
			}
		}
		return arcLabelList;
	}
	
	// Return the arc labels if they exist for each node
	// This is used in ICGWriter
	// Need to check what for...
	public List<List<String>> GetNodeArcs() {
		List<List<String>> nodeArcLabels = new ArrayList<List<String>>();
		NodeList nList = document.getElementsByTagName("node");
		for (int i = 0; i < nList.getLength(); i++) {
			Element node = (Element) nList.item(i);
			NodeList conceptList = node.getElementsByTagName("concept");
			Element concept = (Element) conceptList.item(0);	// We know there is only one concept
			NodeList arcList = concept.getElementsByTagName("arc");
			
			List<String> tmpArcList = new ArrayList<String>();
			for (int j = 0; j < arcList.getLength(); j++) {
				Element feature = (Element) arcList.item(j);
				tmpArcList.add(feature.getAttribute("label"));
			}
			nodeArcLabels.add(tmpArcList);
		}
		return nodeArcLabels;
	}
	
	// Returns the features of all the arc nodes
	public List<List<String>> GetArcFeatures() {
		List<List<String>> featuresList = new ArrayList<List<String>>();
		NodeList nList = document.getElementsByTagName("arc");
		for (int i = 0; i < nList.getLength(); i++) {
			Element arc = (Element) nList.item(i);
			NodeList conceptList = arc.getElementsByTagName("concept");
			Element concept = (Element) conceptList.item(0);
			NodeList featureList = concept.getElementsByTagName("feature");
			List<String> tmpFeaturesList = new ArrayList<String>();
			
			for (int j = 0; j < featureList.getLength(); j++) {
				Element feature = (Element) featureList.item(j);
				tmpFeaturesList.add(feature.getAttribute("value"));
			}
			featuresList.add(tmpFeaturesList);
		}
		return featuresList;
	}
	
	public List<String> GetArcChildren() {
		List<String> arcChildren = new ArrayList<String>();
		NodeList nList = document.getElementsByTagName("arc");
		for (int i = 0; i < nList.getLength(); i++) {
			Element arc = (Element) nList.item(i);
			NodeList childList = arc.getElementsByTagName("child");
			Element child = (Element) childList.item(0);	// Each arc can only have one child
			arcChildren.add(child.getAttribute("node"));
		}
		return arcChildren;
	}
	
	/*
	 * Depending on the UCG the semantic interpretation will differ
	 * GetSemantics returns the semantic interpretations of a UCG
	 * Interpretations are represented as a list of labels
	 * Object -> Relationship -> Landmark becomes [nodeLabel, arcLabel, nodeLabel]
	 * This is used to interpret the score for each relation
	 */
	public List<List<String>> GetSemantics() {
		List<List<String>> semanticsList = new ArrayList<List<String>>();
		NodeList nList = document.getElementsByTagName("node");
		for (int i = 0; i < nList.getLength(); i++) {
			Element node = (Element) nList.item(i);
			NodeList arcList = node.getElementsByTagName("arc");
			for (int j = 0; j < arcList.getLength(); j++) {
				Element arc = (Element) arcList.item(j);
				NodeList childList = arc.getElementsByTagName("child");
				Element child = (Element) childList.item(0);
				List<String> semantic = new ArrayList<String>();
				semantic.add(node.getAttribute("label"));
				semantic.add(arc.getAttribute("label"));
				semantic.add(child.getAttribute("node"));
				semanticsList.add(semantic);

				List<String> semanticCalled = new ArrayList<String>();
				semanticCalled.add(node.getAttribute("called"));
				semanticCalled.add(arc.getAttribute("called"));
				semanticCalled.add(child.getAttribute("called"));
			}
		}
		return semanticsList;
	}
	
	public int GetNumNodes() {
		NodeList nList = document.getElementsByTagName("node");
		return nList.getLength();
	}
	
	public List<List<String>> GetGeoRelations() throws IOException {
		List<List<String>> geoRelations = new ArrayList<List<String>>();
		List<String> arcCalled = new ArrayList<String>();
		List<List<String>> arcFeatures = GetArcFeatures();
		for (int i = 0; i < arcFeatures.size(); i++) {
			List<String> tmpList = arcFeatures.get(i);
			arcCalled.add(tmpList.get(0));
		}
		SpatialRelations SR = new SpatialRelations();
		for (String called : arcCalled) {
			geoRelations.add(SR.getRelation(called));
		}
		return geoRelations;
	}

}
