package com.avispl.symphony.dal.avdenvices.encoderdecoder.haivision.kraken.common.metric;

public enum NetWorkInterfaceBySystemInfoEnum {
    GATEWAY("Gateway", "gateway"),
    IPADDRESS("IpAddress", "ip_address"),
    LINK_MODE("LinkMode", "link_mode"),
    MAC_ADDRESS("MACAddress", "mac_address"),
//    MTU("MACAddress", "mtu"),
//    NAME("Name", "name"),
//    PEER_DNS("PeerDNS", "peerDns"),
    SPEED("Speed", "speed"),
    SUBNET_MASK("SubnetMask", "subnet_mask"),
    ;
    private final String name;
    private final String field;

    /**
     * Constructor for RouteInfoMetric.
     *
     * @param name The name representing the system information category.
     * @param field The field associated with the category.
     */
    NetWorkInterfaceBySystemInfoEnum(String name, String field) {
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
