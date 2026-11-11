package com.ndegwacodes.ecom.order.infrastructure.secondary.service.mpesa;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DarajaTest {

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void normalisesTheCommonKenyanFormats() {
    for (String input : new String[]{"0712345678", "+254712345678", "254712345678", "712345678", "0712 345 678", "(0712) 345-678"}) {
      assertThat(Daraja.normalisePhone(input)).as(input).contains("254712345678");
    }
    assertThat(Daraja.normalisePhone("0110345678")).contains("254110345678");
  }

  @Test
  void rejectsNumbersMpesaCannotCharge() {
    for (String input : new String[]{"", "12345", "0812345678", "+1 555 123 4567", "07123456789", "abc"}) {
      assertThat(Daraja.normalisePhone(input)).as(input).isEmpty();
    }
    assertThat(Daraja.normalisePhone(null)).isEmpty();
  }

  @Test
  void timestampIsEastAfricaTime() {
    assertThat(Daraja.timestamp(Instant.parse("2026-11-11T21:05:09Z"))).isEqualTo("20261112000509");
  }

  @Test
  void passwordIsBase64OfShortcodePasskeyAndTimestamp() {
    String password = Daraja.password("174379", "passkey", "20261111120000");
    assertThat(new String(Base64.getDecoder().decode(password))).isEqualTo("174379passkey20261111120000");
  }

  @Test
  void tokensMatchOnlyOnExactValue() {
    assertThat(Daraja.tokensMatch("secret-token", "secret-token")).isTrue();
    assertThat(Daraja.tokensMatch("secret-tokeN", "secret-token")).isFalse();
    assertThat(Daraja.tokensMatch("short", "secret-token")).isFalse();
    assertThat(Daraja.tokensMatch("anything", "")).isFalse();
    assertThat(Daraja.tokensMatch(null, "secret-token")).isFalse();
  }

  @Test
  void parsesASuccessfulCallback() throws Exception {
    String body = """
      {"Body":{"stkCallback":{"MerchantRequestID":"29115-34620561-1","CheckoutRequestID":"ws_CO_191220191020363925",
      "ResultCode":0,"ResultDesc":"The service request is processed successfully.",
      "CallbackMetadata":{"Item":[{"Name":"Amount","Value":24500.00},{"Name":"MpesaReceiptNumber","Value":"NLJ7RT61SV"},
      {"Name":"TransactionDate","Value":20191219102115},{"Name":"PhoneNumber","Value":254712345678}]}}}}
      """;
    Optional<MpesaCallbackResult> result = Daraja.parseCallback(mapper.readTree(body));
    assertThat(result).contains(new MpesaCallbackResult("ws_CO_191220191020363925", 0,
      "The service request is processed successfully.", 24500L, "NLJ7RT61SV"));
    assertThat(result.get().succeeded()).isTrue();
  }

  @Test
  void parsesACancelledCallbackWithoutMetadata() throws Exception {
    String body = """
      {"Body":{"stkCallback":{"MerchantRequestID":"1","CheckoutRequestID":"ws_CO_1","ResultCode":1032,
      "ResultDesc":"Request cancelled by user"}}}
      """;
    MpesaCallbackResult result = Daraja.parseCallback(mapper.readTree(body)).orElseThrow();
    assertThat(result.succeeded()).isFalse();
    assertThat(result.amount()).isNull();
    assertThat(result.receiptNumber()).isNull();
  }

  @Test
  void rejectsMalformedCallbacks() throws Exception {
    assertThat(Daraja.parseCallback(null)).isEmpty();
    assertThat(Daraja.parseCallback(mapper.readTree("{}"))).isEmpty();
    assertThat(Daraja.parseCallback(mapper.readTree("{\"Body\":{\"stkCallback\":{\"ResultCode\":0}}}"))).isEmpty();
    assertThat(Daraja.parseCallback(mapper.readTree(
      "{\"Body\":{\"stkCallback\":{\"CheckoutRequestID\":\"x\",\"ResultCode\":\"abc\"}}}"))).isEmpty();
  }
}
