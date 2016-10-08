/*
 * Generates all possible combinations of node values
 * For each node value there are possible synonyms
 * Generate a value for each node
 * Nodes are referenced by number
 */

import java.util.*;
import java.io.*;

public class SynonymCombination {
	
	Synonyms syn;
	
	// Combinations of synonyms
	List<List<String>> combinationList = new ArrayList<List<String>>();
	// Synonyms for each node
	List<List<String>> nodeSynonymList = new ArrayList<List<String>>();
	
	// Total number of nodes
	int numNodes;
	
	public SynonymCombination() throws IOException {	
		syn = new Synonyms("Assets/lexical.db");
	}
	
	public void SetNodeFeatures(List<List<String>> nodeFeatures) {
		for (int i = 0; i < nodeFeatures.size(); i++) {
			List<String> tmpList = nodeFeatures.get(i);
			List<String> synonymList = syn.getSynonyms(tmpList.get(0));
			AddSynonymList((synonymList));
		}
	}
	
	// Add a new synonymList to the nodeSynonymList
	public void AddSynonymList(List<String> synonymList) {
		nodeSynonymList.add(synonymList);
	}
	
	public List<List<String>> GetCombinations() {
		numNodes = nodeSynonymList.size();
		List<String> initList = new ArrayList<String>();
		CombinationGenerator(initList, 0);
		return combinationList;
	}
	
	public void CombinationGenerator (List<String> currentCombination, int index) {
		if (index >= numNodes) {
			combinationList.add(currentCombination);
		} else {
			List<String> synonyms = nodeSynonymList.get(index);
			for (String synonym : synonyms) {
				/*
				currentCombination.add(synonym);
				CombinationGenerator(currentCombination, index + 1);
				currentCombination.remove(currentCombination.size() - 1);
				*/
				List<String> newList = new ArrayList<String>();
				newList.addAll(currentCombination);
				newList.add(synonym);
				CombinationGenerator(newList,index + 1);
			}
		}
	}
}
