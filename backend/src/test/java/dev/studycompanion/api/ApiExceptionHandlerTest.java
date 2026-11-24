package dev.studycompanion.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import dev.studycompanion.api.dto.response.ApiErrorResponse;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

class ApiExceptionHandlerTest {
    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void reportsOversizedUploadAsPayloadTooLarge() {
        var response = handler.handleUploadTooLarge(new MaxUploadSizeExceededException(10L * 1024 * 1024));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(response.getBody()).isEqualTo(new ApiErrorResponse("Files must be 10 MB or smaller."));
    }

    @Test
    void reportsMalformedMultipartAsBadRequest() {
        var response = handler.handleMalformedMultipart(new MultipartException("Malformed multipart request"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isNotBlank();
    }
}
