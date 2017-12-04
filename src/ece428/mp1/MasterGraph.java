package ece428.mp1;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class MasterGraph {

    private final ConcurrentHashMap<NodeID, Boolean> workerAcks;
    private final ConcurrentHashMap<NodeID, Boolean> workerHalts;
    private final Servent servent;
    private final ArrayList<NodeID> workerNodes;
    private Integer superStep;
    private long startTime;

    public MasterGraph(final Servent servent, final ArrayList<NodeID> workerNodes) {
        this.superStep = 0;
        this.workerAcks = new ConcurrentHashMap<>();
        this.workerHalts = new ConcurrentHashMap<>();
        this.workerNodes = workerNodes;
        this.clearWorkerAcks();
        this.clearWorkerHalts();
        this.servent = servent;
    }

    private void clearWorkerAcks() {
        for (final NodeID w : this.workerNodes) {
            this.workerAcks.put(w, Boolean.FALSE);
        }
    }

    private void clearWorkerHalts() {
        for (final NodeID w : this.workerNodes) {
            this.workerHalts.put(w, Boolean.TRUE);
        }
    }

    public Integer getSuperStep() {
        return this.superStep;
    }

    public void setSuperStep(final Integer superStep) {
        this.superStep = superStep;
    }

    public void processAcknowledge(final NodeID worker,
//                                   final String graphType,
                                   final Boolean halt) {

        this.ackHalt(worker, halt);
        if (this.hasRecievedAllAcks()) {
            if (this.shouldHalt()) {
                this.stopTimer();
            } else {
                this.advanceSuperstep();
                Network.multicast(this.servent.getWorkerNodes(),
                        Servent.RECEIVE_PORT_GRAPH_MESSAGE,
                        "graph superstep " + String.valueOf(this.superStep));
            }
        }
    }

    private void ackHalt(final NodeID worker, final Boolean halt) {
        this.workerHalts.put(worker, halt); //true indicated worker wants to halt
        this.workerAcks.put(worker, Boolean.TRUE);
    }

    private boolean hasRecievedAllAcks() {
        for (final NodeID w : this.workerNodes) {
            if (!this.workerAcks.get(w)) { // at least 1 node has not ack'd
                return false;
            }
        }
        return true;
    }

    private boolean shouldHalt() {
        for (final NodeID w : this.workerNodes) {
            if (!this.workerHalts.get(w)) { // at least 1 node has voted to not halt
                return false;
            }
        }
        return true;
    }

    public void stopTimer() {
        final long time = System.currentTimeMillis() - this.startTime;
        System.out.println("Finished Computing Graph");
        System.out.println("Time elapsed: " + time + " milliseconds");
    }

    private void advanceSuperstep() {
        this.superStep++;
        this.clearWorkerAcks();
    }

    public void startTimer() {
        this.startTime = System.currentTimeMillis();
    }

    public String serialize() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.startTime).append("`")
                .append(this.superStep).append("`");
        for (final NodeID worker : this.workerNodes) {
            sb.append(worker.serialize()).append(":")
                    .append(this.workerAcks.get(worker).toString()).append(";");
        }
        sb.append("`");
        for (final NodeID worker : this.workerNodes) {
            sb.append(worker.serialize()).append(":")
                    .append(this.workerHalts.get(worker).toString()).append(";");
        }
        return sb.toString();
    }

    public void handleRecovery(final Integer superStep, final NodeID worker) {
        this.workerAcks.put(worker, Boolean.TRUE);
        this.superStep = Math.min(this.superStep, superStep);
        if (this.hasRecievedAllAcks()) {
            Network.multicast(this.servent.getWorkerNodes(),
                    Servent.RECEIVE_PORT_GRAPH_MESSAGE,
                    "graph superstep " + this.superStep.toString());
        }
    }
}
