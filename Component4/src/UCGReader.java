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

}
