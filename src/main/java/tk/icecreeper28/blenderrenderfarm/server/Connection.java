package tk.icecreeper28.blenderrenderfarm.server;

import org.apache.commons.io.FileUtils;
import tk.icecreeper28.blenderrenderfarm.net.Packet;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;

public class Connection extends Thread {

    private Main main;

    private Socket clientSocket;

    private InputStream is;
    private OutputStream os;

    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    private boolean isInitialized = false;
    private boolean isRunning = false;

    private SocketAddress address;

    public Connection(Socket clientSocket, Main main) {
        super();

        this.main = main;
        this.clientSocket = clientSocket;
        try {
            this.is = this.clientSocket.getInputStream();
            this.os = this.clientSocket.getOutputStream();

            this.oos = new ObjectOutputStream(os);
            this.ois = new ObjectInputStream(is);

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

        Packet blenderFilePacket = new Packet("blendFile", "byte[]");
        blenderFilePacket
                .addContent("blendFile", main.getBlenderFileBytes());

        sendPacketToClient(blenderFilePacket);

        Packet renderFramePacket = new Packet("renderFrame", "int")
                .addContent("frame", main.getNextFrame());

        sendPacketToClient(renderFramePacket);

        while (true) {
            Packet packet = null;
            try {
                packet = (Packet) this.ois.readObject();
            } catch (EOFException e) {
                continue;
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                continue;
            }
            log("Received packet " + packet.getPacketID() + " | Transmission time: " + (System.currentTimeMillis() - packet.getCreatedAt()) + "ms");

            String contentName = packet.getHeader("Content");
            String contentType = packet.getHeader("Content-Type");
            log("Content: " + contentName + " | Content-Type: " + contentType);

            if (contentName.equalsIgnoreCase("finishedFrame")) {
                try {
                    System.out.println("Writing to " + main.getWorkingDirPath() + "/frames/" + packet.getContent("fileName"));
                    FileUtils.writeByteArrayToFile(new File(main.getWorkingDirPath() + "/frames/" + packet.getContent("fileName")), (byte[]) packet.getContent("frameImage"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                renderFramePacket = new Packet("renderFrame", "int")
                        .addContent("frame", main.getNextFrame());

                sendPacketToClient(renderFramePacket);
            }
        }

//        destroyConnection();
    }

    public boolean sendPacketToClient(Packet packet) {
        return sendObjectToClient(packet);
    }

    public boolean sendObjectToClient(Object object) {
        try {
            this.oos.writeObject(object);
            this.oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void destroyConnection() {
        try {
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
