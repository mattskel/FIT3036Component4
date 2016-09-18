import java.io.*;
import java.util.*;

public class main {
	public static void main(String[] args) {
		System.out.println("HERE");
		
		UCGWriter writer = new UCGWriter();
		String myString = "The:B-O Mug:I-O on:B-P the:B-S edge:I-S of:I-S the:B-L table:I-L near:B-P the:B-L lamp:I-L";
		writer.SortTaggedUtterance(myString);
		writer.InitialiseDocument();
		writer.GenerateNodes();
		writer.GenerateArcs();
//		writer.BuildXML(0);
		
		SynonymCombination synonymComb = new SynonymCombination();
		
		// Create test synonymLists to add
		List<String> aList = new ArrayList<String>();
		aList.add("a");
		aList.add("aa");
		synonymComb.AddSynonymList(aList);
		
		List<String> bList = new ArrayList<String>();
		bList.add("b");
		bList.add("bb");
		synonymComb.AddSynonymList(bList);
		
		List<List<String>> combinationList = synonymComb.GetCombinations();
		System.out.println(combinationList);
		/*
		UCGReader reader = new UCGReader();
		reader.RetriveUCG("UCG_0.xml");
		reader.GetNodeFeatures();
		*/
		
	}
}
