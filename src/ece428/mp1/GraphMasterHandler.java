package ece428.mp1;

import java.net.InetAddress;

public class GraphMasterHandler extends Handler {

    public GraphMasterHandler(final Servent servent) {
        super(servent);
    }

    public void handleGraphMaster(final String line) {
        System.out.println(line);
        final String[] lineSplit = line.split("\\s+");

//        if (lineSplit.length < 3) {
//            System.out.println("Could not parse request");
//            return;
//        }

        final String command = lineSplit[1];

        switch (command) {
            case "start_processing":
                START_PROCESSING();
                break;
            case "superstep":
                SUPERSTEP(lineSplit[2], lineSplit[3], lineSplit[4]);
                break;
            case "loadgraph":
                if (lineSplit.length == 3) {
                    MASTER_LOADGRAPH(lineSplit[2]);
                } else if (lineSplit.length == 4) {
                    MASTER_LOADGRAPH(lineSplit[2], lineSplit[3]);
                }
                break;
            case "backup_superstep":
                BACKUP_SUPERSTEP(lineSplit[2], lineSplit[3], lineSplit[4]);
                break;
            default:
                System.out.println("Could not parse graph master request");
        }
    }

    private void START_PROCESSING() {
        Network.multicast(this.servent.getWorkerNodes(),
                Servent.RECEIVE_PORT_GRAPH_MESSAGE,
                "graph superstep 0");
    }

    private void SUPERSTEP(final String superstepString,
                           final String hostName,
                           final String haltString) {
//        System.out.println(superstepString);

//        final String[] split = superstepString.split("\\|");
//
//        if (split.length < 3) {
//            System.out.println("master superstep ack missing param");
//            return;
//        }

        final Integer superstep = Integer.parseInt(superstepString);
        final Boolean halt = haltString.equals("halt");
        try {
            final NodeID worker = new NodeID(InetAddress.getByName(hostName));
            this.servent.getMasterGraph().processAcknowledge(worker, halt);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void MASTER_LOADGRAPH(final String filename) {
        if (prepareGraph(filename)) {
            START_PROCESSING();
        }
    }

    private void MASTER_LOADGRAPH(final String filename, final String source) {
        if (prepareGraph(filename)) {
            Network.multicast(this.servent.getWorkerNodes(), Servent.RECEIVE_PORT_GRAPH_MESSAGE, "graph source " + source);
            START_PROCESSING();
        }
    }

    private synchronized void BACKUP_SUPERSTEP(final String superstepString,
                                               final String hostName,
                                               final String haltString) {

        try {
            final NodeID worker = new NodeID(InetAddress.getByName(hostName));
            this.servent.getMasterGraph().handleRecovery(Integer.parseInt(superstepString), worker);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private Boolean prepareGraph(final String filename) {
        System.out.println("LOADING GRAPH " + filename);
        final long startTime = System.currentTimeMillis();

        final String[] split = filename.split("\\.");
        if (split.length < 2) {
            System.out.println("GRAPH FILE NOT FOUND");
            return false;
        }
        switch (split[0]) { // filename must match graphtype
            case GraphType.BITCOIN:
                final BitcoinGraph bitcoinGraph = new BitcoinGraph(this.servent.getWorkerNodes());
                bitcoinGraph.loadGraph("../local/" + filename);
                this.servent.setGraph(bitcoinGraph);
                break;
            case GraphType.AMAZON:
                final AmazonGraph amazonGraph = new AmazonGraph(this.servent.getWorkerNodes());
                amazonGraph.loadGraph("../local/" + filename);
                this.servent.setGraph(amazonGraph);
                break;
            case GraphType.TEST:
                final TestGraph testGraph = new TestGraph(this.servent.getWorkerNodes());
                testGraph.loadGraph("../local/" + filename);
                this.servent.setGraph(testGraph);
                break;
            default:
                System.out.println("GRAPH FILE NOT FOUND");
                break;
        }
        this.servent.getGraph().sendPartition(this.servent.getWorkerNodes());
        final long endTime = System.currentTimeMillis();
        System.out.println("Finished Loading Graph");
        System.out.println("Time elapsed: " + (endTime - startTime) + " milliseconds");

        this.servent.masterGraph.startTimer();
        return true;
    }
}
