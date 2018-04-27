package org.theabconline.smsservice.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.theabconline.smsservice.service.EmailService;

@RequestMapping("/api")
@RestController
public class TestResource {

    @Autowired
    EmailService emailService;

    @RequestMapping(value = "/test", method = RequestMethod.GET)
    public ResponseEntity sendMessage() {
        emailService.send("test email", "lalalala");

        return ResponseEntity.ok().build();
    }
}
