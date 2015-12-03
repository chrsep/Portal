package com.directdev.portal.tools.event;

import com.directdev.portal.tools.model.Schedule;

public class RecyclerClickEvent {
    public final Schedule schedule;
    public RecyclerClickEvent(Schedule schedule){
        this.schedule = schedule;
    }
}
