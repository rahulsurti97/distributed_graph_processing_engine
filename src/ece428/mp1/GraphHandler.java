package ece428.mp1;

public class GraphHandler extends Handler {

    public GraphHandler(final Servent servent) {
        super(servent);
    }

    public void handleGraph(final String line) {
        System.out.println(line);
        final String[] lineSplit = line.split("\\s+");

//        if (lineSplit.length < 3) {
//            System.out.println("Could not parse request");
//            return;
//        }

        final String command = lineSplit[1];

        switch (command) {
            case "loadgraph":
                LOADGRAPHVERTEX(lineSplit[2], lineSplit[3]);
                break;
            case "superstep":
                SUPERSTEP(lineSplit[2]);
                break;
            case "message":
                MESSAGE(lineSplit[2]);
                break;
            case "source":
                SOURCE(lineSplit[2]);
                break;
            case "backup_superstep":
                BACKUP_SUPERSTEP();
                break;
            case "master_revived":
                MASTER_REVIVED();
                break;
            case "clear_graph":
                CLEARGRAPH();
                break;
            default:
                System.out.println("Could not parse graph request");
        }
    }

    private void LOADGRAPHVERTEX(final String type, final String serialized) {
        if (this.servent.getGraph().graphType.equals(GraphType.NONE)) {
            switch (type) {
                case GraphType.BITCOIN:
                    this.servent.setGraph(new BitcoinGraph(this.servent.getWorkerNodes()));
                    break;
                case GraphType.AMAZON:
                    this.servent.setGraph(new AmazonGraph(this.servent.getWorkerNodes()));
                    break;
                case GraphType.TEST:
                    this.servent.setGraph(new TestGraph(this.servent.getWorkerNodes()));
                    break;
            }
        }
        this.servent.getGraph().unserialize(serialized);
        //        System.out.println(serialized);
    }

    private void SUPERSTEP(final String superstepString) {
        System.out.println("Superstep: " + superstepString);

        final Integer superstep = Integer.parseInt(superstepString);
        final StringBuilder sb = new StringBuilder();

        sb.append("master_graph superstep ")
                .append(superstepString).append(" ")
                .append(this.servent.self.toString()).append(" ");

        if (this.servent.getGraph().getSuperstep() >= superstep) {
            sb.append("nothalt");
        } else {
            this.servent.getGraph().setSuperstep(superstep);

            if (this.servent.getGraph().compute()) { // has changed value, dont halt
                sb.append("nothalt");
            } else {
                sb.append("halt");
            }

            this.servent.getGraph().setSuperstepCache(sb.toString());
        }

        final NodeID master;
        if (this.servent.getGraph().getOnBackUpMaster()) {
            master = this.servent.getBackupMaster();
        } else {
            master = this.servent.getGraphMaster();
        }

        Network.sendData(master.getIPAddress().getHostAddress(),
                Servent.RECEIVE_PORT_GRAPH_MESSAGE,
                sb.toString());
    }

    private void MESSAGE(final String messages) {
        this.servent.getGraph().deliverMessages(messages);
    }

    private void SOURCE(final String source) {
        this.servent.getGraph().setSource(source);
    }

    private void BACKUP_SUPERSTEP() {
        final StringBuilder sb = new StringBuilder();
        sb.append("master_graph backup_superstep ")
                .append(this.servent.getGraph().getSuperstep()).append(" ")
                .append(this.servent.self.toString()).append(" ").append("not_halt");

        this.servent.getGraph().setOnBackUpMaster(true);

        Network.sendData(this.servent.getBackupMaster().getIPAddress().getHostAddress(),
                Servent.RECEIVE_PORT_GRAPH_MESSAGE,
                sb.toString());
    }

    private void MASTER_REVIVED() {
        final StringBuilder sb = new StringBuilder();
        sb.append("master_graph backup_superstep ")
                .append(this.servent.getGraph().getSuperstep()).append(" ")
                .append(this.servent.self.toString()).append(" ").append("not_halt");

        this.servent.getGraph().setOnBackUpMaster(false);

        Network.sendData(this.servent.getGraphMaster().getIPAddress().getHostAddress(),
                Servent.RECEIVE_PORT_GRAPH_MESSAGE,
                sb.toString());
    }

    private void CLEARGRAPH() {
        this.servent.setGraph(new Graph(this.servent.getWorkerNodes()));
        this.servent.setMasterGraph(new MasterGraph(this.servent, this.servent.getWorkerNodes()));
        System.out.println(this.servent.getGraph().toString());
    }
}
