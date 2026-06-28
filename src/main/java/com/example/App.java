package com.example;

/** Minimal hello-world entry point for the template. */
public final class App {

    private App() {
    }

    public static String greeting() {
        return "Hello, World!";
    }

    public static void main(String[] args) {
        System.out.println(greeting());
    }
}
