
import java.io.*;
import java.util.*;
import javax.xml.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class ICGWriter {
	
	Document UCGDoc;	// Stores a .xml UCG from the file
	Document ICGDoc;	// Stores a .xml ICG to write to a file
	
	DocumentBuilderFactory docFactory;
	DocumentBuilder docBuilder;
	DOMSource source;
	
	Element rootElement;
	
	UCGReader reader = new UCGReader();	// Responsible for reading UCGs from the file
	
	int numberUCGs;	// The total number of UCGs
	
	public ICGWriter() {}
	
	public void GenerateICG() {}

	/*public void GenerateICG(List<List<Concept>> conceptCombinations, 
							List<List<String>> geoCombinations) {
		
		// Initialise docFactory and docBuilder for creating Document
		docFactory = DocumentBuilderFactory.newInstance();
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (int i = 0; i < numberUCGs; i++) {
			
			reader.RetriveUCG("UCG_" + i);
		
			for (List<Concept> conceptList : conceptCombinations) {
				for (List<String> geoRelationList : geoCombinations) {
					
					ICGDoc = docBuilder.newDocument();
					
					// Create the root element
					rootElement = ICGDoc.createElement("CG");
					ICGDoc.appendChild(rootElement);
					
					GenerateNodes(conceptList);
					GenerateArcs(geoRelationList);
				
				}
			}
		}	
	}*/
	
	public void GenerateNodes(List<Concept> conceptList) {
		List<String> nodeLabel = reader.GetNodeLabels();
		//List<List<String>> nodeArcs = reader.GetNodeArcs();
		for (int i = 0; i < nodeLabel.size(); i++) {
			
			Element node = ICGDoc.createElement("node");
			node.setAttribute("label", nodeLabel.get(i));
			rootElement.appendChild(node);
			
			Element concept = ICGDoc.createElement("concept");
			concept.setAttribute("kind", "known");
			concept.setAttribute("id", conceptList.get(i).GetID());
			node.appendChild(concept);
			
			/*List<String> currentArcs = nodeArcs.get(i);
			for (String arc : currentArcs) {
				
			}*/
			
		}
	}
	
	void GenerateArcs(List<String> geoRelations) {
		
		int arcIndex = 0; 	// keeps track of which arc we are up to in geoRelations
		
		List<List<String>> nodeArcs = reader.GetNodeArcs();
		List<String> arcChildren = reader.GetArcChildren();
		NodeList nList = ICGDoc.getElementsByTagName("node");
		
		for (int i = 0; i < nList.getLength(); i++) {
			Element node = (Element) nList.item(i);
			List<String> currentArcs = nodeArcs.get(i);
			for(String arcLabel : currentArcs) {
				
				Element arc = ICGDoc.createElement("arc");
				arc.setAttribute("label", "arcLabel");
				
				Element concept = ICGDoc.createElement("concept");
				concept.setAttribute("kind", "known");
				concept.setAttribute("id", geoRelations.get(arcIndex));
				arc.appendChild(concept);
				
				Element child = ICGDoc.createElement("child");
				child.setAttribute("node", arcChildren.get(arcIndex));
				arc.appendChild(child);
				
				node.appendChild(arc);
			}
		}
	}
}
