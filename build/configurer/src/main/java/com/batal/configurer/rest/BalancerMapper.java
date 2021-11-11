package com.batal.configurer.rest;

import com.batal.configurer.dtos.v1.balance.Action;
import com.batal.configurer.dtos.v1.balance.BalanceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.util.stream.Collectors.toList;

@Service
public class BalancerMapper {

    private final ConfigLoader loader;

    @Autowired
    public BalancerMapper(ConfigLoader loader) {
        this.loader = loader;
    }

    public BalanceConfig getConfig() {
        return new BalanceConfig(
                loader.getActions().stream()
                        .map(action -> new Action(
                                action.getId(),
                                action.getBalanceMethod(),
                                action.getRate()))
                        .collect(toList()));
    }
}
