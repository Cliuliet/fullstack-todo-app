package com.example.demo.dto;

public class StatsResponse {

    private long total;
    private long completed;
    private long active;
    private long highPriority;

    public StatsResponse(long total, long completed, long active, long highPriority) {
        this.total = total;
        this.completed = completed;
        this.active = active;
        this.highPriority = highPriority;
    }

    public long getTotal() { return total; }
    public long getCompleted() { return completed; }
    public long getActive() { return active; }
    public long getHighPriority() { return highPriority; }
}
