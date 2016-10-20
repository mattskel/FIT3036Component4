import java.io.*;
import java.util.*;

public class main {
	public static void main(String[] args) throws IOException {
		
		/*int port = 6079;
		// Using the server...
        try
        {
            Thread t = new GreetingServer(port);
            t.start();
        }catch(IOException e)
        {
            e.printStackTrace();
        }*/
		
		/* SSP
		 * Generate a tagged string from an orininal string input
		 */ 
		String utterance = "the plate on the table";
		int imageNumber = 0; // IMPORTANT: Correctly set the image number
		SSP ssp = new SSP();
		
		try {
			ssp.RunModel(utterance);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		String myString = "the:B-O hammer:I-O kjkkgg:B-N on:B-P the:B-L table:I-L";
		UCGWriter writerUCG = new UCGWriter();
		writerUCG.SetPrintMode(true);
		writerUCG.Run(ssp.GetTaggedUtterance());
//		writerUCG.Run(myString);
		
		Interpreter interpreter = new Interpreter();
		interpreter.SetImageNumber(imageNumber);
		interpreter.Run("UCG_0.xml");
		interpreter.WriteICG(0);
		
//		System.out.println(interpreter.GetICGObjectIDs());	// The objectIDs only get updated when we print to ICG
		
		
	}
}
