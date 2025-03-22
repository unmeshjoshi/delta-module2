package com.example.deltajava.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for CSV file operations.
 * Provides methods to read records from and write records to CSV files.
 */
public class CsvUtil {
    
    private static final String DELIMITER = ",";
    private static final String QUOTE = "\"";
    private static final String ESCAPED_QUOTE = "\"\"";
    
    /**
     * Writes a list of records to a CSV file.
     *
     * @param records the records to write
     * @param filePath the path to the output file
     * @return the size of the file in bytes
     * @throws IOException if an I/O error occurs
     */
    public static long writeRecords(List<Map<String, String>> records, Path filePath) throws IOException {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        
        // Ensure the parent directory exists
        Files.createDirectories(filePath.getParent());
        
        // Get all column names
        List<String> columnNames = extractColumnNames(records);
        
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            // Write header
            writer.write(String.join(DELIMITER, columnNames));
            writer.newLine();
            
            // Write data rows
            for (Map<String, String> record : records) {
                List<String> values = new ArrayList<>();
                for (String column : columnNames) {
                    String value = record.getOrDefault(column, "");
                    values.add(escapeValue(value));
                }
                writer.write(String.join(DELIMITER, values));
                writer.newLine();
            }
        }
        
        // Return the file size
        return Files.size(filePath);
    }
    
    /**
     * Reads records from a CSV file.
     *
     * @param filePath the path to the CSV file
     * @return a list of records, where each record is a map of column names to values
     * @throws IOException if an I/O error occurs
     */
    public static List<Map<String, String>> readRecords(Path filePath) throws IOException {
        List<Map<String, String>> records = new ArrayList<>();
        
        if (!Files.exists(filePath)) {
            return records;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            // Read header
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return records;
            }
            
            List<String> columnNames = Arrays.asList(headerLine.split(DELIMITER));
            
            // Read data rows
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                List<String> values = splitCsvLine(line);
                Map<String, String> record = new HashMap<>();
                
                for (int i = 0; i < Math.min(columnNames.size(), values.size()); i++) {
                    record.put(columnNames.get(i), unescapeValue(values.get(i)));
                }
                
                records.add(record);
            }
        }
        
        return records;
    }
    
    /**
     * Extracts all column names from the records.
     *
     * @param records the records to extract column names from
     * @return a list of column names
     */
    private static List<String> extractColumnNames(List<Map<String, String>> records) {
        return records.stream()
                .flatMap(record -> record.keySet().stream())
                .distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * Splits a CSV line into values considering quoted values.
     * This is a simplified version and doesn't handle all edge cases.
     *
     * @param line the CSV line to split
     * @return a list of values
     */
    private static List<String> splitCsvLine(String line) {
        // This is a simplified implementation
        // A complete implementation would handle escaped quotes, etc.
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    current.append('\"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        values.add(current.toString());
        return values;
    }
    
    /**
     * Escapes a value for CSV output.
     *
     * @param value the value to escape
     * @return the escaped value
     */
    private static String escapeValue(String value) {
        if (value == null) {
            return "";
        }
        
        if (value.contains(DELIMITER) || value.contains(QUOTE) || value.contains("\n")) {
            return QUOTE + value.replace(QUOTE, ESCAPED_QUOTE) + QUOTE;
        }
        
        return value;
    }
    
    /**
     * Unescapes a value from CSV input.
     *
     * @param value the value to unescape
     * @return the unescaped value
     */
    private static String unescapeValue(String value) {
        if (value == null) {
            return "";
        }
        
        if (value.startsWith(QUOTE) && value.endsWith(QUOTE)) {
            // Remove the outer quotes and unescape any inner quotes
            return value.substring(1, value.length() - 1).replace(ESCAPED_QUOTE, QUOTE);
        }
        
        return value;
    }
} 