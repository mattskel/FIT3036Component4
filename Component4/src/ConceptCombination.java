/*
 * Generates all possible combinations from the Synonym lists
 * Inputs a combination of synonyms
 * Searches the list of concept classes from a specific image
 * finds all possible combination matches for the given input
 * Returns all possible classes
 * Returns possible concept classes as an index in the class list
 */

import java.util.*;

public class ConceptCombination {
	
	// Synonym list 
	List<String> synonymList;
	
	// Concept list to reference
	// We wont create this list here but pass it from the main
	// It is generated from the KB file for each image
	List<Concept> conceptList;
	
	// A list of possible concepts for each of the synonyms
	// From this list we will generate all possible combinations
	List<List<Concept>> possibleConceptList;
	
	// An output list containing the different combinations of concepts
	List<List<Concept>> conceptCombinationList = new ArrayList<List<Concept>>();
	
	boolean allFound;	// Bool to determine if a concept was found for a synonym
	
	int numNodes;
	
	public ConceptCombination() {}
	
	// Get the synonym combination
	public void SetSynonymCombination(List<String> synonymListIn) {
		synonymList = synonymListIn;
	}
	
	// Get the concepts from the KB
	public void SetConceptList(List<Concept> conceptListIn) {
		conceptList = conceptListIn;
	}
	
	// For each node/synonym generate a list of possible Concepts from the KB
	public void GeneratePotentialConcepts() {
		allFound = true;	// Use this to check that a concept exists matching the synonym
		possibleConceptList = new ArrayList<List<Concept>>();
		for (String synonym : synonymList) {
			List<Concept> tmpConceptList = new ArrayList<Concept>();	// Store concepts that match the synonym
			// Iterate over all concepts in the KB
			for (Concept concept : conceptList) {
				if (synonym.equalsIgnoreCase(concept.GetName())) {
					tmpConceptList.add(concept);
				}
			}
			// Check at least concept was found for a synonym
			if (tmpConceptList.size() == 0) {
				allFound = false;
			}
			possibleConceptList.add(tmpConceptList);
		}
	}
	
	public List<List<Concept>> GetCombinations() {
//		List<List<Concept>> = new ArrayList
		return conceptCombinationList;
	}
	
	public void GenerateCombinations(List<List<String>> synCombinationList) {
		for (int i = 0; i < synCombinationList.size(); i++) {
			SetSynonymCombination(synCombinationList.get(i));
			GeneratePotentialConcepts();
			if (allFound) {
				numNodes = synonymList.size();
				List<Concept> initList = new ArrayList<Concept>();
				CombinationGenerator(initList, 0);
			}
		}
	}
	
	// Generates all the possible concept combinations for the given synonyms
	public void CombinationGenerator (List<Concept> currentCombination, int index) {
		if (index >= numNodes) {
			conceptCombinationList.add(currentCombination);
		} else {
			List<Concept> concepts = possibleConceptList.get(index);
			for (Concept concept : concepts) {
				if (!currentCombination.contains(concept)) {
					List<Concept> newList = new ArrayList<Concept>();
					newList.addAll(currentCombination);
					newList.add(concept);
					CombinationGenerator(newList,index + 1);
				}
			}
		}
	}
	
	public int GetNumNodes(){return numNodes;}

}
