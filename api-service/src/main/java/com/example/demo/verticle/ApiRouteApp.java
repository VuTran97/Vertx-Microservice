package com.example.demo.verticle;

import com.example.demo.util.cluster.ClusterUtil;

public class ApiRouteApp {

    public static void main(String[] args) {
        ClusterUtil.clusterDeploy(new ApiRouteVerticle());
    }
}
