package com.example.deltajava.actions;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents table metadata information.
 */
public class Metadata implements Action {
    @JsonProperty
    private String id;
    
    @JsonProperty
    private String name;
    
    @JsonProperty
    private String description;
    
    @JsonProperty
    private String format;
    
    @JsonProperty
    private Map<String, String> configuration;
    
    @JsonProperty
    private Map<String, String> partitionColumns;
    
    @JsonProperty
    private long createdTime;
    
    /**
     * Default constructor for Jackson deserialization.
     */
    public Metadata() {
        this("", "", "", "", new HashMap<>(), new HashMap<>(), 0L);
    }
    
    /**
     * Full constructor.
     */
    public Metadata(String id, String name, String description, String format,
                   Map<String, String> configuration, Map<String, String> partitionColumns,
                   long createdTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.format = format;
        this.configuration = configuration;
        this.partitionColumns = partitionColumns;
        this.createdTime = createdTime;
    }
    
    /**
     * Simple constructor with essential fields.
     */
    public Metadata(String id, String name, String format) {
        this(id, name, "", format, new HashMap<>(), new HashMap<>(), System.currentTimeMillis());
    }
    
    /**
     * Static factory method to create a new Metadata instance.
     * 
     * @param id the table ID
     * @param name the table name
     * @param format the table format (e.g., "parquet")
     * @return the new Metadata instance
     */
    public static Metadata create(String id, String name, String format) {
        return new Metadata(id, name, format);
    }
    
    @Override
    public String getType() {
        return "metadata";
    }
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getFormat() {
        return format;
    }
    
    public void setFormat(String format) {
        this.format = format;
    }
    
    public Map<String, String> getConfiguration() {
        return configuration;
    }
    
    public void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration;
    }
    
    public Map<String, String> getPartitionColumns() {
        return partitionColumns;
    }
    
    public void setPartitionColumns(Map<String, String> partitionColumns) {
        this.partitionColumns = partitionColumns;
    }
    
    public long getCreatedTime() {
        return createdTime;
    }
    
    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }
} 