package com.storm.context;

import com.storm.entity.TokenPayload;

public class UserContext {
    private static final ThreadLocal<TokenPayload> currentUser = new ThreadLocal<>();

    public static void setUser(TokenPayload payload) {
        currentUser.set(payload);
    }

    public static TokenPayload getUser() {
        return currentUser.get();
    }

    public static void clear() {
        currentUser.remove();
    }
}