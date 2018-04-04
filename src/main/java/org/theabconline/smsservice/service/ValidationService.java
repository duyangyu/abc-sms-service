package org.theabconline.smsservice.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationService.class);

    @Value("${jdyun.webhook.secret}")
    String secret;

    public boolean isValid(String payload, String timestamp, String nonce, String sha1) {
        String signatureToken = nonce + ":" + payload + ":" + secret + ":" + timestamp;
        LOGGER.debug("Signature token: {}", signatureToken);
        String actualSignature = DigestUtils.sha1Hex(signatureToken);
        LOGGER.debug("actual signature: {}, expected signature: {}", actualSignature, sha1);

        return actualSignature.equals(sha1);
    }
}
