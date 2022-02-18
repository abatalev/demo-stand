package com.batal.actions.model.messages;

public class FileMessage extends Message {
    private String filename;

    public FileMessage(String id, String filename) {
        super(id, null);
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }
}
