package ece428.mp1;

//public class RoadCAGraph extends Graph {

//    public RoadCAGraph() {
//        super();
//        this.graphType = GraphType.AMAZON;
//    }
//
////    public void loadGraph(final String filename) {
////        try {
////            final FileReader fileReader = new FileReader(new File(filename));
////            final BufferedReader bufferedReader = new BufferedReader(fileReader);
////            String line;
////            String[] lineSplit;
////            Vertex<Double, Double, Double> fromVertex;
////            Vertex<Double, Double, Double> toVertex;
////            Edge<Double> edge;
////
////            System.out.println("Parsing Input File");
////
////            while ((line = bufferedReader.readLine()) != null) {
////                if (line.startsWith("#")) {
////                    continue;
////                }
////                lineSplit = line.split("\t");
////                fromVertex = new Vertex<>(lineSplit[0], 0);
////                toVertex = new Vertex<>(lineSplit[1], 0);
////                edge = new Edge<>(toVertex.toString(), 0);
////
////                this.insert(this.graph, fromVertex, edge);
////            }
////            fileReader.close();
////        } catch (final IOException e) {
////            System.out.println("couldn't find " + filename);
////        }
////    }
////    public void loadGraph(final String filename) {
////        try {
////            final FileReader fileReader = new FileReader(new File(filename));
////            final BufferedReader bufferedReader = new BufferedReader(fileReader);
////            String line;
////            String[] lineSplit;
////            Vertex<Double, Double, Double> fromVertex;
////            Vertex<Double, Double, Double> toVertex;
////            Edge<Double> edge;
////
////            System.out.println("Parsing Input File");
////
////            while ((line = bufferedReader.readLine()) != null) {
////                if (line.startsWith("#")) {
////                    continue;
////                }
////                lineSplit = line.split("\t");
////                fromVertex = new Vertex<>(lineSplit[0], 0);
////                toVertex = new Vertex<>(lineSplit[1], 0);
////                edge = new Edge<>(toVertex, 0);
////
////                this.insert(this.graph, fromVertex, edge);
////            }
////            fileReader.close();
////        } catch (final IOException e) {
////            System.out.println("couldn't find " + filename);
////        }
////    }
////
////    @Override
////    public void unserialize(final String serialized) {
////        System.out.println("AMAZON GRAPH UNSERIALIZE");
////    }
//
//    public synchronized void loadGraph(final String filename) {
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
//            int count = 0;
////            boolean setSource = false;
//            while ((line = bufferedReader.readLine()) != null) {
//                if (line.startsWith("#")) {
//                    continue;
//                }
//                lineSplit = line.split("\t");
//                edge = new Edge<>(lineSplit[1], 0.0);
//
//                fromVertex = new Vertex<>(new Double(Integer.MAX_VALUE), new ArrayList<>());
//                toVertex = new Vertex<>(new Double(Integer.MAX_VALUE), new ArrayList<>());
//                count++;
//
//                this.insert(lineSplit[0], fromVertex, edge);
//                this.insert(lineSplit[1], toVertex);
//            }
//
//            System.out.println("Count: " + count);
//            fileReader.close();
//        } catch (final IOException e) {
//            System.out.println("couldn't find " + filename);
////            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public synchronized Boolean compute() {
//        if (this.computeType.equals(ComputeType.SSSP)) {
//            return sssp();
//        } else if (this.computeType.equals(ComputeType.PAGERANK)) {
//            return pagerank();
//        }
//        return false;
//    }
//
//    private synchronized Boolean sssp() {
////        System.out.println("ROAD COMPUTE");
////        final Iterator it = this.graph.entrySet().iterator();
////        String vertexID;
////        Vertex<Double, Double, Double> vertex;
////        ArrayList<Edge<Double>> edges;
////        Integer min = Integer.MAX_VALUE;
////        Boolean changed = false;
////        Integer old;
////        while (it.hasNext()) {
////            final ConcurrentHashMap.Entry pair = (ConcurrentHashMap.Entry) it.next();
////            vertexID = (String) pair.getKey();
////            vertex = (Vertex<Double, Double, Double>) pair.getValue();
////            edges = vertex.getEdges();
////
////            min = Integer.MAX_VALUE;
////
////            if (vertexID.equals("100") && vertex.getVertexValue().equals(Integer.MAX_VALUE)) {
////                min = 0;
////            }
////
////            while (!vertex.getMessageQueue().isEmpty()) {
////                min = Math.min(min, vertex.getMessageQueue().remove());
////            }
////
////            if (min < vertex.getVertexValue()) {
////                changed = true;
////                vertex.setValue(min);
////                for (final Edge e : edges) {
////                    super.sendMessageToVertex(e.getVertexID(), min + 1);
////                }
////            }
////        }
////        super.sendAllMessages();
//        return false;
//    }
//
//    private synchronized Boolean pagerank() {
//        return false;
//    }
//
//    @Override
//    public synchronized void deliverMessages(final String messages) {
//        final String[] messagesSplit = messages.split("\\|");
//        String[] messageSplit;
//        String vertexID;
//        Double message;
//        Vertex<Double, Double, Double> vertex;
//        for (final String m : messagesSplit) {
//            messageSplit = m.split("\\,");
//            if (messageSplit.length == 2) {
//                vertexID = messageSplit[0];
//                message = Double.parseDouble(messageSplit[1]);
//                vertex = (Vertex<Double, Double, Double>) this.graph.get(vertexID);
//                vertex.addToMessageQueue(message);
//            }
//        }
//    }
//}