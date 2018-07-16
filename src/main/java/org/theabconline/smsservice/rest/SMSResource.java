package org.theabconline.smsservice.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.theabconline.smsservice.service.SMSService;

@RequestMapping("/api")
@RestController
public class SMSResource {

    private final SMSService smsService;

    @Autowired
    public SMSResource(SMSService smsService) {
        this.smsService = smsService;
    }

    @RequestMapping(value = "/sms", method = RequestMethod.POST)
    public ResponseEntity sendMessage(@RequestParam String timestamp,
                                      @RequestParam String nonce,
                                      @RequestHeader(value = "X-JDY-Signature") String sha1,
                                      @RequestBody String message) {
//        smsService.send(message, timestamp, nonce, sha1);
        System.out.println(message);
        return ResponseEntity.ok().build();
    }
}
