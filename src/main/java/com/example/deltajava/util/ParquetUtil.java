package com.example.deltajava.util;

import com.example.deltajava.actions.Action;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for reading and writing Parquet files.
 * Used primarily for checkpoint operations in Delta Lake.
 */
public class ParquetUtil {

    /**
     * Writes a list of records to a Parquet file.
     *
     * @param records the records to write
     * @param filePath the path to write to
     * @return the size of the file in bytes
     * @throws IOException if an I/O error occurs
     */
    public static long writeRecords(List<Map<String, String>> records, java.nio.file.Path filePath) throws IOException {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        
        // Ensure the parent directory exists
        java.nio.file.Files.createDirectories(filePath.getParent());
        
        // Get all column names
        List<String> columnNames = extractColumnNames(records);
        
        // Create an Avro schema for the records
        Schema schema = createRecordsSchema(columnNames);
        
        // Create a Hadoop Path from the Java Path
        Path hadoopPath = new Path(filePath.toString());
        
        // Configure Hadoop to overwrite existing files
        Configuration conf = new Configuration();
        conf.setBoolean("dfs.support.append", true);
        conf.set("fs.file.impl", "org.apache.hadoop.fs.LocalFileSystem");
        conf.setBoolean("mapreduce.fileoutputcommitter.marksuccessfuljobs", false);
        
        // Delete the file if it exists to avoid FileAlreadyExistsException
        FileSystem fs = FileSystem.get(conf);
        if (fs.exists(hadoopPath)) {
            fs.delete(hadoopPath, true);
        }
        
        // Initialize the Parquet writer
        try (ParquetWriter<GenericRecord> writer = AvroParquetWriter
                .<GenericRecord>builder(hadoopPath)
                .withSchema(schema)
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .withConf(conf)
                .build()) {
            
            // Convert each record to an Avro record and write it
            for (Map<String, String> record : records) {
                GenericRecord avroRecord = convertMapToRecord(record, schema);
                writer.write(avroRecord);
            }
        }
        
        // Return the file size
        return java.nio.file.Files.size(filePath);
    }
    
    /**
     * Reads records from a Parquet file.
     *
     * @param filePath the path to read from
     * @return a list of records, where each record is a map of column names to values
     * @throws IOException if an I/O error occurs
     */
    public static List<Map<String, String>> readRecords(java.nio.file.Path filePath) throws IOException {
        if (!java.nio.file.Files.exists(filePath)) {
            return new ArrayList<>();
        }
        
        List<Map<String, String>> records = new ArrayList<>();
        
        // Create a Hadoop Path from the Java Path
        Path hadoopPath = new Path(filePath.toString());
        
        // Initialize the Parquet reader
        try (ParquetReader<GenericRecord> reader = AvroParquetReader
                .<GenericRecord>builder(hadoopPath)
                .withConf(new Configuration())
                .build()) {
            
            GenericRecord record;
            while ((record = reader.read()) != null) {
                Map<String, String> map = convertRecordToMap(record);
                records.add(map);
            }
        }
        
        return records;
    }
    
    /**
     * Creates an Avro schema for records based on column names.
     *
     * @param columnNames the list of column names
     * @return the schema
     */
    private static Schema createRecordsSchema(List<String> columnNames) {
        StringBuilder fieldsJson = new StringBuilder();
        
        for (int i = 0; i < columnNames.size(); i++) {
            fieldsJson.append("{\"name\": \"")
                      .append(columnNames.get(i))
                      .append("\", \"type\": [\"null\", \"string\"], \"default\": null}");
            
            if (i < columnNames.size() - 1) {
                fieldsJson.append(",\n");
            }
        }
        
        String schemaJson = 
            "{\"namespace\": \"com.example.deltajava\",\n" +
            " \"type\": \"record\",\n" +
            " \"name\": \"RecordData\",\n" +
            " \"fields\": [\n" +
            fieldsJson.toString() +
            " ]\n" +
            "}";
        
        return new Schema.Parser().parse(schemaJson);
    }
    
    /**
     * Converts a Map to an Avro GenericRecord.
     *
     * @param map the map to convert
     * @param schema the Avro schema to use
     * @return the record
     */
    private static GenericRecord convertMapToRecord(Map<String, String> map, Schema schema) {
        GenericRecord record = new GenericData.Record(schema);
        
        for (Schema.Field field : schema.getFields()) {
            String fieldName = field.name();
            String value = map.get(fieldName);
            if (value != null) {
                record.put(fieldName, value);
            } else {
                record.put(fieldName, null);
            }
        }
        
        return record;
    }
    
