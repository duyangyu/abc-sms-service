package org.theabconline.smsservice.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.theabconline.smsservice.service.RawMessageService;

@RequestMapping("/api")
@RestController
public class SMSResource {

    private final RawMessageService rawMessageService;

    @Autowired
    public SMSResource(RawMessageService rawMessageService) {
        this.rawMessageService = rawMessageService;
    }

    @RequestMapping(value = "/sms", method = RequestMethod.POST)
    public ResponseEntity sendMessage(@RequestParam String timestamp,
                                      @RequestParam String nonce,
                                      @RequestHeader(value = "X-JDY-Signature") String sha1,
                                      @RequestBody String message) {
        rawMessageService.saveMessage(message, timestamp, nonce, sha1);

        return ResponseEntity.ok().build();
    }
}
