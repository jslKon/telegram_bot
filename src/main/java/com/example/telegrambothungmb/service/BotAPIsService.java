package com.example.telegrambothungmb.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "convert", url = "https://free.currconv.com")
public interface BotAPIsService {

    @GetMapping(path = "/api/v7/convert")
    CurrencyRatio getRatio(@RequestParam("q") String q, @RequestParam("compact") String compact, @RequestParam("apiKey") String apiKey);
}
