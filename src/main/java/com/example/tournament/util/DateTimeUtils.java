package com.example.tournament.util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Utilitaires de manipulation des dates et heures. */
public final class DateTimeUtils {

    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int MINUTES_PER_ROUND_SLOT = 30;

    private DateTimeUtils() {
    }

    /**
     * Décale une heure de {@code index * slotMinutes}.
     *
     * @param kickoff    heure de coup d'envoi du premier match
     * @param index      position du match dans la journée (0-based)
     * @param duration   durée réelle d'un match, utilisée pour dimensionner le créneau
     * @return l'heure de coup d'envoi du match
     */
    public static LocalTime slotTime(LocalTime kickoff, int index, int durationMinutes) {
        int slot = Math.max(durationMinutes, MINUTES_PER_ROUND_SLOT) + MINUTES_PER_ROUND_SLOT;
        return kickoff.plusMinutes((long) slot * index);
    }

    public static LocalTime parseTime(String value, LocalTime defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return LocalTime.parse(value, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return defaultValue;
        }
    }

    public static String format(LocalTime time) {
        return time == null ? "" : time.format(TIME_FORMATTER);
    }

    public static String format(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMATTER);
    }

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
