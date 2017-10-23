package kr.ac.kangnam.fintechdev.payment;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import kr.ac.kangnam.fintechdev.data.Define;
import kr.ac.kangnam.fintechdev.data.PaymentDto;
import kr.ac.kangnam.fintechdev.data.PaymentResultDto;
import kr.ac.kangnam.fintechdev.data.PrdInfoDto;
import kr.ac.kangnam.fintechdev.data.SellerInfoDto;
import kr.ac.kangnam.fintechdev.util.CommonUtil;

/**
 * Created by choi on 2017. 9. 20..
 */

public class PaymentServer {

    private Context context = null;
    private final String LOG_TAG = PaymentServer.class.getSimpleName();

    private ServerSocket serverSocket;

    private boolean isRunning = false;

    public static final int SERVER_PORT = 8080;

    private DBHelper dbHelper = null;

    private DataOutputStream outSeller = null;

    public PaymentServer(Context context)
    {
        this.context = context;
        //DB 객체 오픈
        dbHelper = new DBHelper(context, "payment.db", null, 1);
    }

    //서버 시작
    public void startServer()
    {
        if(serverSocket == null
                || isRunning == false)
        {
            if(serverSocket != null)
            {
                stopServer();
            }

            createServer();
        }

    }

    //서버 중지
    public void stopServer()
    {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        serverSocket = null;
        isRunning = false;
    }

    //서버생성
    private void createServer()
    {
        try {
            isRunning = true;
            serverSocket = new ServerSocket(SERVER_PORT);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isRunning) {
                        Log.d(LOG_TAG, "Server Wait");
                        try {
                            Socket socket = serverSocket.accept();
                            Log.d(LOG_TAG, socket.getInetAddress() + " connect");

                            boolean isSeller = false;
                            //서버와 클라이언트 주소가 같다면 Seller로 판단
                            if(socket.getInetAddress().toString().contains(
                                    CommonUtil.getLocalIpAddress(context)) == true)
                            {
                                isSeller = true;
                            }

                            listenClientSocket(socket, isSeller);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listenClientSocket(final Socket socket, final boolean isSeller) {
        if (socket == null) {
            return;
        }

        new Thread(new Runnable() {
            private DataInputStream in;
            private DataOutputStream out;

            @Override
            public void run() {

                try {
                    out = new DataOutputStream(socket.getOutputStream());
                    in = new DataInputStream(socket.getInputStream());

                    if(isSeller == true)
                    {
                        outSeller = out;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {// 계속 듣기만!!
                    while (in != null) {
                        String msg = in.readUTF();

                        String[] sVals = parseData(msg);

                        if(sVals == null)
                        {
                            continue;
                        }

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

                        if(TextUtils.isEmpty(sType) == true)
                        {
                            continue;
                        }

                        String sRtn = working(sType, sData);

                        if(TextUtils.isEmpty(sRtn) == false)
                        {
                            //out.writeUTF(sRtn);
                            sendData(out, sType, sRtn);
                        }

                    }
                } catch (IOException e) {
                    // 사용접속종료시 여기서 에러 발생. 그럼나간거에요.

                }


            }
        }).start();
    }

    private String working(String sType, String sData) throws IOException {

        if(sType.equalsIgnoreCase(Define.REQ_TYPE_GET_SELLER_INFO) == true)
        {
            return dbHelper.selectSellerInfo();
        }
        else if(sType.equalsIgnoreCase(Define.REQ_TYPE_SET_SELLER_INFO) == true)
        {
            Gson gson = new Gson();
            SellerInfoDto sellerInfo = gson.fromJson(sData, SellerInfoDto.class);

            dbHelper.insertSellerInfo(sellerInfo.getSellerId(), sellerInfo.getSellerName(), sellerInfo.getPhoneNum(), sellerInfo.getQrId());
        }
        else if(sType.equalsIgnoreCase(Define.REQ_TYPE_GET_PRD_INFO) == true)
        {
            return dbHelper.selectPrdInfo(sData);
        }
        else if(sType.equalsIgnoreCase(Define.REQ_TYPE_SET_PRD_INFO) == true)
        {
            Gson gson = new Gson();
            PrdInfoDto prdInfoDto = gson.fromJson(sData, PrdInfoDto.class);

            dbHelper.insertPrdInfo(prdInfoDto.getSellerId(), prdInfoDto.getSellerName(), prdInfoDto.getQrId(), prdInfoDto.getPrdName(), prdInfoDto.getPrdPrice());
        }
        else if(sType.equalsIgnoreCase(Define.REQ_TYPE_SET_PAYMENT) == true)
        {

            Gson gson = new Gson();
            PaymentDto paymentDto = gson.fromJson(sData, PaymentDto.class);

            dbHelper.insertPayment(paymentDto.getSellerId(), paymentDto.getQrId(), paymentDto.getPaymentType(), paymentDto.getBuyerAccountNum(), paymentDto.getPrdId(), paymentDto.getPrdName(), paymentDto.getPrdPrice());

            PaymentResultDto paymentResultDto = new PaymentResultDto();
            paymentResultDto.setPrdPrice(paymentDto.getPrdPrice());
            paymentResultDto.setTotalPrice(dbHelper.selectTotalPrice());

            //판매자에게 결제완료 전달
            sendData(outSeller, Define.REQ_TYPE_PAYMENT_COMPLETE, gson.toJson(paymentResultDto));

            return "ok";
        }
        else if(sType.equalsIgnoreCase(Define.REQ_TYPE_PAYMENT_LIST) == true)
        {
            return dbHelper.selectPaymentList(sData);
        }

        return "";
    }

    private void sendData(DataOutputStream out, String sType, String sMsg) throws IOException {
        if(out == null)
        {
            return;
        }

        if(TextUtils.isEmpty(sMsg) == true)
        {
            sMsg = "";
        }

        out.writeUTF(sType + Define.NET_SEPARATOR + sMsg);
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
