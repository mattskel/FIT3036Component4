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
//		myString = "The:B-O chair:I-O on:B-P the:B-S left:I-S of:I-S the:B-L table:I-L";
//		myString = "The:B-O chair:I-O in:B-P front:I-P of:I-P the:B-L bookcase:I-L";
//		myString = "The:B-O ball:I-O on:B-P the:B-S left:I-S of:I-S the:B-L table:I-L";
//		myString = "The:B-O ball:I-O on:B-P the:B-L table:I-L in:B-P front:I-P of:I-P the:B-L bookcase:I-L";
		myString = "the:B-O ball:I-O on:B-P the:B-S table:I-O in:B-P front:I-S of:I-S the:B-L bookcase:I-L";
		
		/*
		 * The following block takes in a tagged utterance and generates the UCG(s)
		 * Input: "The:B-O bookshelf:I-O behind:B-P..."
		 * Output: UCG_0.xml, UCG_1.xml...
		 */
		UCGWriter writerUCG = new UCGWriter();
		writerUCG.Run(myString);
		
		File folder = new File("UCG");
		File[] listOfFiles = folder.listFiles();

		List<Interpreter> interpreterList = new ArrayList<Interpreter>();
		float maxScore = -1.0f;

		for (int i = 0; i < listOfFiles.length; i++) {
		    if (listOfFiles[i].isFile()) {
		    	String fileName = listOfFiles[i].getName();
		        System.out.println(fileName);
		        Interpreter interpreter = new Interpreter();
				interpreter.Run(fileName);
				if (interpreter.GetScore() > maxScore) {
					interpreterList = new ArrayList<Interpreter>();
					interpreterList.add(interpreter);
				} else if (interpreter.GetScore() == maxScore) {
					interpreterList.add(interpreter);
				}
//				interpreterList.add(interpreter);
		     } 
		}
		
		int interpreterIndex = 0;
		for (Interpreter interpreter : interpreterList) {
			interpreter.WriteICG(interpreterIndex);
			interpreterIndex += 1;
		}
		
//		Interpreter interpreter = new Interpreter();
//		interpreter.Run("UCG_0.xml");
		
		/*writer.SortTaggedUtterance(myString);
		writer.InitialiseDocument();
		writer.GenerateNodes();
		writer.GenerateArcs();*/
		
		// Uploads a UCG from a given file name
		// TODO
		// We need to be able to loop through all of the UCGs available to us
		/*UCGReader reader = new UCGReader();
		reader.RetriveUCG("UCG_0.xml");*/
		
		/*
		 * Need to consider all the possible semantic interpretations of the utterance
		 * The following block generates all the possible combinations of objects and landmarks
		 * The node features are retrieved from the UCG
		 * These are then compared with a list of known synonyms
		 * The names used by the speaker are converted into the known synonyms
		 * "table" becomes [table, desk, dinner_table]
		 * A combination of these synonyms is then generated
		 * [[ball, table], [ball, desk], [ball, dinner_table],...]
		 * The synonyms may not appear as concepts in the image we are referring to
		 */
//		Synonyms syn = new Synonyms("Assets/lexical.db");
//		SynonymCombination synonymComb = new SynonymCombination();
//		List<List<String>> nodeFeatures;
//		nodeFeatures = reader.GetNodeFeatures();
		/*for (int i = 0; i < nodeFeatures.size(); i++) {
			List<String> tmpList = nodeFeatures.get(i);
			List<String> synonymList = syn.getSynonyms(tmpList.get(0));
			synonymComb.AddSynonymList((synonymList));
		}*/
//		synonymComb.SetNodeFeatures(reader.GetNodeFeatures());
//		List<List<String>> synCombinationList = synonymComb.GetCombinations();
		//System.out.println("synCombinationList = " + synCombinationList);
		
		// Retrieves all concepts from a given image
		ConceptReader CR = new ConceptReader();
		List<Concept> concepts = CR.GetFromImage("Assets/Image/image4.kb");
		
		/*
		 * The previously generated synonym combination list generates concept combinations
		 * The synonym has corresponding concepts in the list of concepts
		 * There may be more than one concept for a given synonym
		 * Need to make sure for each set of synonyms, all concept combinations are produced
		 * For example the synonym plate could be either the concept blue_plate3 or green_plate4
		 * A different concept combination needs to be generated for both
		 * [[blue_plate3, white_microwave12, brown_table1], [green_plate4, white_microwave12, brown_table1],...]
		 */
//		ConceptCombination conceptCombination = new ConceptCombination();
//		conceptCombination.SetConceptList(concepts);
		/*for (int i = 0; i < synCombinationList.size(); i++) {
			conceptCombination.GenerateCombinations(synCombinationList.get(i));
		}*/
//		conceptCombination.GenerateCombinations(synCombinationList);
//		List<List<Concept>> currentCombinations = conceptCombination.GetCombinations();
		
		/*List<List<String>> conceptIDs = new ArrayList<List<String>>();
		for (int j = 0; j < currentCombinations.size(); j++) {
			List<Concept> tmpConceptList = currentCombinations.get(j);
			List<String> tmpList = new ArrayList<String>();
			for (Concept concept : tmpConceptList) {
				tmpList.add(concept.GetID());
			}
			conceptIDs.add(tmpList);
		}
		System.out.println("\nConcept Combinations: ");
		System.out.println(conceptIDs);*/
		
		/*
		 * Each arc has a corresponding spatial relation
		 * Typically each arc can either be "on" or "off"
		 * Both spatial relations need to be assessed
		 * For example "on" and "in_front_of" become [[on], [infrontof_off, infrontof_on]]
		 * These possible spatial relations are stored in geoRelations
		 */
		/*List<List<String>> arcFeatures;
		List<String> arcCalled = new ArrayList<String>();
		List<List<String>> geoRelations = new ArrayList<List<String>>();
		arcFeatures = reader.GetArcFeatures();
		for (int i = 0; i < arcFeatures.size(); i++) {
			List<String> tmpList = arcFeatures.get(i);
			arcCalled.add(tmpList.get(0));
			System.out.println(tmpList);
		}
		SpatialRelations SR = new SpatialRelations();
		for (String called : arcCalled) {
			geoRelations.add(SR.getRelation(called));
		}*/
//		List<List<String>> geoRelations = reader.GetGeoRelations();
		
		// Generates all possible arc combinations
		// [[totherightof_off, totherightof_on], [attheedgeof_on]] becomes...
		// [[totherightof_off, attheedgeof_on], [totherightof_on, attheedgeof_on]]
//		GeoCombination geoCombination = new GeoCombination();
//		geoCombination.SetGeoRelations(geoRelations);
		/*List<List<String>> geoCombsList = geoCombination.GetCombinations();
		System.out.println("\nGeo Relation Combinations");
		System.out.println(geoCombsList);*/
		
		// ICGWriter
		/*ICGWriter icgWriter = new ICGWriter();
		List<List<Concept>> cc = conceptCombination.GetCombinations();
		icgWriter.GenerateICG();*/
		
		/*Interpreter interpreter = new Interpreter();
		interpreter.Run("UCG_0.xml");
		interpreter.SetNodeLabels(reader.GetNodeLabels());
		interpreter.SetArcLabels(reader.GetArcLabels());
		interpreter.SetConcepts(conceptCombination.GetCombinations());
//		interpreter.SetRelations(geoCombsList);
		interpreter.SetRelations(geoCombination.GetCombinations());
		interpreter.SetSemantics(reader.GetSemantics());
		interpreter.SetSpeaker(concepts);
		interpreter.GenerateInterpretations();*/
	}
}
