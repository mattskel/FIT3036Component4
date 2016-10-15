import java.io.*;
import java.util.*;

public class Synonyms {
	List<String> items;
	List<List<String>> synonym;
	List<List<Float>> score;
	
	public Synonyms(String filename) throws IOException{
		//prepare file for reading
		FileReader fileReader = new FileReader(filename);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = null;
		int n = -1;
		boolean newItem = false;
		
		items = new ArrayList<String>();
		synonym = new ArrayList<List<String>>();
		score = new ArrayList<List<Float>>();

		//go through each line in the file
		while (true){
			line = bufferedReader.readLine();
			if (line == null) break; //break if the file is completed
			
			if (line.equals("<item>")){ //start reading a new item
				newItem = true;
				synonym.add(new ArrayList<String>());
				score.add(new ArrayList<Float>());
				n++;
			}
			else if (!line.equals("</item>") && !line.equals("")) {
				String[] splitLine = line.split(" ");
				if(newItem) {
					items.add(splitLine[0]);
					newItem = false;
				}
				synonym.get(n).add(splitLine[1]);
				score.get(n).add(Float.parseFloat(splitLine[2]));
			}
		}
	}
	
	public List<String> getSynonyms(String newItem){

		List<String> output = new ArrayList<String>();
		
		//first check if item is already in the dictionary
		for (int i = 0; i < items.size(); i++){
			if (items.get(i).equalsIgnoreCase(newItem)){
				output.add(newItem);
				break;
			}
		}

		//go through all the items and find the matching synonyms
		for (int i = 0; i < items.size(); i++){
			for (int j = 0; j < synonym.get(i).size(); j++){
				
				if (synonym.get(i).get(j).equalsIgnoreCase(newItem)){
					output.add(items.get(i));	//if the item is found, return synonym.
					break;
				}
			}
		}
		if(output.isEmpty()){
			return getSynonyms("thing");
		}
		return output;
	}
}
