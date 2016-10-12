import java.net.*;
import java.io.*;

public class GreetingServer extends Thread
{
    private ServerSocket serverSocket;
    
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
                String imageNumberIn = in.readUTF();
                
                System.out.println(imageNumberIn);
                
                // Create a new SSP
                SSP ssp = new SSP();
                
                try {
        			System.out.println("@HERE");
        			ssp.RunModel(utteranceIn);
        		} catch (Exception e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
                
                System.out.println(ssp.GetTaggedUtterance());
                
                DataOutputStream out =
                new DataOutputStream(server.getOutputStream());
                out.writeUTF("Thank you for connecting to "
                             + server.getLocalSocketAddress() + "\nGoodbye!");
                server.close();
            }
//            catch(SocketTimeoutException s)
//            {
//                System.out.println("Socket timed out!");
//                break;
//            }
            catch(IOException e)
            {
                e.printStackTrace();
                break;
            }
        }
    }
}
