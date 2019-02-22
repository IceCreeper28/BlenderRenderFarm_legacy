package tk.icecreeper28.blenderrenderfarm.server;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;

public class Connection extends Thread {

    private Socket clientSocket;

    private InputStream is;
    private OutputStream os;

    private InputStreamReader isr;
    private OutputStreamWriter osw;

    private BufferedReader br;
    private BufferedWriter bw;

    private boolean isInitialized = false;
    private boolean isRunning = false;

    private SocketAddress address;

    public Connection(Socket clientSocket) {
        super();

        this.clientSocket = clientSocket;
        try {
            this.is = this.clientSocket.getInputStream();
            this.os = this.clientSocket.getOutputStream();

            this.isr = new InputStreamReader(this.is);
            this.osw = new OutputStreamWriter(this.os);

            this.br = new BufferedReader(this.isr);
            this.bw = new BufferedWriter(this.osw);

            this.address = this.clientSocket.getRemoteSocketAddress();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.isInitialized = true;
        this.isRunning = true;

        this.log("Connection object constructed!");
    }

    @Override
    public void run() {
        if (!this.isInitialized) {
            destroyConnection();
            return;
        }
        super.run();

        sendBytesToClient(new byte[] {90, 79, 79, 33});
        sendTextToClient(" = new byte[] {90, 79, 79, 33}");

        destroyConnection();
    }

    public boolean sendTextToClient(String text) {
        try {
            this.bw.write(text + System.lineSeparator());
            this.bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean sendBytesToClient(byte[] bytes) {
        try {
            this.os.write(bytes);
            this.os.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void destroyConnection() {
        try {
            this.br.close();
            this.bw.close();

            this.isr.close();
            this.osw.close();

            this.is.close();
            this.os.close();

            this.clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.log("Connection Closed!");
    }

    private void log(String message) {
        System.out.println("[" + this.address + "] " + message);
    }
}
