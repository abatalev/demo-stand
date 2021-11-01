package com.batal.actions.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement
public class BalancerData {
    private Map<String, Integer> rates = new HashMap<>();

    public Map<String, Integer> getRates() {
        return rates;
    }

    public void setRates(Map<String, Integer> rates) {
        this.rates = rates;
    }
}
