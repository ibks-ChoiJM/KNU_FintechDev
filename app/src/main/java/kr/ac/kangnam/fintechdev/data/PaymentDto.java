package kr.ac.kangnam.fintechdev.data;

/**
 * Created by choi on 2017. 9. 22..
 */

public class PaymentDto {

    private int no;
    private int sellerId;
    private String qrId;
    private String paymentAt;
    private int paymentType = 1;
    private String buyerAccountNum;
    private int prdId;
    private String prdName;
    private long prdPrice;

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public String getQrId() {
        return qrId;
    }

    public void setQrId(String qrId) {
        this.qrId = qrId;
    }

    public String getPaymentAt() {
        return paymentAt;
    }

    public void setPaymentAt(String paymentAt) {
        this.paymentAt = paymentAt;
    }

    public int getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(int paymentType) {
        this.paymentType = paymentType;
    }

    public String getBuyerAccountNum() {
        return buyerAccountNum;
    }

    public void setBuyerAccountNum(String buyerAccountNum) {
        this.buyerAccountNum = buyerAccountNum;
    }

    public int getPrdId() {
        return prdId;
    }

    public void setPrdId(int prdId) {
        this.prdId = prdId;
    }

    public String getPrdName() {
        return prdName;
    }

    public void setPrdName(String prdName) {
        this.prdName = prdName;
    }

    public long getPrdPrice() {
        return prdPrice;
    }

    public void setPrdPrice(long prdPrice) {
        this.prdPrice = prdPrice;
    }
}
