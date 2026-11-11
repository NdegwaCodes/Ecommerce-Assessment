package com.ndegwacodes.ecom.order.infrastructure.secondary.service.mpesa;

import com.fasterxml.jackson.databind.JsonNode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;
import java.util.regex.Pattern;

public final class Daraja {

  private static final ZoneId NAIROBI = ZoneId.of("Africa/Nairobi");
  private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(NAIROBI);
  private static final Pattern KENYAN_MOBILE = Pattern.compile("^254[17]\\d{8}$");

  private Daraja() {
  }

  public static Optional<String> normalisePhone(String input) {
    if (input == null) {
      return Optional.empty();
    }
    String digits = input.replaceAll("[\\s()-]", "").replaceFirst("^\\+", "");
    String phone = digits;
    if (digits.matches("^0[17]\\d{8}$")) {
      phone = "254" + digits.substring(1);
    } else if (digits.matches("^[17]\\d{8}$")) {
      phone = "254" + digits;
    }
    return KENYAN_MOBILE.matcher(phone).matches() ? Optional.of(phone) : Optional.empty();
  }

  public static String timestamp(Instant instant) {
    return TIMESTAMP.format(instant);
  }

  public static String password(String shortcode, String passkey, String timestamp) {
    return Base64.getEncoder()
      .encodeToString((shortcode + passkey + timestamp).getBytes(StandardCharsets.UTF_8));
  }

  public static boolean tokensMatch(String given, String expected) {
    if (given == null || expected == null || expected.isEmpty()) {
      return false;
    }
    return MessageDigest.isEqual(given.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8));
  }

  public static Optional<MpesaCallbackResult> parseCallback(JsonNode body) {
    JsonNode callback = body == null ? null : body.path("Body").path("stkCallback");
    if (callback == null || !callback.path("CheckoutRequestID").isTextual() || !callback.path("ResultCode").canConvertToInt()) {
      return Optional.empty();
    }

    Long amount = null;
    String receipt = null;
    for (JsonNode item : callback.path("CallbackMetadata").path("Item")) {
      String name = item.path("Name").asText();
      JsonNode value = item.path("Value");
      if ("Amount".equals(name) && value.isNumber()) {
        amount = Math.round(value.asDouble());
      } else if ("MpesaReceiptNumber".equals(name) && !value.isMissingNode()) {
        receipt = value.asText();
      }
    }

    return Optional.of(new MpesaCallbackResult(
      callback.path("CheckoutRequestID").asText(),
      callback.path("ResultCode").asInt(),
      callback.path("ResultDesc").asText(""),
      amount,
      receipt
    ));
  }
}
