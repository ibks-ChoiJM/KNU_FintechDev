package kr.ac.kangnam.fintechdev.buyer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import kr.ac.kangnam.fintechdev.R;
import kr.ac.kangnam.fintechdev.data.Define;
import kr.ac.kangnam.fintechdev.data.PaymentDto;
import kr.ac.kangnam.fintechdev.data.PrdInfoDto;
import kr.ac.kangnam.fintechdev.data.QRInfoDto;
import kr.ac.kangnam.fintechdev.payment.PaymentServer;
import kr.ac.kangnam.fintechdev.util.CommonUtil;
import kr.ac.kangnam.fintechdev.util.SocketClientAdapter;

public class BuyerActivity extends AppCompatActivity {

    private final String LOG_TAG = BuyerActivity.class.getSimpleName();

    private SocketClientAdapter clientAdapter = null;

    private String sLocalIPAddress = "";

    private String sServerIPAddress = "";
    private String sQRid = "";

    private PrdInfoDto prdInfo = null;

    private Button btnQR = null;
    private Button btnPayment = null;
    private Button btnOk = null;
    private TextView tvPrice = null;
    private TextView tvPrdName = null;
    private TextView tvSellerName = null;
    private TextView tvAcoountNum = null;
    private EditText edPwd = null;

    private LinearLayout layoutComplete = null;
    private LinearLayout layoutPwd = null;

    private boolean isPaymentComplete = false;

