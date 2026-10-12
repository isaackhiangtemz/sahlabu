package com.sahlabu.community;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

final class SongRepository {
    private SongRepository() { }

    static List<Song> load(Context context) throws IOException, JSONException {
        try (InputStream input = context.getAssets().open("songs.json");
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
            String json = new String(output.toByteArray(), StandardCharsets.UTF_8);
            JSONArray array = new JSONArray(json);
            List<Song> songs = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) songs.add(Song.fromJson(array.getJSONObject(i)));
            return songs;
        }
    }
}
