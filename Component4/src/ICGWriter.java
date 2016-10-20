
import java.io.*;
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
import org.xml.sax.SAXException;

public class ICGWriter {
	
	Document documentUCG;	// Stores a .xml UCG from the file
	Document documentICG;	// Stores a .xml ICG to write to a file
	List<Document> documentList = new ArrayList<Document>();
	
	List<Concept> conceptList;
	List<String> relationList;
	
	DocumentBuilderFactory docFactory;
	DocumentBuilder docBuilder;
	DOMSource source;
	
	TransformerFactory transformerFactory;
    Transformer transformer;
	
	Element rootElement;
	
	UCGReader reader = new UCGReader();	// Responsible for reading UCGs from the file
	
	int numberUCGs;	// The total number of UCGs
	boolean printMode = true;
	
	public ICGWriter() {}
	
	public void SetUCG(Document documentIn) { documentUCG = documentIn; }
	
	public void SetConcepts(List<Concept> conceptsIn) { conceptList = conceptsIn; }
	public void SetArcs(List<String> relationsIn) { relationList = relationsIn; }
	
	public void InitialiseDocument() {
		docFactory = DocumentBuilderFactory.newInstance();
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		documentICG = docBuilder.newDocument();
		
		// Create the root element
		rootElement = documentICG.createElement("CG");
		documentICG.appendChild(rootElement);
	}
	
	public void GenerateICG() {
		int RelationIndex = 0;
		NodeList nList = documentUCG.getElementsByTagName("node");
		for (int i = 0; i < nList.getLength(); i++) {
			
			// For every node in the UCG create a new node in the ICG
			Element nodeICG = documentICG.createElement("node");
			rootElement.appendChild(nodeICG);
			
			// Get the node from the UCG and set the ICG label
			Element nodeUCG = (Element) nList.item(i);
			nodeICG.setAttribute("label", nodeUCG.getAttribute("label"));
			
			// Set the concept element and add it to the node
			Element conceptICG = documentICG.createElement("concept");
			conceptICG.setAttribute("kind", "known");
			conceptICG.setAttribute("id", conceptList.get(i).GetID());
			nodeICG.appendChild(conceptICG);
			
			NodeList arcList = nodeUCG.getElementsByTagName("arc");
			
			for (int j = 0; j < arcList.getLength(); j++) {
				
				// Create the new arc element for the ICG
				Element arcICG = documentICG.createElement("arc");
				
				// Get the arc element from the original UCG
				Element arcUCG = (Element) arcList.item(j);
				
				// Set the new ICG arc label
				arcICG.setAttribute("label", arcUCG.getAttribute("label"));
				
				Element arcConceptICG = documentICG.createElement("concept");
				arcConceptICG.setAttribute("kind", "known");
				arcConceptICG.setAttribute("id", relationList.get(RelationIndex++));
				
				Element arcChildICG = documentICG.createElement("child");
				NodeList childList = arcUCG.getElementsByTagName("child");
				Element child = (Element) childList.item(0);
				arcChildICG.setAttribute("node", child.getAttribute("node"));
				
				arcICG.appendChild(arcConceptICG);
				arcICG.appendChild(arcChildICG);
				
				nodeICG.appendChild(arcICG);
			}
		}
	}
	
	public void CleanDirectory() {
		File directory = new File("ICG");
		File[] directoryFileList = directory.listFiles();
		for (File f : directoryFileList) {
			if (f.isFile() && f.exists()) {
				f.delete();
			}
		}
	}
	
	public void BuildXML (int fileNumber) {
		System.out.println("##################\nICG_" + fileNumber);
		Print();
		transformerFactory = TransformerFactory.newInstance();
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		StreamResult result = new StreamResult(new File("ICG/ICG_" + fileNumber + ".xml"));
		try {
			transformer.transform(new DOMSource(documentICG), result);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void Print() {
		NodeList nList = documentICG.getElementsByTagName("node");
		for (int i = 0; i < nList.getLength(); i++) {
			Element node = (Element) nList.item(i);
			Element concept = (Element) node.getElementsByTagName("concept").item(0);
			NodeList arcList = node.getElementsByTagName("arc"); 
			if (i == 0) {System.out.println(concept.getAttribute("id"));}
			for (int j = 0; j < arcList.getLength(); j++) {
				if (i != 0 && j == 0) {System.out.println(concept.getAttribute("id"));}
				Element arc = (Element) arcList.item(j);
				Element arcConcept = (Element) arc.getElementsByTagName("concept").item(0);
				Element child = (Element) arc.getElementsByTagName("child").item(0);
				String childString = child.getAttribute("node");
				String nodeValue = childString.substring(childString.lastIndexOf("Node") + 4);
				Element childNode = (Element) nList.item(Integer.parseInt(nodeValue));
				Element childConcept = (Element) childNode.getElementsByTagName("concept").item(0);
				System.out.println(" | " + arcConcept.getAttribute("id") + " -> " + childConcept.getAttribute("id"));
			}
		}
	}
	
}
