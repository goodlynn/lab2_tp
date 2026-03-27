package com.example.photogallery.models;

public class ProgressStatus {
    private int current;
    private int total;
    private String message;

    public int getCurrent() { return current; }
    public void setCurrent(int current) { this.current = current; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    @Override
    public String toString() {
        return message;
    }
}