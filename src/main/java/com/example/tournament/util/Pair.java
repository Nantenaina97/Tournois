package com.example.tournament.util;

/** Paire ordonnée (équipe A, équipe B). */
public record Pair<A, B>(A first, B second) {

    public static <T> Pair<T, T> of(T a, T b) {
        return new Pair<>(a, b);
    }

    public boolean same(Object value) {
        return java.util.Objects.equals(first, value) || java.util.Objects.equals(second, value);
    }
}
