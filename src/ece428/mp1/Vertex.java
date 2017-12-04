package ece428.mp1;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Vertex<VertexValue, EdgeValue, MessageValue> {

    private final ArrayList<Edge<EdgeValue>> edges;
    private final ConcurrentLinkedQueue<MessageValue> messageQueue;
    private Double value;

    public Vertex(final Double value, final ArrayList<Edge<EdgeValue>> edges) {
        this.value = value;
        this.edges = edges;
        this.messageQueue = new ConcurrentLinkedQueue<>();
    }

    public Vertex(final Double value, final Edge<EdgeValue> edge) {
        this.value = value;
        this.edges = new ArrayList<>();
        this.edges.add(edge);
        this.messageQueue = new ConcurrentLinkedQueue<>();
    }

    public Vertex(final String serialized) {
        this.messageQueue = new ConcurrentLinkedQueue<>();
        this.edges = new ArrayList<>();
        unserialize(serialized);
    }

    public void unserialize(final String serialized) {
        final String[] split = serialized.split("\\:");
        final String[] edgesSplit;
        this.value = Double.parseDouble(split[0]);

        if (split.length == 2) {
            edgesSplit = split[1].split("\\;");
            for (final String e : edgesSplit) {
                this.edges.add(new Edge<>(e));
            }
        }
    }

    public ConcurrentLinkedQueue<MessageValue> getMessageQueue() {
        return this.messageQueue;
    }

    public void addToMessageQueue(final MessageValue messageValue) {
        this.messageQueue.add(messageValue);
    }

    public void setValue(final Double value) {
        this.value = value;
    }

    public ArrayList<Edge<EdgeValue>> getEdges() {
        return this.edges;
    }

    public void addEdge(final Edge<EdgeValue> edge) {
        this.edges.add(edge);
    }

    @Override
    public synchronized String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("\tVal: ").append(this.getVertexValue().toString()).append("\n")
                .append("\tMessageQueue: \n\t\t");
        final ArrayList<MessageValue> messageArray = new ArrayList<MessageValue>(this.messageQueue);
        for (int i = 0; i < messageArray.size(); i++) {
            sb.append(messageArray.get(i).toString()).append(",");
        }
        sb.append("\n\tEdges:\n");
        for (final Edge<EdgeValue> e : this.edges) {
            sb.append("\t\t").append(e.toString()).append("\n");
        }
        return sb.toString();
    }

    public Double getVertexValue() {
        return this.value;
    }

    public String serialize() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getVertexValue().toString()).append(":");
        for (final Edge<EdgeValue> e : this.edges) {
            sb.append(e.serialize()).append(";");
        }
        return sb.toString();
    }
}
