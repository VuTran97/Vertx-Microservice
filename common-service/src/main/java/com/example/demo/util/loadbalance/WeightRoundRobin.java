package com.example.demo.util.loadbalance;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

public class WeightRoundRobin {

    Random someRandGen = new Random();
    TreeMap<Integer, ServerDetails> pool;
    int totalWeight;

    public void init(ArrayList<ServerDetails> servers){
        this.pool = new TreeMap<Integer, ServerDetails>();
        totalWeight = 0;
        for(ServerDetails s : servers) {
            totalWeight += s.getWeight();
            this.pool.put(totalWeight, s);
        }
    }
    public ServerDetails getNext() {
        int rnd = someRandGen.nextInt(this.totalWeight);
        return pool.ceilingEntry(rnd).getValue();
    }

    public String weightRoundRobin(List<String> serverList){
        String server;
        ArrayList<ServerDetails> servers = new ArrayList<ServerDetails>();

        for(String item : serverList){
            if(item.equalsIgnoreCase("localhost:8081")){
                servers.add(new ServerDetails(50, item));           // treeMap 1: <50, "50,localhost:8081">
            }else if(item.equalsIgnoreCase("localhost:8082")){  //        2: <80, "30,localhost:8082">
                servers.add(new ServerDetails(30, item));           //         3: <100, "20,localhost:8083">
            }else{
                servers.add(new ServerDetails(20, item));
            }
        }
        init(servers);
        server = getNext().getAddress();
        return server;
    }
}
