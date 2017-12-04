package ece428.mp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class BitcoinGraph extends Graph {

    public BitcoinGraph(final ArrayList<NodeID> workers) {
        super(workers);
        this.graphType = GraphType.BITCOIN;
    }

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
//            while ((line = bufferedReader.readLine()) != null) {
//                lineSplit = line.split(",");
//                edge = new Edge<>(lineSplit[1], Integer.parseInt(lineSplit[2]));
//
//                if (!setSource) {
//                    fromVertex = new Vertex<>(new Integer(0), new ArrayList<>());
//                    setSource = true;
//                } else {
//                    fromVertex = new Vertex<>(Integer.MAX_VALUE, new ArrayList<>());
//                }
//                toVertex = new Vertex<>(Integer.MAX_VALUE, new ArrayList<>());
//                count++;
//
//                this.insert(lineSplit[0], fromVertex, edge);
//                this.insert(lineSplit[1], toVertex);
//            }
            while ((line = bufferedReader.readLine()) != null) {
                lineSplit = line.split(",");
                edge = new Edge<>(lineSplit[1], 0.0);

//                if (!setSource) {
//                    fromVertex = new Vertex<>(new Integer(0), new ArrayList<>());
//                    setSource = true;
//                } else {
//                    fromVertex = new Vertex<>(Integer.MAX_VALUE, new ArrayList<>());
//                }
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
//        System.out.println("COMPUTE SSSP");
        final Iterator it = this.graph.entrySet().iterator();
        String vertexID;
        Vertex<Double, Double, Double> vertex;
        ArrayList<Edge<Double>> edges;
        Double min;
        Boolean changed = false;
        while (it.hasNext()) {
            final ConcurrentHashMap.Entry pair = (ConcurrentHashMap.Entry) it.next();
            vertexID = (String) pair.getKey();
            vertex = (Vertex<Double, Double, Double>) pair.getValue();
            edges = vertex.getEdges();

            min = new Double(Integer.MAX_VALUE);

            if (vertexID.equals(this.getSource()) && vertex.getVertexValue().equals(min)) {
//                System.out.println("set source");
                min = 0.0;
            }

            while (!vertex.getMessageQueue().isEmpty()) {
                min = Math.min(min, vertex.getMessageQueue().remove());
            }

            if (min < vertex.getVertexValue()) {
                changed = true;
                vertex.setValue(new Double(min));
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