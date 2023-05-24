package org.example;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Set;

public class Server extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    @Override
    public void start() throws Exception {
        //        vertx.executeBlocking(future -> {
//
//            try{
//                Thread.sleep(5000);
//                BufferedWriter writer = new BufferedWriter(new FileWriter("D:/test.txt"));
//                writer.write("hello");
//                writer.close();
//
//            }catch (Exception e){
//                logger.error("Failed to save world {} ", e);
//            }
//            future.complete();
//        },asyncResult -> {
//            if(asyncResult.succeeded()){
//                System.out.println(asyncResult.result());
//            }else{
//                System.out.println("err: "+asyncResult.cause());
//            }
//
//        });

        try( BufferedWriter writer = new BufferedWriter(new FileWriter("D:/test.txt"))){
            Thread.sleep(3000);
            logger.info("worker...");
            writer.write("hello");
        }catch (Exception e){

        }
    }

    public static void main(String[] args) {
        Set<Thread> threads2 = Thread.getAllStackTraces().keySet();
        System.out.println(Thread.getAllStackTraces().keySet());
        System.out.printf("%-15s \t %-15s \t %-15s \t %s\n", "Name", "State", "Priority", "isDaemon");
        for (Thread t : threads2) {
            System.out.printf("%-15s \t %-15s \t %-15d \t %s\n", t.getName(), t.getState(), t.getPriority(), t.isDaemon());
        }
        System.out.println("--------------------------------");
       Vertx vertx1 = Vertx.vertx();
        Set<Thread> threads3 = Thread.getAllStackTraces().keySet();
        System.out.println(Thread.getAllStackTraces().keySet());
        System.out.printf("%-15s \t %-15s \t %-15s \t %s\n", "Name", "State", "Priority", "isDaemon");
        for (Thread t : threads3) {
            System.out.printf("%-15s \t %-15s \t %-15d \t %s\n", t.getName(), t.getState(), t.getPriority(), t.isDaemon());
        }
        System.out.println("--------------------------------");
//        vertx1.executeBlocking(future -> {
//
//            try{
//                Thread.sleep(5000);
//                BufferedWriter writer = new BufferedWriter(new FileWriter("D:/test.txt"));
//                writer.write("hello");
//                writer.close();
//
//            }catch (Exception e){
//                logger.error("Failed to save world {} ", e);
//            }
//            future.complete();
//        },asyncResult -> {
//            if(asyncResult.succeeded()){
//                System.out.println(asyncResult.result());
//            }else{
//                System.out.println("err: "+asyncResult.cause());
//            }
//
//        });
//
//        Set<Thread> threads4 = Thread.getAllStackTraces().keySet();
//        System.out.println(Thread.getAllStackTraces().keySet());
//        System.out.printf("%-15s \t %-15s \t %-15s \t %s\n", "Name", "State", "Priority", "isDaemon");
//        for (Thread t : threads4) {
//            System.out.printf("%-15s \t %-15s \t %-15d \t %s\n", t.getName(), t.getState(), t.getPriority(), t.isDaemon());
//        }
        vertx1.deployVerticle(new Server(), new DeploymentOptions().setWorker(true), res -> {
            if(res.failed()){
                System.out.println("Failed to deploy worker verticle: "+res.cause());
            }else{
                String depId = res.result();
                System.out.println("deployed verticle "+depId);
                //System.out.println(Thread.activeCount());
                Set<Thread> threads5 = Thread.getAllStackTraces().keySet();
                System.out.println(Thread.getAllStackTraces().keySet());
                System.out.printf("%-15s \t %-15s \t %-15s \t %s\n", "Name", "State", "Priority", "isDaemon");
                for (Thread t : threads5) {
                    System.out.printf("%-15s \t %-15s \t %-15d \t %s\n", t.getName(), t.getState(), t.getPriority(), t.isDaemon());
                }
            }
        });
    }
}
