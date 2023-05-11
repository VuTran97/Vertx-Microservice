package com.example.demo.util.loadbalance;

public class ServerDetails {

    private int weight;
    private String address;

    public ServerDetails(int weight, String address) {
        this.weight = weight;
        this.address = address;
    }

    public int getWeight() {
        return weight;
    }

    public String getAddress() {
        return address;
    }

}
