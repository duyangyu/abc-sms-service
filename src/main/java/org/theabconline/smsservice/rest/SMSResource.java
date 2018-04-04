package org.theabconline.smsservice.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.theabconline.smsservice.service.SMSService;

import java.io.IOException;

@RequestMapping("/api")
@RestController
public class SMSResource {

    @Autowired
    SMSService smsService;

    @RequestMapping(value = "/abc", method = RequestMethod.POST)
    public ResponseEntity sendMessage(@RequestParam String timestamp,
                                      @RequestParam String nonce,
                                      @RequestHeader(value = "X-JDY-Signature") String sha1,
                                      @RequestBody String message) throws IOException {
        smsService.send(message, timestamp, nonce, sha1);

        return ResponseEntity.ok().build();
    }
}
