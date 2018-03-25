package org.theabconline.smsservice.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api")
@RestController
public class SMSResource {

    @RequestMapping(value = "/abc", method = RequestMethod.GET)
    public String abc() {
        return "Hello World";
    }
}
