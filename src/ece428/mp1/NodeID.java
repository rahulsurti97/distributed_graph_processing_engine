package ece428.mp1;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class NodeID implements Comparable<NodeID> {
    private final long startTime;
    private final InetAddress IPAddress;

    public NodeID(final InetAddress IPAddress) {
        this.startTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        this.IPAddress = IPAddress;
    }


    public NodeID(final InetAddress IPAddress, final long startTime) {
        this.startTime = startTime;
        this.IPAddress = IPAddress;
    }

    /**
     * Have to override the hashcode to hash objects into our membership list.
     * We hash solely on the IP address (which we figure ensures uniqueness anyway)
     *
     * @return - Hashed value for the IPAddress member variable.
     */
    @Override
    public synchronized int hashCode() {
        return this.IPAddress.getHostName().hashCode();
    }

    /**
     * Have to override equals in order for hashing to work.
     *
     * @param obj - The object we are comparing against.
     * @return Boolean indicating whether or not the objects are the same.
     */
    @Override
    public synchronized boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Node) {
            final Node other = (Node) obj;
            return this.IPAddress.getHostName().equals(other.getHostName());
        }
        if (obj instanceof NodeID) {
            final NodeID other = (NodeID) obj;
            return this.IPAddress.getHostName().equals(other.getIPAddress().getHostName());
        }
        return false;
    }

    /**
     * Gets the IP Address that identifies this node.
     *
     * @return - IP Address.
     */
    public InetAddress getIPAddress() {
        return this.IPAddress;
    }

    /**
     * Overrides toString for debugging.
     *
     * @return - Stringified version of this object.
     */
    @Override
    public String toString() {
        return new String(this.getIPAddress().getHostName());
    }

//    public void setStartTime(final long startTime) {
//        this.startTime = startTime;
//    }

    /**
     * Gets the start time for this node.
     *
     * @return - The start time.
     */
    public long getStartTime() {
        return this.startTime;
    }

//    public void setIPAddress(final InetAddress IPAddress) {
//        this.IPAddress = IPAddress;
//    }

    @Override
    public int compareTo(final NodeID other) {
        return Integer.parseInt(NodeID.getVMNumber(this.getIPAddress())) - Integer.parseInt(NodeID.getVMNumber(other.getIPAddress()));
    }

    public static String getVMNumber(final InetAddress inetAddress) {
        final String[] splitted = inetAddress.getHostName().split("\\.");
        return splitted[0].substring(splitted[0].length() - 2);
    }

    public Integer getVMNumber() {
        final String[] splitted = this.getIPAddress().getHostName().split("\\.");
        return Integer.parseInt(splitted[0].substring(splitted[0].length() - 2));
    }

    public String serialize() {
        return new String(this.getIPAddress().getHostAddress() + "/" + this.getIPAddress().getHostName());
    }

}