package com.PAP_team_21.flashcards.entities;

public class JsonViewConfig {
    public static class Public {}
    public static class Internal extends Public {}
    public static class Secure extends Internal {}

    public static class BasicStructures extends Internal {}

    public static class ExtendedStructures extends Internal {}

}
