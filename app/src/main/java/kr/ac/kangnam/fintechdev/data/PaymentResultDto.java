package kr.ac.kangnam.fintechdev.data;

/**
 * Created by choi on 2017. 9. 22..
 */

public class PaymentResultDto {


    private long prdPrice;
    private long totalPrice;

    public long getPrdPrice() {
        return prdPrice;
    }

    public void setPrdPrice(long prdPrice) {
        this.prdPrice = prdPrice;
    }

    public long getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(long totalPrice) {
        this.totalPrice = totalPrice;
    }
}
