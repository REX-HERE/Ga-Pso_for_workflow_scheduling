package org.maps.GA;

import org.maps.GA.Gene;

public class ScheduledTaskDetails {
    public Gene g;
    int start_time;
    public int end_time;

    public ScheduledTaskDetails(Gene g, int start_time, int end_time) {
        this.g = g;
        this.start_time = start_time;
        this.end_time = end_time;
    }
}
