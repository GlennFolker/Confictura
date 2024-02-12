# Confictura

> *Dive into the past of a trauma-driven uprising.*

A [Mindustry](https://github.com/Anuken/Mindustry) mod that aims towards an enjoyable storyline spiced up with dazzling sorts of contents.

## Using this Mod

**You cannot simply download the `.zip` and add it to your mods folder**, as this is a Java mod. If you open an issue report revolving around this, I will simply ignore you and refer you to this file. <br>
<br>
Instead, you have two options:
- ### Release

  Head over to the [releases](https://github.com/GlennFolker/Confictura/releases) page. Pay attention to the described Mindustry _minimum game version_. Download the uploaded `Confictura.jar` and put it in the Mindustry mods folder:
   - On Windows, it should be `%APPDATA%\Mindustry\mods\`.
   - On Linux, it should be `$HOME/.local/share/Mindustry/mods/`.
   - On Mac, it should be `$HOME/Library/Application Support/Mindustry/mods/`.
   - On Android, see the game's built-in mod import functionality below.

  You can also use Mindustry's built-in "Import mod" button in the mods menu dialog, restart the game, and play.
- ### Bleeding-Edge

  Make sure you have a GitHub account. Head over to the [actions](https://github.com/GlennFolker/Confictura/actions) page, click the most recent successful workflow runs *(marked by green checkmark)*, scroll down to "Artifacts" section, and download the one titled `Confictura (zipped)`. As the name suggests, **you must unzip it first to extract the actual `.jar`**, then you can import it. If you open an issue report that is exactly this, I too will simply ignore you and refer you to this file. <br>
  <br>
  Be aware that the bleeding-edge builds are **highly unstable**, and might require a certain Mindustry version constraint. Do this at your own risk.

## Reporting Issues

Head over to the [issue tracker](https://github.com/GlennFolker/Confictura/issues/new) page. **Do not use this to propose new contents/mechanics**, I will instantly close it without second thoughts.

## License and Contributing

This project's source codes *(files located under `src/**`)* and assets *(files located under `assets/**`)* are licensed under [GNU GPL v3](/LICENSE), unless explicitly stated otherwise *(usually on file headers)*. The copyright notice is as follows:

> ```
> Confictura: A Mindustry Java mod.
> Copyright (C) 2024 GlennFolker
> 
> This program is free software: you can redistribute it and/or modify
> it under the terms of the GNU General Public License as published by
> the Free Software Foundation, either version 3 of the License, or
> (at your option) any later version.
> 
> This program is distributed in the hope that it will be useful,
> but WITHOUT ANY WARRANTY; without even the implied warranty of
> MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
> GNU General Public License for more details.
> 
> You should have received a copy of the GNU General Public License
> along with this program.  If not, see <https://www.gnu.org/licenses/>.
> ```

Feel free to contribute, but please take these into account:
- Follow the [Mindustry style guidelines](https://github.com/Anuken/Mindustry/blob/master/CONTRIBUTING.md#style-guidelines). This includes code formatting.
- Make sure your proposed requests work both in Desktop and Android.

### Adding Dependencies

**Never** use `implementation` for Mindustry/Arc groups and their submodules. There's a reason they're `compileOnly`; they're only present in compilation and excluded from the final JARs, as on runtime they're resolved from the game instance itself. Other JAR-mod dependencies must also use `compileOnly`. Only ever use `implementation` for external Java libraries that must be bundled with your mod.

## Building

Confictura like any other proper Mindustry Java mods is cross-platform, supporting PC (Windows, Mac, Linux) and Android. This section describes how to build the JARs for both PC and Android. Building these JARs are done through the usage of terminals: `cmd.exe` in Windows, Terminal in Mac, and if you're either on Linux or using a terminal emulator on Android such as Termux, you should already know what you're doing anyway. Following these steps should require basic terminal functionality such as `cd`.

### Desktop Build

Desktop builds are convenient for testing, but will obviously **not** work on Android, so never include this in your releases. Here's how you can build the mod:
1. Open your terminal, and `cd` to your local copy of the mod.
2. Ensure your internet connection on first or clean builds, as the project will try to fetch prerequisites from the internet.
3. Run `gradlew jar` *(replace `gradlew` with `./gradlew` on Mac/Linux)*. This should create a JAR inside `build/libs/` that you can copy over to the Mindustry mods folder to install it.
4. You can also then run `gradlew install` to automatically install the mod JAR, or even `gradlew jar install` to do both compiling and installing at once.

### Android Build

Android builds are automated on the CI hosted by GitHub Actions, so you should be able to just push a commit and wait for the CI to provide your build. If you still want to build locally, though, follow these steps.

#### Installing Android SDK
1. Install [Android SDK](https://developer.android.com/studio#command-line-tools-only), specifically the "**Command line tools only**" section. Download the tools that match your platform.
2. Unzip the Android SDK command line tools inside a folder; let's call it `AndroidSDK/` for now.
3. Inside this folder is a folder named `cmdline-tools/`. Put everything inside `cmdline-tools/` to a new folder named `latest/`, so that the folder structure looks like `AndroidSDK/cmdline-tools/latest/`.
4. Open your terminal, `cd` to the `latest/` folder.
5. Run `sdkmanager --install "platforms;android-34" "build-tools;34.0.0"`. These versions correspond to the `androidSdkVersion` and `androidBuildVersion` properties inside `gradle.properties`, which default to `34` and `34.0.0`, respectively.
6. Set environment variable `ANDROID_SDK_ROOT` as the full path to the `AndroidSDK/` folder you created, and restart your terminal to update the environments.

#### Building
1. Open your terminal, and `cd` to your local copy of the mod.
2. Run `gradlew dex`. This should create a cross-platform JAR inside `build/libs/` that isn't suffixed with `Desktop` that you can copy over to the Mindustry mods folder to install it.
3. You can also then run `gradlew installDex` to automatically install the mod JAR, or even `gradlew dex installDex` to do both compiling and installing at once.
