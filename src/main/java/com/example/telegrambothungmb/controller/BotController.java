package com.example.telegrambothungmb.controller;

import com.example.telegrambothungmb.service.BotAPIsService;
import com.example.telegrambothungmb.service.CurrencyRatio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BotController {

    @Autowired
    BotAPIsService botAPIsService;

    @GetMapping
    public CurrencyRatio test() {
        return botAPIsService.getRatio("USD_VND,EUR_VND", "ultra", "72605790ef0e9cce9fe8");
    }
}
