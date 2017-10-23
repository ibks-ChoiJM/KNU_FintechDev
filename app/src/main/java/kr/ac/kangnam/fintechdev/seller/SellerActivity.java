package kr.ac.kangnam.fintechdev.seller;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import kr.ac.kangnam.fintechdev.R;
import kr.ac.kangnam.fintechdev.data.Define;
import kr.ac.kangnam.fintechdev.data.PaymentResultDto;
import kr.ac.kangnam.fintechdev.data.PrdInfoDto;
import kr.ac.kangnam.fintechdev.data.QRInfoDto;
import kr.ac.kangnam.fintechdev.data.SellerInfoDto;
import kr.ac.kangnam.fintechdev.payment.PaymentServer;
import kr.ac.kangnam.fintechdev.util.CommonUtil;
import kr.ac.kangnam.fintechdev.util.SocketClientAdapter;

public class SellerActivity extends AppCompatActivity {

    private final String LOG_TAG = SellerActivity.class.getSimpleName();

    private PaymentServer server = null;    //서버
    private SocketClientAdapter clientAdapter = null;   //클라이언트 모듈

    private String sLocalIPAddress = "";    //디바이스 IP

    private SellerInfoDto sellerInfo = null;    //판매자 정보
    private PrdInfoDto prdInfo = null;  //

    private ImageView imgQR = null;
    private TextView tvPrice = null;
    private TextView tvPrdName = null;
    private TextView tvSellerName = null;
    private TextView tvAcoountNum = null;
    private Button btnPaymentList = null;

    private  SocketClientAdapter.OnSocketListenCallback onSocketListenCallback = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller);

        //뷰에 대한 객체 받아오기
