import java.io.*;
import java.util.*;

public class ConceptReader {
	
	public ConceptReader(){};
	
	public List<Concept> GetFromImage(String filename) throws IOException{
		
		List<Concept> output = new ArrayList<Concept>();
		
		//prepare file for reading
		FileReader fileReader = new FileReader(filename);
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		String line = null;
		int n = -1;
		
		//go through each line in the file
		while (true) {
			line = bufferedReader.readLine();
			if (line == null) break; //break if the file is completed
			
			if (line.startsWith("define")){
				output.add(new Concept());
				n++;
//				output.get(n).setID(line.substring(line.indexOf("=")+2,line.indexOf(":")));
				output.get(n).setID(line.substring(15,line.indexOf(":"))); // Change M. Skelley
				output.get(n).setName(line.substring(line.indexOf(":")+1,line.length()).toLowerCase());
			}
			
			else if (line.startsWith("\tcolour =")){
				output.get(n).setColour(line.substring(10,line.length()).toLowerCase());
			}
			else if (line.startsWith("\tcolour_coord")){
				output.get(n).setColourCoord(getArray(line));
			}
			else if (line.startsWith("\tsize_wdh")){
				output.get(n).setSize(getArray(line));
			}
			else if (line.startsWith("\tlocation_xyz")){
				output.get(n).setLocation(getArray(line));
			}
			else if (line.startsWith("\tangle")){
				output.get(n).setAngle(Float.parseFloat(line.substring(line.indexOf("=")+2,line.length())));
			}
			
			
		}
		return output;
	}
	
	private float[] getArray(String line){
		float[] output = new float[3];
		
		output[0] = Float.parseFloat(line.substring(line.indexOf("=")+2,line.indexOf(":")));
		output[1] = Float.parseFloat(line.substring(line.indexOf(":")+1,line.lastIndexOf(":")));
		output[2] = Float.parseFloat(line.substring(line.lastIndexOf(":")+1,line.length()));
		
		return output;
	}
}