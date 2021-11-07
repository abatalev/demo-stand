package com.batal.actions.services;

import com.batal.actions.model.GeneralRetrieveAction;
import com.batal.actions.model.GeneralSenderAction;
import com.batal.actions.model.actions.StandardAction;
import com.batal.actions.model.config.ActionConfig;
import com.batal.actions.model.fetchers.DbFetcher;
import com.batal.actions.model.fetchers.MqFetcher;
import com.batal.actions.model.fetchers.RestFetcher;
import com.batal.actions.model.fixers.DbFixer;
import com.batal.actions.model.fixers.LogFixer;
import com.batal.actions.model.interfaces.*;
import com.batal.actions.model.savers.DbSaver;
import com.batal.actions.model.savers.MqSaver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ActionParser {

    private static final Logger log = LoggerFactory.getLogger(ActionParser.class);

    private final DbService dbService;
    private final JmsService jmsService;

    @Autowired
    public ActionParser(
            DbService dbService,
            JmsService jmsService
    ) {
        this.dbService = dbService;
        this.jmsService = jmsService;
    }

    public List<GeneralAction> parse(List<ActionConfig> actions) {
        List<GeneralAction> tmpActions = new ArrayList<>();
        for (ActionConfig action : actions) {
            switch (action.getName()) {
                case "mqdb":
                    tmpActions.add(new GeneralRetrieveAction(new StandardAction(
                            action.getId(),
                            injectSetters(new MqFetcher((String) action.getConfig().get("queueName"))),
                            injectSetters(new DbSaver((String) action.getConfig().get("tableName"))),
                            injectSetters(new LogFixer())
                    )));
                    break;
                case "dbmq":
                    tmpActions.add(new GeneralRetrieveAction(new StandardAction(
                            action.getId(),
                            injectSetters(new DbFetcher((String) action.getConfig().get("tableName"))),
                            injectSetters(new MqSaver((String) action.getConfig().get("queueName"))),
                            injectSetters(new DbFixer((String) action.getConfig().get("tableName")))
                    )));
                    break;
                case "custom":
                    tmpActions.add(new GeneralRetrieveAction(
                            new StandardAction(
                                    action.getId(),
                                    createFetcher(action),
                                    createSaver(action),
                                    createFixer(action))));
                    break;
                case "SenderRest":
                    tmpActions.add(new GeneralSenderAction(new StandardAction(
                            action.getId(),
                            injectSetters(new RestFetcher((String) action.getConfig().get("url"))),
                            injectSetters(new DbSaver((String) action.getConfig().get("tableName"))),
                            injectSetters(new DbFixer((String) action.getConfig().get("tableName")))
                    )));
                    break;
                default:
                    log.info("read config> " + action.getName() + " - error");
                    break;
            }
        }
        return tmpActions;
    }

    private Fixer createFixer(ActionConfig action) {
        Map cfg = (Map) action.getConfig().get("fixer");
        String objType = (String) cfg.get("type");
        switch (objType) {
            case "db":
                return injectSetters(new DbFixer((String) cfg.get("tableName")));
            case "std":
                return injectSetters(new LogFixer());
            default:
                log.info("unknown type of fixer {}", objType);
                return null;
        }
    }

    private Fetcher createFetcher(ActionConfig action) {
        Map cfg = (Map) action.getConfig().get("fetcher");
        String objType = (String) cfg.get("type");
        switch (objType) {
            case "rest":
                return injectSetters(new RestFetcher((String) cfg.get("url")));
            case "mq":
                return injectSetters(new MqFetcher((String) cfg.get("queueName")));
            case "db":
                return injectSetters(new DbFetcher((String) cfg.get("tableName")));
            default:
                log.info("unknown type of fetcher {}", objType);
                return null;
        }
    }

    private Saver createSaver(ActionConfig action) {
        Map cfg = (Map) action.getConfig().get("saver");
        String objType = (String) cfg.get("type");
        switch (objType) {
            case "mq":
                return injectSetters(new MqSaver((String) cfg.get("queueName")));
            case "db":
                return injectSetters(new DbSaver((String) cfg.get("tableName")));
            default:
                log.info("unknown type of saver {}", objType);
                return null;
        }
    }

    private <T> T injectSetters(T obj) {
        if (obj instanceof JmsServiceSetter) {
            ((JmsServiceSetter) obj).setJmsService(jmsService);
        }
        if (obj instanceof DbServiceSetter) {
            ((DbServiceSetter) obj).setDbService(dbService);
        }
        return obj;
    }
}
