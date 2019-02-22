package tk.icecreeper28.blenderrenderfarm.client;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import tk.icecreeper28.blenderrenderfarm.net.Packet;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Main {

    private String cmdLine = "blender -b holopyramid.blend -o //out/frame-####.png -f 10";


    private String ip;
    private int port;

    private Path blenderEXEPath;
    private Path workingDirPath;
    private Path blenderFilePath;
    private Path frameDirPath;

    private Socket socket;

    private InputStream is;
    private OutputStream os;

    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public static void main(String[] args) {
        new Main().init(args);
    }

    private void init(String[] args) {
        parseCMDOptions(args);
        System.out.println("Starting Client");
        System.out.println("Connecting to Server...");
        try {
            this.socket = new Socket(ip, port);

            this.is = socket.getInputStream();
            this.os = socket.getOutputStream();

            this.ois = new ObjectInputStream(is);
            this.oos = new ObjectOutputStream(os);

            System.out.println("Connecting to Server... CONNECTED");
        } catch (IOException e) {
            e.printStackTrace();
        }

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
            System.out.println("Received packet " + packet.getPacketID() + " | Transmission time: " + (System.currentTimeMillis() - packet.getCreatedAt()) + "ms");

            String contentName = packet.getHeader("Content");
            String contentType = packet.getHeader("Content-Type");
            System.out.println("Content: " + contentName + " | Content-Type: " + contentType);

            if (contentName.equalsIgnoreCase("blendFile")) {
                try {
                    System.out.println("Writing to " + this.blenderFilePath);
                    FileUtils.writeByteArrayToFile(new File(this.blenderFilePath.toString()), (byte[]) packet.getContent("blendFile"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (contentName.equalsIgnoreCase("renderFrame")) {
                int frame = (int)packet.getContent("frame");
                if (frame < 0) {
                    System.out.println("No new frame. Exiting.");
                    break;
                }
                renderFrame(frame);
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void parseCMDOptions(String[] args) {
        Options options = new Options();

        Option ipOption = new Option("ip",true, "The ip on which the Client tries to connect to the server.");
        ipOption.setRequired(true);
        options.addOption(ipOption);

        Option portOption = new Option("p", "port", true, "The port on which the Client tries to connect to the server.");
        portOption.setRequired(true);
        options.addOption(portOption);

        Option blenderEXEOption = new Option("b", "blender", true, "The path to the blender.exe");
        blenderEXEOption.setRequired(true);
        options.addOption(blenderEXEOption);

        Option workingDirOption = new Option("w", "working-directory", true, "The path to the working directory.");
        workingDirOption.setRequired(true);
        options.addOption(workingDirOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
        }

        this.ip = cmd.getOptionValue("ip");

        this.port = -1;

        try {
            this.port = Integer.parseInt(cmd.getOptionValue("p"));
        } catch (NumberFormatException e) {
            System.out.println("-p: The port has to be a valid integer number.");
            formatter.printHelp("utility-name", options);

            System.exit(1);
        }

        String blenderEXEPathString = cmd.getOptionValue("b");
        if (!Files.isRegularFile(Paths.get(blenderEXEPathString))) {
            System.out.println("-b: Please enter a valid file path.");
            formatter.printHelp("utility-name", options);

            System.exit(1);
        } else if (!FilenameUtils.getExtension(blenderEXEPathString).equalsIgnoreCase("exe")) {
            System.out.println("-b: The path doesn't point to a .exe file.");
            formatter.printHelp("utility-name", options);

            System.exit(1);
        }
        this.blenderEXEPath = Paths.get(blenderEXEPathString);

        String workingDirPathString = cmd.getOptionValue("w");
        if (!Files.isDirectory(Paths.get(workingDirPathString))) {
            System.out.println("-w: Please enter a valid directory path.");
            formatter.printHelp("utility-name", options);

            System.exit(1);
        }
        this.workingDirPath = Paths.get(workingDirPathString);
        this.blenderFilePath = Paths.get(this.workingDirPath.toString() + File.separator + "blenderFile.blend");
        this.frameDirPath = Paths.get(this.workingDirPath.toString() + File.separator + "frames");
    }

    public void renderFrame(int frame) {
        String cmdLine = this.blenderEXEPath.toString() + " -b \"" + this.blenderFilePath + "\" -o \"" + this.frameDirPath + "/\" -f " + frame;

        Runtime rt = Runtime.getRuntime();

        Process pr = null;
        try {
            System.out.println("Starting rendering of frame " + frame);
            pr = rt.exec(cmdLine);
            BufferedReader br = new BufferedReader(new InputStreamReader(pr.getInputStream()));

            String line = "";
            while ((line = br.readLine()) != null) {
//                System.out.println(line);
            }
            pr.waitFor();
            System.out.println("Finished rendering of frame " + frame);

            File frameFolder = new File(this.frameDirPath.toString());
            String frameFile = FilenameUtils.getName(frameFolder.listFiles()[0].getPath().toString());

            byte[] frameBytes = null;
            try {
                frameBytes = Files.readAllBytes(Paths.get(frameFolder.listFiles()[0].getPath().toString()));
                System.out.println(frameBytes.length + " bytes read.");
            } catch (IOException e) {
                e.printStackTrace();
            }

            Packet finishedFramePacket = new Packet("finishedFrame", "byte[]")
                    .addContent("fileName", frameFile)
                    .addContent("frameImage", frameBytes);

            sendPacketToServer(finishedFramePacket);
            Files.deleteIfExists(Paths.get(frameFolder.listFiles()[0].getPath()));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return;
        }
    }

    public boolean sendPacketToServer(Packet packet) {
        return sendObjectToServer(packet);
    }

    public boolean sendObjectToServer(Object object) {
        try {
            this.oos.writeObject(object);
            this.oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public byte[] readImage (String ImageName) throws IOException {
        // open image
        File imgPath = new File(ImageName);
        BufferedImage bufferedImage = ImageIO.read(imgPath);

        // get DataBufferBytes from Raster
        WritableRaster raster = bufferedImage .getRaster();
        DataBufferByte data   = (DataBufferByte) raster.getDataBuffer();

        return ( data.getData() );
    }

}
