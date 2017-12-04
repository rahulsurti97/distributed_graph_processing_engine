package ece428.mp1;

public class Node {
    private final String IPAddress;
    private final String hostName;

    public Node(final String IPAddress, final String hostName) {
        this.hostName = hostName;
        this.IPAddress = IPAddress;
    }

    public String getHostAddress() {
        return this.IPAddress;
    }

    public String getHostName() {
        return this.hostName;
    }

    @Override
    public String toString() {
        return this.hostName;
    }

    @Override
    public synchronized boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Node) {
            final Node other = (Node) obj;
            return this.hostName.equals(other.getHostName());
        }
        if (obj instanceof NodeID) {
            final NodeID other = (NodeID) obj;
            return this.hostName.equals(other.getIPAddress().getHostName());
        }
        return false;
    }

    @Override
    public synchronized int hashCode() {
        return this.hostName.hashCode();
    }
}
