package ece428.mp1;

public class Edge<EdgeValue> {

    private String vertexID;
    private EdgeValue edgeValue;

    public Edge(final String vertexID, final EdgeValue edgeValue) {
        this.vertexID = vertexID;
        this.edgeValue = edgeValue;
    }

    public Edge(final String serialized) {
        unserialize(serialized);
    }

    public void unserialize(final String serialized) {
        final String[] split = serialized.split("\\,");
        if (split.length == 2) {
            this.edgeValue = (EdgeValue) split[0];
            this.vertexID = split[1];
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Edge) {
            final Edge<EdgeValue> other = (Edge<EdgeValue>) obj;
            return this.getVertexID().equals(other.getVertexID());
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Weight: ").append(this.edgeValue.toString()).append(" | VertexID: ").append(this.vertexID);
        return sb.toString();
    }

    public String getVertexID() {
        return this.vertexID;
    }

    public void setVertexID(final String vertexID) {
        this.vertexID = vertexID;
    }

    public EdgeValue getEdgeValue() {
        return this.edgeValue;
    }

    public void setEdgeValue(final EdgeValue edgeValue) {
        this.edgeValue = edgeValue;
    }

    public String serialize() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.edgeValue).append(",").append(this.vertexID);
        return sb.toString();
    }
}
