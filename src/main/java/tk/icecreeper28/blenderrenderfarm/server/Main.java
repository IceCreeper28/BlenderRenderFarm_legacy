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
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeSet;

public class Main {

    public static ArrayList<Connection> connections = new ArrayList<Connection>();

    private int port = 1337;

    public static void main(String[] args) {
        new Main().init();
    }

    private void init() {
//        Options options = new Options();
//
//        Option portOption = new Option("p", "port", true, "The port on which the Server listens for request.");
//        portOption.setRequired(true);
//        options.addOption(portOption);

//        Option blenderFileOption = new Option("b", "blender-file", true, "The path to the blender file to be rendered.");
//        blenderFileOption.setRequired(true);
//        options.addOption(blenderFileOption);
//
//        Option outDirOption = new Option("o", "output-directory", true, "The path to the directory where the finished frames are saved.");
//        outDirOption.setRequired(true);
//        options.addOption(outDirOption);

//        CommandLineParser parser = new DefaultParser();
//        HelpFormatter formatter = new HelpFormatter();
//        CommandLine cmd = null;
//
//        try {
//            cmd = parser.parse(options, args);
//        } catch (ParseException e) {
//            System.out.println(e.getMessage());
//            formatter.printHelp("utility-name", options);
//
//            System.exit(1);
//        }
//
//        int port = -1;
//
//        try {
//            port = Integer.parseInt(cmd.getOptionValue("p"));
//        } catch (NumberFormatException e) {
//            System.out.println("-p: The port has to be a valid integer number.");
//            formatter.printHelp("utility-name", options);
//
//            System.exit(1);
//        }

//        String blenderFilePath = cmd.getOptionValue("b");
//        if (!Files.isRegularFile(Paths.get(blenderFilePath))) {
//            System.out.println("-b: Please enter a valid file path.");
//            formatter.printHelp("utility-name", options);
//
//            System.exit(1);
//        } else if (!FilenameUtils.getExtension(blenderFilePath).equalsIgnoreCase("blend")) {
//            System.out.println("-b: The path doesn't point to a blender file.");
//            formatter.printHelp("utility-name", options);
//
//            System.exit(1);
//        }
//        Path blenderFile = Paths.get(blenderFilePath);
//
//        String outputDirPath = cmd.getOptionValue("o");
//        if (!Files.isDirectory(Paths.get(outputDirPath))) {
//            System.out.println("-o: Please enter a valid directory path.");
//            formatter.printHelp("utility-name", options);
//
//            System.exit(1);
//        }
//        Path outputDir = Paths.get(outputDirPath);
//        System.out.println(blenderFile);
//        System.out.println(outputDir);

//        String cmdLine = "C:\\Program Files\\Blender Foundation\\Blender\\blender.exe -b \"" + blenderFile.toString() + "\" -o \"" + outputDir.toString() + "/\" -f 200";
//
//        System.out.println(cmdLine);
//        Runtime rt = Runtime.getRuntime();
//
//        Process pr = null;
//        try {
//            pr = rt.exec(cmdLine);
//            BufferedReader br = new BufferedReader(new InputStreamReader(pr.getInputStream()));
//
//            String line = "";
//            while ((line = br.readLine()) != null) {
//                System.out.println(line);
//            }
//            pr.waitFor();
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//        }

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Server started");

            Scanner scanner = new Scanner(System.in);

            while (true) {
                Socket client = server.accept();
                System.out.println("Accepted connection from " + client.getRemoteSocketAddress());
                new Connection(client).start();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
