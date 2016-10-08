import java.io.*;
import java.util.*;

public class Interpreter {
	
	List<String> nodeLabelList = new ArrayList<String>();
	List<String> arcLabelList = new ArrayList<String>();
	
	List<List<Concept>> conceptList = new ArrayList<List<Concept>>();
	List<List<String>> relationList = new ArrayList<List<String>>();
	
	// [[Node0, Arc0, Node1], [Node0, Arc1, Node2]]
	List<List<String>> semanticsList = new ArrayList<List<String>>();
	
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
	
	// Generates every possible combination for concepts and relations
	public void GenerateInterpretations() {
		for (List<Concept> concepts : conceptList) {
			for (List<String> relations : relationList) {
				GenerateSemanticAssignment(concepts, relations);
			}
		}
	}
	
	// Assigns concepts and relations to semantics of a UCG
	public void GenerateSemanticAssignment(List<Concept> concepts, List<String> relations) {
		for (List<String> semantic : semanticsList) {
			Concept object = concepts.get(ConceptIndex(semantic.get(0)));
			String relation = relations.get(RelationIndex(semantic.get(1)));
			Concept landmark = concepts.get(ConceptIndex(semantic.get(2)));
			if (relation.equals("on")) {
				System.out.println(object.GetID() + " " + relation + " " + landmark.GetID());
				SemanticEvaluator se = new SemanticEvaluator();
				System.out.println(se.Location_on(object,landmark));
			} else if (relation.equals("inthecenterof")) {
				System.out.println(object.GetID() + " " + relation + " " + landmark.GetID());
				SemanticEvaluator se = new SemanticEvaluator();
				System.out.println(se.Location_inthecenterof(object,landmark));
			} else if (relation.equals("near")) {
				System.out.println(object.GetID() + " " + relation + " " + landmark.GetID());
				SemanticEvaluator se = new SemanticEvaluator();
				System.out.println(se.Location_near(object,landmark,0));
			} else if (relation.equals("inthecornerof_on")) {
				System.out.println(object.GetID() + " " + relation + " " + landmark.GetID());
				SemanticEvaluator se = new SemanticEvaluator();
				System.out.println(se.Location_inthecornerof_on(object, landmark));
			} else if (relation.startsWith("infrontof")) {
				System.out.println(object.GetID() + " " + relation + " " + landmark.GetID());
				SemanticEvaluator se = new SemanticEvaluator();
				se.SetSpeaker(speaker);
				if (relation.substring(relation.lastIndexOf("_") + 1).equals("off")) {
					System.out.println(se.Location_infrontof(object, landmark, 0));
				} else if (relation.substring(relation.lastIndexOf("_") + 1).equals("on")) {
					System.out.println(se.Location_infrontof(object, landmark, 1));
				}
			} else if (relation.startsWith("inbackof")) {
				System.out.println(object.GetID() + " " + relation + " " + landmark.GetID());
				SemanticEvaluator se = new SemanticEvaluator();
				se.SetSpeaker(speaker);
				if (relation.substring(relation.lastIndexOf("_") + 1).equals("off")) {
					System.out.println(se.Location_inbackof(object, landmark, 0));
				} else if (relation.substring(relation.lastIndexOf("_") + 1).equals("on")) {
					System.out.println(se.Location_inbackof(object, landmark, 1));
				}
			} else if (relation.startsWith("totherightof")) {
				System.out.println(object.GetID() + " " + relation + " " + landmark.GetID());
				SemanticEvaluator se = new SemanticEvaluator();
				se.SetSpeaker(speaker);
				if (relation.substring(relation.lastIndexOf("_") + 1).equals("off")) {
					System.out.println(se.Location_totherightof(object, landmark, 0));
				} else if (relation.substring(relation.lastIndexOf("_") + 1).equals("on")) {
					System.out.println(se.Location_totherightof(object, landmark, 1));
				}
			} else if (relation.startsWith("totheleftof")) {
				System.out.println(object.GetID() + " " + relation + " " + landmark.GetID());
				SemanticEvaluator se = new SemanticEvaluator();
				se.SetSpeaker(speaker);
				if (relation.substring(relation.lastIndexOf("_") + 1).equals("off")) {
					System.out.println(se.Location_totheleftof(object, landmark, 0));
				} else if (relation.substring(relation.lastIndexOf("_") + 1).equals("on")) {
					System.out.println(se.Location_totheleftof(object, landmark, 1));
				}
			}
		}
	}
	
	// Returns the index of a concept given the node label
	public int ConceptIndex(String nodeLabel) {
		int index = 0;
		while (index < nodeLabelList.size() && !nodeLabel.equalsIgnoreCase(nodeLabelList.get(index))) {
			index += 1;
		}
		return index;
	}
	
	// Returns the index of a relation give the arc label
	public int RelationIndex(String arcLabel) {
		int index = 0;
		while (index < arcLabelList.size() && !arcLabel.equalsIgnoreCase(arcLabelList.get(index))) {
			index += 1;
		}
		return index;
	}
	
}
