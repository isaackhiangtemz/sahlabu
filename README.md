# SA HLABU

An original, offline-first Android hymnbook starter app for The Salvation Army Community. Song content is bundled in the app, and the project requests no internet permission. The included songs are editable placeholders, not a real hymn collection.

## Open and run

1. Install Android Studio and its Android SDK.
2. Open the `SA-HLABU` folder in Android Studio.
3. Let Android Studio sync the project. The first sync may need internet access to download Gradle and the Android Gradle Plugin; the installed app does not need internet to browse songs.
4. Select an emulator or connected Android phone and choose **Run**.
5. To create an APK, use **Build → Build APK(s)** in Android Studio.

The project uses Java, Android Gradle Plugin 9.3.2, Gradle 9.5.0, compile SDK 37, target SDK 34, and minimum Android API 23. Use the Gradle JDK bundled with Android Studio. On Windows, you can also run `gradlew.bat assembleDebug` from the project folder.

## Add and edit songs

Edit `app/src/main/assets/songs.json`. The file is UTF-8, so Mizo characters and diacritics can be entered directly. Keep it as a JSON array, with commas between song objects. Give each song a unique `id` and `number`.

```json
{
  "id": "my-song-004",
  "number": "004",
  "title": "Your song title",
  "category": "Praise",
  "language": "Mizo",
  "verses": ["First verse text", "Second verse text"],
  "chorus": "Chorus text, or an empty string",
  "solfa": "d  r  m  f  | s  s  l  s",
  "musicNotes": "[C] Chord symbols can be placed in brackets.",
  "originalKey": "C",
  "attribution": "Author, source, and permission details"
}
```

Fields `verses`, `chorus`, `solfa`, `musicNotes`, and `attribution` may be empty if a song does not use them. `verses` can be an array of verse strings or one string. `solfa` can be a string or an array of lines. Each song can have its own language and category.

For songs with chord symbols, wrap each chord in square brackets, for example `[C]`, `[Am]`, or `[G7]`. Choosing a different display key transposes bracketed chord symbols in verses, chorus, and music notes. Solfa remains relative. Unbracketed notation is displayed as entered. If there is no `originalKey`, the key selector is hidden.

After editing the song data, sync or rebuild the app. Favorites, theme choice, and each song's selected display key are saved on the device.

## Included features

- Search by number, title, category, language, or lyric text.
- Browse category chips and a full song list.
- Favorite songs saved locally on the device.
- Large, readable song pages, previous/next buttons, and swipe navigation.
- Optional verse, chorus, Solfa, music notes, key selection, and attribution.
- Share a formatted hymn as text using Android's share menu.
- Light and dark themes.
- No sign-in, backend, analytics, or internet permission.

The project uses original generic branding and does not include an official Salvation Army emblem.

