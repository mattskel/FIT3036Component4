import java.io.*;
import java.util.*;

public class main {
	public static void main(String[] args) throws IOException {
		
		String myString;
//		myString = "The:B-O Mug:I-O on:B-P the:B-S edge:I-S of:I-S the:B-L table:I-L near:B-P the:B-L lamp:I-L";
//		myString = "The:B-O plate:I-O near:B-P the:B-L microwave:I_L on:B-P the:B_S edge:I-S of:I-S the:B-L table:I-L";
//		myString = "The:B-O plate:I-O to:B-P the:B-S right:I-S of:I-S the:B-L microwave:I_L on:B-P the:B-L table:I-L";
//		myString = "The:B-O hammer:I-O in:B-P the:B-S center:I-S of:I-S the:B-L microwave:I_L on:B-P the:B-L table:I-L";
//		myString = "The:B-O plate:I-O in:B-P the:B-S center:I-S of:I-S the:B-L table:I-L";
//		myString = "The:B-O plate:I-O near:B-P the:B-L microwave:I-L";
//		myString = "The:B-O plate:I-O on:B-P the:B-S corner:I-S of:I-S the:B-L table:I-L";
//		myString = "The:B-O plate:I-O near:B-P the:B-L ball:O-L on:B-P the:B-S corner:I-S of:I-S the:B-L table:I-L";
//		myString = "The:B-O chair:I-O in:B-P front:I-P of:I-P the:B-L chest:I-L";
//		myString = "The:B-O hammer:I-O behind:B-P the:B-L ball:I-L";
//		myString = "The:B-O bookshelf:I-O behind:B-P the:B-L table:I-L";
//		myString = "The:B-O chair:I-O on:B-P the:B-S right:I-S of:I-S the:B-L table:I-L";
//		myString = "The:B-O chair:I-O in:B-P front:I-P of:I-P the:B-L bookcase:I-L";
		myString = "The:B-O ball:I-O on:B-P the:B-S left:I-S of:I-S the:B-L table:I-L";
		
		UCGWriter writer = new UCGWriter();
		writer.SortTaggedUtterance(myString);
		writer.InitialiseDocument();
		writer.GenerateNodes();
		writer.GenerateArcs();
		
		// Uploads a UCG from a given file name
		UCGReader reader = new UCGReader();
		reader.RetriveUCG("UCG_0.xml");
		
		
		System.out.println(reader.GetNodeArcs());
		System.out.println("!HERE");
		System.out.println(reader.GetNodeLabels());
		System.out.println(reader.GetArcLabels());
		
		System.out.println(reader.GetSemantics());
		
		// Generates all possible combinations for nodes from the synonym KB
		//[[plate, microwave, table], [plate, microwave, desk],...]
		Synonyms syn = new Synonyms("Assets/lexical.db");
		SynonymCombination synonymComb = new SynonymCombination();
		List<List<String>> nodeFeatures;
		nodeFeatures = reader.GetNodeFeatures();
		for (int i = 0; i < nodeFeatures.size(); i++) {
			List<String> tmpList = nodeFeatures.get(i);
			List<String> synonymList = syn.getSynonyms(tmpList.get(0));
			synonymComb.AddSynonymList((synonymList));
		}
		List<List<String>> synCombinationList = synonymComb.GetCombinations();
		System.out.println("Synonym Combinations: ");
		System.out.println(synCombinationList);
		
		// Retrieves all concepts from a given image
		ConceptReader CR = new ConceptReader();
		List<Concept> concepts = CR.GetFromImage("Assets/Image/image4.kb");
		
		// Generates all possible combinations of concept objects
		// [[blue_plate3, white_microwave12, brown_table1], [green_plate4, white_microwave12, brown_table1],...]
		ConceptCombination conceptCombination = new ConceptCombination();
		conceptCombination.SetConceptList(concepts);
		for (int i = 0; i < synCombinationList.size(); i++) {
			conceptCombination.GenerateCombinations(synCombinationList.get(i));
		}
		List<List<Concept>> currentCombinations = conceptCombination.GetCombinations();
		List<List<String>> conceptIDs = new ArrayList<List<String>>();
		for (int j = 0; j < currentCombinations.size(); j++) {
			List<Concept> tmpConceptList = currentCombinations.get(j);
			List<String> tmpList = new ArrayList<String>();
			for (Concept concept : tmpConceptList) {
				tmpList.add(concept.GetID());
			}
			conceptIDs.add(tmpList);
		}
		System.out.println("\nConcept Combinations: ");
		System.out.println(conceptIDs);
		
		// The following code generates a list for each arc
		// The list contains the possible geo relations from the KB
		//[[near], [attheedgeof_on]]
		List<List<String>> arcFeatures;
		List<String> arcCalled = new ArrayList<String>();
		List<List<String>> geoRelations = new ArrayList<List<String>>();
		arcFeatures = reader.GetArcFeatures();
		for (int i = 0; i < arcFeatures.size(); i++) {
			List<String> tmpList = arcFeatures.get(i);
			arcCalled.add(tmpList.get(0));
		}
		SpatialRelations SR = new SpatialRelations();
		for (String called : arcCalled) {
			geoRelations.add(SR.getRelation(called));
		}
		
		// Generates all possible arc combinations
		// [[totherightof_off, totherightof_on], [attheedgeof_on]] becomes...
		// [[totherightof_off, attheedgeof_on], [totherightof_on, attheedgeof_on]]
		GeoCombination geoCombination = new GeoCombination();
		geoCombination.SetGeoRelations(geoRelations);
		List<List<String>> geoCombsList = geoCombination.GetCombinations();
		System.out.println("\nGeo Relation Combinations");
		System.out.println(geoCombsList);
		
		// ICGWriter
		ICGWriter icgWriter = new ICGWriter();
		List<List<Concept>> cc = conceptCombination.GetCombinations();
		icgWriter.GenerateICG();
		
		// Need to get the speaker details and give it to the evaluator
		int conceptIndex = 0;
//		System.out.println(concepts.size());
		while (conceptIndex < concepts.size()) {
			Concept concept = concepts.get(conceptIndex);
			if (concept.GetName().equals("speaker")) {
				break;
			}
			conceptIndex++;
		}
		
		Concept speakerConcept = concepts.get(conceptIndex);
//		System.out.println(speakerConcept.GetName());
		
		Interpreter interpreter = new Interpreter();
		interpreter.SetNodeLabels(reader.GetNodeLabels());
		interpreter.SetArcLabels(reader.GetArcLabels());
		interpreter.SetConcepts(conceptCombination.GetCombinations());
		interpreter.SetRelations(geoCombsList);
		interpreter.SetSemantics(reader.GetSemantics());
		interpreter.SetSpeaker(concepts);
		interpreter.GenerateInterpretations();
	}
}
