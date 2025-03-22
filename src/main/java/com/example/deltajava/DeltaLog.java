package com.example.deltajava;

import com.example.deltajava.actions.*;
import com.example.deltajava.util.FileNames;
import com.example.deltajava.util.JsonUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * DeltaLog is responsible for managing the transaction log of a Delta table.
 * It handles reading and writing log files, maintaining table state, and
 * providing the necessary infrastructure for optimistic concurrency control.
 */
public class DeltaLog {

    /**
     * Path to the table's log directory
     */
    private final Path logPath;

    /**
     * Path to the table's data directory
     */
    private final Path dataPath;

    /**
     * Lock for coordination of concurrent operations
     */
    private final ReentrantLock deltaLogLock = new ReentrantLock();

    /**
     * Current snapshot of the table state
     */
    private Snapshot currentSnapshot;
    /**
     * Creates a DeltaLog for the specified table path.
     *
     * @param tablePath the path to the Delta table
     */
    private DeltaLog(String tablePath) {
        Path tablePathObj = Paths.get(tablePath);
        this.logPath = tablePathObj.resolve("_delta_log");
        this.dataPath = tablePathObj.resolve("data");
        this.currentSnapshot = new Snapshot(this, -1, new ArrayList<>());
    }

    /**
     * Gets or creates a DeltaLog for the specified table path.
     *
     * @param tablePath the path to the Delta table
     * @return a DeltaLog instance
     */
    public static DeltaLog forTable(String tablePath) {
        return new DeltaLog(tablePath);
    }

    /**
     * Updates the snapshot to the latest version.
     *
     * @return the updated snapshot
     * @throws IOException if an I/O error occurs
     */
    public Snapshot update() throws IOException {
        try {
            deltaLogLock.lock();

            // Get the latest version
            long latestVersion = getLatestVersion();

            // If no change or no log files exist yet, return current snapshot
            if (latestVersion == currentSnapshot.getVersion()) {
                return currentSnapshot;
            }

            List<Action> allActions = new ArrayList<>();

            // No usable checkpoint, read all actions from all versions
            List<Long> versions = listVersions();
            for (Long version : versions) {
                allActions.addAll(readVersion(version));
            }

            // Create a new snapshot with the complete state
            currentSnapshot = new Snapshot(this, latestVersion, allActions);
            return currentSnapshot;
        } finally {
            deltaLogLock.unlock();
        }
    }

    /**
     * Reads the actions for a specific version.
     *
     * @param version the version to read
     * @return the list of actions for that version
     * @throws IOException if an I/O error occurs
     */
    public List<Action> readVersion(long version) throws IOException {
        List<Action> actions = new ArrayList<>();
        Path versionFile = logPath.resolve(FileNames.deltaFile(version));

        if (!Files.exists(versionFile)) {
            return actions;
        }

        List<String> lines = Files.readAllLines(versionFile);
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                Action action = JsonUtil.fromJson(line);
                actions.add(action);
            }
        }

        return actions;
    }

    /**
     * Gets the current snapshot of the table state.
     *
     * @return the current snapshot
     * @throws IOException if an I/O error occurs
     */
    public Snapshot snapshot() throws IOException {
        return update();
    }

    /**
     * Lists all versions in the log directory.
     *
     * @return a list of version numbers
     * @throws IOException if an I/O error occurs
     */
    public List<Long> listVersions() throws IOException {
        if (!Files.exists(logPath)) {
            return new ArrayList<>();
        }

        return Files.list(logPath)
                .filter(file -> file.getFileName().toString().matches("\\d{20}\\.json"))
                .map(file -> {
                    String fileName = file.getFileName().toString();
                    String versionStr = fileName.substring(0, fileName.indexOf('.'));
                    return Long.parseLong(versionStr);
                })
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Gets the latest version in the log.
     *
     * @return the latest version, or -1 if no versions exist
     * @throws IOException if an I/O error occurs
     */
    public long getLatestVersion() throws IOException {
        List<Long> versions = listVersions();
        return versions.isEmpty() ? -1 : versions.get(versions.size() - 1);
    }


    /**
     * Writes actions to a new version file.
     *
     * @param version the version to write
     * @param actions the actions to write
     * @throws IOException if an I/O error occurs
     */
    public void write(long version, List<Action> actions) throws IOException {
        try {
            deltaLogLock.lock();

            // Ensure log directory exists
            Files.createDirectories(logPath);

            // Create the log file for this version
            Path versionFile = logPath.resolve(FileNames.deltaFile(version));

            // Write all actions as JSON
            try (var writer = Files.newBufferedWriter(versionFile)) {
                for (Action action : actions) {
                    String json = JsonUtil.toJson(action);
                    writer.write(json);
                    writer.newLine();
                }
                writer.flush();
            }

            // Update the snapshot after writing
            update();

        } finally {
            deltaLogLock.unlock();
        }
    }

    /**
     * Gets the path to the table.
     *
     * @return the table path as a string
     */
    public String getTablePath() {
        return logPath.getParent().toString();
    }

    /**
     * Checks if the table exists (has at least one log file).
     *
     * @return true if the table exists, false otherwise
     */
    public boolean tableExists() {
        try {
            return Files.exists(logPath) && !listVersions().isEmpty();
        } catch (IOException e) {
            return false;
        }
    }
} 