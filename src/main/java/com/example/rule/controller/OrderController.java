package com.example.rule.controller;

import com.example.rule.model.Order;
import com.example.rule.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author minnxu
 */
@RestController
@RequestMapping(value = "/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/getDiscount")
    private ResponseEntity<Double> getDiscount(@RequestBody Order orderRequest) {
        double discountedAmount = orderService.getDiscountWithRuleEnabled(orderRequest);
        return ResponseEntity.ok().body(discountedAmount);
    }

    @PostMapping("/getDiscountOff")
    private ResponseEntity<Double> getDiscountOff(@RequestBody Order orderRequest) {
        double discountedAmount = orderService.getDiscountWithRuleDisabled(orderRequest);
        return ResponseEntity.ok().body(discountedAmount);
    }

}
