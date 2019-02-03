package com.rickes.testsocket.rickesIO.server;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;

public class User {

    private final Socket userSocket;
    private final PrintWriter writer;
    private final BufferedReader reader;
    private final ArrayList<String> channels;

    User(Socket userSocket) throws IOException {
        this.userSocket = userSocket;
        channels = new ArrayList<>();
        writer = new PrintWriter(userSocket.getOutputStream(), true);
        reader = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));
    }

    Socket getUserSocket() {
        return userSocket;
    }

    PrintWriter getWriter() {
        return writer;
    }

    BufferedReader getReader() {
        return reader;
    }

    @Override
    public String toString() {
        return "User{" +
                "userSocket=" + userSocket + "}";
    }

    void addChannel(String channel) {
        synchronized (channels) {
            if(!channels.contains(channel)) {
                channels.add(channel);
            }
        }
    }

    boolean isSubscribedOn(String channel) {
        synchronized (channels) {
            return channels.contains(channel);
        }
    }

    public void send(JSONObject messageJSON) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                writer.println(Objects.requireNonNull(messageJSON));
            }
        }.start();
    }
}
