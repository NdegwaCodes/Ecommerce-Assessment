package com.ndegwacodes.ecom.order.infrastructure.secondary.service.mpesa;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class MpesaService {

  private static final Logger log = LoggerFactory.getLogger(MpesaService.class);

  private final RestClient restClient;
  private final String consumerKey;
  private final String consumerSecret;
  private final String shortcode;
  private final String passkey;
  private final String transactionType;
  private final String partyB;
  private final String callbackUrl;
  private final String callbackToken;
  private final String accountReference;

  private String cachedToken;
  private Instant tokenExpiresAt = Instant.EPOCH;

  public MpesaService(@Value("${application.mpesa.environment}") String environment,
                      @Value("${application.mpesa.consumer-key}") String consumerKey,
                      @Value("${application.mpesa.consumer-secret}") String consumerSecret,
                      @Value("${application.mpesa.shortcode}") String shortcode,
                      @Value("${application.mpesa.passkey}") String passkey,
                      @Value("${application.mpesa.transaction-type}") String transactionType,
                      @Value("${application.mpesa.party-b}") String partyB,
                      @Value("${application.mpesa.callback-base-url}") String callbackBaseUrl,
                      @Value("${application.mpesa.callback-token}") String callbackToken,
                      @Value("${application.mpesa.account-reference}") String accountReference) {
    String baseUrl = "production".equalsIgnoreCase(environment)
      ? "https://api.safaricom.co.ke"
      : "https://sandbox.safaricom.co.ke";
    this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    this.consumerKey = consumerKey;
    this.consumerSecret = consumerSecret;
    this.shortcode = shortcode;
    this.passkey = passkey;
    this.transactionType = "CustomerBuyGoodsOnline".equals(transactionType)
      ? "CustomerBuyGoodsOnline"
      : "CustomerPayBillOnline";
    this.partyB = partyB == null || partyB.isBlank() ? shortcode : partyB;
    this.callbackUrl = callbackBaseUrl.replaceAll("/+$", "") + "/api/orders/mpesa/callback/" + callbackToken;
    this.callbackToken = callbackToken;
    this.accountReference = accountReference.length() > 12 ? accountReference.substring(0, 12) : accountReference;
  }

  public boolean isConfigured() {
    return !consumerKey.isBlank() && !consumerSecret.isBlank() && !shortcode.isBlank()
      && !passkey.isBlank() && !callbackToken.isBlank() && callbackUrl.startsWith("https://");
  }

  public boolean isValidCallbackToken(String token) {
    return Daraja.tokensMatch(token, callbackToken);
  }

  public MpesaStkPush stkPush(String phone, long amount, String description) {
    if (!isConfigured()) {
      throw new MpesaPaymentException(MpesaPaymentException.Reason.NOT_CONFIGURED, "M-Pesa payments are not available right now");
    }
    String timestamp = Daraja.timestamp(Instant.now());
    Map<String, Object> request = new LinkedHashMap<>();
    request.put("BusinessShortCode", shortcode);
    request.put("Password", Daraja.password(shortcode, passkey, timestamp));
    request.put("Timestamp", timestamp);
    request.put("TransactionType", transactionType);
    request.put("Amount", amount);
    request.put("PartyA", phone);
    request.put("PartyB", partyB);
    request.put("PhoneNumber", phone);
    request.put("CallBackURL", callbackUrl);
    request.put("AccountReference", accountReference);
    request.put("TransactionDesc", description.length() > 13 ? description.substring(0, 13) : description);

    try {
      JsonNode response = restClient.post()
        .uri("/mpesa/stkpush/v1/processrequest")
        .header("Authorization", "Bearer " + accessToken())
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .body(JsonNode.class);
      if (response == null || !"0".equals(response.path("ResponseCode").asText())) {
        throw new MpesaPaymentException(MpesaPaymentException.Reason.REJECTED, "M-Pesa did not accept the request. Try again.");
      }
      return new MpesaStkPush(response.path("MerchantRequestID").asText(), response.path("CheckoutRequestID").asText());
    } catch (RestClientException e) {
      log.error("STK push failed: {}", e.getMessage());
      throw new MpesaPaymentException(MpesaPaymentException.Reason.REJECTED, "M-Pesa did not accept the request. Try again.");
    }
  }

  public Optional<Integer> stkQuery(String checkoutRequestId) {
    if (!isConfigured()) {
      return Optional.empty();
    }
    String timestamp = Daraja.timestamp(Instant.now());
    Map<String, Object> request = Map.of(
      "BusinessShortCode", shortcode,
      "Password", Daraja.password(shortcode, passkey, timestamp),
      "Timestamp", timestamp,
      "CheckoutRequestID", checkoutRequestId);
    try {
      JsonNode response = restClient.post()
        .uri("/mpesa/stkpushquery/v1/query")
        .header("Authorization", "Bearer " + accessToken())
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .body(JsonNode.class);
      if (response == null || !response.path("ResultCode").canConvertToInt()) {
        return Optional.empty();
      }
      return Optional.of(response.path("ResultCode").asInt());
    } catch (RestClientException e) {
      return Optional.empty();
    }
  }

  private synchronized String accessToken() {
    if (cachedToken != null && Instant.now().isBefore(tokenExpiresAt)) {
      return cachedToken;
    }
    String credentials = Base64.getEncoder()
      .encodeToString((consumerKey + ":" + consumerSecret).getBytes(StandardCharsets.UTF_8));
    JsonNode response = restClient.get()
      .uri("/oauth/v1/generate?grant_type=client_credentials")
      .header("Authorization", "Basic " + credentials)
      .retrieve()
      .body(JsonNode.class);
    if (response == null || !response.path("access_token").isTextual()) {
      throw new MpesaPaymentException(MpesaPaymentException.Reason.REJECTED, "Could not reach M-Pesa. Try again.");
    }
    long expiresIn = response.path("expires_in").asLong(3599);
    cachedToken = response.path("access_token").asText();
    tokenExpiresAt = Instant.now().plusSeconds(Math.max(60, expiresIn - 60));
    return cachedToken;
  }
}
