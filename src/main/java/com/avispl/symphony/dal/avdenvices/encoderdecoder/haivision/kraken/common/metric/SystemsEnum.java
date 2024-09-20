package com.avispl.symphony.dal.avdenvices.encoderdecoder.haivision.kraken.common.metric;

public enum SystemsEnum {
    CURRENTTIME("Currenttime", "currenttime"),
    UPTIME("Uptime", "uptime"),
    VERSION("Version", "version"),
    ;
    private final String name;
    private final String field;

    /**
     * Constructor for RouteInfoMetric.
     *
     * @param name The name representing the system information category.
     * @param field The field associated with the category.
     */
    SystemsEnum(String name, String field) {
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
