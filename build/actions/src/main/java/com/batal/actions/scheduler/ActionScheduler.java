package com.batal.actions.scheduler;

import com.batal.actions.services.ActionService;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ActionScheduler {
    private static final Logger log = LoggerFactory.getLogger(ActionScheduler.class);
    private final ActionService actionService;

    @Autowired
    public ActionScheduler(ActionService actionService) {
        this.actionService = actionService;
    }

    @Scheduled(fixedDelayString = "${actions.scheduler.delay}")
    @Timed
    public void runSchedule() {
        log.info("runSchedule");
        actionService.runActions();
    }
}
