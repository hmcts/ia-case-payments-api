package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients;


import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config.ServiceTokenGeneratorConfiguration.SERVICE_AUTHORIZATION;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentGroupsResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.ServiceRequestRequest;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.ServiceRequestResponse;

@FeignClient(name = "service-request-api", url = "${payment.api.url}")
public interface ServiceRequestApi {

    @PostMapping(value = "/service-request", consumes = "application/json")
    ServiceRequestResponse createServiceRequest(
        @RequestHeader(AUTHORIZATION) String authorization,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorization,
        @RequestBody ServiceRequestRequest serviceRequestRequest
    );

    @GetMapping(value = "/cases/{ccdCaseNumber}/paymentgroups",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    PaymentGroupsResponse getServiceRequests(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTHORIZATION) String serviceAuthorisation,
        @PathVariable("ccdCaseNumber") String ccdCaseNumber
    );
}
