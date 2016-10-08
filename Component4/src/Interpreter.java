import java.io.*;
import java.util.*;

public class Interpreter {
	
	UCGReader reader;
	
	List<String> nodeLabelList = new ArrayList<String>();
	List<String> arcLabelList = new ArrayList<String>();
	
	List<List<Concept>> conceptList = new ArrayList<List<Concept>>();
	List<List<String>> relationList = new ArrayList<List<String>>();
	
	// [[Node0, Arc0, Node1], [Node0, Arc1, Node2]]
	// The semantic list is a list representation of the UCG
	List<List<String>> semanticsList = new ArrayList<List<String>>();
	List<Float> scoreList = new ArrayList<Float>();
	List<List<Integer>> highScoreIndex = new ArrayList<List<Integer>>();	// Stores an index of the highest scores
	
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
	
	/*
	 * The semantics of a UCG are unique
	 * The utterance "the ball on the table in front of the bookcase" can have two semantic interpretations
	 * [[Node0, Arc0, Node1], [Node0, Arc1, Node2]] or [[Node0, Arc0, Node1], [Node1, Arc1, Node2]]
	 * semantics stores the interpretation of a single UCG
	 */
	public void SetSemantics(List<List<String>> semantics) {
		semanticsList = semantics;
	}	
	
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
	
	// Generates every possible combination for concepts and relations
	// Generates a score for each possible interpretation and stores it in a list
	public void GenerateInterpretations() {
		float currentMax = 0.0f;
		int conceptIndex = 0;
		for (List<Concept> concepts : conceptList) {
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
	
	public void WriteICG(int index) {
		for (List<Integer> indexList : highScoreIndex) {
			ICGWriter writer = new ICGWriter();
			if (index == 0) {
				writer.CleanDirectory();
			}
			writer.SetUCG(reader.GetUCGDocument());
			writer.SetConcepts(conceptList.get(indexList.get(0)));
			writer.SetArcs(relationList.get(indexList.get(1)));
			writer.InitialiseDocument();
			writer.GenerateICG();
			writer.BuildXML(index);
		}
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
				tmpScore.add(se.Location_inthecornerof_on(object, landmark));
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
			}
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
		
		reader = new UCGReader();
		reader.RetriveUCG(fileName);
		
		SynonymCombination synonymComb = new SynonymCombination();
		synonymComb.SetNodeFeatures(reader.GetNodeFeatures());
		List<List<String>> synCombinationList = synonymComb.GetCombinations();
		
		// Retrieves all concepts from a given image
		ConceptReader CR = new ConceptReader();
		List<Concept> concepts = CR.GetFromImage("Assets/Image/image4.kb");
		
		ConceptCombination conceptCombination = new ConceptCombination();
		conceptCombination.SetConceptList(concepts);
		
		conceptCombination.GenerateCombinations(synCombinationList);
		List<List<Concept>> currentCombinations = conceptCombination.GetCombinations();
		
		List<List<String>> geoRelations = reader.GetGeoRelations();
		GeoCombination geoCombination = new GeoCombination();
		geoCombination.SetGeoRelations(geoRelations);
		
		SetNodeLabels(reader.GetNodeLabels());
		SetArcLabels(reader.GetArcLabels());
		SetConcepts(conceptCombination.GetCombinations());
		SetRelations(geoCombination.GetCombinations());
		SetSemantics(reader.GetSemantics());
		SetSpeaker(concepts);
		GenerateInterpretations();
	}
	
}
