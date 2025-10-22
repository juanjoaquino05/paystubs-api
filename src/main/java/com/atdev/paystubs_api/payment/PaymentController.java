package com.atdev.paystubs_api.payment;

import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@NoArgsConstructor
public class PaymentController {

    @PostMapping(value = "/process")
    public ResponseEntity<?> process() throws Exception {
        return ResponseEntity.ok("test");
    }
}
