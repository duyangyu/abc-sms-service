package org.theabconline.smsservice.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.theabconline.smsservice.dto.SmsStatusDTO;
import org.theabconline.smsservice.service.RecordService;
import org.theabconline.smsservice.service.SmsMessageService;

import java.util.List;

@RequestMapping("/api")
@RestController
public class SMSResource {

    private final RecordService recordService;

    private final SmsMessageService smsMessageService;

    @Autowired
    public SMSResource(RecordService recordService,
                       SmsMessageService smsMessageService) {
        this.recordService = recordService;
        this.smsMessageService = smsMessageService;
    }

    @RequestMapping(value = "/sms", method = RequestMethod.POST)
    public ResponseEntity sendMessage(@RequestParam String timestamp,
                                      @RequestParam String nonce,
                                      @RequestHeader(value = "X-JDY-Signature") String sha1,
                                      @RequestBody String message) {
        recordService.saveRawMessage(message, timestamp, nonce, sha1);

        return ResponseEntity.ok().build();
    }

    @RequestMapping(value = "/aliyun", method = RequestMethod.POST)
    public ResponseEntity createUser(@RequestBody List<SmsStatusDTO> smsStatusDTOList) {
        smsMessageService.handleCallback(smsStatusDTOList);

        return ResponseEntity.ok().body("{\"code\": 0}");
    }
}
