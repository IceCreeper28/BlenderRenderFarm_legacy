package tk.icecreeper28.blenderrenderfarm.client;

import org.apache.commons.cli.Options;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    String cmdLine = "blender -b holopyramid.blend -o //out/frame-####.png -f 10";

    private BufferedReader br;
    private BufferedWriter bw;

    private String ip = "::1";
    private int port = 1337;

    private Socket socket;

    public static void main(String[] args) {
        new Main().init();
    }

    private void init() {
        System.out.println("Starting Client");

//        Options options = new Options();

        System.out.println("Connecting to Server...");
        socket = null;
        try {
            socket = new Socket(ip, port);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("Connecting to Server... CONNECTED");

        try
        {
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            String line = "";
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
            System.out.println("Closing connection...");
            br.close();
            bw.close();
            socket.close();
            System.out.println("Closing connection... CLOSED");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
