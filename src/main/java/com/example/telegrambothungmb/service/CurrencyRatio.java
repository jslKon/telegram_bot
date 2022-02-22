package com.example.telegrambothungmb.service;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
public class CurrencyRatio {

    private Map<String, String> ratios;

    @JsonAnySetter
    public void setMap(String key, String value){
        if(ratios == null){
            ratios = new HashMap<>();
        }
        ratios.put(key, value);
    }
}
