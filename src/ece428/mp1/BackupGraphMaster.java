package ece428.mp1;

import java.io.IOException;
import java.net.DatagramPacket;

public class BackupGraphMaster extends Servent {
    private Boolean isActiveMaster;

    public BackupGraphMaster() throws IOException {
        super();
        this.isActiveMaster = false;
    }

    public Boolean getActiveMaster() {
        return this.isActiveMaster;
    }

    public void setActiveMaster(final Boolean activeMaster) {
        this.isActiveMaster = activeMaster;
    }

    @Override
    protected void retrieveData(final DatagramPacket incomingPacket) throws IOException {
        super.retrieveData(incomingPacket);
        if (!this.membershipList.listEntries.get(this.INTRODUCER_NODE).getAlive()) {
            if (!this.isActiveMaster) {
                this.setActiveMaster(true);
                Network.multicast(this.getWorkerNodes(), Servent.RECEIVE_PORT_GRAPH_MESSAGE,
                        "graph backup_superstep");
                this.setMasterGraph(new MasterGraph(this, this.getWorkerNodes()));
                this.masterGraph.setSuperStep(Integer.MAX_VALUE);
//                System.out.println("MASTER DOWN");
            }
        } else {
//            System.out.println("MASTER UP");
            if (this.isActiveMaster) {
                Network.multicast(this.getWorkerNodes(), Servent.RECEIVE_PORT_GRAPH_MESSAGE,
                        "graph master_revived");
            }
            this.setActiveMaster(false);
        }
    }
}
