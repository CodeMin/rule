package com.example.rule.service;

import com.example.rule.core.DroolsManager;
import com.example.rule.model.Order;
import com.example.rule.model.OrderDiscount;
import com.example.rule.model.Rule;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author minnxu
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private DroolsManager droolsManager;

    public double getDiscountWithRuleEnabled(Order order) {
        String dslContent = "package test;\n"
                + "import com.example.rule.model.Order;\n"
                + "global com.example.rule.model.OrderDiscount $orderDiscount;\n"
                + "rule \"Order Discount\"\n"
                + "  enabled true\n"
                + "  when\n"
                + "    $order: Order(customer.name contains \"Min\")\n"
                + "  then"
                + "    $order.setAmount($order.getAmount() * 0.5);\n"
//                + "    $orderDiscount.setAmount($order.getAmount());\n"
                + "end";
        Rule rule = Rule.of(123L, "Order Discount", dslContent);
        droolsManager.createOrUpdateRules(Arrays.asList(rule));

        OrderDiscount orderDiscount = new OrderDiscount();
        Map<String, Object> globalMap = new HashMap<>();
        globalMap.put("orderDiscount", orderDiscount);
        droolsManager.setGlobals(globalMap);

        droolsManager.execute(order);
        return order.getAmount();
    }

    public double getDiscountWithRuleDisabled(Order order) {
        String dslContent = "package test;\n"
                + "import com.example.rule.model.Order;\n"
                + "global com.example.rule.model.OrderDiscount $orderDiscount;\n"
                + "rule \"Order Discount\"\n"
                + "  enabled false\n"
                + "  when\n"
                + "    $order: Order(customer.name contains \"Min\")\n"
                + "  then"
                + "    $order.setAmount($order.getAmount() * 0.5);\n"
//                + "    $orderDiscount.setAmount($order.getAmount());\n"
                + "end";
        Rule rule = Rule.of(123L, "Order Discount", dslContent);
        droolsManager.createOrUpdateRules(Arrays.asList(rule));

        OrderDiscount orderDiscount = new OrderDiscount();
        Map<String, Object> globalMap = new HashMap<>();
        globalMap.put("orderDiscount", orderDiscount);
        droolsManager.setGlobals(globalMap);

        droolsManager.execute(order);
        return order.getAmount();
    }

}
