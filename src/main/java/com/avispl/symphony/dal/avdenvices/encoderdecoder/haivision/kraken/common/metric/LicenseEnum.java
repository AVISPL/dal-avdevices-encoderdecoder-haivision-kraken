package com.avispl.symphony.dal.avdenvices.encoderdecoder.haivision.kraken.common.metric;

/**
 * Enum representing various license.
 *
 * @author Harry / Symphony Dev Team<br>
 * Created on 9/16/2024
 * @since 1.0.0
 */
public enum LicenseEnum {
    ACTIVE_BYPASS_SESSION("ActiveBypassSessions", "active_bypass_sessions"),
    ACTIVE_TRANSCODER_SESSIONS("ActiveTranscoderSessions", "active_transcoder_sessions"),
    CREATION_DATE("CreationDate", "creation_date"),
    EXPIRATION("Expiration", "expiration"),
    IS_LICENSE_VALID("IsLicenseValid", "is_license_valid"),
    LICENCE_MAC_ADDRESS("LicenseMACAddress", "license_mac_address"),
    MAX_ENCODERS("Max_Encoders", "max_encoders"),
    SYSTEM_INSTANCE("SystemInstanceUUID", "system_instance_uuid"),
    VERSION_LIMIT("VersionLimit", "version_limit"),
    VERSION_LIMIT_TEXT("VersionLimitText", "version_limit_text"),
    ;
    private final String name;
    private final String field;

    /**
     * Constructor for RouteInfoMetric.
     *
     * @param name The name representing the system information category.
     * @param field The field associated with the category.
     */
    LicenseEnum(String name, String field) {
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
