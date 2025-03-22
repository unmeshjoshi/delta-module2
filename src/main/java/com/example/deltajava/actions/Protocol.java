package com.example.deltajava.actions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.ArrayList;
import java.util.List;

/**
 * Protocol action that defines the minimum reader and writer versions.
 */
@JsonTypeName("protocol")
public class Protocol implements Action {
    
    private final int minReaderVersion;
    private final int minWriterVersion;
    private final List<String> readerFeatures;
    private final List<String> writerFeatures;
    
    /**
     * Default constructor for Jackson deserialization.
     */
    public Protocol() {
        this(1, 1, new ArrayList<>(), new ArrayList<>());
    }
    
    /**
     * Creates a new Protocol action.
     *
     * @param minReaderVersion the minimum reader version
     * @param minWriterVersion the minimum writer version
     */
    public Protocol(int minReaderVersion, int minWriterVersion) {
        this(minReaderVersion, minWriterVersion, new ArrayList<>(), new ArrayList<>());
    }
    
    /**
     * Creates a new Protocol action with specified features.
     *
     * @param minReaderVersion the minimum reader version
     * @param minWriterVersion the minimum writer version
     * @param readerFeatures list of reader features
     * @param writerFeatures list of writer features
     */
    public Protocol(
            int minReaderVersion,
            int minWriterVersion,
            List<String> readerFeatures,
            List<String> writerFeatures) {
        this.minReaderVersion = minReaderVersion;
        this.minWriterVersion = minWriterVersion;
        this.readerFeatures = readerFeatures != null ? readerFeatures : new ArrayList<>();
        this.writerFeatures = writerFeatures != null ? writerFeatures : new ArrayList<>();
    }
    
    /**
     * Gets the minimum reader version.
     *
     * @return the minimum reader version
     */
    @JsonProperty("minReaderVersion")
    public int getMinReaderVersion() {
        return minReaderVersion;
    }
    
    /**
     * Gets the minimum writer version.
     *
     * @return the minimum writer version
     */
    @JsonProperty("minWriterVersion")
    public int getMinWriterVersion() {
        return minWriterVersion;
    }
    
    /**
     * Gets the list of reader features.
     *
     * @return the reader features
     */
    @JsonProperty("readerFeatures")
    public List<String> getReaderFeatures() {
        return readerFeatures;
    }
    
    /**
     * Gets the list of writer features.
     *
     * @return the writer features
     */
    @JsonProperty("writerFeatures")
    public List<String> getWriterFeatures() {
        return writerFeatures;
    }
    
    @Override
    public String getType() {
        return "protocol";
    }
} 