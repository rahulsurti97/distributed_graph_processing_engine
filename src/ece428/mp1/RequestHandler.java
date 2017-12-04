package ece428.mp1;

import java.util.ArrayList;

public class RequestHandler extends Handler {

    public RequestHandler(final Servent servent) {
        super(servent);
    }

    public void handleRequest(final String line) {
//        System.out.println(line);
        final String[] lineSplit = line.split("\\s+");

        if (lineSplit.length < 4) {
            System.out.println("Could not parse request");
            return;
        }

        final String responseHostAddress = lineSplit[1];
        final String responseHostName = lineSplit[2];
        final String command = lineSplit[3];

        switch (command) {
            case "put":
                PUT(responseHostAddress,
                        responseHostName,
                        lineSplit[4],
                        lineSplit[5]);
                break;
            case "get":
                GET(responseHostAddress,
                        responseHostName,
                        lineSplit[4],
                        lineSplit[5]);
                break;
            case "ls":
                LS(responseHostAddress,
                        responseHostName,
                        lineSplit[4]);
                break;
            default:
                System.out.println("Could not parse request");
        }
    }

    private void PUT(final String hostAddress,
                     final String hostName,
                     final String localFileName,
                     final String SDFSFileName) {


//        final Long lastPut = masterData.getFileToTimeStampsMap().get(localFileName);
//        if (lastPut != null) {
//            if (System.currentTimeMillis() - lastPut < 5000) {
//                System.out.println("put too soon");
//                return;
//            }
//        }

        final ArrayList<Node> replicaSet = this.servent.masterData.getReplicaSetForInsert(SDFSFileName, this.servent.membershipList, hostAddress, hostName);

        final StringBuilder sb = new StringBuilder();
        sb.append("response put ")
                .append(localFileName).append("|")
                .append(SDFSFileName).append("|");

        for (final Node node : replicaSet) {
            //dont send back original node for scp
            if (!node.getHostAddress().equals(hostAddress)) {
                sb.append(node.getHostAddress()).append(",")
                        .append(node.getHostName()).append(";");
            }
        }
        Network.sendData(hostAddress, Servent.RECEIVE_PORT_SDFS, sb.toString());

        this.servent.masterData.syncMasters(this.servent.membershipList);

        //		sshpass - p 'Flasm420!' scp test.txt ssaxen4 @fa17 -cs425 - g39 - 02. cs.illinois.edu:~ / ECE428_mp3 / sdfs /

        //request sequence number from master
        //serialize blocks
        //master lets us know if file exists in sdfs
        //if not replicate
        //if so, delete blocks at all nodes in replica set, and then replicate

        //replicate:
        //serialize blocks
        //randomly send blocks to 2 other nodes in replica set
        //maybe just send to other 3 for simplicity
        //in this case we need replica set to be 5 nodes
        //otherwise send to current sdfs directory

    }

    private void GET(final String hostAddress,
                     final String hostName,
                     final String localFileName,
                     final String SDFSFileName) {
        //request replica set from master
        //query 2 non-failed nodes from replica set
        //pass in file name to both nodes, get blocks
        //unserialize blocks

        final ArrayList<Node> replicaSet = this.servent.masterData.getReplicaSet(SDFSFileName);
        final StringBuilder sb = new StringBuilder();

        if (replicaSet.isEmpty()) {
            Network.sendData(hostAddress, Servent.RECEIVE_PORT_SDFS, "response get file_not_found");
        } else {
            sb.append("response get ")
                    .append(localFileName).append("|")
                    .append(SDFSFileName).append("|")
                    .append(hostAddress).append(",")
                    .append(hostName);
            Network.sendData(replicaSet.get(0).getHostAddress(), Servent.RECEIVE_PORT_SDFS, sb.toString());
        }
    }

//    private void DELETE(final String hostAddress,
//                        final String hostName,
//                        final String SDFSFileName) {
//        //request sequence number from master
//        //get replica set from master
//        //send delete signal for filename to all nodes in replica set
//        final String line = "response delete [IP's]";
//        Network.sendData(hostAddress, Servent.RECEIVE_PORT_SDFS, line);
//    }

    private void LS(final String hostAddress,
                    final String hostName,
                    final String localFileName) {
        final ArrayList<Node> replicaSet = this.servent.masterData.getReplicaSet(localFileName);

        final StringBuilder sb = new StringBuilder();
        sb.append("response ls ");
        if (replicaSet.isEmpty()) {
            sb.append("file_not_found");
        }
        for (final Node replica : replicaSet) {
            sb.append(replica.getHostName()).append("|");
        }

        Network.sendData(hostAddress, Servent.RECEIVE_PORT_SDFS, sb.toString());
    }
}
