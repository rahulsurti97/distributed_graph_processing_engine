package ece428.mp1;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class MasterData {
    private ConcurrentHashMap<String, Long> fileToTimeStampsMap;
    private ConcurrentHashMap<Node, ArrayList<String>> nodeToFilesMap;
    private ConcurrentHashMap<String, ArrayList<Node>> fileToNodesMap;

    public MasterData() {
        this.nodeToFilesMap = new ConcurrentHashMap<>();
        this.fileToNodesMap = new ConcurrentHashMap<>();
        this.fileToTimeStampsMap = new ConcurrentHashMap<>();
    }

    public MasterData(final String serialized) {
        this.nodeToFilesMap = new ConcurrentHashMap<>();
        this.fileToNodesMap = new ConcurrentHashMap<>();
        this.fileToTimeStampsMap = new ConcurrentHashMap<>();
        this.unserialize(serialized);
    }

    public synchronized void unserialize(final String serialized) {
        this.nodeToFilesMap = new ConcurrentHashMap<>();
        this.fileToNodesMap = new ConcurrentHashMap<>();
        this.fileToTimeStampsMap = new ConcurrentHashMap<>();

        final String[] serialSplit = serialized.split("\\`");

        if (serialSplit.length < 3) {
            return;
        }

        final String nodeToFileSerialized = serialSplit[0];
        final String fileToNodeSerialized = serialSplit[1];
        final String fileToTimestampSerialized = serialSplit[2];

        final String[] nodeToFiles = nodeToFileSerialized.split("\\|");
        final String[] fileToNodes = fileToNodeSerialized.split("\\|");
        final String[] fileToTimestamps = fileToTimestampSerialized.split("\\|");

        String[] nodeToFileSplit;
        String nodeSerialized;
        String[] nodeSplit;
        String[] inetSplit;
        String hostName;
        String ip;
        String[] fileSplit;
        Node node;
        ArrayList<String> files;

        for (final String nodeToFile : nodeToFiles) {
            if (nodeToFile.isEmpty()) {
                continue;
            }
            nodeToFileSplit = nodeToFile.split("\\:");
            nodeSerialized = nodeToFileSplit[0];
            nodeSplit = nodeSerialized.split("\\/");
            ip = nodeSplit[0];
            hostName = nodeSplit[1];
            fileSplit = nodeToFileSplit[1].split("\\,");

            files = new ArrayList<>(Arrays.asList(fileSplit));
            node = new Node(ip, hostName);

            this.nodeToFilesMap.put(node, files);
        }

        String[] fileToNodeSplit;
        String file;
        String[] nodesSplit;
        ArrayList<Node> nodes;

        for (final String fileToNode : fileToNodes) {
            if (fileToNode.isEmpty()) {
                continue;
            }
            fileToNodeSplit = fileToNode.split("\\:");
            file = fileToNodeSplit[0];

            nodesSplit = fileToNodeSplit[1].split("\\,");

            nodes = new ArrayList<>();
            for (final String temp : nodesSplit) {
                nodeSplit = temp.split("\\/");
                ip = nodeSplit[0];
                hostName = nodeSplit[1];
                node = new Node(ip, hostName);
                nodes.add(node);
            }

            this.fileToNodesMap.put(file, nodes);
        }


        String[] fileToTimeStampSplit;
        for (final String fileToTimeStamp : fileToTimestamps) {
            fileToTimeStampSplit = fileToTimeStamp.split("\\:");
            file = fileToTimeStampSplit[0];
            this.fileToTimeStampsMap.put(file, Long.parseLong(fileToTimeStampSplit[1]));
        }
    }

    public ConcurrentHashMap<String, Long> getFileToTimeStampsMap() {
        return this.fileToTimeStampsMap;
    }

    public synchronized ArrayList<Node> getReplicaSetForInsert(final String SDFSFilename,
                                                               final MembershipList membershipList,
                                                               final String hostAddress,
                                                               final String hostName) {
        if (this.fileToNodesMap.get(SDFSFilename) == null) {
            this.fileToTimeStampsMap.put(SDFSFilename, getCurrentTime());


            this.insertInHashMaps(SDFSFilename, membershipList, hostAddress, hostName);
        }
        return this.fileToNodesMap.get(SDFSFilename);
    }

    private long getCurrentTime() {
        return LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public synchronized void insertInHashMaps(final String SDFSFilename, final MembershipList membershipList, final String hostAddress, final String hostName) {
        final ArrayList<Node> replicaSet = getNewReplicaSet(membershipList, hostAddress, hostName);
        this.fileToNodesMap.put(SDFSFilename, replicaSet);

        for (final Node node : replicaSet) {
            this.insertFileAtNode(SDFSFilename, node);
        }
    }

    public synchronized ArrayList<Node> getNewReplicaSet(final MembershipList membershipList, final String hostAddress, final String hostName) {
        final ArrayList<Node> replicaSet = new ArrayList<>();

        final ArrayList<NodeID> allKeys = new ArrayList<>(membershipList.listEntries.keySet());

        InetAddress inetAddress;
        final Random rand = new Random();
        NodeID node;
        for (int i = 0; i < 2; i++) {
            while (allKeys.size() > 0) {
                node = allKeys.remove(rand.nextInt(allKeys.size()));
                if (membershipList.listEntries.get(node).getAlive()) {
                    inetAddress = node.getIPAddress();
                    if (!inetAddress.getHostAddress().equals(hostAddress)) {
                        replicaSet.add(new Node(inetAddress.getHostAddress(), inetAddress.getHostName()));
                        break;
                    }
                }
            }
        }
        replicaSet.add(new Node(hostAddress, hostName));
        return replicaSet;
    }

    public void insertFileAtNode(final String file, final Node node) {
        final ArrayList<String> fileList = this.getFileList(node);
        fileList.add(file);
        this.nodeToFilesMap.put(node, fileList);
    }

    public synchronized ArrayList<String> getFileList(final Node node) {
        final ArrayList<String> fileList = this.nodeToFilesMap.get(node);
        if (fileList == null) {
            return new ArrayList<>();
        }
        return fileList;
    }

    @Override
    public String toString() {
        if (this.nodeToFilesMap.isEmpty() && this.fileToTimeStampsMap.isEmpty()) {
            return "";
        }

        final StringBuilder sb = new StringBuilder();

        Iterator it = this.nodeToFilesMap.entrySet().iterator();
        sb.append("\nNode:File\n");
        while (it.hasNext()) {
            final ConcurrentHashMap.Entry pair = (ConcurrentHashMap.Entry) it.next();
            final Node node = (Node) pair.getKey();
            sb.append(node.toString()).append(":\n");
            final ArrayList<String> files = this.nodeToFilesMap.get(node);
            if (files != null) {
                for (int i = 0; i < files.size(); i++) {
                    sb.append("\t").append(files.get(i)).append("\n");
                }
            }
        }

        sb.append("\nFile:Node\n");
        it = this.fileToNodesMap.entrySet().iterator();
        while (it.hasNext()) {
            final ConcurrentHashMap.Entry pair = (ConcurrentHashMap.Entry) it.next();
            final String file = (String) pair.getKey();
            sb.append(file).append(":\n");
            final ArrayList<Node> nodes = this.fileToNodesMap.get(file);
            if (nodes != null) {
                for (int i = 0; i < nodes.size(); i++) {
                    sb.append("\t").append(nodes.get(i).toString()).append("\n");
                }
            }
        }

        sb.append("\nFile:Timestamp\n");
        it = this.fileToTimeStampsMap.entrySet().iterator();
        while (it.hasNext()) {
            final ConcurrentHashMap.Entry pair = (ConcurrentHashMap.Entry) it.next();
            final String file = (String) pair.getKey();
            sb.append(file).append(":\n\t")
                    .append(this.fileToTimeStampsMap.get(file).toString()).append("\n");
        }

        return sb.toString();
    }

    public synchronized void rereplicate(final ArrayList<NodeID> deadNodes, final MembershipList membershipList) {
        for (final NodeID node : deadNodes) {
            final Node failed = new Node(node.getIPAddress().getHostAddress(), node.getIPAddress().getHostName());
            if (membershipList.isMainMaster() && this.nodeToFilesMap.get(failed) != null) {
                System.out.println("rereplicating for " + node.getIPAddress().getHostName());

                final ArrayList<String> filesToReplicate = (ArrayList<String>) this.nodeToFilesMap.get(failed).clone();

                if (filesToReplicate == null) {
                    System.out.println("no files found");
                    return;
                }

                for (final String file : filesToReplicate) {
                    this.fileToNodesMap.get(file).remove(failed);
                    final Node newReplica = membershipList.getNewReplica(this.fileToNodesMap.get(file));

                    if (newReplica == null) {
                        return;
                    }

                    final StringBuilder sb = new StringBuilder();
                    sb.append("response put ")
                            .append(file).append("|")
                            .append(file).append("|")
                            .append(newReplica.getHostAddress()).append(",")
                            .append(newReplica.getHostName()).append(";");

                    for (final Node replica : this.fileToNodesMap.get(file)) {
                        Network.sendData(replica.getHostAddress(), Servent.RECEIVE_PORT_SDFS, sb.toString());
                    }

                    this.insertFileAtNode(file, newReplica);
                    this.insertNodeAtFile(newReplica, file);

                }

                this.nodeToFilesMap.remove(failed);
                this.syncMasters(membershipList);
            }
        }
    }

    public void insertNodeAtFile(final Node node, final String file) {
        final ArrayList<Node> nodeList = this.getReplicaSet(file);
        nodeList.add(node);
        this.fileToNodesMap.put(file, nodeList);
    }

    public synchronized void syncMasters(final MembershipList membershipList) {
        final String serialized = this.serialize();
        final ArrayList<NodeID> masters = membershipList.getMasters();

        StringBuilder sb;
        for (final NodeID master : masters) {
            if (!membershipList.getSelf().equals(master)) {
                sb = new StringBuilder();
                sb.append("master update ").append(serialized);
                Network.sendData(master.getIPAddress().getHostAddress(), Servent.RECEIVE_PORT_SDFS, sb.toString());
            }
        }
    }

    public synchronized ArrayList<Node> getReplicaSet(final String SDFSFilename) {
        ArrayList<Node> replicaSet = this.fileToNodesMap.get(SDFSFilename);
        if (replicaSet == null) {
            replicaSet = new ArrayList<>();
        }
        return replicaSet;
    }

    public synchronized String serialize() {
        final StringBuilder sb = new StringBuilder();

        Iterator it = this.nodeToFilesMap.entrySet().iterator();
        while (it.hasNext()) {
            final ConcurrentHashMap.Entry pair = (ConcurrentHashMap.Entry) it.next();
            final Node node = (Node) pair.getKey();
            sb.append(node.getHostAddress())
                    .append("/")
                    .append(node.getHostName())
                    .append(":");
            final ArrayList<String> files = this.nodeToFilesMap.get(node);
            if (files != null) {
                for (int i = 0; i < files.size(); i++) {
                    sb.append(files.get(i));
                    if (i == files.size() - 1) {
                        sb.append("|");
                    } else {
                        sb.append(",");
                    }
                }
            }
        }

        sb.append("`");

        it = this.fileToNodesMap.entrySet().iterator();
        while (it.hasNext()) {
            final ConcurrentHashMap.Entry pair = (ConcurrentHashMap.Entry) it.next();
            final String file = (String) pair.getKey();
            sb.append(file).append(":");
            final ArrayList<Node> nodes = this.fileToNodesMap.get(file);
            if (nodes != null) {
                for (int i = 0; i < nodes.size(); i++) {
                    sb.append(nodes.get(i).getHostAddress())
                            .append("/")
                            .append(nodes.get(i).getHostName());
                    if (i == nodes.size() - 1) {
                        sb.append("|");
                    } else {
                        sb.append(",");
                    }
                }
            }
        }

        sb.append("`");

        it = this.fileToTimeStampsMap.entrySet().iterator();
        while (it.hasNext()) {
            final ConcurrentHashMap.Entry pair = (ConcurrentHashMap.Entry) it.next();
            final String file = (String) pair.getKey();
            sb.append(file).append(':')
                    .append(this.fileToTimeStampsMap.get(file).toString()).append("|");
        }

        return sb.toString();
    }

    public void removeFile(final String file, final MembershipList membershipList) {
        final ArrayList<Node> replicaSet = this.fileToNodesMap.get(file);
        if (replicaSet == null) {
            System.out.println("couldn't find replica set for remove");
            return;
        }

        StringBuilder sb;
        ArrayList<String> files;
        for (final Node replica : replicaSet) {
            files = this.nodeToFilesMap.get(replica);
            files.remove(file);
            if (files.isEmpty()) {
                this.nodeToFilesMap.remove(replica);
            } else {
                this.nodeToFilesMap.put(replica, files);
            }

            sb = new StringBuilder();
            sb.append("response delete ")
                    .append("local_unused").append("|")
                    .append(file);
            Network.sendData(replica.getHostAddress(), Servent.RECEIVE_PORT_SDFS, sb.toString());
        }

        this.fileToNodesMap.remove(file);
        this.fileToTimeStampsMap.remove(file);

        this.syncMasters(membershipList);
    }
}

