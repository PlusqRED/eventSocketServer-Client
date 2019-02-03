package com.rickes.testsocket.rickesIO.server;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerSocket {
    private static final Logger logger = Logger.getLogger(ServerSocket.class);
    private final int port;
    private java.net.ServerSocket serverSocket;
    private final ArrayList<User> users;
    private final HashMap<String, CustomRunnable> bindings;

    private static final JSONObject connectMessage = new JSONObject() {{
        put("event", Event.CONNECTED.toString());
    }};
    private static final JSONObject disconnectMessage = new JSONObject() {{
        put("event", Event.DISCONNECTED.toString());
    }};

    public ServerSocket(final int port) {
        this.port = port;
        this.users = new ArrayList<>();
        this.bindings = new HashMap<>();

        try {
            serverSocket = new java.net.ServerSocket(port);
            Thread connectingThread = new Thread("connecting") {
                @Override
                public void run() {
                    super.run();
                    while (!serverSocket.isClosed()) {
                        logger.info("Waiting for user...");
                        try {
                            applyUser(new User(serverSocket.accept()));
                        } catch (IOException e) {
                            logger.info(getName() + " is closed!");
                        }
                    }
                }
            };
            connectingThread.start();
        } catch (IOException e) {
            logger.fatal("Server is not activated!");
        }
    }

    private void applyUser(final User user) {
        Thread applying = new Thread("applying_new_user") {
            @Override
            public void run() {
                super.run();
                synchronized (users) {
                    users.add(user);
                }
                onUserConnected();
                user.send(connectMessage);
                logger.info("User successfully applied!");
                logger.info(users);
            }
        };

        Thread listenUser = new Thread("listen_to_user_" + user.getUserSocket().getPort()) {
            @Override
            public void run() {
                super.run();
                try {
                    String message, event;
                    while (!user.getUserSocket().isClosed()) {
                        if((message = user.getReader().readLine()) != null) {
                            JSONObject user_message_JSON = new JSONObject(message);
                            event = user_message_JSON.getString("event");
                            if(event.equals("subscribe")) {
                                user.addChannel(user_message_JSON.getJSONObject("data").getString("channel"));
                            } else {
                                if(bindings.containsKey(event)) {
                                    bindings.get(event).run(user_message_JSON.getJSONObject("data"));
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    synchronized (users) {
                        users.remove(user);
                    }
                    onUserDisconnected();
                    logger.info("Successfully disconnected! " + user);
                }
            }
        };

        listenUser.start();
        applying.start();
    }

    public ServerSocket on(String event, CustomRunnable runnable) {
        synchronized (bindings) {
            bindings.put(event, runnable);
        }
        return this;
    }

    public void close() throws IOException {
        synchronized (users) {
            for (User user : users) {
                new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        user.send(disconnectMessage);
                        try {
                            sleep(10000);
                            user.getUserSocket().close();
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }
                        onUserDisconnected();
                    }
                }.start();
            }
            users.clear();
        }
        serverSocket.close();
        logger.info("Server successfully closed!");
    }

    public void sendOn(String channel, Object message) {
        JSONObject messageJSON = new JSONObject();
        messageJSON.put("event", channel);
        messageJSON.put("data", message);
        synchronized (users) {
            for (User user : users) {
                if(user.isSubscribedOn(channel)) {
                    logger.info("Sended on " + channel);
                    user.send(messageJSON);
                }
            }
        }
    }

    public void onUserConnected() {}

    public void onUserDisconnected() {}


}