//        imgQR = (ImageView) findViewById(R.id.img_QR);
//        tvPrice = (TextView) findViewById(R.id.text_Price);;
//        tvPrdName = (TextView) findViewById(R.id.text_Product);;
//        tvSellerName = (TextView) findViewById(R.id.text_Seller);;
//        tvAcoountNum = (TextView) findViewById(R.id.text_Account_Num);;
//        btnPaymentList = (Button) findViewById(R.id.btn_Payment_List);


        /*
        * 판매자의 APP에 로컬 서버를 생성하고
        * 판매자 클라이언트는 로컬 서버에 접속
        * 구매자 클라이언트도 판매자의 로컬 서버에 접속
        */

        //IP 주소 받아오기 (서버생성후 클라이언트 접속을 위해 필요)
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

        //서버생성
        startServer();

        //결재 내역리스트 버튼 연결
        //(10)
    }

    @Override
    protected void onStart() {
        super.onStart();

        //서버에 접속하지 않은 경우 서버 접속
        if(clientAdapter == null)
        {
            connectServer();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //종료시


        if(server != null)
        {
            //서버 중지
            server.stopServer();
        }

        if(clientAdapter != null)
        {
            //클라이언트 모듈 리스너 제거
            clientAdapter.removeListenCallback(onSocketListenCallback);
            //클라이언트 과 서버 접속 해지
            clientAdapter.disconnect();

        }
    }

    private void startServer()
    {
        //서버생성
        server = new PaymentServer(getApplicationContext());
        server.startServer();
    }

    private void connectServer()
    {
        //소켓 모듈 생성 및 연결
        clientAdapter = new SocketClientAdapter();
        clientAdapter.connect(sLocalIPAddress, PaymentServer.SERVER_PORT);

        //연결정보 받아오기 위한 리스너 생성 및 등록
        clientAdapter.setConnectCallback(new SocketClientAdapter.OnSocketConnectCallback() {
            @Override
            public void onConnect() {
                //소켓이 연결 되었다면 사용자 정보 요청
                //(1)
            }

            @Override
            public void onFail(String msg) {
                Toast.makeText(SellerActivity.this, msg, Toast.LENGTH_SHORT).show();
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


                //판매자정보 받아오기
                if(type.equalsIgnoreCase(Define.REQ_TYPE_GET_SELLER_INFO) == true)
                {
                    //Json 데이터 파싱
                    Gson gson = new Gson();
                    SellerInfoDto sellerInfo = gson.fromJson(data, SellerInfoDto.class);

                    //판매자 정보 확인
                    checkSellerInfo(sellerInfo);
                }
                //상품정보 받아오기
                else if(type.equalsIgnoreCase(Define.REQ_TYPE_GET_PRD_INFO) == true)
                {
                    //Json 데이터 파싱
                    Gson gson = new Gson();
                    prdInfo = gson.fromJson(data, PrdInfoDto.class);

                    //상품정보 보여주기
                    setViewPrdInfo();
                }
                //결제완료
                else if(type.equalsIgnoreCase(Define.REQ_TYPE_PAYMENT_COMPLETE) == true)
                {
                    //Json 데이터 파싱
                    Gson gson = new Gson();
                    PaymentResultDto paymentResultDto = gson.fromJson(data, PaymentResultDto.class);

                    //결제정보 보여주기
                    //(7)
                }
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(SellerActivity.this, msg, Toast.LENGTH_SHORT).show();
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

                //판매자정보 등록 완료
                if(type.equalsIgnoreCase(Define.REQ_TYPE_SET_SELLER_INFO) == true)
                {
                    //상품정보 등록
                    reqSetPrdInfo();
                }
                //상품정보 등록 완료
                else if(type.equalsIgnoreCase(Define.REQ_TYPE_SET_PRD_INFO) == true)
                {
                    //상품정보 요청
                    reqGetPrdInfo();
                }
            }

            @Override
            public void onFail(String msg) {
                Toast.makeText(SellerActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkSellerInfo(SellerInfoDto sellerInfo)
    {
        /*
        판매자 정보가 있는지 체크
        판매자 정보가 없다면 등록 팝업 노출
        판매자 정보가 있다면 상품 정보 요청
        */

        //판매자 정보가 없거나 판매자 이름이 없다면 판매자 등록이 안되어 있는것으로 판단
        if(sellerInfo == null
                || TextUtils.isEmpty(sellerInfo.getSellerName()) == true)
        {
            /*
            판매자 정보 등록 팝업 생성
            판매자 입력 정보 받아오기
            판매자 등록 요청
            */
            //(2)
        }
        else
        {

            //멤버변수 판매자 정보 셋팅
            this.sellerInfo = sellerInfo;
            //상품 정보 요청
            //(3)
        }

    }

    private void setViewPrdInfo()
    {
        if(prdInfo == null)
        {
            return;
        }

        //싱픔정보 셋팅
        //(5)
        //가격정보
        //상품이름
        //판매자이름
        //계좌번호(QR id)


        //(6)
        //QR코드 생성
        //QR코드 이미지 셋팅
    }

    private Bitmap getQRcode()
    {
        if(sellerInfo == null)
        {
            return null;
        }

        //QR코드 정보 셋팅
        //IP : 구매자가 접속 할수 있는 서버IP(판매자 IP동일)
        //판매자 QR ID : QR코드를 구분할수 있는 유일 값
        QRInfoDto qrInfo = new QRInfoDto();
        qrInfo.setIp(sLocalIPAddress);
        qrInfo.setQrId(sellerInfo.getQrId());

        //JSON 생성
        Gson gson = new Gson();
        String sJson = gson.toJson(qrInfo);

        //Json 정보를 통한 QR코드 생성
        Bitmap bmpQR = CommonUtil.generateQRCode(sJson);

        //QR코드 값 반환
        return bmpQR;
    }

    private void showPaymentComplete(PaymentResultDto paymentResultDto)
    {
        if(paymentResultDto == null)
        {
            return;
        }


        //노출될 결제 정보 셋팅
        //(8)

        //결제 완료를 알려주기 위한 기본 팝업 노출
        //(9)

    }

    private void showPaymentList()
    {
        //결제내역 팝업 노출
        //(11)
    }

    private void reqGetSellerInfo()
    {
        //서버에 판매자정보 요청
        clientAdapter.sendMessage(Define.REQ_TYPE_GET_SELLER_INFO, "");
    }

    private void reqSetSellerInfo(int nSellerNum, String sSellerName, String sPhoneNum)
    {
        //판매자정보 객체 생성
        sellerInfo = new SellerInfoDto();
        sellerInfo.setSellerId(nSellerNum);
        sellerInfo.setSellerName(sSellerName);
        sellerInfo.setPhoneNum(sPhoneNum);
        sellerInfo.setQrId(Define.QR_CODE_BASE + nSellerNum);

        //JSON 생성
        Gson gson = new Gson();
        String sJson = gson.toJson(sellerInfo);

        //서버에 판매자정보 요청
        clientAdapter.sendMessage(Define.REQ_TYPE_SET_SELLER_INFO, sJson);
    }

    private void reqGetPrdInfo()
    {
        //서버에 상품정보 요청
        clientAdapter.sendMessage(Define.REQ_TYPE_GET_PRD_INFO, sellerInfo.getQrId());
    }

    private void reqSetPrdInfo()
    {
        //상품정보 객체 생성
        //(4)
        PrdInfoDto prdSetInfo = new PrdInfoDto();
        prdSetInfo.setSellerId(sellerInfo.getSellerId());
        prdSetInfo.setSellerName(sellerInfo.getSellerName());
        prdSetInfo.setQrId(sellerInfo.getQrId());
//        prdSetInfo.setPrdName("배추");
//        prdSetInfo.setPrdPrice(1000);

        //JSON 생성
        Gson gson = new Gson();
        String sJson = gson.toJson(prdSetInfo);

        //서버에 상품정보 요청
        clientAdapter.sendMessage(Define.REQ_TYPE_SET_PRD_INFO, sJson);
    }



}
