package ece428.mp1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Network {

    public static String receiveData(final ServerSocket serverSocket) {
        String line = "";
        try {
            final Socket socket = serverSocket.accept();
            final DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            line = dataInputStream.readUTF();
            dataInputStream.close();
            socket.close();
        } catch (final Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
        return line;
    }

    public static void multicast(final ArrayList<NodeID> nodes, final Integer portNumber, final String data) {
        for (final NodeID n : nodes) {
            sendData(n.getIPAddress().getHostAddress(), portNumber, data);
        }
    }

    public static void sendData(final String hostAddress, final Integer portNumber, final String data) {
        try {
            final Socket socket = new Socket(hostAddress, portNumber);
            final DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeUTF(data);
            dataOutputStream.close();
            socket.close();
        } catch (final Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }
}
