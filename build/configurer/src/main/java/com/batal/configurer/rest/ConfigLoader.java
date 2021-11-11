package com.batal.configurer.rest;

import com.batal.configurer.dtos.v1.*;
import com.batal.configurer.model.Action;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Arrays.asList;

@Service
public class ConfigLoader {

    public List<Action> getActions() {
        return asList(
                new Action("a1", "mqdb", "fixed", 5,
                        new MqDbConfig("TBL1", "DEV.QUEUE.1")),
                new Action("a2", "dbmq", "fixed", 5,
                        new MqDbConfig("TBL1", "DEV.QUEUE.1")),
                new Action("yaru", "custom", "fixed", 5,
                        new CustomConfig(
                                new RestConfig("http://ya.ru"),
                                new MqConfig("DEV.QUEUE.1"),
                                new StdConfig()
                        )),
                new Action("googlecom", "custom", "fixed", 5,
                        new CustomConfig(
                                new RestConfig("http://google.com"),
                                new DbConfig("TBL1"),
                                new StdConfig()
                        )));
    }
}
