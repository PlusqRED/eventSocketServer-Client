package com.rickes.testsocket;

import com.rickes.testsocket.rickesIO.server.CustomRunnable;
import com.rickes.testsocket.rickesIO.server.user.UserSocket;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;

public class UserSide {
    private static final Logger logger = Logger.getLogger(UserSide.class);

    public static void main(String[] args) throws InterruptedException, IOException {
        BasicConfigurator.configure();
        UserSocket userSocket = new UserSocket("localhost", 9999);
        JSONObject object = new JSONObject();
        object.put("name", "Oleg");
        object.put("surname", "Vinograd");
        object.put("age", 19);
        userSocket.subscribe("news");
        userSocket.on("news", object1 -> {
            logger.info(object1);
        });
        for(int i = 0; i < 10; i++) {
            userSocket.send("greetings", object);
            Thread.sleep(500);
        }

    }
}
