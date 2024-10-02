/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdenvices.encoderdecoder.haivision.kraken.common.metric;

/**
 * Enum representing various stream.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 9/16/2024
 * @since 1.0.0
 */
public enum StreamInfoEnum {
    STREAM_ID("StreamID", "uuid"),
    STREAM_NAME("StreamName", "name"),
    INPUT_STREAMS("Input", "input"),
    AUTO_START("AutoStart", "auto_start"),
    PASSTHRU("Passthru", "passthru_uuid"),
    TRANSCODER("Transcoder", "transcoder"),
    OUTPUTS("Output", "outputs"),
    METADATA("Metadata", "metadatas"),
    MODE( "Mode","mode"),
    STATE( "State", "state"),
    STATUS("Status","status");

    private final String name;
    private final String field;

    /**
     * Constructor for DeviceInfoMetric.
     *
     * @param name The name representing the system information category.
     * @param field The field associated with the category.
     */
    StreamInfoEnum(String name, String field) {
        this.name = name;
        this.field = field;
    }

    /**
     * Retrieves {@link #name}
     *
     * @return value of {@link #name}
     */
    public String getName() {
        return name;
    }

    /**
     * Retrieves {@link #field}
     *
     * @return value of {@link #field}
     */
    public String getField() {
        return field;
    }
}
