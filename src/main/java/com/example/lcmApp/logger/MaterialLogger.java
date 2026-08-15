package com.example.lcmApp.logger;

import com.example.lcmApp.dto.LogEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.io.BufferedReader;
import java.util.*;

@Component
public class MaterialLogger {

    private static final Path LOG_PATH = Paths.get("logs/material-operations.json");
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault());
    private final ObjectMapper objectMapper;

    public MaterialLogger() {
        this.objectMapper = new ObjectMapper();
        try {
            Files.createDirectories(LOG_PATH.getParent());
        } catch (IOException ignored) {}
    }

    public void logMaterialAddition(String materialName, double quantity, String unit, String user) {
        LogEntry entry = new LogEntry(
                "ADDED",
                materialName,
                quantity,
                unit,
                user,
                FORMATTER.format(Instant.now())
        );
        writeEntry(entry);
    }

    public void logMaterialWriteOff(String materialName, double quantity, String unit, String user) {
        LogEntry entry = new LogEntry(
                "ISSUE",
                materialName,
                quantity,
                unit,
                user,
                FORMATTER.format(Instant.now())
        );
        writeEntry(entry);
    }

    private void writeEntry(LogEntry entry) {
        try {
            String jsonLine = objectMapper.writeValueAsString(entry) + System.lineSeparator();
            // Записываем в файл с добавлением (append)
            Files.write(LOG_PATH, jsonLine.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            // Логируем ошибку через стандартный SLF4J, чтобы не потерять
            org.slf4j.LoggerFactory.getLogger(MaterialLogger.class)
                    .error("Не удалось записать лог в JSON: {}", e.getMessage());
        }
    }

    // Метод для чтения всех записей из JSON-файла
    public static List<LogEntry> readAllLogs() throws IOException {
        if (!Files.exists(LOG_PATH)) {
            return List.of();
        }
        List<LogEntry> entries = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(LOG_PATH)) {
            String line;
            ObjectMapper mapper = new ObjectMapper();
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                LogEntry entry = mapper.readValue(line, LogEntry.class);
                entries.add(entry);
            }
        }
        return entries;
    }
    
    public static void clearLogs() {
    final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MaterialLogger.class);
    try {
        if (Files.exists(LOG_PATH)) {
            String absolutePath = LOG_PATH.toAbsolutePath().toString();
            Files.delete(LOG_PATH);
            logger.info("Лог операций с материалами очищен. Файл: {}", absolutePath);
        } else {
            logger.debug("Файл логов не найден, очистка не требуется: {}", LOG_PATH);
        }
    } catch (IOException e) {
        logger.error("Не удалось очистить лог операций с материалами: {}", e.getMessage(), e);
    }
  }

}