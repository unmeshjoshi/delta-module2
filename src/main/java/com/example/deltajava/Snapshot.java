package com.example.deltajava;

import com.example.deltajava.actions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a snapshot of the Delta table at a specific version.
 * Contains the state of the table, including the active files and metadata.
 */
public class Snapshot {
    
    /** The DeltaLog this snapshot belongs to */
    private final DeltaLog deltaLog;
    
    /** The version of this snapshot */
    private final long version;
    
    /** Actions that were applied to create this snapshot */
    private final List<Action> actions;
    
    /** Metadata of the table in this snapshot */
    private Metadata metadata;
    
    /** Protocol version of the table in this snapshot */
    private Protocol protocol;
    
    /** Map of path to active files in this snapshot */
    private final Map<String, AddFile> activeFiles = new HashMap<>();
    
    /**
     * Creates a new snapshot of the table at the specified version.
     *
     * @param deltaLog the DeltaLog this snapshot belongs to
     * @param version the version of this snapshot
     * @param actions the actions that were applied to create this snapshot
     */
    public Snapshot(DeltaLog deltaLog, long version, List<Action> actions) {
        this.deltaLog = deltaLog;
        this.version = version;
        this.actions = new ArrayList<>(actions);
        
        // Initialize the snapshot state
        initializeState();
    }
    
    /**
     * Initializes the state of the snapshot by replaying the actions.
     */
    private void initializeState() {
        for (Action action : actions) {
            if (action instanceof AddFile) {
                AddFile addFile = (AddFile) action;
                activeFiles.put(addFile.getPath(), addFile);
            } else if (action instanceof RemoveFile) {
                RemoveFile removeFile = (RemoveFile) action;
                activeFiles.remove(removeFile.getPath());
            } else if (action instanceof Metadata) {
                metadata = (Metadata) action;
            } else if (action instanceof Protocol) {
                protocol = (Protocol) action;
            }
            // CommitInfo is ignored for state calculation
        }
    }
    
    /**
     * Gets the version of this snapshot.
     *
     * @return the version number
     */
    public long getVersion() {
        return version;
    }
    
    /**
     * Gets the metadata of the table in this snapshot.
     *
     * @return the metadata, or null if not set
     */
    public Metadata getMetadata() {
        return metadata;
    }
    
    /**
     * Gets the protocol version of the table in this snapshot.
     *
     * @return the protocol, or null if not set
     */
    public Protocol getProtocol() {
        return protocol;
    }
    
    /**
     * Gets all active files in this snapshot.
     *
     * @return a list of active files
     */
    public List<AddFile> getAllFiles() {
        return new ArrayList<>(activeFiles.values());
    }
    
    /**
     * Gets active files that match the specified predicate.
     *
     * @param predicate an optional predicate to filter files, or null to get all files
     * @return a list of matching files
     */
    public List<AddFile> getFiles(String predicate) {
        if (predicate == null || predicate.isEmpty()) {
            return getAllFiles();
        }
        
        // Simple filtering based on path matching for now
        // A real implementation would parse and evaluate predicates
        return activeFiles.values().stream()
                .filter(file -> file.getPath().contains(predicate))
                .collect(Collectors.toList());
    }
    
    /**
     * Gets the DeltaLog this snapshot belongs to.
     *
     * @return the DeltaLog
     */
    public DeltaLog getDeltaLog() {
        return deltaLog;
    }
    
    /**
     * Gets the actions that were applied to create this snapshot.
     *
     * @return the list of actions
     */
    public List<Action> getActions() {
        return new ArrayList<>(actions);
    }
    
    /**
     * Gets the active (not removed) AddFile actions from this snapshot.
     *
     * @return a list of active AddFile actions
     */
    public List<AddFile> getActiveFiles() {
        return activeFiles.values().stream()
                .collect(Collectors.toList());
    }
} 