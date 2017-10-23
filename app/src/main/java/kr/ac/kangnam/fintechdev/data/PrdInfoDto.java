package kr.ac.kangnam.fintechdev.data;

/**
 * Created by choi on 2017. 9. 22..
 */

public class PrdInfoDto {

    private int prdId;
    private int sellerId;
    private String sellerName;
    private String qrId;
    private String createdAt;
    private String updatedAt;
    private String prdName;
    private long prdPrice;

    public int getPrdId() {
        return prdId;
    }

    public void setPrdId(int prdId) {
        this.prdId = prdId;
    }

    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getQrId() {
        return qrId;
    }

    public void setQrId(String qrId) {
        this.qrId = qrId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
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
