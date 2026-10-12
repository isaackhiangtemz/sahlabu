package com.sahlabu.community;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MainActivity extends Activity {
    private static final String PREFS = "sa_hlabu_preferences";
    private static final String[] KEYS = {"C", "C♯", "D", "E♭", "E", "F", "F♯", "G", "A♭", "A", "B♭", "B"};
    private static final Pattern BRACKETED_CHORD = Pattern.compile("\\[([A-Ga-g])([#b♯♭]?)([^\\]]*)\\]");

    private final List<Song> songs = new ArrayList<>();
    private final Set<String> favorites = new HashSet<>();

    private SharedPreferences prefs;
    private String query = "";
    private String categoryFilter = "All songs";
    private String page = "home";
    private boolean favoritesOnly;
    private boolean darkMode;
    private Song currentSong;
    private List<Song> detailContext = new ArrayList<>();
    private LinearLayout resultsContainer;

    private int backgroundColor;
    private int surfaceColor;
    private int inkColor;
    private int mutedColor;
    private int accentColor;
    private int borderColor;
    private int goldColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        darkMode = prefs.getBoolean("dark_mode", false);
        Set<String> savedFavorites = prefs.getStringSet("favorites", new HashSet<String>());
        if (savedFavorites != null) favorites.addAll(savedFavorites);
        updatePalette();
        try {
            songs.addAll(SongRepository.load(this));
        } catch (Exception error) {
            Toast.makeText(this, "Could not load the bundled song list.", Toast.LENGTH_LONG).show();
        }
        renderHome();
    }

    private void updatePalette() {
        if (darkMode) {
            backgroundColor = Color.rgb(23, 22, 21);
            surfaceColor = Color.rgb(36, 34, 32);
            inkColor = Color.rgb(246, 239, 231);
            mutedColor = Color.rgb(187, 175, 163);
            accentColor = Color.rgb(227, 123, 134);
            borderColor = Color.rgb(61, 55, 50);
            goldColor = Color.rgb(230, 193, 122);
        } else {
            backgroundColor = Color.rgb(247, 244, 239);
            surfaceColor = Color.WHITE;
            inkColor = Color.rgb(38, 35, 32);
            mutedColor = Color.rgb(117, 109, 102);
            accentColor = Color.rgb(146, 40, 56);
            borderColor = Color.rgb(233, 227, 217);
            goldColor = Color.rgb(196, 154, 85);
        }
        getWindow().setStatusBarColor(Color.rgb(91, 27, 40));
        getWindow().setNavigationBarColor(darkMode ? backgroundColor : Color.rgb(35, 32, 30));
    }

    private void renderHome() {
        page = favoritesOnly ? "favorites" : "home";
        updatePalette();
        LinearLayout root = makeRoot();
        root.addView(makeHeader(null));

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout body = verticalLayout();
        body.setPadding(dp(20), dp(18), dp(20), dp(22));
        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        addLabel(body, favoritesOnly ? "YOUR SAVED SONGS" : "YOUR SONGBOOK", 11, accentColor, true);
        TextView heading = text(favoritesOnly ? "Favorites" : "Find a hymn", 27, inkColor, true);
        LinearLayout.LayoutParams headingParams = margins(0, 3, 0, 14);
        body.addView(heading, headingParams);

        EditText search = new EditText(this);
        search.setSingleLine(true);
        search.setTextSize(15);
        search.setTextColor(inkColor);
        search.setHintTextColor(mutedColor);
        search.setHint("Search number, title or opening line");
        search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        search.setPadding(dp(16), dp(12), dp(16), dp(12));
        search.setBackground(rounded(surfaceColor, dp(16), borderColor));
        search.setText(query);
        body.addView(search, margins(0, 0, 0, 14));

        if (!favoritesOnly) {
            HorizontalScrollView categories = new HorizontalScrollView(this);
            categories.setHorizontalScrollBarEnabled(false);
            LinearLayout chips = new LinearLayout(this);
            chips.setOrientation(LinearLayout.HORIZONTAL);
            for (String category : categories()) {
                TextView chip = text(category, 13, category.equals(categoryFilter) ? Color.WHITE : inkColor, true);
                chip.setGravity(Gravity.CENTER);
                chip.setPadding(dp(15), dp(9), dp(15), dp(9));
                chip.setBackground(rounded(category.equals(categoryFilter) ? accentColor : surfaceColor,
                        dp(24), category.equals(categoryFilter) ? accentColor : borderColor));
                chip.setOnClickListener(view -> {
                    categoryFilter = category;
                    renderHome();
                });
                chips.addView(chip, margins(0, 0, 8, 0));
            }
            categories.addView(chips);
            body.addView(categories, margins(0, 0, 0, 20));
        }

        resultsContainer = verticalLayout();
        body.addView(resultsContainer);
        renderSongResults();

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence value, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence value, int start, int before, int count) {
                query = value.toString();
                renderSongResults();
            }
            @Override public void afterTextChanged(Editable value) { }
        });

        root.addView(makeBottomNavigation());
        setContentView(root);
    }

    private void renderSongResults() {
        if (resultsContainer == null) return;
        resultsContainer.removeAllViews();
        List<Song> visible = visibleSongs();
        TextView count = text(favoritesOnly ? visible.size() + " SAVED" : visible.size() + " SONGS",
                11, mutedColor, true);
        resultsContainer.addView(count, margins(2, 0, 0, 10));

        if (visible.isEmpty()) {
            TextView empty = text(favoritesOnly ? "No favorites yet. Tap the heart on a hymn to save it."
                    : "No songs match that search.", 15, mutedColor, false);
            empty.setPadding(dp(16), dp(18), dp(16), dp(18));
            empty.setBackground(rounded(surfaceColor, dp(16), borderColor));
            resultsContainer.addView(empty);
            return;
        }

        for (Song song : visible) {
            resultsContainer.addView(songCard(song), margins(0, 0, 0, 10));
        }
    }

    private View songCard(Song song) {
        LinearLayout card = verticalLayout();
        card.setPadding(dp(16), dp(14), dp(14), dp(14));
        card.setBackground(rounded(surfaceColor, dp(17), borderColor));
        card.setClickable(true);
        card.setOnClickListener(view -> openSong(song, visibleSongs()));

        LinearLayout top = horizontalLayout();
        TextView number = text("NO. " + song.number, 10, accentColor, true);
        number.setGravity(Gravity.CENTER);
        number.setPadding(dp(10), dp(6), dp(10), dp(6));
        number.setBackground(rounded(backgroundColor, dp(20), borderColor));
        top.addView(number, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView heart = text(favorites.contains(song.id) ? "♥" : "♡", 25,
                favorites.contains(song.id) ? accentColor : mutedColor, false);
        heart.setGravity(Gravity.CENTER);
        heart.setContentDescription(favorites.contains(song.id) ? "Remove from favorites" : "Add to favorites");
        top.addView(heart, new LinearLayout.LayoutParams(dp(42), dp(38)));
        heart.setOnClickListener(view -> {
            toggleFavorite(song);
            renderSongResults();
        });
        card.addView(top);

        TextView title = text(song.title, 18, inkColor, true);
        card.addView(title, margins(0, 7, 0, 2));
        card.addView(text(song.category + (song.language.isEmpty() ? "" : "  ·  " + song.language),
                12, mutedColor, false));
        TextView preview = text(song.firstLine(), 14, mutedColor, false);
        preview.setMaxLines(1);
        preview.setEllipsize(android.text.TextUtils.TruncateAt.END);
        card.addView(preview, margins(0, 8, 0, 0));
        return card;
    }

    private void openSong(Song song, List<Song> context) {
        currentSong = song;
        detailContext = new ArrayList<>(context);
        if (detailContext.isEmpty()) detailContext.addAll(songs);
        renderDetail();
    }

    private void renderDetail() {
        if (currentSong == null) {
            renderHome();
            return;
        }
        page = "detail";
        updatePalette();
        LinearLayout root = makeRoot();
        root.addView(makeHeader(() -> renderHome()));

        ScrollView scroll = new ScrollView(this);
        LinearLayout body = verticalLayout();
        body.setPadding(dp(20), dp(20), dp(20), dp(28));
        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        addLabel(body, "HYMN  " + currentSong.number, 11, accentColor, true);
        TextView title = text(currentSong.title, 30, inkColor, true);
        title.setLineSpacing(dp(2), 1f);
        body.addView(title, margins(0, 7, 0, 5));
        body.addView(text(currentSong.category + (currentSong.language.isEmpty() ? "" : "  ·  " + currentSong.language),
                14, mutedColor, false), margins(0, 0, 0, 18));

        LinearLayout actions = horizontalLayout();
        TextView save = actionButton(favorites.contains(currentSong.id) ? "♥  Saved" : "♡  Save hymn", false);
        save.setOnClickListener(view -> {
            toggleFavorite(currentSong);
            renderDetail();
        });
        TextView share = actionButton("↗  Share hymn", true);
        share.setOnClickListener(view -> shareSong(currentSong));
        actions.addView(save, new LinearLayout.LayoutParams(0, dp(46), 1f));
        LinearLayout.LayoutParams shareParams = new LinearLayout.LayoutParams(0, dp(46), 1f);
        shareParams.setMargins(dp(9), 0, 0, 0);
        actions.addView(share, shareParams);
        body.addView(actions, margins(0, 0, 0, 18));

        if (!currentSong.originalKey.isEmpty()) addKeyPicker(body, currentSong);

        for (int i = 0; i < currentSong.verses.size(); i++) {
            addLabel(body, "VERSE " + (i + 1), 10, goldColor, true);
            TextView verse = text(transposedText(currentSong.verses.get(i), currentSong), 21, inkColor, false);
            verse.setLineSpacing(dp(8), 1.12f);
            body.addView(verse, margins(0, 6, 0, 20));
        }
        if (!currentSong.chorus.trim().isEmpty()) {
            addLabel(body, "CHORUS", 10, goldColor, true);
            TextView chorus = text(transposedText(currentSong.chorus, currentSong), 21, inkColor, false);
            chorus.setLineSpacing(dp(8), 1.12f);
            body.addView(chorus, margins(0, 6, 0, 20));
        }
        if (!currentSong.solfa.trim().isEmpty()) {
            addLabel(body, "SOLFA", 10, goldColor, true);
            TextView solfa = text(currentSong.solfa, 16, inkColor, false);
            solfa.setTypeface(Typeface.MONOSPACE);
            solfa.setLineSpacing(dp(5), 1.1f);
            solfa.setPadding(dp(15), dp(14), dp(15), dp(14));
            solfa.setBackground(rounded(surfaceColor, dp(15), borderColor));
            body.addView(solfa, margins(0, 6, 0, 18));
        }
        if (!currentSong.musicNotes.trim().isEmpty()) {
            addLabel(body, "MUSIC NOTES", 10, goldColor, true);
            TextView notes = text(transposedText(currentSong.musicNotes, currentSong), 16, inkColor, false);
            notes.setLineSpacing(dp(5), 1.1f);
            notes.setPadding(dp(15), dp(14), dp(15), dp(14));
            notes.setBackground(rounded(surfaceColor, dp(15), borderColor));
            body.addView(notes, margins(0, 6, 0, 18));
        }
        if (!currentSong.attribution.trim().isEmpty()) {
            TextView attribution = text(currentSong.attribution, 12, mutedColor, false);
            body.addView(attribution, margins(0, 3, 0, 18));
        }

        LinearLayout navigation = horizontalLayout();
        TextView previous = actionButton("←  Previous", false);
        TextView next = actionButton("Next  →", false);
        previous.setOnClickListener(view -> stepSong(-1));
        next.setOnClickListener(view -> stepSong(1));
        navigation.addView(previous, new LinearLayout.LayoutParams(0, dp(46), 1f));
        LinearLayout.LayoutParams nextParams = new LinearLayout.LayoutParams(0, dp(46), 1f);
        nextParams.setMargins(dp(9), 0, 0, 0);
        navigation.addView(next, nextParams);
        body.addView(navigation, margins(0, 6, 0, 0));

        GestureDetector detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onDown(MotionEvent event) { return false; }
            @Override public boolean onFling(MotionEvent start, MotionEvent end, float velocityX, float velocityY) {
                float dx = end.getX() - start.getX();
                float dy = end.getY() - start.getY();
                if (Math.abs(dx) > dp(100) && Math.abs(dx) > Math.abs(dy) * 1.25f) {
                    stepSong(dx < 0 ? 1 : -1);
                    return true;
                }
                return false;
            }
        });
        scroll.setOnTouchListener((view, event) -> {
            detector.onTouchEvent(event);
            return false;
        });
        setContentView(root);
    }

    private void addKeyPicker(LinearLayout body, Song song) {
        LinearLayout card = verticalLayout();
        card.setPadding(dp(15), dp(13), dp(15), dp(12));
        card.setBackground(rounded(surfaceColor, dp(16), borderColor));
        addLabel(card, "DISPLAY KEY", 10, goldColor, true);

        List<String> choices = new ArrayList<>(Arrays.asList(KEYS));
        if (!choices.contains(song.originalKey)) choices.add(0, song.originalKey);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, choices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner picker = new Spinner(this);
        picker.setAdapter(adapter);
        picker.setBackground(rounded(backgroundColor, dp(10), borderColor));
        picker.setPadding(dp(10), dp(4), dp(10), dp(4));
        String savedKey = prefs.getString("key_" + song.id, song.originalKey);
        int selected = Math.max(0, choices.indexOf(savedKey));
        picker.setSelection(selected);
        card.addView(picker, margins(0, 6, 0, 4));
        picker.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String chosen = choices.get(position);
                if (!chosen.equals(prefs.getString("key_" + song.id, song.originalKey))) {
                    prefs.edit().putString("key_" + song.id, chosen).apply();
                    renderDetail();
                }
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
        TextView hint = text("Chord symbols written in [brackets] follow this key. Solfa stays relative.",
                12, mutedColor, false);
        card.addView(hint, margins(0, 1, 0, 0));
        body.addView(card, margins(0, 0, 0, 20));
    }

    private void stepSong(int delta) {
        if (detailContext.isEmpty()) return;
        int index = -1;
        for (int i = 0; i < detailContext.size(); i++) {
            if (detailContext.get(i).id.equals(currentSong.id)) {
                index = i;
                break;
            }
        }
        if (index < 0) return;
        int next = index + delta;
        if (next < 0 || next >= detailContext.size()) {
            Toast.makeText(this, next < 0 ? "You are at the first song." : "You are at the last song.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        currentSong = detailContext.get(next);
        renderDetail();
    }

    private void shareSong(Song song) {
        StringBuilder shareText = new StringBuilder("SA HLABU · THE SALVATION ARMY COMMUNITY\n\n")
                .append(song.number).append(" — ").append(song.title).append('\n')
                .append(song.category);
        if (!song.language.isEmpty()) shareText.append(" · ").append(song.language);
        String key = prefs.getString("key_" + song.id, song.originalKey);
        if (!key.isEmpty()) shareText.append("\nKey: ").append(key);
        shareText.append("\n\n");
        for (int i = 0; i < song.verses.size(); i++) {
            shareText.append("Verse ").append(i + 1).append('\n')
                    .append(transposedText(song.verses.get(i), song)).append("\n\n");
        }
        if (!song.chorus.trim().isEmpty()) {
            shareText.append("Chorus\n").append(transposedText(song.chorus, song)).append("\n\n");
        }
        if (!song.solfa.trim().isEmpty()) shareText.append("Solfa\n").append(song.solfa).append("\n\n");
        if (!song.musicNotes.trim().isEmpty()) {
            shareText.append("Music notes\n").append(transposedText(song.musicNotes, song)).append("\n\n");
        }
        if (!song.attribution.trim().isEmpty()) shareText.append(song.attribution);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, song.number + " — " + song.title);
        intent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        startActivity(Intent.createChooser(intent, "Share hymn"));
    }

    private String transposedText(String text, Song song) {
        if (text == null || text.isEmpty() || song.originalKey.isEmpty()) return text == null ? "" : text;
        String displayKey = prefs.getString("key_" + song.id, song.originalKey);
        int original = pitchValue(song.originalKey);
        int display = pitchValue(displayKey);
        if (original < 0 || display < 0) return text;
        int shift = (display - original + 12) % 12;
        if (shift == 0) return text;

        Matcher matcher = BRACKETED_CHORD.matcher(text);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String chordRoot = matcher.group(1) + matcher.group(2);
            int note = pitchValue(chordRoot);
            if (note < 0) continue;
            String replacement = "[" + KEYS[(note + shift) % 12] + matcher.group(3) + "]";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private int pitchValue(String name) {
        if (name == null || name.trim().isEmpty()) return -1;
        String normalized = name.trim().replace('♯', '#').replace('♭', 'b');
        char letter = Character.toUpperCase(normalized.charAt(0));
        int pitch;
        switch (letter) {
            case 'C': pitch = 0; break;
            case 'D': pitch = 2; break;
            case 'E': pitch = 4; break;
            case 'F': pitch = 5; break;
            case 'G': pitch = 7; break;
            case 'A': pitch = 9; break;
            case 'B': pitch = 11; break;
            default: return -1;
        }
        if (normalized.length() > 1) {
            char accidental = normalized.charAt(1);
            if (accidental == '#') pitch++;
            if (accidental == 'b') pitch--;
        }
        return (pitch + 12) % 12;
    }

    private void renderAbout() {
        page = "about";
        updatePalette();
        LinearLayout root = makeRoot();
        root.addView(makeHeader(null));
        ScrollView scroll = new ScrollView(this);
        LinearLayout body = verticalLayout();
        body.setPadding(dp(20), dp(22), dp(20), dp(24));
        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        addLabel(body, "ABOUT THIS SONGBOOK", 11, accentColor, true);
        body.addView(text("SA HLABU", 30, inkColor, true), margins(0, 7, 0, 8));
        body.addView(text("A simple offline hymnbook for The Salvation Army Community.",
                16, mutedColor, false), margins(0, 0, 0, 18));

        LinearLayout card = verticalLayout();
        card.setPadding(dp(17), dp(16), dp(17), dp(16));
        card.setBackground(rounded(surfaceColor, dp(17), borderColor));
        addLabel(card, "ADD OR EDIT SONGS", 10, goldColor, true);
        TextView guide = text("In Android Studio, open app/src/main/assets/songs.json. Add a song object to the list and sync or rebuild the app. The README in the project folder includes the data format and a sample.",
                15, inkColor, false);
        guide.setLineSpacing(dp(5), 1.08f);
        card.addView(guide, margins(0, 8, 0, 0));
        body.addView(card);

        root.addView(makeBottomNavigation());
        setContentView(root);
    }

    private LinearLayout makeBottomNavigation() {
        LinearLayout bar = horizontalLayout();
        bar.setGravity(Gravity.CENTER);
        bar.setPadding(dp(14), dp(9), dp(14), dp(9));
        bar.setBackgroundColor(surfaceColor);
        bar.setElevation(dp(8));
        TextView hymns = navItem("♫  HYMNS", !favoritesOnly && !"about".equals(page));
        hymns.setOnClickListener(view -> {
            favoritesOnly = false;
            categoryFilter = "All songs";
            renderHome();
        });
        TextView saved = navItem("♥  SAVED", favoritesOnly);
        saved.setOnClickListener(view -> {
            favoritesOnly = true;
            renderHome();
        });
        TextView about = navItem("ⓘ  ABOUT", "about".equals(page));
        about.setOnClickListener(view -> renderAbout());
        bar.addView(hymns, new LinearLayout.LayoutParams(0, dp(44), 1f));
        bar.addView(saved, new LinearLayout.LayoutParams(0, dp(44), 1f));
        bar.addView(about, new LinearLayout.LayoutParams(0, dp(44), 1f));
        return bar;
    }

    private TextView navItem(String label, boolean active) {
        TextView item = text(label, 11, active ? accentColor : mutedColor, true);
        item.setGravity(Gravity.CENTER);
        item.setClickable(true);
        return item;
    }

    private LinearLayout makeHeader(Runnable backAction) {
        LinearLayout header = horizontalLayout();
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(18), dp(12), dp(14), dp(12));
        header.setBackgroundColor(Color.rgb(91, 27, 40));

        if (backAction != null) {
            TextView back = text("‹", 31, Color.WHITE, false);
            back.setGravity(Gravity.CENTER);
            header.addView(back, new LinearLayout.LayoutParams(dp(38), dp(42)));
            back.setOnClickListener(view -> backAction.run());
        } else {
            TextView mark = text("SA", 12, Color.rgb(91, 27, 40), true);
            mark.setGravity(Gravity.CENTER);
            mark.setBackground(rounded(goldColor, dp(13), goldColor));
            header.addView(mark, new LinearLayout.LayoutParams(dp(38), dp(38)));
        }

        LinearLayout brand = verticalLayout();
        TextView title = text("SA HLABU", 17, Color.WHITE, true);
        TextView subtitle = text("THE SALVATION ARMY  ·  COMMUNITY", 9, Color.rgb(235, 213, 190), true);
        subtitle.setLetterSpacing(0.06f);
        brand.addView(title);
        brand.addView(subtitle, margins(0, 2, 0, 0));
        header.addView(brand, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView theme = text(darkMode ? "☀" : "☾", 21, Color.WHITE, false);
        theme.setGravity(Gravity.CENTER);
        theme.setContentDescription("Toggle light and dark theme");
        header.addView(theme, new LinearLayout.LayoutParams(dp(42), dp(42)));
        theme.setOnClickListener(view -> {
            darkMode = !darkMode;
            prefs.edit().putBoolean("dark_mode", darkMode).apply();
            if ("detail".equals(page)) renderDetail();
            else if ("about".equals(page)) renderAbout();
            else renderHome();
        });
        return header;
    }

    private LinearLayout makeRoot() {
        LinearLayout root = verticalLayout();
        root.setBackgroundColor(backgroundColor);
        return root;
    }

    private List<Song> visibleSongs() {
        List<Song> result = new ArrayList<>();
        String search = query.trim().toLowerCase(Locale.ROOT);
        for (Song song : songs) {
            if (favoritesOnly && !favorites.contains(song.id)) continue;
            if (!favoritesOnly && !"All songs".equals(categoryFilter)
                    && !song.category.equals(categoryFilter)) continue;
            if (!search.isEmpty() && !song.searchableText().contains(search)) continue;
            result.add(song);
        }
        return result;
    }

    private List<String> categories() {
        Set<String> found = new LinkedHashSet<>();
        found.add("All songs");
        for (Song song : songs) if (!song.category.trim().isEmpty()) found.add(song.category);
        return new ArrayList<>(found);
    }

    private void toggleFavorite(Song song) {
        if (favorites.contains(song.id)) favorites.remove(song.id);
        else favorites.add(song.id);
        prefs.edit().putStringSet("favorites", new HashSet<>(favorites)).apply();
    }

    private TextView actionButton(String label, boolean primary) {
        TextView button = text(label, 13, primary ? Color.WHITE : accentColor, true);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(11), 0, dp(11), 0);
        button.setBackground(rounded(primary ? accentColor : surfaceColor, dp(13),
                primary ? accentColor : borderColor));
        button.setClickable(true);
        return button;
    }

    private void addLabel(LinearLayout parent, String label, int size, int color, boolean bold) {
        TextView view = text(label, size, color, bold);
        view.setLetterSpacing(0.08f);
        parent.addView(view);
    }

    private TextView text(String value, float size, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setGravity(Gravity.CENTER_VERTICAL);
        return view;
    }

    private LinearLayout verticalLayout() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private LinearLayout horizontalLayout() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        return layout;
    }

    private GradientDrawable rounded(int color, int radius, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(radius);
        if (strokeColor != Color.TRANSPARENT) drawable.setStroke(dp(1), strokeColor);
        return drawable;
    }

    private LinearLayout.LayoutParams margins(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return params;
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onBackPressed() {
        if ("detail".equals(page)) {
            renderHome();
        } else if ("about".equals(page) || "favorites".equals(page)) {
            favoritesOnly = false;
            renderHome();
        } else {
            super.onBackPressed();
        }
    }
}
