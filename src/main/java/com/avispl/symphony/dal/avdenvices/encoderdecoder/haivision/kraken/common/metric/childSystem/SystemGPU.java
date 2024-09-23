/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdenvices.encoderdecoder.haivision.kraken.common.metric.childSystem;

/**
 * Enum representing various system GPUs.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 9/16/2024
 * @since 1.0.0
 */
public enum SystemGPU {
    SYS_GPU_VIDEO_CLOCK("SystemGPUsVideoClock", "videoClock"),
    SYS_GPU_TEMP("SystemGPUsTemperature", "temperature"),
    SYS_GPU_NAME("SystemGPUsName", "name"),
    SYS_GPU_SERIAL("SystemGPUsSerial", "serial"),
    SYS_GPU_MEM_USED("SystemGPUsMemoryUsed", "memoryUsed"),
    SYS_GPU_MEM_TOTAL("SystemGPUsMemoryTotal", "memoryTotal"),
    ;
    private final String name;
    private final String field;

    /**
     * Constructor for RouteInfoMetric.
     *
     * @param name The name representing the system information category.
     * @param field The field associated with the category.
     */
    SystemGPU(String name, String field) {
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
