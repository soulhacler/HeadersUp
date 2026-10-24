# HeadersUp ⚽

A free, open-source **Heads Up-style party game for Android**, built entirely around famous footballers.

Hold the phone on your forehead with the screen facing out. Your mates describe the footballer on screen, and you tilt to answer:

- **Tilt down**: correct
- **Tilt up**: pass

No accounts, no adverts, no in-app purchases and no internet needed.

## Features

- More than 750 real footballers: current stars, retired players and legends from around the world
- Six categories: All Players, Premier League Legends, Current Stars, World Football Legends, England Players and Difficult Players
- 30, 60 or 90 second rounds with an on-screen countdown
- Tilt detection using the phone's gravity or accelerometer sensor, plus on-screen **Pass** and **Correct** buttons as a fallback
- A sound and a vibration for every correct answer and pass
- No repeats within a round: each round shuffles the category's names and works through them in order
- End-of-round score, with lists of the players you got and the players you passed
- Dark, football-themed landscape interface with bright green accents
- Works fully offline, and the only permission it asks for is vibrate

## Install the APK on your phone

1. On your phone, open this repository's **Releases** page and download `HeadersUp-debug.apk` from the **latest** release.
   You can also open the **Actions** tab, pick the newest successful **Build APK** run and download the `HeadersUp-debug-apk` artifact. It's a zip file, so you'll need to unzip it first.
2. Open the downloaded file. Android will ask you to allow installs from your browser or file manager the first time; allow it.
3. Tap **Install**, then open **HeadersUp**.

The APK is a debug build, signed with a standard debug key. That's fine for installing on your own phone.

## How to play

1. Choose a category and a round length, then press **KICK OFF**.
2. Put the phone on your forehead, screen facing outwards, during the 3-second countdown.
3. Everyone else gives clues without saying the name.
4. Tilt the phone **down** (screen towards the floor) when you guess correctly, or **up** (screen towards the ceiling) to pass. Bring it back upright before the next tilt.
5. When the whistle goes, check your score and the lists of players you got and passed, then press **PLAY AGAIN**.

## Building from source

### Requirements

- JDK 17 or newer
- Android SDK with platform 35 (Android Studio Ladybug or newer installs this for you)

### Android Studio

Open the project folder, wait for Gradle to sync, then press **Run** with your phone connected (USB debugging on) or an emulator running.

### Command line

```bash
# Tell Gradle where the Android SDK is, if ANDROID_HOME isn't already set
echo "sdk.dir=$HOME/Android/Sdk" > local.properties

./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`. To install it on a connected phone:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Continuous integration

Every push runs `.github/workflows/build.yml`. It validates the player database, builds the debug APK and uploads it as a workflow artifact. Pushes to the default branch also update the **latest** release with the new APK.

## Project layout

```
app/src/main/
├── assets/players.json            # The footballer database
├── java/io/github/soulhacler/headersup/
│   ├── MainActivity.kt            # Entry point and screen switching
│   ├── data/Players.kt            # Player model, categories, JSON loading
│   ├── game/GameViewModel.kt      # Round logic, timer, scoring
│   ├── game/TiltDetector.kt       # Sensor-based tilt detection
│   ├── game/Feedback.kt           # Sounds and vibration
│   └── ui/                        # Jetpack Compose screens and theme
└── res/                           # Icon, theme, strings
tools/validate_players.py          # Checks the database (run in CI)
```

## Adding or editing footballers

Edit `app/src/main/assets/players.json`. Each entry has a name and zero or more tags:

```json
{ "name": "Ian Wright", "tags": ["premier_league_legend", "england"] }
```

| Tag | Category |
| --- | --- |
| *(any player)* | All Players |
| `premier_league_legend` | Premier League Legends |
| `current` | Current Stars |
| `world_legend` | World Football Legends |
| `england` | England Players |
| `difficult` | Difficult Players |

Then run `python3 tools/validate_players.py` to check for duplicates and typos.

## Licence

[MIT](LICENSE). Use it, change it and share it freely.
