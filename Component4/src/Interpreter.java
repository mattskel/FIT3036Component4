/*
 * Interpreter
 * Considers every possible combination from a UCG
 * Interprets each possible combination
 * Returns the highest score
 */

import java.io.*;
import java.util.*;

public class Interpreter {
	
	UCGReader reader;
	
	int imageNumber;
	String UCGFilename;
	
	List<String> nodeLabelList = new ArrayList<String>();
	List<String> arcLabelList = new ArrayList<String>();
	
	List<List<Concept>> conceptList = new ArrayList<List<Concept>>();
	List<List<String>> relationList = new ArrayList<List<String>>();
	
	// [[Node0, Arc0, Node1], [Node0, Arc1, Node2]]
	// The semantic list is a list representation of the UCG
	List<List<String>> semanticsList = new ArrayList<List<String>>();
	List<Float> scoreList = new ArrayList<Float>();
	List<List<Integer>> highScoreIndex = new ArrayList<List<Integer>>();	// Stores an index of the highest scores
	
	List<String> ICGObjectIDs = new ArrayList<String>();
	
	float absoluteMax = 0.0f;
	
	Concept speaker;
	
	public Interpreter() {}
	
	public void SetNodeLabels(List<String> nodeLabels) {
		nodeLabelList = nodeLabels;
	}
	
	public void SetArcLabels(List<String> arcLabels) {
		arcLabelList = arcLabels;
	}
	
	public void SetConcepts(List<List<Concept>> concepts) {
		conceptList = concepts;
	}
	
	public void SetRelations(List<List<String>> relations) {
		relationList = relations;
	}
	
	public void SetImageNumber(int imageNumberIn) {
		imageNumber = imageNumberIn;
	}
	
	/*
	 * The semantics of a UCG are unique
	 * The utterance "the ball on the table in front of the bookcase" can have two semantic interpretations
	 * [[Node0, Arc0, Node1], [Node0, Arc1, Node2]] or [[Node0, Arc0, Node1], [Node1, Arc1, Node2]]
	 * semantics stores the interpretation of a single UCG
	 */
	public void SetSemantics(List<List<String>> semantics) {
		semanticsList = semantics;
	}	
	
	/*
	 * Sets the speaker concept
	 * This is required for the projective relations
	 */
	public void SetSpeaker(List<Concept> concepts) {
		int conceptIndex = 0;
		
		while (conceptIndex < concepts.size()) {
			Concept concept = concepts.get(conceptIndex);
			if (concept.GetName().equals("speaker")) {
				break;
			}
			conceptIndex++;
		}
		speaker = concepts.get(conceptIndex);
	}
	
	public float GetScore() { return absoluteMax; }
	
	public List<String> GetICGObjectIDs() { return ICGObjectIDs; }
	
	// Generates every possible combination for concepts and relations
	// Generates a score for each possible interpretation and stores it in a list
	public void GenerateInterpretations() {
		float currentMax = 0.0f;
		int conceptIndex = 0;
		for (List<Concept> concepts : conceptList) {
			//System.out.println(concepts.get(0).GetID());
			int relationIndex = 0;
			for (List<String> relations : relationList) {
				List<Float> interpretationScore = GenerateSemanticAssignment(concepts, relations);
				Float totalScore = 1.0f;
				for (float score : interpretationScore) {
					totalScore *= score;
				}
				scoreList.add(totalScore);
				if (totalScore > currentMax) {
					currentMax = totalScore;
					highScoreIndex = new ArrayList<List<Integer>>();
					List<Integer> tmpList = new ArrayList<Integer>();
					tmpList.add(conceptIndex);
					tmpList.add(relationIndex);
					highScoreIndex.add(tmpList);
				} else if (totalScore == currentMax) {
					System.out.println(conceptIndex);
					List<Integer> tmpList = new ArrayList<Integer>();
					tmpList.add(conceptIndex);
					tmpList.add(relationIndex);
					highScoreIndex.add(tmpList);
				}
				relationIndex += 1;
			}
			conceptIndex += 1;
		}
		absoluteMax = currentMax;
		System.out.println(absoluteMax);
//		System.out.println(highScoreIndex);
	}
	
	public int WriteICG(int index) {
		
		reader.RetriveUCG(UCGFilename);
		
		for (List<Integer> indexList : highScoreIndex) {
			ICGWriter writer = new ICGWriter();
			if (index == 0) {
				writer.CleanDirectory();
			}
			writer.SetUCG(reader.GetUCGDocument());
			writer.SetConcepts(conceptList.get(indexList.get(0)));
			ICGObjectIDs.add(conceptList.get(indexList.get(0)).get(0).GetID());	// Stores the object IDs
//			System.out.println(conceptList.get(indexList.get(0)).get(0).GetID());
			writer.SetArcs(relationList.get(indexList.get(1)));
			writer.InitialiseDocument();
			writer.GenerateICG();
			writer.BuildXML(index);
			index += 1;
		}
		// We return this value so the next interpreter knows what number to use to write a new ICG file
		return index;
	}
	
