package dev.studycompanion.application.exception;

public class AppException extends RuntimeException {
    private final Type type;

    public AppException(Type type, String message) {
        super(message);
        this.type = type;
    }

    public Type type() { return type; }

    public static AppException notFound(String message) { return new AppException(Type.NOT_FOUND, message); }
    public static AppException badRequest(String message) { return new AppException(Type.BAD_REQUEST, message); }
    public static AppException conflict(String message) { return new AppException(Type.CONFLICT, message); }
    public static AppException badGateway(String message) { return new AppException(Type.BAD_GATEWAY, message); }
    public static AppException serviceUnavailable(String message) {
        return new AppException(Type.SERVICE_UNAVAILABLE, message);
    }
    public static AppException internalServerError(String message) {
        return new AppException(Type.INTERNAL_SERVER_ERROR, message);
    }

    public enum Type {
        BAD_REQUEST, NOT_FOUND, CONFLICT, BAD_GATEWAY, SERVICE_UNAVAILABLE, INTERNAL_SERVER_ERROR
    }
}
