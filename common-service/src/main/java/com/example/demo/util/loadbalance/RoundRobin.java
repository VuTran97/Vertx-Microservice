package com.example.demo.util.loadbalance;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobin {
    private static AtomicInteger SERVER_INDEX = new AtomicInteger(0);

    public String roundRobin(List<String> ipList) {
        int index = SERVER_INDEX.getAndIncrement();
        if (index >= ipList.size() - 1) {
            SERVER_INDEX.set(0);
        }
        return ipList.get(index);
    }
}
