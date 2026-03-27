package com.example.photogallery.models;

public class SimpleProgressBuilder implements ProgressBuilder {
    private final ProgressStatus status;

    public SimpleProgressBuilder() {
        this.status = new ProgressStatus();
    }

    @Override
    public void setCurrent(int current) {
        status.setCurrent(current);
    }

    @Override
    public void setTotal(int total) {
        status.setTotal(total);
    }

    @Override
    public void buildMessage() {
        int current = status.getCurrent();
        int total = status.getTotal();
        status.setMessage(String.format("%d из %d", current, total));
    }

    @Override
    public ProgressStatus getResult() {
        return status;
    }
}