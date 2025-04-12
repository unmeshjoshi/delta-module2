# DeltaTable with Atomic Writes and Transaction Log

This repo contains **Task 3** in building a Delta Lake-like storage layer. In this task, you will introduce a transaction log to manage data file consistency and implement atomic, versioned commits.

---

## 🎯 Goal

Enhance the `DeltaTable` by adding support for **atomic commits** using a **transaction log**. Each insert operation should now be tracked via structured metadata actions that are logged in a single versioned file.

---

## 💡 Motivation: Why Atomic Writes?

In the previous tasks, every `insert()` call wrote new Parquet files. However:

- What happens if the program crashes halfway through writing multiple files?
- How do we know which files belong to which insert?
- How do we **recover** the correct state?

To solve this, we:
- Generate **log entries** that describe what we wrote (called **Actions**).
- Store those log entries atomically in a versioned JSON file (like `00000000000000000001.json`).
- Make all file additions and deletions **explicit and versioned**.
- All the reads should only happen at a specific version of the latest snapshot

## Project Structure

```
src/main/java/com/example/deltajava/
├── actions/               # Action classes for the Delta log
│   ├── Action.java        # Base interface for all actions
│   ├── AddFile.java       # Adding files to the table
│   ├── CommitInfo.java    # Commit metadata
│   ├── Metadata.java      # Table metadata
│   ├── Protocol.java      # Protocol versioning 
│   └── RemoveFile.java    # Removing files from the table
├── util/
│   ├── JsonUtil.java      # JSON serialization utilities
│   ├── FileNames.java     # Delta file naming utilities
│   ├── CsvUtil.java       # CSV processing utilities
│   ├── ParquetUtil.java   # Parquet file handling
├── DeltaLog.java          # Core Delta log management
├── DeltaTable.java        # Table operations interface 
└── Snapshot.java          # Point-in-time view of the table
```


## Assignment

- Implement DeltaTable.readAll method to pass the test testReadRecordsAtAVersion