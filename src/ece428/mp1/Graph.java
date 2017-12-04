package ece428.mp1;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class Graph<VertexValue, EdgeValue, MessageValue> {

    protected final ConcurrentHashMap<String, Vertex<VertexValue, EdgeValue, MessageValue>> graph;
    private final ArrayList<NodeID> workers;
    private final ArrayList<ConcurrentHashMap<String, Vertex<VertexValue, EdgeValue, MessageValue>>> partitionedGraph;
    private final ArrayList<String> partitionMessages;
    protected String graphType;
    protected String computeType;
    private String source;
    private Integer superstep;
    private String superstepCache;
    private Boolean onBackUpMaster;

    public Graph(final ArrayList<NodeID> workers) {
        this.graph = new ConcurrentHashMap<>();
        this.partitionedGraph = new ArrayList<>();
        this.partitionMessages = new ArrayList<>();
        for (int i = 0; i < workers.size(); i++) {
            this.partitionMessages.add("graph message ");
        }
        this.graphType = GraphType.NONE;
        this.computeType = ComputeType.PAGERANK;
        this.source = "";
        this.superstep = 0;
        this.onBackUpMaster = false;
        this.workers = workers;
    }

    public Boolean getOnBackUpMaster() {
        return this.onBackUpMaster;
    }

    public void setOnBackUpMaster(final Boolean onBackUpMaster) {
        this.onBackUpMaster = onBackUpMaster;
    }

    public String getSuperstepCache() {
        return this.superstepCache;
    }

    public void setSuperstepCache(final String superstepCache) {
        this.superstepCache = superstepCache;
    }

    public Integer getSuperstep() {
        return this.superstep;
    }

    public void setSuperstep(final Integer superstep) {
        this.superstep = superstep;
    }

    public synchronized void unserialize(final String serialized) {
//        System.out.println("GRAPH UNSERIALIZE");

        final String[] graphSplit = serialized.split("\\|");
        String[] entrySplit;
        String vertexID;
        Vertex<VertexValue, EdgeValue, MessageValue> vertex;
        for (final String entry : graphSplit) {
            entrySplit = entry.split(">");
            if (entrySplit.length < 2) {
                System.out.println("COULDN'T UNSERIALIZE GRAPH");
            } else {
                vertexID = entrySplit[0];
                vertex = new Vertex<>(entrySplit[1]);
                this.graph.put(vertexID, vertex);
            }
        }
    }

    public synchronized String toString() {
        final Iterator it = this.graph.entrySet().iterator();
        Vertex<VertexValue, EdgeValue, MessageValue> vertex;
        int vertexCount = 0;
        int edgeCount = 0;
        while (it.hasNext()) {
            final ConcurrentHashMap.Entry pair = (ConcurrentHashMap.Entry) it.next();
            vertex = (Vertex<VertexValue, EdgeValue, MessageValue>) pair.getValue();
            vertexCount++;
            edgeCount += vertex.getEdges().size();
        }
        return "VertexCount: " + vertexCount + "\nEdgeCount:   " + edgeCount;
    }

    public synchronized void printGraph() {
        final StringBuilder sb = new StringBuilder();
        final Iterator it = this.graph.entrySet().iterator();
        String vertexID;
        Vertex<VertexValue, EdgeValue, MessageValue> vertex;
        int vertexCount = 0;
        int edgeCount = 0;
        while (it.hasNext()) {
            final ConcurrentHashMap.Entry pair = (ConcurrentHashMap.Entry) it.next();
            vertexID = (String) pair.getKey();
            vertex = (Vertex<VertexValue, EdgeValue, MessageValue>) pair.getValue();
            vertexCount++;
            edgeCount += vertex.getEdges().size();
            sb.append("VertexID: ").append(vertexID).append("\n").append(vertex.toString());
        }
        System.out.println(sb.toString());
        System.out.println("VertexCount: " + vertexCount + "\nEdgeCount:   " + edgeCount);
    }

    public synchronized void sendPartition(final ArrayList<NodeID> workerNodes) {
        this.partition(workerNodes.size());
        StringBuilder sb;
        Iterator it;
        String vertexID;
        Vertex<VertexValue, EdgeValue, MessageValue> vertex;
        Boolean notSent = true;

        final StringBuilder progress = new StringBuilder();
        progress.append("[");
        for (int i = 0; i < workerNodes.size(); i++) {
            progress.append(" ");
        }
        progress.append(" ]\r");

        System.out.println("Distributing Graph");
        for (int i = 0; i < workerNodes.size(); i++) {
            progress.replace(i + 1, i + 3, "=>");
            System.out.print(progress.toString());

            sb = new StringBuilder();
            sb.append(this.graphType).append(" ");

            it = this.partitionedGraph.get(i).entrySet().iterator();
            while (it.hasNext()) {
                final ConcurrentHashMap.Entry pair = (ConcurrentHashMap.Entry) it.next();
                vertexID = (String) pair.getKey();
                vertex = (Vertex<VertexValue, EdgeValue, MessageValue>) pair.getValue();
                sb.append(vertexID).append(">").append(vertex.serialize()).append("|");

                notSent = true;
                if (sb.length() > 60000) {
                    notSent = false;
                    Network.sendData(workerNodes.get(i).getIPAddress().getHostAddress(),
                            Servent.RECEIVE_PORT_GRAPH_MESSAGE,
                            "graph loadgraph " + sb.toString());
                    sb = new StringBuilder();
                    sb.append(this.graphType).append(" ");
                }
            }
            if (notSent) {
                Network.sendData(workerNodes.get(i).getIPAddress().getHostAddress(),
                        Servent.RECEIVE_PORT_GRAPH_MESSAGE,
                        "graph loadgraph " + sb.toString());
            }
        }
    }

    private synchronized void partition(final Integer NUM_WORKERS) {
        for (int i = 0; i < NUM_WORKERS; i++) {
            this.partitionedGraph.add(new ConcurrentHashMap<>());
        }

        final Iterator it = this.graph.entrySet().iterator();
        String vertexID;
        Vertex<VertexValue, EdgeValue, MessageValue> vertex;
        Integer index;
        while (it.hasNext()) {
            final ConcurrentHashMap.Entry pair = (ConcurrentHashMap.Entry) it.next();
            vertexID = (String) pair.getKey();
            vertex = (Vertex<VertexValue, EdgeValue, MessageValue>) pair.getValue();
            index = vertexID.hashCode() % NUM_WORKERS;
            this.partitionedGraph.get(index).put(vertexID, vertex);
        }
    }

    public synchronized Boolean compute() {
        System.out.println("GRAPH COMPUTE: MUST BE OVERRIDDEN");
        return false;
    }

    public synchronized void deliverMessages(final String messages) {
        System.out.println("GRAPH DELIVER MESSAGE: MUST BE OVERRIDDEN");
    }

    protected void insert(final String vertexID, final Vertex<VertexValue, EdgeValue, MessageValue> vertex, final Edge<EdgeValue> edge) {
        final Vertex<VertexValue, EdgeValue, MessageValue> existing = this.graph.get(vertexID);
        if (existing == null) {
            vertex.addEdge(edge);
            this.graph.put(vertexID, vertex);
        } else {
            existing.addEdge(edge);
            this.graph.put(vertexID, existing);
        }
    }

    protected void insert(final String vertexID, final Vertex<VertexValue, EdgeValue, MessageValue> vertex) {
        this.graph.putIfAbsent(vertexID, vertex);
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(final String source) {
        System.out.println("SETTING SOURCE");
        this.source = source;
        this.computeType = ComputeType.SSSP;
    }

    protected synchronized void sendMessageToVertex(final String vertexID, final MessageValue messageValue) {
        if (this.graph.keySet().contains(vertexID)) {
//            System.out.println("sending " + messageValue.toString() + " to local " + vertexID);

            this.graph.get(vertexID).addToMessageQueue(messageValue);
        } else {
//            System.out.println("sending " + messageValue.toString() + " to remote " + vertexID);
            this.addPartitionMessage(vertexID, messageValue);
        }
    }

    private synchronized void addPartitionMessage(final String vertexID, final MessageValue messageValue) {
        final ArrayList<NodeID> workers = this.workers;
        final int index = vertexID.hashCode() % 7;
        final StringBuilder sb = new StringBuilder();

        sb.append(this.partitionMessages.get(index)).append("|")
                .append(vertexID).append(",")
                .append(messageValue.toString());

        final String message = sb.toString();
        if (message.length() < 60000) {
            this.partitionMessages.set(index, message);
        } else {
            Network.sendData(this.workers.get(index).getIPAddress().getHostAddress(),
                    Servent.RECEIVE_PORT_GRAPH_MESSAGE,
                    message);
            this.partitionMessages.set(index, "graph message ");
        }
    }

    protected synchronized void sendAllMessages() {
        String message;
        for (int i = 0; i < this.partitionMessages.size(); i++) {
            message = (String) this.partitionMessages.get(i);
            if (!message.equals("graph message ")) {
                Network.sendData(this.workers.get(i).getIPAddress().getHostAddress(),
                        Servent.RECEIVE_PORT_GRAPH_MESSAGE,
                        message);
            }
        }
    }
}