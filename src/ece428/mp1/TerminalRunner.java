package ece428.mp1;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

public class TerminalRunner {

    private static final File SDFSDirectory = new File("../sdfs/");
    private static final File LocalDirectory = new File("../local/");

    private final String type;
    private String hostName = null;
    private ArrayList<String> commandList = null;
    private String SDFSFileName = null;
    private String localFileName = null;
    private String password = null;

    public TerminalRunner(final String type) {
        this.type = type;
    }

    public TerminalRunner(final String type, final String hostName, final String localFileName, final String SDFSFileName)
            throws Exception {
        this.type = type;
        this.hostName = hostName;
        this.localFileName = localFileName;
        this.SDFSFileName = SDFSFileName;
        this.password = new BufferedReader(new FileReader("../pw.txt")).readLine();
        this.commandList = new ArrayList<>(Arrays.asList("/bin/sh", "-c"));
    }

    public TerminalRunner(final String type, final String localFileName, final String SDFSFileName)
            throws Exception {
        this.type = type;
        this.hostName = "";
        this.localFileName = localFileName;
        this.SDFSFileName = SDFSFileName;
        this.password = new BufferedReader(new FileReader("../pw.txt")).readLine();
        this.commandList = new ArrayList<>(Arrays.asList("/bin/sh", "-c"));
    }

    public void run() throws Exception {
        switch (this.type) {
            case "store":
                this.STORE();
                break;
            case "put":
                this.PUT();
                break;
            case "get":
                this.GET();
                break;
            case "transfer":
                this.TRANSFER();
                break;
            case "clear":
                this.CLEAR();
                break;
            case "clear_local":
                this.CLEAR_LOCAL();
                break;
            case "delete":
                this.DELETE();
                break;
            case "local":
                this.LOCAL();
                break;
            case "cp":
                this.CP();
                break;
        }
    }

    private void STORE() {
        final File[] listFiles = SDFSDirectory.listFiles();
        System.out.println("SDFS Contents:");
        for (final File f : listFiles) {
            System.out.println("\t" + f.getName());
        }
    }

    private void PUT() throws Exception {
        final StringBuilder sb = new StringBuilder();
//		 sshpass -p 'Flasm420!' scp test.txt ssaxen4@fa17-cs425-g39-02.cs.illinois.edu:~/ECE428_mp3/sdfs/
        sb.append("sshpass -p '").append(this.password).append("' scp ").append("-o StrictHostKeyChecking=no ")
                .append("../sdfs/").append(this.SDFSFileName).append(" ")
                .append("rsurti2@").append(this.hostName).append(":").append("~/ECE428_mp4/sdfs/");
        this.commandList.add(sb.toString());
//        System.out.println(sb.toString());
//        System.out.println("storing at " + this.hostName);
        runCommand();
    }

    private void GET() throws Exception {
        final StringBuilder sb = new StringBuilder();
//		 sshpass -p 'Flasm420!' scp test.txt ssaxen4@fa17-cs425-g39-02.cs.illinois.edu:~/ECE428_mp3/sdfs/
        sb.append("sshpass -p '").append(this.password).append("' scp ").append("-o StrictHostKeyChecking=no ")
                .append("../sdfs/").append(this.SDFSFileName).append(" ")
                .append("rsurti2@").append(this.hostName).append(":").append("~/ECE428_mp4/local/").append(this.localFileName);
        this.commandList.add(sb.toString());
//        System.out.println(sb.toString());
//        System.out.println("storing at " + this.hostName);
        runCommand();
    }

    private void TRANSFER() throws Exception {
        final StringBuilder sb = new StringBuilder();
//		 sshpass -p 'Flasm420!' scp test.txt ssaxen4@fa17-cs425-g39-02.cs.illinois.edu:~/ECE428_mp3/sdfs/
        sb.append("sshpass -p '").append(this.password).append("' scp ").append("-o StrictHostKeyChecking=no ")
                .append("../local/").append(this.localFileName).append(" ")
                .append("rsurti2@").append(this.hostName).append(":").append("~/ECE428_mp4/local/").append(this.localFileName);
        this.commandList.add(sb.toString());
//        System.out.println(sb.toString());
//        System.out.println("storing at " + this.hostName);
        runCommand();
    }

    private void CLEAR() throws Exception {
        this.commandList = new ArrayList<>(Arrays.asList("/bin/sh", "-c"));
        this.commandList.add("rm -rf ../sdfs/*");
        runCommand();
    }

    private void CLEAR_LOCAL() throws Exception {
        this.commandList = new ArrayList<>(Arrays.asList("/bin/sh", "-c"));
        this.commandList.add("rm -rf ../local/*");
        runCommand();
    }

    private void DELETE() throws Exception {
        this.commandList = new ArrayList<>(Arrays.asList("/bin/sh", "-c"));
        this.commandList.add("rm -rf ../sdfs/" + this.SDFSFileName);
        runCommand();
    }

    private void LOCAL() {
        final File[] listFiles = LocalDirectory.listFiles();
        System.out.println("Local Contents:");
        for (final File f : listFiles) {
            System.out.println("\t" + f.getName());
        }
    }

    private void CP() throws Exception {
        final StringBuilder sb = new StringBuilder();
        sb.append("cp ../local/").append(this.localFileName).append(" ../sdfs/").append(this.SDFSFileName);
        this.commandList.add(sb.toString());
        runCommand();
    }

    private void runCommand() throws Exception {
        final long startTime = System.currentTimeMillis();
        final Process process = Runtime.getRuntime().exec(this.commandList.toArray(new String[0]));
        process.waitFor();

//        System.out.println(this.commandList.get(this.commandList.size() - 1));

        final long endTime = System.currentTimeMillis();
//        System.out.println("Time elapsed: " + (endTime - startTime) + " milliseconds");
    }
}