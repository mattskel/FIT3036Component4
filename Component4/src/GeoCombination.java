import java.util.*;

public class GeoCombination {
	
	List<List<String>> arcGeoRelations;	// Original geo relations of arcs
	List<List<String>> geoRelationCombinations = new ArrayList<List<String>>(); // List of possible arc geo relation combinations
	int numArcs; // Total number of arcs
	
	public GeoCombination() {}
	
	public void SetGeoRelations(List<List<String>> arcGeoRelationsIn) {
		arcGeoRelations = arcGeoRelationsIn;
	}
	
	public List<List<String>> GetCombinations() {
		numArcs = arcGeoRelations.size();
		List<String> initList = new ArrayList<String>();
		CombinationGenerator(initList, 0);
		return geoRelationCombinations;
	}
	
	public void CombinationGenerator (List<String> currentCombination, int index) {
		if (index >= numArcs) {
			geoRelationCombinations.add(currentCombination);
		} else {
			List<String> geoRelations = arcGeoRelations.get(index);
			for (String relation : geoRelations) {
				/*
				currentCombination.add(synonym);
				CombinationGenerator(currentCombination, index + 1);
				currentCombination.remove(currentCombination.size() - 1);
				*/
				List<String> newList = new ArrayList<String>();
				newList.addAll(currentCombination);
				newList.add(relation);
				CombinationGenerator(newList,index + 1);
			}
		}
	}
	
}
