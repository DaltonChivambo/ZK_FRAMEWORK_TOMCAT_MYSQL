package org.example.model;

public class StatusCount {
    private final String status;
    private final long total;

    public StatusCount(String status, long total) {
        this.status = status;
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public long getTotal() {
        return total;
    }
}
