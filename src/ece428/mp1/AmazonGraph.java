package ece428.mp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class AmazonGraph extends Graph {

    public AmazonGraph(final ArrayList<NodeID> workers) {
        super(workers);
        this.graphType = GraphType.AMAZON;
    }
//    public void loadGraph(final String filename) {
//        try {
//            final FileReader fileReader = new FileReader(new File(filename));
//            final BufferedReader bufferedReader = new BufferedReader(fileReader);
//            String line;
//            String[] lineSplit;
//            Vertex<Double, Double, Double> fromVertex;
//            Vertex<Double, Double, Double> toVertex;
//            Edge<Double> edge;
//
//            System.out.println("Parsing Input File");
//
//            while ((line = bufferedReader.readLine()) != null) {
//                if (line.startsWith("#")) {
//                    continue;
//                }
//                lineSplit = line.split("\t");
//                fromVertex = new Vertex<>(lineSplit[0], 0);
//                toVertex = new Vertex<>(lineSplit[1], 0);
//                edge = new Edge<>(toVertex, 0);
//
//                this.insert(this.graph, fromVertex, edge);
//            }
//            fileReader.close();
//        } catch (final IOException e) {
//            System.out.println("couldn't find " + filename);
//        }
//    }
//
//    @Override
//    public void unserialize(final String serialized) {
//        System.out.println("AMAZON GRAPH UNSERIALIZE");
//    }

    public synchronized void loadGraph(final String filename) {
        try {
            final FileReader fileReader = new FileReader(new File(filename));
            final BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            String[] lineSplit;
            Vertex<Double, Double, Double> fromVertex;
            Vertex<Double, Double, Double> toVertex;
            Edge<Double> edge;

            System.out.println("Parsing Input File");

            int count = 0;
//            boolean setSource = false;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("#")) {
                    continue;
                }
                lineSplit = line.split("\t");
                edge = new Edge<>(lineSplit[1], 0.0);

                fromVertex = new Vertex<>(new Double(Integer.MAX_VALUE), new ArrayList<>());
                toVertex = new Vertex<>(new Double(Integer.MAX_VALUE), new ArrayList<>());
                count++;

                this.insert(lineSplit[0], fromVertex, edge);
                this.insert(lineSplit[1], toVertex);
            }

            System.out.println("Count: " + count);
            fileReader.close();
        } catch (final IOException e) {
            System.out.println("couldn't find " + filename);
//            e.printStackTrace();
        }
    }

    @Override
    public synchronized Boolean compute() {
        if (this.computeType.equals(ComputeType.SSSP)) {
            return sssp();
        } else if (this.computeType.equals(ComputeType.PAGERANK)) {
            return pagerank();
        }
        return false;
    }

    private synchronized Boolean sssp() {
//        System.out.println("AMAZON COMPUTE");
        final Iterator it = this.graph.entrySet().iterator();
        String vertexID;
        Vertex<Double, Double, Double> vertex;
        ArrayList<Edge<Double>> edges;
        Double min;
        Boolean changed = false;
        Integer old;
        while (it.hasNext()) {
            final ConcurrentHashMap.Entry pair = (ConcurrentHashMap.Entry) it.next();
            vertexID = (String) pair.getKey();
            vertex = (Vertex<Double, Double, Double>) pair.getValue();
            edges = vertex.getEdges();

            min = new Double(Integer.MAX_VALUE);

            if (vertexID.equals(this.getSource()) && vertex.getVertexValue().equals(min)) {
                min = 0.0;
            }

            while (!vertex.getMessageQueue().isEmpty()) {
                min = Math.min(min, vertex.getMessageQueue().remove());
            }

            if (min < vertex.getVertexValue()) {
                changed = true;
                vertex.setValue(min);
                for (final Edge e : edges) {
                    super.sendMessageToVertex(e.getVertexID(), min + 1);
                }
            }
        }
        super.sendAllMessages();
        return changed;
    }

    private synchronized Boolean pagerank() {
//        System.out.println("COMPUTE PAGERANK");

        final Iterator it = this.graph.entrySet().iterator();
        String vertexID;
        Vertex<Double, Double, Double> vertex;
        ArrayList<Edge<Double>> edges;
        Boolean changed = false;
        Double sum;
        while (it.hasNext()) {
            final ConcurrentHashMap.Entry pair = (ConcurrentHashMap.Entry) it.next();
            vertexID = (String) pair.getKey();
            vertex = (Vertex<Double, Double, Double>) pair.getValue();
            edges = vertex.getEdges();

            if (getSuperstep().equals(0)) {
                vertex.setValue(1.0 / this.graph.keySet().size());
//                System.out.println("setting initial value");
                return true;
            }

            if (getSuperstep() >= 1) {
                sum = 0.0;
                while (!vertex.getMessageQueue().isEmpty()) {
                    sum += vertex.getMessageQueue().remove();
                }
                vertex.setValue(0.15 / this.graph.keySet().size() + 0.85 * sum);
            }

            if (getSuperstep() < 30) {
                for (final Edge e : edges) {
                    super.sendMessageToVertex(e.getVertexID(), vertex.getVertexValue() / edges.size());
                }
                changed = true;
            }
        }
        super.sendAllMessages();
        return changed;
    }

    @Override
    public synchronized void deliverMessages(final String messages) {
        final String[] messagesSplit = messages.split("\\|");
        String[] messageSplit;
        String vertexID;
        Double message;
        Vertex<Double, Double, Double> vertex;
        for (final String m : messagesSplit) {
            messageSplit = m.split("\\,");
            if (messageSplit.length == 2) {
                vertexID = messageSplit[0];
                message = Double.parseDouble(messageSplit[1]);
                vertex = (Vertex<Double, Double, Double>) this.graph.get(vertexID);
                vertex.addToMessageQueue(message);
            }
        }
    }
}

