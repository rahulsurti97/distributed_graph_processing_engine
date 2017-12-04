package ece428.mp1;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MembershipList {

    private final HashSet<NodeID> deadNodes;
    ConcurrentHashMap<NodeID, MembershipListEntry> listEntries;
    private NodeID self;

    public MembershipList(final NodeID self) {
        this.listEntries = new ConcurrentHashMap<>();
        this.deadNodes = new HashSet<>();
        this.self = self;
    }

    public MembershipList(final ConcurrentHashMap<NodeID, MembershipListEntry> listEntries) {
        this.listEntries = listEntries;
        this.deadNodes = new HashSet<>();
        MembershipListEntry entry;
        final Iterator it = this.listEntries.entrySet().iterator();
        while (it.hasNext()) {
            final ConcurrentHashMap.Entry pair = (ConcurrentHashMap.Entry) it.next();
            final NodeID nodeID = (NodeID) pair.getKey();
            entry = this.listEntries.get(nodeID);
            if (!entry.getAlive()) {
                this.deadNodes.add(nodeID);
            }
        }
    }

    public NodeID getSelf() {
        return this.self;
    }

    public void setSelf(final NodeID self) {
        this.self = self;
    }

    public synchronized Node getNewReplica(final ArrayList<Node> replicaSet) {
        final Iterator i = this.listEntries.entrySet().iterator();
        ConcurrentHashMap.Entry pair;
        NodeID key;
        MembershipListEntry entry;
        final ArrayList<NodeID> masters = this.getMasters();
        while (i.hasNext()) {
            pair = (ConcurrentHashMap.Entry) i.next();
            key = (NodeID) pair.getKey();
            entry = this.listEntries.get(key);
            if (entry.getAlive() &&
                    !replicaSet.contains(key) &&
                    !masters.contains(key)) {
                return new Node(key.getIPAddress().getHostAddress(), key.getIPAddress().getHostName());
            }
        }
        return null;
    }

    public synchronized ArrayList<NodeID> getMasters() {
        final ArrayList<NodeID> ret = new ArrayList<>();
        final Iterator i = this.listEntries.entrySet().iterator();
        ConcurrentHashMap.Entry pair;
        NodeID key;
        MembershipListEntry entry;

        while (i.hasNext()) {
            pair = (ConcurrentHashMap.Entry) i.next();
            key = (NodeID) pair.getKey();
            entry = this.listEntries.get(key);
            if (entry.isMaster() && entry.getAlive()) {
                ret.add(key);
            }
        }
        return ret;
    }

    public ArrayList<NodeID> getDeadNodesArray() {
        final Iterator<NodeID> i = this.deadNodes.iterator();
        final ArrayList<NodeID> nodes = new ArrayList<>();
        NodeID nodeID;
        while (i.hasNext()) {
            nodeID = i.next();
            nodes.add(nodeID);
        }

        return nodes;
    }

    public synchronized boolean isMainMaster() {
        return this.self.equals(this.getMainMaster());
    }

    public synchronized NodeID getMainMaster() {
        final ArrayList<NodeID> masters = this.getMasters();
        Collections.sort(masters);
        return masters.get(0);
    }

    /**
     * Adds a node into the membership list.
     *
     * @param nodeID - The node we want to add into the membership list.
     */
    public synchronized void addNewNode(final NodeID nodeID) {
        this.listEntries.put(
                nodeID,
                new MembershipListEntry()
        );
    }

    /**
     * Pretty printing for debugging.
     *
     * @return Pretty printing of the string.
     */
    @Override
    public synchronized String toString() {
        final StringBuilder sb = new StringBuilder();
        final List<NodeID> list = new ArrayList<>(this.listEntries.keySet());

        Collections.sort(list);

        for (final NodeID nodeID : list) {
            final String serverNumber = NodeID.getVMNumber(nodeID.getIPAddress());
            final MembershipListEntry curr = this.listEntries.get(nodeID);

            sb.append("\n")
                    .append("Server ")
                    .append(serverNumber)
                    .append(" - ");
            if (curr.getAlive()) {
                sb.append("A - ");
            } else {
                sb.append("D - ");
            }
            sb.append("Heartbeat: ").append(curr.getHeartBeatCounter());
            if (curr.isMaster()) {
                sb.append(" - M");
            }
//            if (nodeID.equals(this.self)) {
//                sb.append(" - S");
//            }
        }
        return sb.toString();
    }

    /**
     * This is the merge function for a membership list. It merges the two membership lists and updates
     * the list in accordance to the gossiping algorithm.
     *
     * @param other A different node's membership list.
     */
    public synchronized void updateEntries(final MembershipList other) throws IOException {
        final Iterator it = other.listEntries.entrySet().iterator();
        while (it.hasNext()) {
            final ConcurrentHashMap.Entry pair = (ConcurrentHashMap.Entry) it.next();
            final NodeID otherKey = (NodeID) pair.getKey();
            final MembershipListEntry otherEntry = other.listEntries.get(otherKey);
            final MembershipListEntry thisEntry = this.listEntries.get(otherKey);
            if (thisEntry != null) {
                final int otherHeartBeatCount = otherEntry.getHeartBeatCounter();
                final int thisHeartBeatCount = thisEntry.getHeartBeatCounter();
                if (otherHeartBeatCount < 4 && thisEntry.getLocalTime() < 0) {
                    if (thisEntry.getAlive()) {
                        new PrintStream(new FileOutputStream(new File("../output.txt"))).println(("NODE REJOIN\n"));
                    }
                    this.addNewNode(otherKey, 0);
                }
                if (otherHeartBeatCount > thisHeartBeatCount) {
                    thisEntry.setHeartBeatCounter(otherHeartBeatCount);
                    thisEntry.updateLocalTime();
                }
                thisEntry.setMaster(thisEntry.isMaster() || otherEntry.isMaster());
            } else if (otherEntry.getAlive()) {
                this.addNewNode(otherKey, otherEntry.getHeartBeatCounter(), otherEntry.isMaster());
            }
        }

        final Iterator i = this.listEntries.entrySet().iterator();
        while (i.hasNext()) {
            final ConcurrentHashMap.Entry pair = (ConcurrentHashMap.Entry) i.next();
            final NodeID key = (NodeID) pair.getKey();
            final MembershipListEntry thisEntry = this.listEntries.get(key);
            if (getCurrentTime() - thisEntry.getLocalTime() > 3000) {
                new PrintStream(new FileOutputStream(new File("../output.txt"))).println(("NODE DIED!\n"));
//				if (thisEntry.getAlive()) {
//					new PrintStream(new FileOutputStream(new File("../output.txt"))).println(("NODE DIED!\n"));
//				}
                this.deadNodes.add(key);
                thisEntry.setAlive(false);
                thisEntry.setLocalTime(-1);
                thisEntry.setMaster(false);
            } else {
                thisEntry.setAlive(true);
            }
        }
    }

    /**
     * Adds a node into the membership list.
     *
     * @param nodeID           - The node we want to add into the membership list.
     * @param heartBeatCounter - The heartbeat counter we want to set when we add the node.
     */
    public synchronized void addNewNode(final NodeID nodeID, final int heartBeatCounter) {
        this.listEntries.put(
                nodeID,
                new MembershipListEntry(heartBeatCounter)
        );
    }

    public synchronized void addNewNode(final NodeID nodeID, final int heartBeatCounter, final boolean isMaster) {
        this.listEntries.put(
                nodeID,
                new MembershipListEntry(heartBeatCounter, isMaster)
        );
    }

    /**
     * Gets the current time in milliseconds.
     *
     * @return - Current time.
     */
    public static long getCurrentTime() {
        return LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * Increments a node's heartbeat counter.
     *
     * @param nodeID - A node in the network.
     */
    public synchronized void incrementHeartBeatCount(final NodeID nodeID) {
        final MembershipListEntry entry = this.listEntries.get(nodeID);
        if (entry != null) {
            entry.setHeartBeatCounter(entry.getHeartBeatCounter() + 1);
            this.listEntries.put(nodeID, entry);
        }
    }

    public synchronized boolean electMasters() {
        Iterator i = this.listEntries.entrySet().iterator();
        ConcurrentHashMap.Entry pair;
        NodeID key;
        MembershipListEntry entry;

        boolean didElect = false;

        int masterCount = 0;
        while (i.hasNext()) {
            pair = (ConcurrentHashMap.Entry) i.next();
            key = (NodeID) pair.getKey();

            entry = this.listEntries.get(key);
            if (entry.isMaster() && entry.getAlive()) {
                final Integer VMNumber = Integer.parseInt(NodeID.getVMNumber(key.getIPAddress()));
                final Integer selfNumber = Integer.parseInt(NodeID.getVMNumber(this.self.getIPAddress()));
                if (VMNumber < selfNumber) {
                    return false;
                }
                masterCount++;
            }
        }

        i = this.listEntries.entrySet().iterator();
        while (masterCount < 3 && i.hasNext()) {
            pair = (ConcurrentHashMap.Entry) i.next();
            key = (NodeID) pair.getKey();
            entry = this.listEntries.get(key);
            if (entry.getAlive() && !entry.isMaster()) {
                entry.setMaster(true);
                this.listEntries.put(key, entry);
                masterCount++;
                didElect = true;
                //TODO:transfer metadata
            }
        }

        return didElect;
    }


//    public synchronized MembershipList copy() {
//        final MembershipList copy = new MembershipList();
//        final Iterator i = this.listEntries.entrySet().iterator();
//        ConcurrentHashMap.Entry pair;
//        NodeID nodeID;
//        MembershipListEntry entry;
//
//        while (i.hasNext()) {
//            pair = (ConcurrentHashMap.Entry) i.next();
//            nodeID = (NodeID) pair.getKey();
//            entry = this.listEntries.get(nodeID);
//            copy.listEntries.put(nodeID, entry);
//        }
//
//        return copy;
//    }
}
