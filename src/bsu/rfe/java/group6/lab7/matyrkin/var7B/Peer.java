package bsu.rfe.java.group6.lab7.matyrkin.var7B;

import java.net.InetSocketAddress;

public class Peer {
    private final String name;
    private final String address;
    private final int port;

    public Peer(String name, String address, int port) {
        this.name = name;
        this.address = address;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return name + " (" + address + ":" + port + ")";
    }
}