	/*
	 * For every possible combination of concepts and relations we need to generate a score
	 * Each of the semantic relations in the semanticList is assigned a score between 0 and 1
	 * The total score is the product of all semantic scores for a UCG
	 */
	public List<Float> GenerateSemanticAssignment(List<Concept> concepts, List<String> relations) {
		List<Float> tmpScore = new ArrayList<Float>();
		for (List<String> semantic : semanticsList) {
//			System.out.println(semantic);
			Concept object = concepts.get(ConceptIndex(semantic.get(0)));
			String relation = relations.get(RelationIndex(semantic.get(1)));
			Concept landmark = concepts.get(ConceptIndex(semantic.get(2)));
			System.out.println(object.GetID() + " " + relation + " " + landmark.GetID());
			SemanticEvaluator se = new SemanticEvaluator();
			if (relation.equals("on")) {
//				System.out.println(se.Location_on(object,landmark));
				tmpScore.add(se.Location_on(object,landmark));
			} else if (relation.equals("inthecenterof")) {
//				System.out.println(se.Location_inthecenterof(object,landmark));
				tmpScore.add(se.Location_inthecenterof(object,landmark));
			} else if (relation.equals("near")) {
//				System.out.println(se.Location_near(object,landmark,0));
				tmpScore.add(se.Location_near(object,landmark,0));
			} else if (relation.equals("inthecornerof_on")) {
//				System.out.println(se.Location_inthecornerof_on(object, landmark));
				tmpScore.add(se.Location_inthecornerof(object, landmark, true));
			} else if (relation.equals("inthecornerof_off")) {
				tmpScore.add(se.Location_inthecornerof(object, landmark, false));
			} else if (relation.equals("inside")) {
				tmpScore.add(se.Location_inside(object, landmark));
			} else if (relation.equals("at")) {
				tmpScore.add(se.Location_at(object, landmark));
			} else if (relation.equals("far")) {
				tmpScore.add(se.Location_far(object, landmark));
			} else if (relation.equals("attheedgeof_on")){
				tmpScore.add(se.Location_attheedgeof(object, landmark, true));
			} else if (relation.equals("attheedgeof_off")){
				tmpScore.add(se.Location_attheedgeof(object, landmark, false));
			} else if (relation.equals("attheendof_on")){
				tmpScore.add(se.Location_attheendof(object, landmark, true));
			} else if (relation.equals("attheendof_off")){
				tmpScore.add(se.Location_attheendof(object, landmark, false));
			} else if (relation.equals("above")){
				tmpScore.add(se.Location_above(object, landmark));
			} else if (relation.equals("under")) {
				tmpScore.add(se.Location_under(object, landmark));
				
				
			} else if (relation.startsWith("infrontof")) {
				se.SetSpeaker(speaker);
				if (relation.substring(relation.lastIndexOf("_") + 1).equals("off")) {
//					System.out.println(se.Location_infrontof(object, landmark, 0));
					tmpScore.add(se.Location_infrontof(object, landmark, 0));
				} else if (relation.substring(relation.lastIndexOf("_") + 1).equals("on")) {
//					System.out.println(se.Location_infrontof(object, landmark, 1));
					tmpScore.add(se.Location_infrontof(object, landmark, 1));
				}
			} else if (relation.startsWith("inbackof")) {
				se.SetSpeaker(speaker);
				if (relation.substring(relation.lastIndexOf("_") + 1).equals("off")) {
//					System.out.println(se.Location_inbackof(object, landmark, 0));
					tmpScore.add(se.Location_inbackof(object, landmark, 0));
				} else if (relation.substring(relation.lastIndexOf("_") + 1).equals("on")) {
//					System.out.println(se.Location_inbackof(object, landmark, 1));
					tmpScore.add(se.Location_inbackof(object, landmark, 1));
				}
			} else if (relation.startsWith("totherightof")) {
				se.SetSpeaker(speaker);
				if (relation.substring(relation.lastIndexOf("_") + 1).equals("off")) {
//					System.out.println(se.Location_totherightof(object, landmark, 0));
					tmpScore.add(se.Location_totherightof(object, landmark, 0));
				} else if (relation.substring(relation.lastIndexOf("_") + 1).equals("on")) {
//					System.out.println(se.Location_totherightof(object, landmark, 1));
					tmpScore.add(se.Location_totherightof(object, landmark, 1));
				}
			} else if (relation.startsWith("totheleftof")) {
				se.SetSpeaker(speaker);
				if (relation.substring(relation.lastIndexOf("_") + 1).equals("off")) {
//					System.out.println(se.Location_totheleftof(object, landmark, 0));
					tmpScore.add(se.Location_totheleftof(object, landmark, 0));
				} else if (relation.substring(relation.lastIndexOf("_") + 1).equals("on")) {
//					System.out.println(se.Location_totheleftof(object, landmark, 1));
					tmpScore.add(se.Location_totheleftof(object, landmark, 1));
				}
			} else { //if none of them match, give score for at
//				System.out.println(se.Location_near(object,landmark,0));
				tmpScore.add(se.Location_at(object,landmark));
			} 
		}
		
		//System.out.println(tmpScore);
		for (int i = 0; i < tmpScore.size(); i++) {
			{tmpScore.set(i, (float) Math.round(tmpScore.get(i)*1000) / 1000);} //round out errors
			
		}
		System.out.println(tmpScore);
		return tmpScore;
	}
	
