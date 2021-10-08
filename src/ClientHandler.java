import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{

  public static ArrayList<ClientHandler> clientHandler = new ArrayList<>();
  private Socket socket;
  private BufferedReader bufferedReader;//reads message sent by client.
  private BufferedWriter bufferedWriter;// writes messages sent to the client.
  private String clientUsername;

  public ClientHandler(Socket socket){
    try {
      this.socket = socket;

      //2 types of streams bit stream or character stream, character stream is appropiate to send messages
      // To maximize efficiency
      this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
     //same to the bufferReader
     this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
     this.clientUsername = bufferedReader.readLine();
     clientHandler.add(this);
     broadcastMessage("SERVER:"+ clientUsername + "Has entered the chat");
    }catch(IOException e){
      closeEverything(socket, bufferedReader, bufferedWriter);
    }
  }

  @Override
  public void run(){
      String messageFromClient;

      while (socket.isConnected()){
        try{
          messageFromClient = bufferedReader.readLine();
          broadcastMessage(messageFromClient);
        }catch(IOException e){
          closeEverything(socket, bufferedReader, bufferedWriter);
          break;
        }
      }
  }

  
  public void broadcastMessage( String messageToSend){
    for(ClientHandler clientHandler: clientHandler){
      try{
        if(!clientHandler.clientUsername.equals(clientUsername)){
          //Want to broadcast the message
          clientHandler.bufferedWriter.write(messageToSend);
          //Doesnt send a new line character
          clientHandler.bufferedWriter.newLine();
          //flush our buffer writer, doesnt get sent down it's an upward stream
          clientHandler.bufferedWriter.flush();
        }
      }catch(IOException e){
        closeEverything(socket, bufferedReader, bufferedWriter);
      }
    }
  }
  public void removeClientHandler(){
    clientHandler.remove(this);
    broadcastMessage("SERVER "+ clientUsername+ "Has left the chat!" );
  }

  public void closeEverything(Socket socket,BufferedReader bufferedReader, BufferedWriter bufferedWriter ){
    removeClientHandler();
    try{
      if(bufferedReader != null){
        bufferedReader.close();
      }
      if(bufferedWriter != null){
        bufferedWriter.close();
      }
      if(socket != null){
        socket.close();
      }
    }catch(IOException e){
      e.printStackTrace();
    }
  }
}
