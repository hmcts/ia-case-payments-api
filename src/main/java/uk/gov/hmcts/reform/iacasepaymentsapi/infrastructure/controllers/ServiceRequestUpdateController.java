package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.controllers;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.CaseMetaData;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.CcdDataService;

@Api(tags = {"Update service request controller"})
@SwaggerDefinition(tags = {@Tag(name = "ServiceRequestUpdateController", description = "Update service request")})
@RestController
@Slf4j
public class ServiceRequestUpdateController {

    private static final String JURISDICTION = "IA";
    private static final String CASE_TYPE = "Asylum";

    private final CcdDataService ccdDataService;

    public ServiceRequestUpdateController(CcdDataService ccdDataService) {
        this.ccdDataService = ccdDataService;
    }

    @PutMapping(path = "/service-request-update",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Ways to pay will call this API and send the status of payment with other details")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback processed."),
        @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<SubmitEventDetails> serviceRequestUpdate(
        @RequestBody ServiceRequestUpdateDto serviceRequestUpdateDto) {

        String caseId = serviceRequestUpdateDto.getCcdCaseNumber();

        CaseMetaData caseMetaData =
            new CaseMetaData(Event.UPDATE_PAYMENT_STATUS,
                             JURISDICTION,
                             CASE_TYPE,
                             Long.parseLong(caseId),
                             serviceRequestUpdateDto.getPayment().getStatus(),
                             serviceRequestUpdateDto.getPayment().getReference());

        SubmitEventDetails response = ccdDataService.updatePaymentStatus(caseMetaData);
        return ResponseEntity
            .status(response.getCallbackResponseStatusCode())
            .body(response);
    }
}
