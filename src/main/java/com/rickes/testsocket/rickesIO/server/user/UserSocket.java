package com.rickes.testsocket.rickesIO.server.user;

import com.rickes.testsocket.rickesIO.server.CustomRunnable;
import com.rickes.testsocket.rickesIO.server.Event;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

public class UserSocket {
    private static final Logger logger = Logger.getLogger(UserSocket.class);
    private final Socket socket;
    private final BufferedReader in;
    private final PrintWriter out;
    private final HashMap<String, CustomRunnable> subscribedChannels;

    public UserSocket(String host, int port) throws IOException {
        socket = new Socket(host, port);
        subscribedChannels = new HashMap<>();
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        Thread userSocketThread = new Thread("receiving_thread") {
            @Override
            public void run() {
                super.run();
                String message;
                while (!socket.isClosed()) {
                    try {
                        if((message = in.readLine()) != null) {
                            if (!receiveMessage(message)) {
                                break;
                            }
                        }
                    } catch (IOException e) {
                        logger.fatal("Cannot read message from server!");
                    }
                }
            }
        };
        userSocketThread.start();
    }

    private boolean receiveMessage(String message) throws IOException {
        JSONObject messageJSON = new JSONObject(message);
        JSONObject emptyJSON = new JSONObject();
        String event = messageJSON.getString("event");
        logger.info("Event: " + event);
        if (Event.CONNECTED.toString().equals(event)) {
            logger.info("Successfully connected to server!");
        } else if (Event.DISCONNECTED.toString().equals(event)) {
            logger.info("Disconnecting...");
            socket.close();
            return false;
        } else if(subscribedChannels.containsKey(event)) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    subscribedChannels.get(event).run(messageJSON.getJSONObject("data"));
                }
            }.start();
        }
        return true;
    }

    public void send(String event, Object message) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                JSONObject messageJSON = new JSONObject();
                messageJSON.put("event", event);
                messageJSON.put("data", message);
                out.println(messageJSON);
            }
        }.start();
    }

    public void subscribe(String channel) {
        send("subscribe", new JSONObject() {{ put("channel", channel); }});
    }

    public void on(String channel, CustomRunnable runnable) {
        subscribedChannels.put(channel, runnable);
    }

    public Socket getSocket() {
        return socket;
    }
}
