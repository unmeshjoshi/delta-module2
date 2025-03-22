package com.example.deltajava.actions;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents commit metadata information.
 */
public class CommitInfo implements Action {
    @JsonProperty
    private String version;
    
    @JsonProperty
    private long timestamp;
    
    @JsonProperty
    private String operation;
    
    @JsonProperty
    private Map<String, String> operationParameters;
    
    @JsonProperty
    private String commitVersion;
    
    /**
     * Default constructor for Jackson deserialization.
     */
    public CommitInfo() {
        this("", 0L, "", new HashMap<>(), "");
    }
    
    /**
     * Full constructor.
     */
    public CommitInfo(String version, long timestamp, String operation, 
                      Map<String, String> operationParameters, String commitVersion) {
        this.version = version;
        this.timestamp = timestamp;
        this.operation = operation;
        this.operationParameters = operationParameters;
        this.commitVersion = commitVersion;
    }
    
    /**
     * Creates a CommitInfo with current timestamp.
     */
    public static CommitInfo create(String operation) {
        return new CommitInfo(
            "1",
            Instant.now().toEpochMilli(),
            operation,
            new HashMap<>(),
            "1"
        );
    }
    
    @Override
    public String getType() {
        return "commitInfo";
    }
    
    // Getters and Setters
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public void setOperation(String operation) {
        this.operation = operation;
    }
    
    public Map<String, String> getOperationParameters() {
        return operationParameters;
    }
    
    public void setOperationParameters(Map<String, String> operationParameters) {
        this.operationParameters = operationParameters;
    }
    
    public String getCommitVersion() {
        return commitVersion;
    }
    
    public void setCommitVersion(String commitVersion) {
        this.commitVersion = commitVersion;
    }
    
    /**
     * Adds an operation parameter.
     */
    public CommitInfo withParameter(String key, String value) {
        this.operationParameters.put(key, value);
        return this;
    }
} 