	/*
	 * Given a node label returns the index of that nodeLabel in the nodeLabelList
	 * For example nodeLabel = Node2 and nodeLabelList = [Node0, Node1, Node2]
	 * returns index of Node2 in the list, index = 2
	 */
	public int ConceptIndex(String nodeLabel) {
		int index = 0;
		while (index < nodeLabelList.size() && !nodeLabel.equalsIgnoreCase(nodeLabelList.get(index))) {
			index += 1;
		}
		return index;
	}
	
	/*
	 * Given an arc label returns the index of that arcLabel in the arcLabelList
	 * arcLabel = Arc1 and arcLabelList = [Arc0, Arc1] will return index 1
	 */
	public int RelationIndex(String arcLabel) {
		int index = 0;
		while (index < arcLabelList.size() && !arcLabel.equalsIgnoreCase(arcLabelList.get(index))) {
			index += 1;
		}
		return index;
	}
	
	public void Run(String fileName) throws IOException {
		
		UCGFilename = fileName;
		
		reader = new UCGReader();
		reader.RetriveUCG(fileName);
		
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
		SynonymCombination synonymComb = new SynonymCombination();
		synonymComb.SetNodeFeatures(reader.GetNodeFeatures());
		List<List<String>> synCombinationList = synonymComb.GetCombinations();
		
		// Retrieves all concepts from a given image
		ConceptReader CR = new ConceptReader();
		List<Concept> concepts = CR.GetFromImage("Assets/Image/image" + (imageNumber + 1) + ".kb");
		
		/* Set up the conceptCombination to generate combinations */
		ConceptCombination conceptCombination = new ConceptCombination();
		conceptCombination.SetConceptList(concepts);
		
		/*
		 * The previously generated synonym combination list generates concept combinations
		 * The synonym has corresponding concepts in the list of concepts
		 * There may be more than one concept for a given synonym
		 * Need to make sure for each set of synonyms, all concept combinations are produced
		 * For example the synonym plate could be either the concept blue_plate3 or green_plate4
		 * A different concept combination needs to be generated for both
		 * [[blue_plate3, white_microwave12, brown_table1], [green_plate4, white_microwave12, brown_table1],...]
		 */
		conceptCombination.GenerateCombinations(synCombinationList);
		List<List<Concept>> currentCombinations = conceptCombination.GetCombinations();
		
		/*
		 * Each arc has a corresponding spatial relation
		 * Typically each arc can either be "on" or "off"
		 * Both spatial relations need to be assessed
		 * For example "on" and "in_front_of" become [[on], [infrontof_off, infrontof_on]]
		 * These possible spatial relations are stored in geoRelations
		 */
		
		/* Set up the geoRelations to generate combinations */
		List<List<String>> geoRelations = reader.GetGeoRelations();
		GeoCombination geoCombination = new GeoCombination();
		geoCombination.SetGeoRelations(geoRelations);
		
		System.out.println(geoRelations);
		
		SetNodeLabels(reader.GetNodeLabels());
		SetArcLabels(reader.GetArcLabels());
		SetConcepts(conceptCombination.GetCombinations());
		System.out.println(conceptCombination.GetNumNodes());
		for (List<Concept> conceptList : conceptCombination.GetCombinations()) {
			for (Concept concept : conceptList) {
				System.out.println(concept.GetID());
			}
		}
		SetRelations(geoCombination.GetCombinations());
		SetSemantics(reader.GetSemantics());
		SetSpeaker(concepts);
		GenerateInterpretations();
	}
	
}
