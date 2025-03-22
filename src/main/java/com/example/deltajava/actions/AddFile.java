package com.example.deltajava.actions;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an action to add a file to the table.
 */
public class AddFile implements Action {
    @JsonProperty
    private String path;
    
    @JsonProperty
    private Map<String, String> partitionValues;
    
    @JsonProperty
    private long size;
    
    @JsonProperty
    private long modificationTime;
    
    @JsonProperty
    private boolean dataChange;
    
    @JsonProperty
    private Map<String, String> stats;
    
    @JsonProperty
    private String tags;
    
    /**
     * Default constructor for Jackson deserialization.
     */
    public AddFile() {
        this("", new HashMap<>(), 0L, 0L, true, new HashMap<>(), "");
    }
    
    /**
     * Full constructor.
     */
    public AddFile(String path, Map<String, String> partitionValues, long size, 
                  long modificationTime, boolean dataChange, 
                  Map<String, String> stats, String tags) {
        this.path = path;
        this.partitionValues = partitionValues;
        this.size = size;
        this.modificationTime = modificationTime;
        this.dataChange = dataChange;
        this.stats = stats;
        this.tags = tags;
    }
    
    /**
     * Simple constructor with essential fields.
     */
    public AddFile(String path, long size, long modificationTime) {
        this(path, new HashMap<>(), size, modificationTime, true, new HashMap<>(), "");
    }
    
    @Override
    public String getType() {
        return "add";
    }
    
    // Getters and Setters
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
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
    
    public long getModificationTime() {
        return modificationTime;
    }
    
    public void setModificationTime(long modificationTime) {
        this.modificationTime = modificationTime;
    }
    
    public boolean isDataChange() {
        return dataChange;
    }
    
    public void setDataChange(boolean dataChange) {
        this.dataChange = dataChange;
    }
    
    public Map<String, String> getStats() {
        return stats;
    }
    
    public void setStats(Map<String, String> stats) {
        this.stats = stats;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
} 