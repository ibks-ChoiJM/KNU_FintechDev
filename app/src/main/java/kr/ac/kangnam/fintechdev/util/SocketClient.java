package kr.ac.kangnam.fintechdev.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by choi on 2017. 8. 22..
 */

public class SocketClient {

//    private BufferedReader bufferR = null;
//    private BufferedWriter bufferW = null;
    private DataInputStream clientIn;
    private DataOutputStream clientOut;
    private Socket client = null;

    public void connect(String ip, int port) throws IOException {

        disconnect();

        client = new Socket(ip, port);

//        bufferR = new BufferedReader(new InputStreamReader(client.getInputStream()));
//        bufferW = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        clientOut = new DataOutputStream(client.getOutputStream());
        clientIn = new DataInputStream(client.getInputStream());
    }

    public void disconnect() throws IOException {
        if (client == null
                || client.isClosed() == true) {
            return;
        }

        client.close();
    }

    //보내기
    public void send(String data) throws IOException {
        if (client == null
                || client.isClosed() == true) {
            return;
        }

        clientOut.writeUTF(data);
    }

    public DataInputStream getReadStream()
    {
        return clientIn;
    }


}
