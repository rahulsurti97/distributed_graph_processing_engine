package ece428.mp1;

import java.io.BufferedReader;
import java.io.FileReader;

public class Main {

    public static void main(final String[] args) throws Exception {
        /**
         * Check which node is the introducer vs a regular servent
         */

        final Integer machineNumber = Integer.parseInt(new BufferedReader(new FileReader("../number.txt")).readLine());
        if (machineNumber == 1) {
            final Introducer introducer = new Introducer();
            introducer.startServent();
        } else if (machineNumber == 2) {
            final BackupGraphMaster backupGraphMaster = new BackupGraphMaster();
            backupGraphMaster.startServent();
        } else {
            final Servent servent = new Servent();
            servent.startServent();
        }
    }
}
