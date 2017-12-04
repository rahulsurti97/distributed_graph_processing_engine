package ece428.mp1;


public class InputParser {

    Servent servent;
    String masterHostAddress;
    NodeID nodeID;
    String inputCommand;
    MembershipList membershipList;
    MasterData masterData;

    public InputParser(final String inputCommand,
                       final String masterHostAddress,
                       final NodeID nodeID,
                       final MembershipList membershipList,
                       final MasterData masterData,
                       final Servent servent)
            throws Exception {
        this.servent = servent;
        this.masterHostAddress = masterHostAddress;
        this.nodeID = nodeID;
        this.inputCommand = inputCommand;
        this.membershipList = membershipList;
        this.masterData = masterData;
    }

    public void run() throws Exception {
        final String[] commandSplit = this.inputCommand.trim().split("\\s+");
        if (this.inputCommand.isEmpty()) {
            return;
        }
        final String cmd = commandSplit[0].toLowerCase();
        boolean invalid = false;
        StringBuilder line = new StringBuilder();
        line.append("request ")
                .append(this.nodeID.getIPAddress().getHostAddress()).append(" ")
                .append(this.nodeID.getIPAddress().getHostName()).append(" ")
                .append(cmd.toLowerCase()).append(" ");
        switch (cmd) {
            case "put":
                if (commandSplit.length == 3) {
                    final String localFileName = commandSplit[1];
                    final String SDFSFileName = commandSplit[2];
                    line.append(localFileName).append(" ").append(SDFSFileName);
                } else {
                    invalid = true;
                }
                break;
            case "get":
                if (commandSplit.length == 3) {
                    line.append(commandSplit[1]).append(" ").append(commandSplit[2]);
                } else {
                    invalid = true;
                }
                break;
            case "delete":
                if (commandSplit.length == 2) {
                    line = new StringBuilder();
                    line.append("master delete ").append(commandSplit[1]);
                } else {
                    invalid = true;
                }
                break;
            case "ls":
                if (commandSplit.length == 2) {
                    line.append(commandSplit[1]);
                } else {
                    invalid = true;
                }
                break;
            case "store":
                if (commandSplit.length == 1) {
                    final TerminalRunner terminalRunner = new TerminalRunner("store");
                    terminalRunner.run();
                    return;
                } else {
                    invalid = true;
                }
                break;
            case "master":
                if (commandSplit.length == 1) {
                    if (this.membershipList.isMainMaster()) {
                        System.out.println("MAIN");
                    }
                    System.out.println(this.masterData.toString());
                    return;
                } else {
                    invalid = true;
                }
                break;
            case "local":
                if (commandSplit.length == 1) {
                    final TerminalRunner terminalRunner = new TerminalRunner("local");
                    terminalRunner.run();
                    return;
                } else {
                    invalid = true;
                }
                break;
            case "memlist":
                if (commandSplit.length == 1) {
                    System.out.println(this.membershipList.toString());
                    return;
                } else {
                    invalid = true;
                }
                break;
            case "clear":
                for (int i = 0; i < 50; i++) {
                    System.out.println("");
                }
                return;
            case "pagerank":
                if (commandSplit.length == 2) {
                    this.PAGE_RANK(commandSplit[1]);
                    return;
                }
                invalid = true;
                break;
            case "sssp":
                if (commandSplit.length == 3) {
                    this.SSSP(commandSplit[1], commandSplit[2]);
                    return;
                }
                invalid = true;
                break;
            case "getgraph":
                System.out.println(this.servent.getGraph().toString());
                return;
            case "printgraph":
                this.servent.getGraph().printGraph();
                return;
            case "process":
                if (commandSplit.length == 1) {
                    this.PROCESS();
                }
                return;
            case "cleargraph":
                CLEARGRAPH();
                return;
            case "searchgraph":
                if (commandSplit.length == 2) {
                    if (this.servent.getGraph().graph.get(commandSplit[1]) != null) {
                        System.out.println(this.servent.getGraph().graph.get(commandSplit[1]).toString());
                    }
                    return;
                }
                invalid = true;
                break;
            default:
                invalid = true;
        }
        if (!invalid) {
            Network.sendData(this.membershipList.getMainMaster().getIPAddress().getHostAddress(), Servent.RECEIVE_PORT_SDFS, line.toString());
        } else {
            System.out.println("> INVALID COMMAND!");
        }
    }

    private void PAGE_RANK(final String filename) throws Exception {
        final TerminalRunner terminalRunner = new TerminalRunner("transfer",
                this.servent.getGraphMaster().getIPAddress().getHostName(),
                filename,
                filename);
        terminalRunner.run();
        Network.sendData(this.servent.getGraphMaster().getIPAddress().getHostAddress(),
                Servent.RECEIVE_PORT_GRAPH_MESSAGE,
                "master_graph loadgraph " + filename);
    }

    private void SSSP(final String filename, final String source) throws Exception {
        final TerminalRunner terminalRunner = new TerminalRunner("transfer",
                this.servent.getGraphMaster().getIPAddress().getHostName(),
                filename,
                filename);
        terminalRunner.run();
        Network.sendData(this.servent.getGraphMaster().getIPAddress().getHostAddress(),
                Servent.RECEIVE_PORT_GRAPH_MESSAGE,
                "master_graph loadgraph " + filename + " " + source);
    }

    private void PROCESS() {
        final StringBuilder line = new StringBuilder();
        line.append("master_graph start_processing");
        Network.sendData(this.servent.getGraphMaster().getIPAddress().getHostAddress(),
                Servent.RECEIVE_PORT_GRAPH_MESSAGE,
                line.toString());
    }

    private void CLEARGRAPH() {
        this.servent.setGraph(new Graph(this.servent.getWorkerNodes()));
        this.servent.setMasterGraph(new MasterGraph(this.servent, this.servent.getWorkerNodes()));
        System.out.println(this.servent.getGraph().toString());
    }

//    private void LOADGRAPH(final String graphType) {
//        final long startTime = System.currentTimeMillis();
//
//        switch (graphType) {
//            case GraphType.BITCOIN:
//                final BitcoinGraph bitcoinGraph = new BitcoinGraph();
//                bitcoinGraph.loadGraph("../local/bitcoin.csv");
//                this.servent.setGraph(bitcoinGraph);
//                break;
//            case GraphType.AMAZON:
//                final AmazonGraph amazonGraph = new AmazonGraph();
//                amazonGraph.loadGraph("../local/amazon.txt");
//                this.servent.setGraph(amazonGraph);
//                break;
//            case GraphType.TEST:
//                final TestGraph testGraph = new TestGraph();
//                testGraph.loadGraph("../local/test.txt");
//                this.servent.setGraph(testGraph);
//                break;
//        }
//        this.servent.getGraph().sendPartition(this.servent.getWorkerNodes());
//        final long endTime = System.currentTimeMillis();
//        System.out.println("Finished Loading Graph");
//        System.out.println("Time elapsed: " + (endTime - startTime) + " milliseconds");
//        this.PROCESS();
//    }
}
