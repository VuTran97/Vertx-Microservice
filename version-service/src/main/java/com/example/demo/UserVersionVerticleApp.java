package com.example.demo;

import com.example.demo.util.cluster.ClusterUtil;
import com.example.demo.verticle.UserVersionVerticle;

public class UserVersionVerticleApp {
    public static void main(String[] args) {
        ClusterUtil.clusterDeploy(new UserVersionVerticle());
    }
}
