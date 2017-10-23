package kr.ac.kangnam.fintechdev.seller;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import kr.ac.kangnam.fintechdev.R;
import kr.ac.kangnam.fintechdev.data.Define;
import kr.ac.kangnam.fintechdev.data.PaymentDto;
import kr.ac.kangnam.fintechdev.util.SocketClientAdapter;

import static kr.ac.kangnam.fintechdev.R.id.text_Total_Price;

/**
 * Created by choi on 2017. 9. 20..
 */

public class PaymentListDlg extends Dialog {

    private Context context = null;

    private Button btnClose = null;
    private Button btnDate = null;
    private Button btnSearch = null;

    private TextView textListTitle = null;
    private TextView textTotalPrice = null;

    private ListView list = null;

    private PaymentListAdapter listAdapter = null;

    private SocketClientAdapter clientAdapter = null;

    private  SocketClientAdapter.OnSocketListenCallback onSocketListenCallback = null;

    private ArrayList<PaymentDto> arrayPayment = null;

    public void setClientSocketAdapter(SocketClientAdapter clientAdapter) {
        this.clientAdapter = clientAdapter;
    }

    public PaymentListDlg(@NonNull Context context) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 다이얼로그 외부 화면 흐리게 표현
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.0f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.dialog_payment_list);

        btnClose = (Button) findViewById(R.id.btn_Close);
        btnDate = (Button) findViewById(R.id.btn_Date);
        btnSearch = (Button) findViewById(R.id.btn_Search);

        textListTitle = (TextView) findViewById(R.id.text_List_Title);
        textTotalPrice = (TextView) findViewById(text_Total_Price);

        list = (ListView) findViewById(R.id.list);

        //리스트 항목을 표사 해줄 Adapter 생성 및 리스트에 셋팅
        listAdapter = new PaymentListAdapter();
        list.setAdapter(listAdapter);

        //요청한 정보 받아오기 위한 리스너 생성
        onSocketListenCallback = new SocketClientAdapter.OnSocketListenCallback() {
            @Override
            public void onListen(String type, String data) {

                //결제 정보를 받아옴
                if(type.equalsIgnoreCase(Define.REQ_TYPE_PAYMENT_LIST) == true)
                {
                    //리스트에 뿌려줌
                    setList(data);
                }
            }

            @Override
            public void onError(String msg) {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        };

        if(clientAdapter != null)
        {
            //요청 상태에 대한 리스너 생성 및 등록
            clientAdapter.addListenCallback(onSocketListenCallback);
        }

        //보여주는 내용 초기화
        textListTitle.setText(
                String.format(context.getString(R.string.title_payment_state), ""));
        textTotalPrice.setText(
                String.format(context.getString(R.string.text_total_price), 0));

        //닫기 버튼을 눌렀을때
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        //초기값으로 현재 날자 선택
        //현재 날짜 선택
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        //값 초기화
        btnDate.setText(sdf.format(date));
        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //텍스트에 보여지는 날짜를 기준으로 DatePicker 값을 셋팅
                int nYear = 0;
                int nMonth = 0;
                int nDay = 0;
                try {
                    String sDate = btnDate.getText().toString();
                    nYear = Integer.parseInt(sDate.substring(0, 4));
                    nMonth = Integer.parseInt(sDate.substring(5, 7)) - 1;
                    nDay = Integer.parseInt(sDate.substring(8, 10));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    nYear = 2017;
                    nMonth = 8;
                    nDay = 29;
                }


                //선택한 날짜 받아오기
                //public void onDateSet(DatePicker datePicker, int i, int i1, int i2)
                //i : 년도
                //i2 : 월 (0 ~ 11이어서 +1을 해줘야함)
                //i2 : 일
                DatePickerDialog pickerDialog = new DatePickerDialog(context,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                                String sDate = String.format("%d-%02d-%02d", i, i1 + 1, i2);
                                btnDate.setText(sDate);
                            }
                        }, nYear, nMonth, nDay);

                pickerDialog.show();
            }
        });

        //검색 버튼 눌었을때
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //결제 내역 조회
                reqList();
            }
        });

    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        //다이얼록 종료 시 소켓 리스너 제거
        if(clientAdapter != null)
        {
            clientAdapter.removeListenCallback(onSocketListenCallback);
        }
    }

    private void reqList()
    {
        //선택된 날짜의 결제 내역 조회
        clientAdapter.sendMessage(Define.REQ_TYPE_PAYMENT_LIST, btnDate.getText().toString());
    }

    private void setList(String data)
    {
        //Json 데이터 파싱
        Gson gson = new Gson();
        arrayPayment = gson.fromJson(data, new TypeToken<ArrayList<PaymentDto>>(){}.getType());

        //리스트 갱신 요청
        listAdapter.notifyDataSetChanged();
        list.scrollTo(0,0);

        //총 금액 표사
        long lTotal = 0;
        for(int i = 0; i < arrayPayment.size(); i++)
        {
            lTotal += arrayPayment.get(i).getPrdPrice();
        }

        textListTitle.setText(
                String.format(context.getString(R.string.title_payment_state), btnDate.getText().toString()));
        textTotalPrice.setText(
                String.format(context.getString(R.string.text_total_price), lTotal));
    }

    public class PaymentListAdapter extends BaseAdapter
    {
       @Override
        public int getCount() {

           //몇개의 리스트 항목을 보여줄 것이진 개수 반환
            if(arrayPayment == null)
            {
                return 0;
            }
            return arrayPayment.size();
        }

        @Override
        public Object getItem(int i) {
            return arrayPayment.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            //해당 position에 보여 줄 항목 뷰 반환

            //레이아웃 불러오기
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.item_payment, null, false);
            }

            //항목의 값을 셋팅팅
           TextView textDate = view.findViewById(R.id.text_Date);
            TextView textPrice = view.findViewById(R.id.text_Price);

            textDate.setText(arrayPayment.get(i).getPaymentAt());
            textPrice.setText(String.valueOf(arrayPayment.get(i).getPrdPrice()));


            return view;
        }
    }

}
