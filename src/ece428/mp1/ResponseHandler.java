package ece428.mp1;

public class ResponseHandler extends Handler {

    public ResponseHandler(final Servent servent) {
        super(servent);
    }

    public void handleResponse(final String line) throws Exception {
        final String[] lineSplit = line.split("\\s+");
        final String command = lineSplit[1];

//        System.out.println(line);
        final String responseBody = lineSplit[2];

        switch (command) {
            case "put":
                PUT(responseBody);
                break;
            case "get":
                GET(responseBody);
                break;
            case "delete":
                DELETE(responseBody);
                break;
            case "ls":
                LS(responseBody);
                break;
            default:
                System.out.println("Could not parse response");
        }
    }

    private void PUT(final String responseBody) throws Exception {
//        System.out.println("> PUT");
        final String[] responseBodySplit = responseBody.split("\\|");

        final String localFileName = responseBodySplit[0];
        final String SDFSFileName = responseBodySplit[1];

        final String[] replicaSet = responseBodySplit[2].split("\\;");

        String[] nodeSplit;
        String IPAddress;
        String hostName;
        TerminalRunner terminalRunner;
        terminalRunner = new TerminalRunner("cp", localFileName, SDFSFileName);
        terminalRunner.run();
        for (final String node : replicaSet) {
            nodeSplit = node.split("\\,");
            IPAddress = nodeSplit[0];
            hostName = nodeSplit[1];
            terminalRunner = new TerminalRunner("put", hostName, localFileName, SDFSFileName);
            terminalRunner.run();
        }
    }

    private void GET(final String responseBody) throws Exception {
//        System.out.println("> GET");
        final String[] responseBodySplit = responseBody.split("\\|");

        if (responseBodySplit.length < 3) {
            System.out.println(responseBody);
            return;
        }

        final String localFileName = responseBodySplit[0];
        final String SDFSFileName = responseBodySplit[1];

        final String[] replicaSet = responseBodySplit[2].split("\\;");

        String[] nodeSplit;
//        String IPAddress;
        String hostName;
        TerminalRunner terminalRunner;
        for (final String node : replicaSet) {
            nodeSplit = node.split("\\,");
//            IPAddress = nodeSplit[0];
            hostName = nodeSplit[1];
            terminalRunner = new TerminalRunner("get", hostName, localFileName, SDFSFileName);
            terminalRunner.run();
        }
    }

    private void DELETE(final String responseBody) throws Exception {
//        System.out.println("> DELETE");
//        System.out.println(responseBody);

        final String[] responseBodySplit = responseBody.split("\\|");
        final String localFileName = responseBodySplit[0];
        final String SDFSFileName = responseBodySplit[1];

//        System.out.println(localFileName);
        final TerminalRunner terminalRunner = new TerminalRunner("delete", "", localFileName, SDFSFileName);
        terminalRunner.run();
    }

    private void LS(final String responseBody) {
//        System.out.println("> LS");
//        System.out.println(responseBody);
        final StringBuilder sb = new StringBuilder();
        final String[] responseBodySplit = responseBody.split("\\|");
        for (int i = 0; i < responseBodySplit.length; i++) {
            sb.append("\t").append(responseBodySplit[i]).append("\n");
        }
        System.out.println(sb.toString());
    }
}
