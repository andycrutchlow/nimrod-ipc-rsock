package com.nimrodtechs.rsock.test.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor

public class MarketDataRequest {

    public MarketDataRequest() {
        System.out.println("HERE");
    }

    public String getStock() {
        return stock;
    }

    private String stock;
}