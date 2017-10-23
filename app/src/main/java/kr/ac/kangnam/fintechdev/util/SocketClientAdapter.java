package kr.ac.kangnam.fintechdev.util;

import android.os.AsyncTask;
import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;

import kr.ac.kangnam.fintechdev.data.Define;

/**
 * Created by choi on 2017. 8. 22..
 */

public class SocketClientAdapter {

    //연결 상태 콜백 인터페이스
    public interface OnSocketConnectCallback{
        public void onConnect();
        public void onFail(String msg);
    }

    //요청 정보 콜백 인터페이스
    public interface OnSocketListenCallback{
        public void onListen(String type, String data);
        public void onError(String msg);
    }

    //요청 콜백 인터페이스
    public interface OnSocketSendCallback{
        public void onSendComplete(String type);
        public void onFail(String msg);
    }

    private static SocketClientAdapter gIns = null;

    public static SocketClientAdapter getIns()
    {
        if(gIns == null)
        {
            gIns = new SocketClientAdapter();
        }

        return gIns;
    }

    private SocketClient client = null;

    private boolean isStop = true;

    private OnSocketConnectCallback connectCallback;
    private ArrayList<OnSocketListenCallback> arraylistenCallback = new ArrayList<>();
    private OnSocketSendCallback sendCallback;

    public void setConnectCallback(OnSocketConnectCallback connectCallback) {
        this.connectCallback = connectCallback;
    }

    public void addListenCallback(OnSocketListenCallback listenCallback) {

        if(arraylistenCallback.contains(listenCallback) == false) {
            arraylistenCallback.add(listenCallback);
        }
    }

    public void removeListenCallback(OnSocketListenCallback listenCallback) {
        arraylistenCallback.remove(listenCallback);
    }

    public void setSendCallback(OnSocketSendCallback sendCallback) {
        this.sendCallback = sendCallback;
    }

    //서버 접속
    public void connect(final String ip, final int port)
    {
        new AsyncTask<Void, String, Void>() {

            String sConnectError = "";
            String sClientError = "";

            @Override
            protected Void doInBackground(Void... voids) {


                try {

                    client = new SocketClient();
                    client.connect(ip, port);
                    if(connectCallback != null)
                    {
                        connectCallback.onConnect();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    sConnectError = e.getMessage();
                    return null;
                }

                try {

                    isStop = false;

                    while (isStop == false)
                    {
                        String sMsg = client.getReadStream().readUTF();

                        if(TextUtils.isEmpty(sMsg) == false)
                        {
                            publishProgress(sMsg);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    sClientError = e.getMessage();
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                super.onProgressUpdate(values);

                if(values == null)
                {
                    return;
                }

                for(int i = 0; i < values.length; i++)
                {
                    if(arraylistenCallback != null)
                    {
                        String[] sVals = parseData(values[i]);
                        String sType = "";
                        String sData = "";
                        if(sVals.length > 0)
                        {
                            sType = sVals[0];
                        }

                        if(sVals.length > 1)
                        {
                            sData = sVals[1];
                        }

                        for(int j = 0 ; j < arraylistenCallback.size(); j++)
                        {
                            arraylistenCallback.get(j).onListen(sType, sData);
                        }
                    }

                }

            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                if(TextUtils.isEmpty(sConnectError) == false
                        && connectCallback != null)
                {
                    connectCallback.onFail(sConnectError);
                }

                else if(TextUtils.isEmpty(sClientError) == false
                        && arraylistenCallback != null)
                {
                    for(int j = 0 ; j < arraylistenCallback.size(); j++)
                    {
                        arraylistenCallback.get(j).onError(sClientError);
                    }
                }
            }
        }.execute();

    }

    public void disconnect()
    {
        isStop = true;
        if(client == null)
        {
            return;
        }

        try {
            client.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(final String sType, final String sMsg)
    {
        if(client == null)
        {
            return;
        }

        new AsyncTask<Void, Integer, Void>() {

            private String sErr = "";

            private ArrayList<String> arrayMsg = new ArrayList<>();

            @Override
            protected Void doInBackground(Void... voids) {


                try {

                    sendData(sType, sMsg);

                } catch (IOException e) {
                    e.printStackTrace();
                    sErr = e.getMessage();

                }

                return null;
            }


            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                if(sendCallback != null)
                {
                    if(TextUtils.isEmpty(sErr) == true)
                    {
                        sendCallback.onSendComplete(sType);
                    }
                    else
                    {
                        sendCallback.onFail(sErr);
                    }

                }

            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

    }

    private void sendData(String sType, String sMsg) throws IOException {
        if(client == null)
        {
            return;
        }

        if(TextUtils.isEmpty(sMsg) == true)
        {
            sMsg = "";
        }

        client.send(sType + Define.NET_SEPARATOR + sMsg);
    }

    //0 : type
    //1 : data
    private String[] parseData(String sMsg)
    {
        if(TextUtils.isEmpty(sMsg) == true)
        {
            return null;
        }
        return sMsg.split(Define.NET_SEPARATOR);
    }

}
