package kr.ac.kangnam.fintechdev.seller;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import kr.ac.kangnam.fintechdev.R;

/**
 * Created by choi on 2017. 9. 20..
 */

public class SellerInfoDlg extends Dialog {

    //판매자 정보 콜백 인터페이스
    public interface OnSellerInfoListener
    {
        void onSellerInfo(int nSellerNum, String sSellerName, String sPhoneNum);
    }

    private EditText editSellerNum = null;
    private EditText editSeller = null;
    private EditText editPhoneNum = null;

    private OnSellerInfoListener onSellerInfoListener = null;

    public void setOnSellerInfoListener(OnSellerInfoListener onSellerInfoListener) {
        this.onSellerInfoListener = onSellerInfoListener;
    }

    public SellerInfoDlg(@NonNull Context context) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 다이얼로그 외부 화면 흐리게 표현
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setAttributes(lpWindow);

        setContentView(R.layout.dialog_seller);

        Button btnSave = (Button) findViewById(R.id.btn_Save);
        Button btnCancel = (Button) findViewById(R.id.btn_Cancel);

        editSellerNum = (EditText) findViewById(R.id.edit_Seller_Num);
        editSeller = (EditText) findViewById(R.id.edit_Seller);
        editPhoneNum = (EditText) findViewById(R.id.edit_Phone_Num);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //빈 정보 있는 예외 처리
                if(TextUtils.isEmpty(editSellerNum.toString()) == true)
                {
                    Toast.makeText(getContext(), R.string.msg_input_seller_num, Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(editSeller.toString()) == true)
                {
                    Toast.makeText(getContext(), R.string.msg_input_seller_name, Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(editPhoneNum.toString()) == true)
                {
                    Toast.makeText(getContext(), R.string.msg_input_phone_num, Toast.LENGTH_SHORT).show();
                    return;
                }

                //리스너가 등록되어 있다면 등록 정보 보냄
                if(onSellerInfoListener != null)
                {
                    onSellerInfoListener.onSellerInfo(
                            Integer.valueOf(editSellerNum.getText().toString()),
                            editSeller.getText().toString().toString(),
                            editPhoneNum.getText().toString().toString()
                    );
                }

                dismiss();
            }
        });

        //취소 선택시 팝업 닫기
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}
