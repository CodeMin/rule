package com.example.rule.service;

import com.example.rule.model.Order;
import com.example.rule.model.OrderDiscount;

/**
 * @author minnxu
 */
public interface OrderService {

    double getDiscountWithRuleEnabled(Order order);

    double getDiscountWithRuleDisabled(Order order);

}
