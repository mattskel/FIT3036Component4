import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class GreetingServer extends Thread
{
    private ServerSocket serverSocket;
    
    List<Interpreter> interpreterList;
    int imageNumber;
    
    public GreetingServer(int port) throws IOException
    {
        serverSocket = new ServerSocket(port);
//        serverSocket.setSoTimeout(50000);
    }
    
    public void run()
    {
        while(true)
        {
            try
            {
                System.out.println("Waiting for client on port " +
                                   serverSocket.getLocalPort() + "...");
                Socket server = serverSocket.accept();
                System.out.println("Just connected to " + server.getRemoteSocketAddress());
                DataInputStream in = new DataInputStream(server.getInputStream());
//                System.out.println(in.readUTF());
                
                String utteranceIn = in.readUTF();
                imageNumber = Integer.parseInt(in.readUTF());
                
                System.out.println(imageNumber);
                System.out.println("HERE");
                
                // Create a new SSP
                SSP ssp = new SSP();
                
                try {
        			ssp.RunModel(utteranceIn);
        		} catch (Exception e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
                
                System.out.println(ssp.GetTaggedUtterance());
                
                UCGWriter writerUCG = new UCGWriter();
        		writerUCG.Run(ssp.GetTaggedUtterance());
        		
        		int interpreterIndex = 0;	// Interpreter index is used to name the ICG files
        		
        		Interpret();
        		
        		String outputString = "";
        		boolean firstString = true;
        		for (Interpreter interpreter : interpreterList) {
        			interpreterIndex = interpreter.WriteICG(interpreterIndex);	// We also need to update the interpreter index
        			System.out.println(interpreter.GetICGObjectIDs());
        			for (String object : interpreter.GetICGObjectIDs()) {
        				if (firstString) {
        					firstString = false;
        				} else {
        					outputString += ", ";
        				}
        				outputString += object;
        			}
        		}
                
                DataOutputStream out = new DataOutputStream(server.getOutputStream());
                out.writeUTF(outputString);
//                out.writeUTF("Thank you for connecting to "
//                             + server.getLocalSocketAddress() + "\nGoodbye!");
                server.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
                break;
            }
        }
    }
    
    public void Interpret() throws IOException {
    	File folder = new File("UCG");
		File[] listOfFiles = folder.listFiles();
    	interpreterList = new ArrayList<Interpreter>();
    	float maxScore = -1.0f;

		for (int i = 0; i < listOfFiles.length; i++) {
		    if (listOfFiles[i].isFile()) {
		    	String fileName = listOfFiles[i].getName();
		        System.out.println(fileName);
		        Interpreter interpreter = new Interpreter();
		        System.out.println(imageNumber);
		        interpreter.SetImageNumber(imageNumber);
				interpreter.Run(fileName);
				if (interpreter.GetScore() > maxScore) {
					interpreterList = new ArrayList<Interpreter>();
					interpreterList.add(interpreter);
				} else if (interpreter.GetScore() == maxScore) {
					interpreterList.add(interpreter);
				}
//				interpreterList.add(interpreter);
		     } 
		}
    }
}
