package com.rickes.testsocket;

import com.rickes.testsocket.rickesIO.server.CustomRunnable;
import com.rickes.testsocket.rickesIO.server.ServerSocket;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;

public class Main {
    public static final Logger logger = Logger.getLogger(Main.class);
    public static void main(String[] args) throws IOException, InterruptedException {
        BasicConfigurator.configure();
        ServerSocket serverSocket = new ServerSocket(9999);
        serverSocket.on("greetings", logger::info);
        JSONObject message = new JSONObject();
        message.put("dayn", "daun");
        for(int i = 0; i < 10; i++) {
            serverSocket.sendOn("news", message);
            logger.info("news sended aaa");
            Thread.sleep(1000);
        }
    }
}