    /**
     * Converts an Avro GenericRecord to a Map.
     *
     * @param record the record to convert
     * @return the map
     */
    private static Map<String, String> convertRecordToMap(GenericRecord record) {
        Map<String, String> map = new HashMap<>();
        
        for (Schema.Field field : record.getSchema().getFields()) {
            String fieldName = field.name();
            Object value = record.get(fieldName);
            if (value != null) {
                map.put(fieldName, value.toString());
            }
        }
        
        return map;
    }
    
    /**
     * Extracts all column names from the records.
     *
     * @param records the records to extract column names from
     * @return a list of column names
     */
    private static List<String> extractColumnNames(List<Map<String, String>> records) {
        return records.stream()
                .flatMap(record -> record.keySet().stream())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Writes a list of actions to a Parquet file.
     *
     * @param actions the actions to write
     * @param filePath the path to write to
     * @throws IOException if an I/O error occurs
     */
    public static void writeActions(List<Action> actions, java.nio.file.Path filePath) throws IOException {
        // Create an Avro schema for the Action class
        Schema schema = createActionSchema();
        
        // Create a Hadoop Path from the Java Path
        Path hadoopPath = new Path(filePath.toString());
        
        // Configure Hadoop to overwrite existing files
        Configuration conf = new Configuration();
        conf.setBoolean("dfs.support.append", true);
        conf.set("fs.file.impl", "org.apache.hadoop.fs.LocalFileSystem");
        conf.setBoolean("mapreduce.fileoutputcommitter.marksuccessfuljobs", false);
        
        // Delete the file if it exists to avoid FileAlreadyExistsException
        FileSystem fs = FileSystem.get(conf);
        if (fs.exists(hadoopPath)) {
            fs.delete(hadoopPath, true);
        }
        
        // Initialize the Parquet writer
        try (ParquetWriter<GenericRecord> writer = AvroParquetWriter
                .<GenericRecord>builder(hadoopPath)
                .withSchema(schema)
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .withConf(conf)
                .build()) {
            
            // Convert each action to an Avro record and write it
            for (Action action : actions) {
                GenericRecord record = convertActionToRecord(action, schema);
                writer.write(record);
            }
        }
    }

    /**
     * Reads actions from a Parquet file.
     *
     * @param filePath the path to read from
     * @return a list of actions
     * @throws IOException if an I/O error occurs
     */
    public static List<Action> readActions(java.nio.file.Path filePath) throws IOException {
        // Create a Hadoop Path from the Java Path
        Path hadoopPath = new Path(filePath.toString());
        
        List<Action> actions = new ArrayList<>();
        
        // Initialize the Parquet reader
        try (ParquetReader<GenericRecord> reader = AvroParquetReader
                .<GenericRecord>builder(hadoopPath)
                .withConf(new Configuration())
                .build()) {
            
            GenericRecord record;
            while ((record = reader.read()) != null) {
                Action action = convertRecordToAction(record);
                actions.add(action);
            }
        }
        
        return actions;
    }

    /**
     * Creates an Avro schema for the Action class.
     *
     * @return the schema
     */
    private static Schema createActionSchema() {
        // For simplicity, we'll serialize the action as JSON within a record
        // In a real implementation, you'd create a proper Avro schema that matches
        // all the different action types
        String schemaJson = 
            "{\"namespace\": \"com.example.deltajava\",\n" +
            " \"type\": \"record\",\n" +
            " \"name\": \"ActionRecord\",\n" +
            " \"fields\": [\n" +
            "     {\"name\": \"actionType\", \"type\": \"string\"},\n" +
            "     {\"name\": \"actionJson\", \"type\": \"string\"}\n" +
            " ]\n" +
            "}";
        
        return new Schema.Parser().parse(schemaJson);
    }

    /**
     * Converts an Action to an Avro GenericRecord.
     *
     * @param action the action to convert
     * @param schema the Avro schema to use
     * @return the record
     */
    private static GenericRecord convertActionToRecord(Action action, Schema schema) {
        GenericRecord record = new GenericData.Record(schema);
        record.put("actionType", action.getClass().getSimpleName());
        record.put("actionJson", JsonUtil.toJson(action));
        return record;
    }

    /**
     * Converts an Avro GenericRecord to an Action.
     *
     * @param record the record to convert
     * @return the action
     * @throws IOException if the record cannot be converted
     */
    private static Action convertRecordToAction(GenericRecord record) throws IOException {
        String actionType = record.get("actionType").toString();
        String actionJson = record.get("actionJson").toString();
        return JsonUtil.fromJson(actionJson);
    }
} 