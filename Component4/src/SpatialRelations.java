import java.io.*;
import java.util.*;

public class SpatialRelations {
	
	List<String> concepts;
	List<List<String>> called;
	List<String> output;
	
	public SpatialRelations() throws IOException{
		
		//read file into a string array
		FileReader fileReader = new FileReader("Assets/geometric_relations.kb");
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		concepts = new ArrayList<String>();
		called = new ArrayList<List<String>>();
		String line = null;
		int n = -1;
		
		//go through each line in the file
		while (true) {
			line = bufferedReader.readLine();
//			System.out.println(line);
			if (line == null) break; //break if the file is completed
			
			if (line.startsWith("define")){ //add each concept
				concepts.add(line.substring(line.indexOf("_")+1,line.indexOf(":")));
				called.add(new ArrayList<String>());
				n++;
			}
//			else if (line.startsWith("\tcalled")){ //add all phrases for each concept
			else if (line.startsWith("called")){
				called.get(n).add(line.substring(line.indexOf("=")+1));
			}
		}
		bufferedReader.close();
		
	}
	
	public List<String> getRelation(String newPhrase){
		
		output = new ArrayList<String>();
		
		//go through all the phases and find the matching concepts
		for (int i = 0; i < concepts.size(); i++){
			for (int j = 0; j < called.get(i).size(); j++){
				
				if (called.get(i).get(j).equals(newPhrase)){
					output.add(concepts.get(i));	//if the phrase is found, return concept.
					break;
				}
			}
		}
		
		return output;
	}

}
