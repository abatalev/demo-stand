package com.batal.actions.model.interfaces;

import java.time.LocalDateTime;

public interface GeneralAction {
    String getId();

    void run(LocalDateTime finishTime, int rate);
}
