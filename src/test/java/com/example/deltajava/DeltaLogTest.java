package com.example.deltajava;

import com.example.deltajava.actions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the DeltaLog class.
 */
public class DeltaLogTest {
    
    @TempDir
    Path tempDir;
    
    private String tablePath;
    private DeltaLog deltaLog;
    
    @BeforeEach
    void setUp() {
        tablePath = tempDir.resolve("test_delta_log").toString();
        deltaLog = DeltaLog.forTable(tablePath);
    }
    
    @AfterEach
    void tearDown() {
        // Clean up if needed
    }
    
    @Test
    void testInitialization() throws IOException {
        // Verify that the DeltaLog initializes correctly
        assertNotNull(deltaLog, "DeltaLog should not be null");
        assertEquals(-1, deltaLog.getLatestVersion(), "Initial version should be -1 for a new table");
        assertFalse(deltaLog.tableExists(), "Table should not exist yet");
    }
    
    @Test
    void testWriteAndReadVersion() throws IOException {
        // Create some test actions
        List<Action> actions = new ArrayList<>();
        actions.add(new Protocol());
        actions.add(new Metadata(UUID.randomUUID().toString(), "Test Table", "csv"));
        
        // Write the actions as version 0
        deltaLog.write(0, actions);
        
        // Verify that the table now exists
        assertTrue(deltaLog.tableExists(), "Table should exist after writing");
        assertEquals(0, deltaLog.getLatestVersion(), "Latest version should be 0");
        
        // Read back the actions
        List<Action> readActions = deltaLog.readVersion(0);
        
        // Verify the actions
        assertEquals(actions.size(), readActions.size(), "Should read back the same number of actions");
        assertTrue(readActions.get(0) instanceof Protocol, "First action should be Protocol");
        assertTrue(readActions.get(1) instanceof Metadata, "Second action should be Metadata");
    }
    
    @Test
    void testMultipleVersions() throws IOException {
        // Write version 0
        List<Action> actions0 = new ArrayList<>();
        actions0.add(new Protocol());
        actions0.add(new Metadata(UUID.randomUUID().toString(), "Test Table", "csv"));
        deltaLog.write(0, actions0);
        
        // Write version 1
        List<Action> actions1 = new ArrayList<>();
        actions1.add(new AddFile("data/file1.csv", 100, System.currentTimeMillis()));
        deltaLog.write(1, actions1);
        
        // Write version 2
        List<Action> actions2 = new ArrayList<>();
        actions2.add(new AddFile("data/file2.csv", 200, System.currentTimeMillis()));
        deltaLog.write(2, actions2);
        
        // Verify the latest version
        assertEquals(2, deltaLog.getLatestVersion(), "Latest version should be 2");
        
        // Verify that we can list all versions
        List<Long> versions = deltaLog.listVersions();
        assertEquals(3, versions.size(), "Should have 3 versions");
        assertTrue(versions.contains(0L), "Versions should contain 0");
        assertTrue(versions.contains(1L), "Versions should contain 1");
        assertTrue(versions.contains(2L), "Versions should contain 2");
        
        // Verify that we can read each version
        assertEquals(2, deltaLog.readVersion(0).size(), "Version 0 should have 2 actions");
        assertEquals(1, deltaLog.readVersion(1).size(), "Version 1 should have 1 action");
        assertEquals(1, deltaLog.readVersion(2).size(), "Version 2 should have 1 action");
    }
    
    @Test
    void testSnapshot() throws IOException {
        // Write version 0 with initial actions
        List<Action> actions0 = new ArrayList<>();
        actions0.add(new Protocol());
        actions0.add(new Metadata(UUID.randomUUID().toString(), "Test Table", "csv"));
        deltaLog.write(0, actions0);
        
        // Write version 1 with a file addition
        List<Action> actions1 = new ArrayList<>();
        actions1.add(new AddFile("data/file1.csv", 100, System.currentTimeMillis()));
        deltaLog.write(1, actions1);
        
        // Get a snapshot
        Snapshot snapshot = deltaLog.snapshot();
        
        // Verify the snapshot
        assertNotNull(snapshot, "Snapshot should not be null");
        assertEquals(1, snapshot.getVersion(), "Snapshot version should be 1");
        
        // Verify the snapshot state
        assertNotNull(snapshot.getProtocol(), "Snapshot should have protocol");
        assertNotNull(snapshot.getMetadata(), "Snapshot should have metadata");
        assertEquals(1, snapshot.getAllFiles().size(), "Snapshot should have 1 file");
        
        // Write version 2 with a file removal
        List<Action> actions2 = new ArrayList<>();
        actions2.add(new RemoveFile("data/file1.csv", System.currentTimeMillis()));
        deltaLog.write(2, actions2);
        
        // Update the snapshot
        snapshot = deltaLog.update();
        
        // Verify the updated snapshot
        assertEquals(2, snapshot.getVersion(), "Updated snapshot version should be 2");
        assertEquals(0, snapshot.getAllFiles().size(), "Updated snapshot should have 0 files");
    }
} 