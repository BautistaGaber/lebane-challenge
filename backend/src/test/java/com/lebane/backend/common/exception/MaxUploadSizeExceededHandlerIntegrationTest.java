package com.lebane.backend.common.exception;

import com.lebane.backend.support.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link MaxUploadSizeExceededException} is raised by the servlet container while parsing the
 * multipart body, before the {@code DispatcherServlet} gets involved, so MockMvc cannot produce it
 * (the configured {@code spring.servlet.multipart.max-*} limits are not enforced there).
 *
 * <p>Instead of calling the handler directly, a throwaway controller throws the very same
 * exception so the request still travels through the real {@code DispatcherServlet} and the real
 * {@link GlobalExceptionHandler}. It shares the cached application context of the other integration
 * tests, so no extra context is started.
 */
@Import(MaxUploadSizeExceededHandlerIntegrationTest.TriggerController.class)
@DisplayName("MaxUploadSizeExceededException mapping")
class MaxUploadSizeExceededHandlerIntegrationTest extends AbstractIntegrationTest {

    private static final String TRIGGER_ENDPOINT = "/test/max-upload-size-exceeded";

    @Test
    @DisplayName("returns 400 with a dedicated message")
    void shouldMapMaxUploadSizeExceededToBadRequest() throws Exception {

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get(TRIGGER_ENDPOINT))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.estado").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.mensaje").value("The uploaded file exceeds the maximum allowed size"))
                .andExpect(jsonPath("$.ruta").value(TRIGGER_ENDPOINT))
                .andExpect(jsonPath("$.erroresValidacion", hasSize(0)));
    }

    @RestController
    static class TriggerController {

        @GetMapping(TRIGGER_ENDPOINT)
        void fail() {
            throw new MaxUploadSizeExceededException(10L * 1024 * 1024);
        }
    }
}