    private SocketClientAdapter.OnSocketListenCallback onSocketListenCallback = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer);

        btnQR = (Button) findViewById(R.id.btn_QR);
        btnPayment = (Button) findViewById(R.id.btn_Payment);
        btnOk = (Button) findViewById(R.id.btn_Ok);
        tvPrice = (TextView) findViewById(R.id.text_Price);;
        tvPrdName = (TextView) findViewById(R.id.text_Product);;
        tvSellerName = (TextView) findViewById(R.id.text_Seller);;
        tvAcoountNum = (TextView) findViewById(R.id.text_Account_Num);;
        edPwd = (EditText) findViewById(R.id.edit_Pwd);

        layoutComplete = (LinearLayout) findViewById(R.id.layout_Payment_Complete);
        layoutPwd = (LinearLayout) findViewById(R.id.layout_Input_Pwd);

        //IP 주소 받아오기 (서버 접속을 위해 필요)
        sLocalIPAddress = CommonUtil.getLocalIpAddress(getApplicationContext());

        Log.d(LOG_TAG, "IP : " + sLocalIPAddress);

        //와이파이 환경이 아닌경우 IP주소를 받아 올수 없으므로 에러 문구 표시
        if(TextUtils.isEmpty(sLocalIPAddress) == true
                || sLocalIPAddress.equalsIgnoreCase("0.0.0.0") == true)
        {
            Toast.makeText(this, R.string.msg_need_connect_wifi, Toast.LENGTH_SHORT).show();

            finish();
            return;
        }

        //QR 코드 인식 버튼 눌렀을때
        btnQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //QR코드 카메라 호출
                new IntentIntegrator(BuyerActivity.this).initiateScan();

            }
        });

        //결제 버튼 눌렀을때
        btnPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //결제 요청
                reqPayment();
            }
        });

        //결제 완료 후 노출
        //확인 버튼 눌렀을때 화면 초기화
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //데이터 초기화
                prdInfo = null;
                isPaymentComplete = false;
                setViewPrdInfo();
            }
        });

        setViewPrdInfo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //QR 코드 결과 값
        //  com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE
        //  = 0x0000c0de; // Only use bottom 16 bits
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result == null) {
                // 취소됨
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {

                //Json 파싱
                Gson gson = new Gson();
                QRInfoDto qrInfo = gson.fromJson(result.getContents(), QRInfoDto.class);

                //QR코드 정보가 정상적이지 않다면 취소로 판단
                if(qrInfo ==  null
                        || TextUtils.isEmpty(qrInfo.getQrId()) == true
                        || TextUtils.isEmpty(qrInfo.getIp()) == true)
                {
                    // 취소됨
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
                    return;
                }

                //서버 접속
                sQRid = qrInfo.getQrId();
                sServerIPAddress = qrInfo.getIp();
                connectServer();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

//        if(clientAdapter == null)
//        {
//            connectServer();
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Activity 종료시 서버 접속 종료
        disConnectServer();
    }

    private void disConnectServer()
    {
        if(clientAdapter != null)
        {
            clientAdapter.removeListenCallback(onSocketListenCallback);
            clientAdapter.disconnect();
        }
    }

    private void connectServer()
    {
        //서버접속

        disConnectServer();

        clientAdapter = new SocketClientAdapter();
        clientAdapter.connect(sServerIPAddress, PaymentServer.SERVER_PORT);
        clientAdapter.setConnectCallback(new SocketClientAdapter.OnSocketConnectCallback() {
            @Override
            public void onConnect() {
                reqGetPrdInfo();
            }

            @Override
            public void onFail(String msg) {
                Toast.makeText(BuyerActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        //요청한 정보 받아오기 위한 리스너 생성
        onSocketListenCallback = new SocketClientAdapter.OnSocketListenCallback() {
            @Override
            public void onListen(String type, String data) {

                if(TextUtils.isEmpty(type) == true)
                {
                    return;
                }

                //상품정보 받아오기
                if(type.equalsIgnoreCase(Define.REQ_TYPE_GET_PRD_INFO) == true)
                {
                    //Json 파싱
                    Gson gson = new Gson();
                    prdInfo = gson.fromJson(data, PrdInfoDto.class);

                    //상품정보 보여주기
                    setViewPrdInfo();
                }
                //결제 완료 정보 받아오기
                else if(type.equalsIgnoreCase(Define.REQ_TYPE_SET_PAYMENT) == true)
                {
                    isPaymentComplete = true;
                    //결제 완료 화면 보여주기
                    setViewPrdInfo();
                }

            }

            @Override
            public void onError(String msg) {
                Toast.makeText(BuyerActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        };


        //요청한 정보 받아오기 위한 리스너 등록
        clientAdapter.addListenCallback(onSocketListenCallback);

        //요청 상태에 대한 리스너 생성 및 등록
        clientAdapter.setSendCallback(new SocketClientAdapter.OnSocketSendCallback() {
            @Override
            public void onSendComplete(String type) {
                if(TextUtils.isEmpty(type) == true)
                {
                    return;
                }

            }

            @Override
            public void onFail(String msg) {
                Toast.makeText(BuyerActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setViewPrdInfo()
    {
        //상품정보 혹은 상품이름이 없다면 초기 화면 보여줌
        if(prdInfo == null
                || TextUtils.isEmpty(prdInfo.getPrdName()) == true)
        {
            tvPrice.setText("");
            tvPrdName.setText("");
            tvSellerName.setText("");
            tvAcoountNum.setText("");
            edPwd.setText("");

            btnOk.setVisibility(View.GONE);
            btnPayment.setVisibility(View.GONE);
            layoutComplete.setVisibility(View.GONE);
            layoutPwd.setVisibility(View.GONE);
        }
        else
        {
            tvPrice.setText(String.valueOf(prdInfo.getPrdPrice()));
            tvPrdName.setText(prdInfo.getPrdName());
            tvSellerName.setText(prdInfo.getSellerName());
            tvAcoountNum.setText(prdInfo.getQrId());

            //결제 완료 화면
            if(isPaymentComplete == true)
            {
                btnOk.setVisibility(View.VISIBLE);
                btnPayment.setVisibility(View.GONE);
                layoutComplete.setVisibility(View.VISIBLE);
                layoutPwd.setVisibility(View.GONE);
            }
            //결제 화면
            else
            {
                btnOk.setVisibility(View.GONE);
                btnPayment.setVisibility(View.VISIBLE);
                layoutComplete.setVisibility(View.GONE);
                layoutPwd.setVisibility(View.VISIBLE);
            }
        }

    }

    private void reqGetPrdInfo()
    {
        //QR정보를 이용한 상품정보 요청
        clientAdapter.sendMessage(Define.REQ_TYPE_GET_PRD_INFO, sQRid);
    }


    private void reqPayment()
    {
        //비밀번호 자리수 비교
        if(edPwd.getText().toString().length() < 6)
        {
            Toast.makeText(this, R.string.msg_input_password, Toast.LENGTH_SHORT).show();
            return;
        }

        // 결제 정보 셋팅
        PaymentDto paymentDto = new PaymentDto();
        paymentDto.setSellerId(prdInfo.getSellerId());
        paymentDto.setQrId(prdInfo.getQrId());
        paymentDto.setPaymentType(1);
        paymentDto.setBuyerAccountNum("");
        paymentDto.setPrdId(prdInfo.getPrdId());
        paymentDto.setPrdName(prdInfo.getPrdName());
        paymentDto.setPrdPrice(prdInfo.getPrdPrice());

        //Json 생성
        Gson gson = new Gson();

        //결제 요청
        clientAdapter.sendMessage(Define.REQ_TYPE_SET_PAYMENT, gson.toJson(paymentDto));
    }

}
