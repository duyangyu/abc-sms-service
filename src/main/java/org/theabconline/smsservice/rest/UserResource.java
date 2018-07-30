package org.theabconline.smsservice.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.theabconline.smsservice.service.UserService;

@RequestMapping("/api")
@RestController
public class UserResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserResource.class);

    private final UserService userService;

    @Autowired
    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public ResponseEntity createUser(@RequestParam String timestamp,
                                     @RequestParam String nonce,
                                     @RequestHeader(value = "X-JDY-Signature") String sha1,
                                     @RequestBody String message) {
        userService.createUser(message, timestamp, nonce, sha1);

        return ResponseEntity.ok().build();
    }

}
