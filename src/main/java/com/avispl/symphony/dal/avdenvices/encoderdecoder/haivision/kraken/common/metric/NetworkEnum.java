/*
 *  Copyright (c) 2024 AVI-SPL, Inc. All Rights Reserved.
 */

package com.avispl.symphony.dal.avdenvices.encoderdecoder.haivision.kraken.common.metric;

/**
 * Enum representing various network.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 9/16/2024
 * @since 1.0.0
 */
public enum NetworkEnum {
    DEFAULT_INTERFACE("DefaultInterface", "defaultInterface"),
    DNS_SERVER_0("DNSServer0", "dnsServer-0"),
    HOSTNAME("Hostname", "hostname"),
    IP_FORWARD("IPForward", "ipForward"),
    NTP_ADDRESS("NTPAddress", "ntpAddress"),
    SUBNET_MASK("SubnetMask", "subnetMask"),
    ;
    private final String name;
    private final String field;

    /**
     * Constructor for RouteInfoMetric.
     *
     * @param name The name representing the system information category.
     * @param field The field associated with the category.
     */
    NetworkEnum(String name, String field) {
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
