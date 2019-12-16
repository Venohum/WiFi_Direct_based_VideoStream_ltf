package com.example.dell.wi_fi_direct_based_videostream_ltf.Packet;

public class Packet{

    private String type;//数据包类型
    protected byte [] data;//数据
    Packet (String type){
        this.type=type;
    }

    public Packet() {

    }
}
