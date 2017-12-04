package ece428.mp1;

public class MasterHandler extends Handler {

    MasterHandler(final Servent servent) {
        super(servent);
    }

    public void handleMaster(final String line) {
//        System.out.println(line);
        final String[] lineSplit = line.split("\\s+");

        if (lineSplit.length < 2) {
            System.out.println("Could not parse request");
            return;
        }

        final String command = lineSplit[1];
        switch (command) {
            case "update":
                MASTER_UPDATE(lineSplit[2]);
                break;
            case "delete":
                MASTER_DELETE(lineSplit[2]);
                break;
            default:
                System.out.println("Could not parse master graph request");
        }
//        System.out.println(this.servent.masterData.toString());
    }

    private void MASTER_UPDATE(final String serialized) {
        this.servent.setMasterData(new MasterData(serialized));
    }

    private void MASTER_DELETE(final String filename) {
        this.servent.masterData.removeFile(filename, this.servent.membershipList);
    }
}

