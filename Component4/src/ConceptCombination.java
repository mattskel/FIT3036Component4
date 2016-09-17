/*
 * Generates all possible combinations from the Synonym lists
 * Inputs a combination of synonyms
 * Searches the list of concept classes for a specific image
 * finds all possible combination matches for the given input
 * Returns all possible classes
 * Returns possible classes as an index in the class list
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
	List<List<Concept>> possibleConceptList = new ArrayList<List<Concept>>();
	
	// An output list containing the different combinations of concepts
	List<List<Concept>> conceptCombinationList = new ArrayList<List<Concept>>();
	
	int numNodes;
	
	public ConceptCombination() {}
	
	// Get the synonym combination
	public void GetSynonymCombination(List<String> synonymListIn) {
		synonymList = synonymListIn;
	}
	
	// Get the concepts from the KB
	public void AddConceptList(List<Concept> conceptListIn) {
		conceptList = conceptListIn;
	}
	
	// For each node/synonym generate a list of possible Concepts from the KB
	public void GeneratePotentialConcepts() {
		for (String synonym : synonymList) {
			// Store concepts that match the synonym
			List<Concept> tmpConceptList = new ArrayList<Concept>();
			// Iterate over all concepts in the KB
			for (Concept concept : conceptList) {
				if (synonym.equalsIgnoreCase(concept.GetName())) {
					tmpConceptList.add(concept);
				}
			}
			possibleConceptList.add(tmpConceptList);
		}
	}
	
	public List<List<Concept>> GetCombinations() {
		numNodes = synonymList.size();
		List<Concept> initList = new ArrayList<Concept>();
		CombinationGenerator(initList, 0);
		return conceptCombinationList;
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

}
