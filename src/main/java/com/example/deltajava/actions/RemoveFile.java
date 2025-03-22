package com.example.deltajava.actions;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an action to remove a file from the table.
 */
public class RemoveFile implements Action {
    @JsonProperty
    private String path;
    
    @JsonProperty
    private long deletionTimestamp;
    
    @JsonProperty
    private boolean dataChange;
    
    @JsonProperty
    private Map<String, String> partitionValues;
    
    @JsonProperty
    private long size;
    
    /**
     * Default constructor for Jackson deserialization.
     */
    public RemoveFile() {
        this("", 0L, true, new HashMap<>(), 0L);
    }
    
    /**
     * Full constructor.
     */
    public RemoveFile(String path, long deletionTimestamp, boolean dataChange,
                      Map<String, String> partitionValues, long size) {
        this.path = path;
        this.deletionTimestamp = deletionTimestamp;
        this.dataChange = dataChange;
        this.partitionValues = partitionValues;
        this.size = size;
    }
    
    /**
     * Simple constructor with essential fields.
     */
    public RemoveFile(String path, long deletionTimestamp) {
        this(path, deletionTimestamp, true, new HashMap<>(), 0L);
    }
    
    @Override
    public String getType() {
        return "remove";
    }
    
    // Getters and Setters
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public long getDeletionTimestamp() {
        return deletionTimestamp;
    }
    
    public void setDeletionTimestamp(long deletionTimestamp) {
        this.deletionTimestamp = deletionTimestamp;
    }
    
    public boolean isDataChange() {
        return dataChange;
    }
    
    public void setDataChange(boolean dataChange) {
        this.dataChange = dataChange;
    }
    
    public Map<String, String> getPartitionValues() {
        return partitionValues;
    }
    
    public void setPartitionValues(Map<String, String> partitionValues) {
        this.partitionValues = partitionValues;
    }
    
    public long getSize() {
        return size;
    }
    
    public void setSize(long size) {
        this.size = size;
    }
} 