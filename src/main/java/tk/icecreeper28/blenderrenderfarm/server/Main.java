package tk.icecreeper28.blenderrenderfarm.server;

import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Main {

    public static ArrayList<Connection> connections = new ArrayList<Connection>();

    private int port;
    private byte[] bFileBytes;
    private Path blenderFilePath;
    private Path workingDirPath;

    private int startFrame = 0;
    private int currentFrame = startFrame-1;
    private int endFrame = 4;

    public static void main(String[] args) {
        new Main().init(args);
    }

    private void init(String[] args) {
        parseCMDOptions(args);

        //Load blender file
        try {
            bFileBytes = Files.readAllBytes(blenderFilePath);
            System.out.println(bFileBytes.length + " bytes read.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Server started");

            while (true) {
                Socket client = server.accept();
                System.out.println("Accepted connection from " + client.getRemoteSocketAddress());
                new Connection(client, this).start();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void parseCMDOptions(String[] args) {
        Options options = new Options();

        Option portOption = new Option("p", "port", true, "The port on which the Server listens for request.");
        portOption.setRequired(true);
        options.addOption(portOption);

        Option blenderFileOption = new Option("b", "blender-file", true, "The path to the blender file to be rendered.");
        blenderFileOption.setRequired(true);
        options.addOption(blenderFileOption);

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

        this.port = -1;

        try {
            this.port = Integer.parseInt(cmd.getOptionValue("p"));
        } catch (NumberFormatException e) {
            System.out.println("-p: The port has to be a valid integer number.");
            formatter.printHelp("utility-name", options);

            System.exit(1);
        }

        String blenderFilePathString = cmd.getOptionValue("b");
        if (!Files.isRegularFile(Paths.get(blenderFilePathString))) {
            System.out.println("-b: Please enter a valid file path.");
            formatter.printHelp("utility-name", options);

            System.exit(1);
        } else if (!FilenameUtils.getExtension(blenderFilePathString).equalsIgnoreCase("blend")) {
            System.out.println("-b: The path doesn't point to a blender file.");
            formatter.printHelp("utility-name", options);

            System.exit(1);
        }
        this.blenderFilePath = Paths.get(blenderFilePathString);

        String workingDirPathString = cmd.getOptionValue("w");
        if (!Files.isDirectory(Paths.get(workingDirPathString))) {
            System.out.println("-w: Please enter a valid directory path.");
            formatter.printHelp("utility-name", options);

            System.exit(1);
        }
        this.workingDirPath = Paths.get(workingDirPathString);
    }

    public byte[] getBlenderFileBytes() {
        return bFileBytes;
    }

    public Path getWorkingDirPath() {
        return workingDirPath;
    }

    public int getNextFrame() {
        if (currentFrame < endFrame) {
            currentFrame++;
            System.out.println("Current frame: " + currentFrame);
            return currentFrame;
        }
        return -1;
    }
}
