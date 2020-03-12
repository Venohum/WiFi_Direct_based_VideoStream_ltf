package com.example.dell.wi_fi_direct_based_videostream_ltf.Packet;

import java.io.Serializable;

public class HeartBeatPacket extends Packet implements Serializable {//心跳包

    private static final long serialVersionUID = 3559533002594201716L;
    private String IP;
    private String device_name;
    private String service_tyep;
    private boolean is_relay_node;
    public HeartBeatPacket(String type,String IP,String device_name,String service_tyep,Boolean is_relay_node) {
        super(type);
        this.IP=IP;
        this.device_name = device_name;
        this.is_relay_node = is_relay_node;
        this.service_tyep=service_tyep;

    }
    public String getIP() {
        return IP;
    }
    public String getDevice_name() {
        return device_name;
    }
    public String getService_tyep() {
        return service_tyep;
    }
    public boolean getisIs_relay_node() {
        return is_relay_node;
    }
    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }
    public void setService_tyep(String service_tyep) {
        this.service_tyep = service_tyep;
    }
    public void setIs_relay_node(boolean is_relay_node) {
        this.is_relay_node = is_relay_node;
    }
    public void setIP(String IP){
        this.IP=IP;
    }
}
