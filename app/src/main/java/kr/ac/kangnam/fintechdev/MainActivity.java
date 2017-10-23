package kr.ac.kangnam.fintechdev;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import kr.ac.kangnam.fintechdev.buyer.BuyerActivity;
import kr.ac.kangnam.fintechdev.seller.SellerActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //액티비티의 타이틀 지정
        //(1)
//        getSupportActionBar().setTitle(getString(R.string.title_select_mode));

        //버튼 객체 받아오기
//        Button btnSeller = (Button) findViewById(R.id.btn_Mode_Seller);
//        Button btnBuyer = (Button) findViewById(R.id.btn_Mode_Buyer);

        //판매자 버튼 클릭 이벤트
        //클릭시 판매자 화면으로 이동
        //(2)

        //구매자 버튼 클릭 이벤트
        //클릭시 구매자 화면으로 이동
        //(3)

    }

}
