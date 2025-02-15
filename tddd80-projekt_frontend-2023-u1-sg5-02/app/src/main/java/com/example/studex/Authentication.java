package com.example.studex;

public class Authentication {
    private static String ACCESS_TOKEN_KEY = null;
    private static String username;
    private static String id;

    public static void clearAccessToken() {
        ACCESS_TOKEN_KEY = null;
    }
    public static void clearUsername() {
        ACCESS_TOKEN_KEY = null;
    }
    public static void clearId() {
        ACCESS_TOKEN_KEY = null;
    }
    public static boolean isLoggedIn() {
        return ACCESS_TOKEN_KEY != null;
    }

    public static void setAccessTokenKey(String accessTokenKey) {
        ACCESS_TOKEN_KEY = accessTokenKey;
    }

    public static String getAccessTokenKey() {
        return ACCESS_TOKEN_KEY;
    }

    public static String getUsername() {
        return username;
    }
    public static void setUsername(String username) {
        Authentication.username = username.toLowerCase();
    }

    public static String getId() {
        return id;
    }

    public static void setId(String id) {
        Authentication.id = id;
    }
}
