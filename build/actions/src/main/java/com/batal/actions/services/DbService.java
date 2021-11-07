package com.batal.actions.services;

import com.batal.actions.model.Message;
import io.micrometer.core.annotation.Timed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Timed
public class DbService {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public DbService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    @Timed
    public Message getMessage(String tableName) {
        List<Message> messages = jdbcTemplate.query(
                "select id,payload from " + tableName +
                        " where status = 0" +
                        " for update skip locked",
                (rs, rsnum) -> new Message(
                        rs.getString("id"),
                        rs.getString("payload")));
        if (!messages.isEmpty()) {
            for (Message message : messages) {
                jdbcTemplate.update(
                        "update " + tableName +
                                " set status = 1 " +
                                " where id = ?",
                        new Object[]{message.getId()});
                return message;
            }
        }
        return null;
    }

    @Transactional
    public void fix(String tableName, String msgId, int code, String remark){
        jdbcTemplate.update("update " + tableName + " set status = ? where id = ?", code, msgId);
    }

    @Transactional
    public void put(String tableName, Message message){
        jdbcTemplate.update("insert into " + tableName + " (id,status,payload) values (?,?,?)",
                message.getId(), 0, message.getPayload());
    }
}
