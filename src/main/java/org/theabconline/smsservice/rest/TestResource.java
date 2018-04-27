package org.theabconline.smsservice.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.theabconline.smsservice.dto.SmsDTO;
import org.theabconline.smsservice.dto.SmsExceptionDTO;
import org.theabconline.smsservice.service.EmailService;
import org.theabconline.smsservice.service.LogService;

@RequestMapping("/api")
@RestController
public class TestResource {

    @Autowired
    LogService logService;

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public ResponseEntity sendMessage() {
        logService.logFailure(new SmsExceptionDTO(new SmsDTO("phone number", "template code", "params"), "error message"));

        return ResponseEntity.ok().build();
    }
}
