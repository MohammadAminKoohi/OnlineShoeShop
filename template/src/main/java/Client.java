import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public void start() throws IOException{
        Socket socket = new Socket("localhost", 8080);
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        String input;
        Scanner scanner= new Scanner(System.in);
        while(true){
            input=scanner.nextLine();
            dataOutputStream.writeUTF(input);
            System.out.println(dataInputStream.readUTF());
        }
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        client.start();
    }
}
