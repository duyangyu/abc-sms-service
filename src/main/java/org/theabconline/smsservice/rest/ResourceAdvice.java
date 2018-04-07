package org.theabconline.smsservice.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

@ControllerAdvice(basePackageClasses = SMSResource.class)
public class ResourceAdvice {

//    @ExceptionHandler({IOException.class, RuntimeException.class, Exception.class})
    public ResponseEntity errorHandler() {
        return ResponseEntity.badRequest().build();
    }
}
