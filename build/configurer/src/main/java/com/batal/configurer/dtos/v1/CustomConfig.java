package com.batal.configurer.dtos.v1;

public class CustomConfig {
    private Object saver;
    private Object fixer;
    private Object fetcher;

    public CustomConfig(Object fetcher, Object saver, Object fixer) {
        this.saver = saver;
        this.fixer = fixer;
        this.fetcher = fetcher;
    }

    public Object getSaver() {
        return saver;
    }

    public Object getFixer() {
        return fixer;
    }

    public Object getFetcher() {
        return fetcher;
    }
}
