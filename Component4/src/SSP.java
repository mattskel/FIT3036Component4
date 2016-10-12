import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class SSP {
	
	String allTaggedFile;
	String arffHeaderFile;
	
//	String originalString;	// The original string eg. "the small table close to the small ball" 
	
	List<String> positionWordList = new ArrayList<String>();	// Stores the position words
	List<String> prepositionWordList = new ArrayList<String>();	// Stores the preposition words
	List<String> stopWordList = new ArrayList<String>();	// Stores stop words
	List<String> wekaHeader = new ArrayList<String>();	// Stores the weka header
	
	List<String> tagList; // Stores the tags for the words in the utterance
	String taggedOutput;
	
	List<String> isFinal;
	List<String> isThat;
	List<String> isThe;
	List<String> pos;
	List<String> isOf;
	List<String> isPW;
	List<String> isPP;
	List<String> isStop;
	List<String> relativePosition;
	List<String> pwof;
	List<String> nnp;
	List<String> pwpw;
	
	BufferedReader breader;
	BufferedWriter bwriter;
	File wekaInputFile;
	
//	wekaInputFile = new File("data/arff/WekaPredictionInput.arff");
	
	public SSP() {
		ReadFromPositionWordList();
		ReadFromPrepositionWordList();
		ReadFromStopWordList();
	}
	
	public String GetTaggedUtterance() { return taggedOutput; }
	
	/*
	 * Takes an input file of semi tagged utterances and generates an arff file
	 * For example portrait:O | on:P | the wall:L | above:P | the table:L could be an input
	 * 
	 */
	public void GenerateArffForModel(String fileName, String outputFileName) {
		System.out.println("Generating the .arff file for the model..");
		ReadFromWekaHeaderFile();
		
		ReadFromPositionWordList();
		ReadFromPrepositionWordList();
		ReadFromStopWordList();
		
		int testFrequency = 40;
		
		List<String> testReportList = new ArrayList<String>();
		
//		wekaInputFile = new File("data/arff/WekaModelInput.arff");
		wekaInputFile = new File(outputFileName);

        try {
            
        	if (!wekaInputFile.exists()) {
        		wekaInputFile.createNewFile();
        	}
        	
        	FileReader freader = new FileReader(fileName);
        	breader = new BufferedReader(freader);
        	
			wekaInputFile.createNewFile();
			FileWriter fwriter = new FileWriter(wekaInputFile.getAbsoluteFile());
	        bwriter = new BufferedWriter(fwriter);
	            
	        for (int i = 0; i < wekaHeader.size(); i++) {
	        	bwriter.write(wekaHeader.get(i));
	        	bwriter.newLine();
	        }
	        bwriter.newLine();
	        int index = 0;
//	        int lineNumber = 22;
	        String newLine = null;
	        while((newLine = breader.readLine()) != null) {
	        	try {
	        	List<String> classLabelList = GenerateClassLabels(newLine, 1);
	        	String originalUtterance = GenerateOriginalString(GenerateClassLabels(newLine,0));
	        	if(index % testFrequency == 0) {testReportList.add("\n" + originalUtterance);}
//	        	List<String> poSTagArray = GeneratePoSTagArray(originalString);
	        	List<List<String>> features = GenerateFeatures(originalUtterance);
	        	
	        	if (index % testFrequency == 0) {System.out.println(originalUtterance);}
	        	
	        	for (int i = 0; i < classLabelList.size(); i++) {
	        		String wekaArffString = "";
	        		for (int j = 0; j < features.size(); j++) {
	        			List<String> currentFeatures = features.get(j);
	        			String feature = currentFeatures.get(i);
	        			wekaArffString += feature + ",";
	        		}
	        		
	        		// This is here to test every 50th utterance
	        		wekaArffString += classLabelList.get(i);
	        		if(index % testFrequency == 0) {testReportList.add(wekaArffString);}
	        		if (index % testFrequency == 0) {System.out.println(wekaArffString);}
	        		bwriter.write(wekaArffString);
	        		bwriter.newLine();
//	        		lineNumber+=1;
	        	}
	        	
	        	} catch (Exception e) {
	        		e.printStackTrace();
	        	}
	        	System.out.println(index);
	        	index += 1;
	        }
	        bwriter.close();
	        breader.close();
	            
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        PrintTestReport(testReportList);
        System.out.println("Finished generating .arff for model.");
	}
	
	/*
	 * A method to print the test report to a .txt file
	 */
	public void PrintTestReport(List<String> testReportIn) {
		System.out.println("Printing the test report to file...");
		wekaInputFile = new File("data/TestReport.txt");

        try {
        	if (!wekaInputFile.exists()) {
        		wekaInputFile.createNewFile();
        	}
        	
			wekaInputFile.createNewFile();
			FileWriter fwriter = new FileWriter(wekaInputFile.getAbsoluteFile());
	        bwriter = new BufferedWriter(fwriter);
	            
	        for (int i = 0; i < testReportIn.size(); i++) {
	        	bwriter.write(testReportIn.get(i));
	        	bwriter.newLine();
	        }
	        bwriter.close();
	        breader.close();
            
        } catch (IOException e) {
        	// TODO Auto-generated catch block
		e.printStackTrace();
        }
	}
	
	/*
	 * Allows us to add a new smi tagged utterance to the already existing .arff file
	 */
	public void AddUtteranceToInput(String utteranceIn) {
		List<String> classLabelList = GenerateClassLabels(utteranceIn, 1);
    	String originalUtterance = GenerateOriginalString(GenerateClassLabels(utteranceIn,0));
//    	List<String> poSTagArray = GeneratePoSTagArray(originalString);
    	List<List<String>> features = GenerateFeatures(originalUtterance);
    	for (int i = 0; i < classLabelList.size(); i++) {
    		String wekaArffString = "";
    		for (int j = 0; j < 14; j++) {
    			List<String> currentFeatures = features.get(j);
    			String feature = currentFeatures.get(i);
    			wekaArffString += feature + ",";
    		}
    		wekaArffString += classLabelList.get(i);
    		try {
    		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("data/arff/WekaModelInput.arff", true)));
    		    out.println(wekaArffString);
    		    out.close();
    		} catch (IOException e) {
    		    //exception handling left as an exercise for the reader
    		}
    	}
    	/*
		try {
		    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("data/arff/WekaModelInput.arff", true)));
		    out.println(utteranceIn);
		    out.close();
		} catch (IOException e) {
		    //exception handling left as an exercise for the reader
		}
		*/
	}
	
	/*
	 * For building a single utterance arff file
	 * This is then given to the model to calculate the tags
	 * Anytime a user speaks an utterance, one of these will need to be built
	 */
	public void BuildArffModelForPredictions(String utteranceIn) {
		System.out.println("Building the .arff file for the predictions...");
		ReadFromWekaHeaderFile();	// Read in the header
		
		ReadFromPositionWordList();
		ReadFromPrepositionWordList();
		ReadFromStopWordList();
		
		// Make sure that the .arff file is clean of previous versions
		PrintWriter writer;
		try {
			writer = new PrintWriter("data/arff/WekaPredictionInput.arff");
			writer.print("");
			writer.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		wekaInputFile = new File("data/arff/WekaPredictionInput.arff");

        try {
        	if (!wekaInputFile.exists()) {
        		wekaInputFile.createNewFile();
        	}
        	
			wekaInputFile.createNewFile();
			FileWriter fwriter = new FileWriter(wekaInputFile.getAbsoluteFile());
	        bwriter = new BufferedWriter(fwriter);
	            
	        for (int i = 0; i < wekaHeader.size(); i++) {
	        	bwriter.write(wekaHeader.get(i));
	        	bwriter.newLine();
	        }
	        bwriter.newLine();
	        try {
	        	List<List<String>> features = GenerateFeatures(utteranceIn);
	        	List<String> utteranceList = new ArrayList<String>(Arrays.asList(utteranceIn.split(" ")));
	        	for (int i = 0; i < utteranceList.size(); i++) {
	        		String wekaArffString = "";
	        		for (int j = 0; j < features.size(); j++) {
	        			List<String> currentFeatures = features.get(j);
	        			String feature = currentFeatures.get(i);
	        			wekaArffString += feature + ",";
	        		}
	        		wekaArffString += "?";
	        		bwriter.write(wekaArffString);
	        		bwriter.newLine();
	        	}
	        } catch (Exception e) {}
	        bwriter.close();
	        breader.close();
        } catch (Exception e) {}
	}
	
	/*
	 * Run an arff file with unknown tags on a previously built model
	 */
	public void RunModel(String utteranceIn) throws Exception {
		System.out.println(utteranceIn);
//		System.out.println(prepositionWordList);
		
		ReadFromPositionWordList();
		ReadFromPrepositionWordList();
		ReadFromStopWordList();
		
		BuildArffModelForPredictions(utteranceIn);
//		Classifier tree = (Classifier) weka.core.SerializationHelper.read("models/weka/j48.model");

		tagList = new ArrayList<String>();
		J48 cls = (J48) weka.core.SerializationHelper.read("models/weka/j48.model");
//		J48 cls = (J48) weka.core.SerializationHelper.read("/Users/matthewskelley/University/FIT3036/Models/j48_Oct_11.model");
//		Classifier cls = (Classifier) weka.core.SerializationHelper.read("/Users/matthewskelley/University/FIT3036/Tagger/J48.model");
        
        DataSource source = new DataSource("data/arff/WekaPredictionInput.arff");
        Instances instances = source.getDataSet();
        instances.setClassIndex(instances.numAttributes()-1);
        
        for (int i = 0; i < instances.numInstances(); i++) {
            
            Instance newInstance = instances.instance(i);
            double predictVal = cls.classifyInstance(newInstance);
            
            String predictString = instances.classAttribute().value((int)predictVal);
            tagList.add(predictString);
        }
        
        taggedOutput = "";
        int tagIndex = 0;
        List<String> utteranceList = new ArrayList<String>(Arrays.asList(utteranceIn.split(" ")));
        boolean firstWord = true;
        for (String word : utteranceList) {
        	if (firstWord) {
        		firstWord= false; 
        	} else {
        		taggedOutput += " ";
        	}
        	taggedOutput += word + ":" + tagList.get(tagIndex);
        	tagIndex += 1;
        }
        System.out.println(tagList);
        System.out.println(taggedOutput);
//		System.out.println(prepositionWordList);

	}
	
	/*
	 * Takes as input an arff file with word features
	 * builds an J48 decision tree from the arff file
	 */
	public void BuildModel(String inputArffFile) throws Exception {
		
		Classifier cls = new J48();
		
		Instances inst = new Instances (
								new BufferedReader(
										new FileReader(inputArffFile)));
		inst.setClassIndex(inst.numAttributes() - 1);
		cls.buildClassifier(inst);
		
		
		 ObjectOutputStream oos = new ObjectOutputStream(
                 new FileOutputStream("models/weka/j48.model"));
		 oos.writeObject(cls);
		 oos.flush();
		 oos.close();
		 
		 // deserialize model
		 ObjectInputStream ois = new ObjectInputStream(
		                           new FileInputStream("models/weka/j48.model"));
		 Classifier clsIn = (Classifier) ois.readObject();
		 ois.close();
		
		DataSource sourceNew = new DataSource("data/arff/WekaPredictionInput.arff");
        Instances instances = sourceNew.getDataSet();
        instances.setClassIndex(instances.numAttributes()-1);
        
        List<String> tagList = new ArrayList<String>();
        
        for (int i = 0; i < instances.numInstances(); i++) {
            
            Instance newInstance = instances.instance(i);
            double predictVal = cls.classifyInstance(newInstance);
            
            String predictString = instances.classAttribute().value((int)predictVal);
            tagList.add(predictString);
        }
        System.out.println(tagList);
	}
	
	// Generates the features
	// Input: "the small table close to the small ball"
	// Output:	[no-final, no-final, no-final,...],
	//			[no-that, no-that, no-that,...],
	//			[yes-the, no-the, no-the,...],...
	public List<List<String>> GenerateFeatures(String utteranceIn) {
//		System.out.println("\nGenerating features...");
		List<String> utteranceList = new ArrayList<String>(Arrays.asList(utteranceIn.split(" ")));
		List<List<String>> features = new ArrayList<List<String>>();
		features.add(IS_FINAL(utteranceList));
		features.add(IS_THAT(utteranceList));
		List<String> theList = IS_THE(utteranceList);
		features.add(theList);
		String taggedString = null;
		try {
			taggedString = RunPoSTagger(utteranceIn);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		List<String> tagList = GeneratePoSTagArray(taggedString);
		features.add(tagList);
		List<String> ofList = IS_OF(utteranceList);
		features.add(ofList);
		List<String> pwList = IS_PW(utteranceList);
		features.add(pwList);
		List<String> prepList = IS_PP(utteranceList);
		features.add(prepList);
		features.add(IS_STOP(utteranceList));
		features.add(RELATIVE_POSITION(utteranceList));
		features.add(PWOF(pwList, ofList));
		features.add(NNP(tagList, prepList));
		features.add(PWPW(pwList));
		features.add(PPTHE(theList,prepList));
		features.add(THEPW(theList,pwList));
		features.add(FIRSTPP(prepList));
//		System.out.println("Finished Generating Features");
		return features;
	}
	
	// Generates class labels for a semi-tagged string
	// Input: the small table:O | close to:P | the small ball:L
	// Output: 	[the:B-O, small:I-O, table:I-O,...] flag = 0
	//			[B-O,I-O,I-O,...] flag = 1
	public List<String> GenerateClassLabels(String taggedUtterance, int flag) {
		
		List<String> classLabelList = new ArrayList<String>();
		String[] tmpList = taggedUtterance.split("\\| ");
		
		for (String tmpString : tmpList) {
			List<String> splitList = new ArrayList<String>(Arrays.asList(tmpString.split(":")));
			List<String> splitWords = new ArrayList<String>(Arrays.asList(splitList.get(0).split(" ")));

//			System.out.println(splitList);
			char label = splitList.get(1).charAt(0);
			
			boolean beginWord = true;
			for (String word : splitWords) {
				if (beginWord) {
					if (flag == 0) {
						classLabelList.add(word + ":B-" + label);
					} else if (flag == 1) {
						classLabelList.add("B-" + label);
					}
					beginWord = false;
				} else {
					if (flag == 0) {
					classLabelList.add(word + ":I-" + label);
					} else if (flag == 1) {
						classLabelList.add("I-" + label);
					}
				}
			}
			
		}
		return classLabelList;
	}
	
	// Generates the original string format from classLabelList
	// Removes all labels
	// Input: [the:B-O, small:I-O, table:I-O,...]
	// Output: the small table close to the small ball
	public String GenerateOriginalString(List<String> classLabelList) {
		String originalString = "";
		boolean beginWord = true;
		for (String labeledWord : classLabelList) {
			if (beginWord) {
				originalString += labeledWord.substring(0, labeledWord.lastIndexOf(':'));
				beginWord = false;
			} else {
				originalString += " " + labeledWord.substring(0, labeledWord.lastIndexOf(':'));
			}
		}
		return originalString;
	}
	
	// Generates a PoS for an utterance
	// Uses the Stanford PoS
	// Input: the small table close to the small ball
	// Output: the_DT small_JJ table_NN close_NN to_TO the_DT small_JJ ball_NN 
	public String RunPoSTagger(String inputString) throws IOException, ClassNotFoundException {
        
        String tagString;
        String tagged = null;
        MaxentTagger tagger = new MaxentTagger("models/wsj-0-18-left3words-distsim.tagger");
        tagged = tagger.tagString(inputString);
        
        return tagged;
    }
	
	// Generates a List<String> for PoS of an utterance
	// Input: the_DT small_JJ table_NN close_NN to_TO the_DT small_JJ ball_NN
	// Output: [DT, JJ, NN, NN, TO, DT, JJ, NN]
	public List<String> GeneratePoSTagArray(String poSTagIn) {
        
        ArrayList<String> poSTagArray = new ArrayList<String>();
        List<String> tmpList = new ArrayList<String>(Arrays.asList(poSTagIn.split(" ")));
        
        for (int i = 0; i < tmpList.size(); i++) {
            String taggedString = tmpList.get(i);
            poSTagArray.add(taggedString.substring(taggedString.lastIndexOf("_") + 1));
        }
        return poSTagArray;
    }
	
	// Reads the position word file
	// Creates a list of all the position words
    public void ReadFromPositionWordList() {
        
        String lineOut = "";
        String fileName = "data/PositionWordList.txt";
        
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            
            while ((lineOut = bufferedReader.readLine()) != null) {
                positionWordList.add(lineOut);
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    // Reads the preposition word file
 	// Creates a list of all the preposition words
    public void ReadFromPrepositionWordList() {
        String lineOut = "";
        String fileName = "data/PrepositionWordList.txt";
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((lineOut = bufferedReader.readLine()) != null) {
                prepositionWordList.add(lineOut);
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    // Reads the stop word file
  	// Creates a list of all the stop words
    public void ReadFromStopWordList() {
        
        String lineOut = "";
        String fileName = "data/StopWordList.txt";
        
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((lineOut = bufferedReader.readLine()) != null) {
                stopWordList.add(lineOut);
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    // Input: [the, small, table, close, to, the, small, ball]
    // Output: [no-final, no-final, no-final, no-final, no-final, no-final, no-final, yes-final]
    public List<String> IS_FINAL(List<String> wordListIn) {
        
        List<String> outputList = new ArrayList<String>();
        for (int i = 0; i < wordListIn.size(); i++) {
            if (i == wordListIn.size() - 1) {
                outputList.add("yes-final");
            }
            else {
                outputList.add("no-final");
            }
        }
        return outputList;
    }
    
    // Input: [that, small, table, close, to, that, small, ball]
    // Ouput: [yes-that, no-that, no-that, no-that, no-that, yes-that, no-that, no-that]
    public List<String> IS_THAT(List<String> wordListIn) {
        List<String> outputList = new ArrayList<String>();
        for (int i = 0; i < wordListIn.size(); i++) {
            if (wordListIn.get(i).equalsIgnoreCase("that")) {
                outputList.add("yes-that");
            }
            else {
                outputList.add("no-that");
            }
        }
        return outputList;
    }
    
    // Input: [the, small, ball, on, the, edge, of, the, table]
    // Ouput: [yes-the, no-the, no-the, no-the, yes-the, no-the, no-the, yes-the, no-the]
    public List<String> IS_THE(List<String> wordListIn) {
        List<String> outputList = new ArrayList<String>();
        for (int i = 0; i < wordListIn.size(); i++) {
            if (wordListIn.get(i).equalsIgnoreCase("the")) {
                outputList.add("yes-the");
            }
            else {
                outputList.add("no-the");
            }
        }
        return outputList;
    }
    
    // Input: [the, small, ball, on, the, edge, of, the, table]
    // Ouput: [yes-the, no-the, no-the, no-the, yes-the, no-the, no-the, yes-the, no-the]
    public List<String> IS_OF(List<String> wordListIn) {
        List<String> outputList = new ArrayList<String>();
        for (int i = 0; i < wordListIn.size(); i++) {
            if (wordListIn.get(i).equalsIgnoreCase("of")) {
                outputList.add("yes-of");
            }
            else {
                outputList.add("no-of");
            }
        }
        return outputList;
    }
    
    // Input: [the, small, ball, next, to, the, back, of, the, chair]
    // Output: [no-pw, no-pw, no-pw, no-pw, no-pw, no-pw, yes-pw, no-pw, no-pw, no-pw]		
    public List<String> IS_PW(List<String> wordListIn) {
        List<String> outputList = new ArrayList<String>();
        for (int i = 0; i < wordListIn.size(); i++) {
            int j = 0;
            while (j < positionWordList.size() && !positionWordList.get(j).equalsIgnoreCase(wordListIn.get(i))) {
                j++;
            }
            if (j < positionWordList.size()) {
                outputList.add("yes-pw");
            }
            else {
                outputList.add("no-pw");
            }
        }
        return outputList;
    }
    
    // Input: [the, small, ball, next, to, the, back, of, the, chair]
    // Output: [no-pp, no-pp, no-pp, yes-pp, yes-pp, no-pp, no-pp, no-pp, no-pp, no-pp]
    public List<String> IS_PP(List<String> wordListIn) {
        List<String> outputList = new ArrayList<String>();
        for (int i = 0; i < wordListIn.size(); i++) {
            int j = 0;
            while (j < prepositionWordList.size() && !prepositionWordList.get(j).equalsIgnoreCase(wordListIn.get(i))) {
                j++;
            }
            if (j < prepositionWordList.size()) {
                outputList.add("yes-pp");
            }
            else {
                outputList.add("no-pp");
            }
        }
        return outputList;
    }
    
    // Input: [the, small, ball, next, to, the, back, of, the, chair]
    // Output: [yes-stop, yes-stop, no-stop, yes-stop, yes-stop, yes-stop, yes-stop, yes-stop, yes-stop, no-stop]
    public List<String> IS_STOP(List<String> wordListIn) {
        List<String> outputList = new ArrayList<String>();
        for (int i = 0; i < wordListIn.size(); i++) {
            int j = 0;
            while (j < stopWordList.size() && !stopWordList.get(j).equalsIgnoreCase(wordListIn.get(i))) {
                j++;
            }
            if (j < stopWordList.size()) {
                outputList.add("yes-stop");
            }
            else {
                outputList.add("no-stop");
            }
        }
        return outputList;
    }
    
    // Input: [the, small, ball, next, to, the, back, of, the, chair]
    //Output: [0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9]
    public List<String> RELATIVE_POSITION(List<String> wordListIn) {
        List<String> outputList = new ArrayList<String>();
        for (int i = 0; i < wordListIn.size(); i++) {
            outputList.add(String.valueOf((float)i/(float)wordListIn.size()));
        }
        return outputList;
    }
    
    // Is this word in the list of position words, while the next word (if exists) is "of"? 
    // If there is no next word, this is a “no”
    // Input:	[..., no-pw, yes-pw, no-pw no-pw, ...] and
    // 			[..., no-of, no-of, yes-of, no-of, ...]
    // Output: [..., no-pwof, yes-pwof, no-pwof, ...]
    public List<String> PWOF(List<String> positionWordListIn, List<String> ofListIn) {
        List<String> outputList = new ArrayList<String>();
        for (int i = 0; i < positionWordListIn.size() - 1; i++) {
            if (positionWordListIn.get(i).equals("yes-pw") && ofListIn.get(i+1).equals("yes-of")) {
                outputList.add("yes-pwof");
            }
            else {
                outputList.add("no-pwof");
            }
        }
        outputList.add("no-pwof");
        return outputList;
    }
    
    // If the current PoS NN, while the next word (if exists) is in the list of prepositions? 
    // Input: 	[..., JJ, NN, NN, TO, ...] 
    //			[..., no-pp, no-pp, yes-pp, yes-pp, ...]
    // Output: [..., no-NNP, yes-NNP, yes-NNP, no-NNP, ...]
    public List<String> NNP(List<String> tagListIn, List<String> prepositionListIn) {
        List<String> outputList = new ArrayList<String>();
        for (int i = 0; i < tagListIn.size() - 1; i++) {
            if (tagListIn.get(i).equals("NN") && prepositionListIn.get(i+1).equals("yes-pp")) {
                outputList.add("yes-NNP");
            }
            else {
                outputList.add("no-NNP");
            }
        }
        outputList.add("no-NNP");
        return outputList;
    }
    
    
    // Is this word in the list of position words, while the next word (if exists) is also in the list? 
    // Input: [..., yes-pw, yes-pw, no-pw,...]
    // Output: [..., yes-pwpw, no-pwpw,...]
    public List<String> PWPW(List<String> positionWordListIn) {
        List<String> outputList = new ArrayList<String>();
        for (int i = 0; i < positionWordListIn.size() - 1; i++) {
            if (positionWordListIn.get(i).equals("yes-pw") && positionWordListIn.get(i+1).equals("yes-pw")) {
                outputList.add("yes-pwpw");
            }
            else {
                outputList.add("no-pwpw");
            }
        }
        outputList.add("no-pwpw");
        return outputList;
    }
    
    // Is the word "the" and the following word a preposition
    // Input: 	[..., no-the, yes-the,... ]
    //    		[..., yes-pp, yes-pp,...]
    // Output:	[..., 1, 0,...]
    public List<String> PPTHE(List<String> theListIn, List<String> prepositionListIn) {
    	List<String> outputList = new ArrayList<String>();
    	outputList.add("0");
    	for (int i = 1; i < theListIn.size(); i++) {
    		if (theListIn.get(i).equals("yes-the") && prepositionListIn.get(i-1).equals("yes-pp")) {
    			outputList.add("1");
    		} else {
    			outputList.add("0");
    		}
    	}
    	return outputList;
    }
    
    public List<String> THEPW(List<String> theListIn, List<String> positionWordListIn) {
    	List<String> outputList = new ArrayList<String>();
    	for (int i = 0; i < theListIn.size() - 1; i++) {
    		if (theListIn.get(i).equals("yes-the") && positionWordListIn.get(i+1).equals("yes-pw")) {
    			outputList.add("1");
    		} else {
    			outputList.add("0");
    		}
    	}
    	outputList.add("0");
    	return outputList;
    }
    
    // A binary feature to distinguish between all words before and including the first preposition and after
    // All words before and including the preposition have value 0
    // All words after the first preposition have value 1
    public List<String> FIRSTPP (List<String> prepositionListIn) {
    	List<String> outputList = new ArrayList<String>();
    	int index = 0;
    	while (index < prepositionListIn.size() && prepositionListIn.get(index).equals("no-pp")) {
    		outputList.add("0");
    		index+=1;
    	}
    	while (index < prepositionListIn.size() && prepositionListIn.get(index).equals("yes-pp")) {
    		outputList.add("0");
    		index+=1;
    	}
    	while (index < prepositionListIn.size()) {
    		outputList.add("1");
    		index+=1;
    	}
    	return outputList;
    }
    
    public void ReadFromWekaHeaderFile() {
        
        String lineOut = "";
        String fileName = "data/arff/WekaHeader.arff";
        
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((lineOut = bufferedReader.readLine()) != null) {
                wekaHeader.add(lineOut);
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    
    public void WriteWekaInput(List<String> wekaInputStringIn) {
        ReadFromWekaHeaderFile();
        try {
            File file = new File("data/arff/WekaInput.arff");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            for (int i = 0; i < wekaHeader.size(); i++) {
                bw.write(wekaHeader.get(i));
                bw.newLine();
            }
            bw.newLine();
            for (int i = 0; i < wekaInputStringIn.size(); i++) {
                bw.write(wekaInputStringIn.get(i));
                bw.newLine();
            }
            bw.close();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}

