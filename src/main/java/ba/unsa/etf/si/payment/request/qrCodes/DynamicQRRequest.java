package ba.unsa.etf.si.payment.request.qrCodes;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class DynamicQRRequest {


    @NotBlank
    private String receiptId;

    @NotBlank
    private String businessName;

    @NotBlank
    private String service;

    @NotNull
    @DecimalMin("0.0")
    private Double totalPrice;

    @NotNull
    @Min(0)
    private Long bankAccountId;

    public DynamicQRRequest(@NotBlank String receiptId, @NotBlank String businessName, @NotBlank String service,  Double amount, Long bankAccountID) {
        this.receiptId = receiptId;
        this.businessName = businessName;
        this.service = service;
        this.totalPrice = amount;
        this.bankAccountId = bankAccountID;
    }

    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Long getBankAccountId() {
        return bankAccountId;
    }

    public void setBankAccountId(Long bankAccountId) {
        this.bankAccountId = bankAccountId;
    }

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }
}