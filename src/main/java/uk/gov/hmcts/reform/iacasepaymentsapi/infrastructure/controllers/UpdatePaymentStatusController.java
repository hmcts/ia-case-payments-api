package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.CaseMetaData;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentDto;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.CcdDataService;

@Tag(name = "Update payment status controller")
@RestController
@Slf4j
public class UpdatePaymentStatusController {

    private static final String JURISDICTION = "IA";
    private static final String CASE_TYPE = "Asylum";

    private final CcdDataService ccdDataService;

    public UpdatePaymentStatusController(CcdDataService ccdDataService) {
        this.ccdDataService = ccdDataService;
    }

    @Operation(
        summary = "Update payment status", description = "Update payment status",
        responses =
        {
            @ApiResponse(responseCode = "200", description = "Updated payment status successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @PutMapping(path = "/payment-updates",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SubmitEventDetails> updatePaymentStatus(@RequestBody PaymentDto paymentDto) {

        String caseId = paymentDto.getCcdCaseNumber();

        CaseMetaData caseMetaData =
            new CaseMetaData(Event.UPDATE_PAYMENT_STATUS, JURISDICTION,
                             CASE_TYPE, Long.valueOf(caseId), paymentDto.getStatus(), paymentDto.getReference());

        SubmitEventDetails response = ccdDataService.updatePaymentStatus(caseMetaData);
        return ResponseEntity.status(response.getCallbackResponseStatusCode()).body(response);
    }
}
