package com.sahlabu.community;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class Song {
    final String id;
    final String number;
    final String title;
    final String category;
    final String language;
    final List<String> verses;
    final String chorus;
    final String solfa;
    final String musicNotes;
    final String originalKey;
    final String attribution;

    private Song(String id, String number, String title, String category, String language,
                 List<String> verses, String chorus, String solfa, String musicNotes,
                 String originalKey, String attribution) {
        this.id = id;
        this.number = number;
        this.title = title;
        this.category = category;
        this.language = language;
        this.verses = verses;
        this.chorus = chorus;
        this.solfa = solfa;
        this.musicNotes = musicNotes;
        this.originalKey = originalKey;
        this.attribution = attribution;
    }

    static Song fromJson(JSONObject object) {
        String number = object.optString("number", "");
        String id = object.optString("id", number);
        return new Song(
                id,
                number,
                object.optString("title", "Untitled hymn"),
                object.optString("category", "Uncategorized"),
                object.optString("language", ""),
                readList(object.opt("verses")),
                object.optString("chorus", ""),
                readText(object.opt("solfa")),
                object.optString("musicNotes", ""),
                object.optString("originalKey", ""),
                object.optString("attribution", "")
        );
    }

    String searchableText() {
        StringBuilder text = new StringBuilder(number).append(' ')
                .append(title).append(' ').append(category).append(' ')
                .append(language).append(' ');
        for (String verse : verses) text.append(verse).append(' ');
        text.append(chorus).append(' ').append(solfa).append(' ').append(musicNotes);
        return text.toString().toLowerCase(Locale.ROOT);
    }

    String firstLine() {
        for (String verse : verses) {
            String trimmed = verse.trim();
            if (!trimmed.isEmpty()) {
                int lineEnd = trimmed.indexOf('\n');
                return lineEnd >= 0 ? trimmed.substring(0, lineEnd).trim() : trimmed;
            }
        }
        return "Add the song text in the bundled song data.";
    }

    private static List<String> readList(Object value) {
        List<String> result = new ArrayList<>();
        if (value instanceof JSONArray) {
            JSONArray array = (JSONArray) value;
            for (int i = 0; i < array.length(); i++) {
                result.add(array.optString(i, ""));
            }
        } else if (value instanceof String && !((String) value).trim().isEmpty()) {
            result.add((String) value);
        }
        return result;
    }

    private static String readText(Object value) {
        if (value instanceof JSONArray) {
            JSONArray array = (JSONArray) value;
            List<String> parts = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) parts.add(array.optString(i, ""));
            return join(parts, "\n");
        }
        return value instanceof String ? (String) value : "";
    }

    private static String join(List<String> parts, String separator) {
        StringBuilder text = new StringBuilder();
        for (String part : parts) {
            if (text.length() > 0) text.append(separator);
            text.append(part);
        }
        return text.toString();
    }
}
