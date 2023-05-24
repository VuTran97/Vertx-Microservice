package org.example;

import com.example.demo.util.cluster.ClusterUtil;
import org.example.verticle.UserVerticle;

public class UserVerticleApp {
    public static void main(String[] args) {
        ClusterUtil.clusterDeploy(new UserVerticle());
    }
}
