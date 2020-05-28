package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class PaymentResponse {

    private String reference;

    @JsonProperty("date_created")
    private Date dateCreated;

    private String status;

    @JsonProperty("payment_group_reference")
    private String paymentGroupReference;

    @JsonProperty("status_histories")
    private List<StatusHistories> statusHistories;

    private PaymentResponse() {
    }

    public PaymentResponse(String reference, Date dateCreated,
                           String status, String paymentGroupReference,
                           List<StatusHistories> statusHistories) {
        this.reference = reference;
        this.dateCreated = dateCreated;
        this.status = status;
        this.paymentGroupReference = paymentGroupReference;
        this.statusHistories = statusHistories;
    }

    public String getReference() {
        return reference;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentGroupReference() {
        return paymentGroupReference;
    }

    public List<StatusHistories> getStatusHistories() {
        return statusHistories;
    }
}
