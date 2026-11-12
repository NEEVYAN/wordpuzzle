# Word Puzzle

A Wordscapes-style word game for Android. You swipe across a wheel of letters to make words, and the words fill up a crossword grid above it. I wrote it in Kotlin with XML layouts, for the Android game developer assessment.

## Running it

Open the folder in Android Studio, let Gradle sync, and hit Run. It needs Android 7.0 (API 24) or higher. If you just want an APK, use Build > Build Bundle(s) / APK(s) > Build APK(s).

I built it against AGP 8.4.1 and Gradle 8.6, so if your Android Studio complains about versions, that's the first thing to check.

## What's in it

- A crossword grid on top and a letter wheel (3 to 5 letters) at the bottom
- Swiping across the wheel draws a line that follows your finger
- Different feedback for a grid word, a bonus word, a word you already found, and a wrong word
- When the grid is full, the next level starts by itself
- 12 levels, loaded from a JSON file
- You get 500 coins every time you finish a level
- Music and sound effects, each with its own switch in Settings
- English, Hindi and Spanish
- A Support section in Settings with a FAQ, Privacy Policy and Terms of Service

## How it's put together

I used MVVM with one Activity and a stack of Fragments: Home, then Level Select, then the Game screen. Settings opens from Home or from the pause menu. Pause is just an overlay on the Game screen, not a separate screen.

There's a single `GameViewModel`, shared by all the screens. It holds the game state (current level, wheel letters, found words, bonus words, paused or not) and decides what counts as a valid word. The fragments only draw whatever state they're given and pass taps back to the ViewModel. They collect the state with `repeatOnLifecycle`, and clear their view references in `onDestroyView`, so nothing hangs around after a screen closes.

A few things I paid attention to because the assignment asked for them:

- **Back button.** Inside a level, back pauses the game, and back again resumes it. On the other screens it works as normal. I also wrote a small `push()` helper that ignores a second tap on the same button, so you can't end up with two copies of the same screen.
- **Rotation.** The ViewModel survives it, and the landscape layout has its own XML file.
- **Leaving the app.** Progress is saved to SharedPreferences every time something changes, so closing or backgrounding the app doesn't lose anything. The game also pauses itself when the app goes to the background, but not when you just rotate the phone.
- **Language.** Changing it uses AppCompat's per-app locale support. The activity restarts, but the ViewModel and the back stack stay.

## The swipe

The letter wheel (`LetterWheelView`) is one custom View that draws everything on a Canvas. There are no child views for the letters.

When your finger moves, I don't only check where it is now. Android batches fast touch events, so I take all the earlier points from the batch too and walk along the path between them in small steps, checking each step against the letters. That's what stops a quick swipe from jumping over a letter.

Other rules:

- A letter can only be used once per swipe.
- If you slide back onto the previous letter, the last one is removed, so you can fix a mistake without lifting your finger.
- Selected letters grow slightly, and you get a small haptic tick.
- When you lift your finger, the word goes to the ViewModel, which says whether it was valid, bonus, already found or wrong. The screen then plays the matching animation and sound.
- If the wheel gets disabled during a swipe (for example when you pause), the swipe is cancelled.

## Level data

Levels are in `app/src/main/assets/levels.json`. Each one looks like this:

```json
{
  "letters": "STAR",
  "words": ["STAR", "RATS", "ARTS", "TSAR", "SAT", "RAT", "ART", "TAR"],
  "bonus": ["TARS"]
}
```

`letters` is what's on the wheel, `words` are the words that go in the grid, and `bonus` are extra valid words that don't. I don't store the grid layout. `CrosswordGenerator` builds it when the level loads: it places the longest word first, then crosses the other words onto it wherever they share a letter, and rejects spots where words would touch sideways. Any word it can't fit just becomes a bonus word.

So to add a level, you only add an entry to the JSON. Every word has to be makeable from the wheel letters, or it'll never be found.

## Sound

The sound effects and music are short WAV files in `res/raw`. I generated them myself, so they're simple. If you want better ones, drop in files with the same names and nothing else needs to change.

## Things I know aren't perfect

- If Android kills the app completely, your found words and coins come back, but the wheel order and pause state don't.
- Levels are read on the main thread when the app starts. The file is tiny, so it's fine here, but I'd move it off the main thread for a bigger game.
- The Privacy Policy and Terms are generic text I wrote as a starting point. The contact email in them is a placeholder, and they should be properly reviewed before this goes on the Play Store.
- I wrote the Hindi and Spanish translations myself, so they could use a native speaker's check.

## contact 
for the time being i added the contact as support@wordpuzzle.example i can also add neerajcoc1455@gmail.com
