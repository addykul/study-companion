package dev.studycompanion.application.model;

public record DocumentUploadCommand(String originalFileName, byte[] content) {
    public DocumentUploadCommand {
        content = content == null ? new byte[0] : content.clone();
    }

    public byte[] content() { return content.clone(); }
    public int contentSize() { return content.length; }
}
