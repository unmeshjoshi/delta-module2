package com.example.deltajava.util;

/**
 * Utility class for formatting file names in the Delta log.
 */
public class FileNames {
    
    /**
     * The format for Delta log file names.
     */
    public static final String DELTA_FILE_FORMAT = "%020d.json";
    
    /**
     * Formats a version number into a Delta log file name.
     *
     * @param version the version number
     * @return the formatted file name
     */
    public static String deltaFile(long version) {
        return String.format(DELTA_FILE_FORMAT, version);
    }
    
    /**
     * Parses a version number from a Delta log file name.
     *
     * @param fileName the file name
     * @return the version number
     * @throws NumberFormatException if the file name is not a valid Delta log file name
     */
    public static long fileVersion(String fileName) {
        if (!fileName.endsWith(".json")) {
            throw new IllegalArgumentException("Not a JSON file: " + fileName);
        }
        return Long.parseLong(fileName.substring(0, fileName.length() - 5));
    }
} 