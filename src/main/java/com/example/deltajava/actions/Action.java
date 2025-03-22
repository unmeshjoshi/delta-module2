package com.example.deltajava.actions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Base interface for actions that can be logged in transactions.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Protocol.class, name = "protocol"),
    @JsonSubTypes.Type(value = CommitInfo.class, name = "commitInfo"),
    @JsonSubTypes.Type(value = AddFile.class, name = "add"),
    @JsonSubTypes.Type(value = RemoveFile.class, name = "remove"),
    @JsonSubTypes.Type(value = Metadata.class, name = "metadata")
})
public interface Action {
    /**
     * Returns the action type.
     * 
     * @return the action type
     */
    String getType();
} 