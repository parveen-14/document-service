package com.educationloan.document.validate;

import com.educationloan.document.globalExceptionHandling.CustomException.InvalidFileException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
class DocumentValidatorTest {

    private final DocumentValidator validator = new DocumentValidator();

    @Test
    void validateFileType_validFile() throws Exception {
        log.info("TEST START: validFile");

        MultipartFile file = mock(MultipartFile.class);

        byte[] pdfBytes = "%PDF-1.4 test content".getBytes();

        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getInputStream())
                .thenReturn(new ByteArrayInputStream(pdfBytes));

        assertDoesNotThrow(() -> validator.validateFileType(file));

        log.info("TEST PASSED: valid file");
    }

    @Test
    void validateFileType_emptyFile() {

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        InvalidFileException ex = assertThrows(
                InvalidFileException.class,
                () -> validator.validateFileType(file));

        assertEquals("Uploaded file must not be empty", ex.getMessage());
    }

    @Test
    void validateFileType_fileTooLarge() {
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(6 * 1024 * 1024L);

        InvalidFileException ex = assertThrows(
                InvalidFileException.class,
                () -> validator.validateFileType(file));

        assertEquals("File size must be less than or equal to 5 MB", ex.getMessage());
    }

    @Test
    void validateFileType_invalidMimeType() throws Exception {

        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);

        // fake non-supported content
        when(file.getInputStream())
                .thenReturn(new ByteArrayInputStream("plain text data".getBytes()));

        assertThrows(InvalidFileException.class,
                () -> validator.validateFileType(file));
    }

    @Test
    void validateFileType_inputStreamFailure() throws Exception {
        MultipartFile file = mock(MultipartFile.class);

        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);

        when(file.getInputStream())
                .thenThrow(new RuntimeException("IO error"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> validator.validateFileType(file));

        assertTrue(ex.getMessage().contains("File validation failed"));
    }
}