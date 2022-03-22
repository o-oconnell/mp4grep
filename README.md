mp4grep
-------
mp4grep is a tool that transcribes and searches audio files, caching the results for fast repeated searches. Out of the box, it only supports single-channel, 16000 Hz wav files, so mp4grep ships with `mp4grep-convert` which converts mp3, mp4, ogg, webm, mov, wav, and avi to the correct format.

### Screenshots
![all](https://github.com/o-oconnell/mp4grep/blob/main/screenshots/mp4grep-example.png)
![help](https://github.com/o-oconnell/mp4grep/blob/main/screenshots/mp4grep-help.png)

### Compatible transcription models
mp4grep depends on [Vosk](https://alphacephei.com/vosk/) to transcribe audio. You can download models from Vosk's [official list](https://alphacephei.com/vosk/models).

### Installation
1. Install [Opam](https://opam.ocaml.org/)

2. Create a new switch (version of the compiler) by running `opam switch create mp4grep 4.12.0+domains+effects`. This version is necessary as mp4grep will be updated to take advantage of multicore domains in OCaml.

3. Install parmap on your new switch: `opam install parmap`.

4. Download [mp4grep](https://github.com/o-oconnell/mp4grep/tags), untar/zip it, and cd into its directory.

5. Execute `source configure.sh --prefix [location to install mp4grep]`. On Linux a good choice might be `~/.local/bin`, which is often in your $PATH.

6. Run `make install`.

7. You will need to specify your Vosk-compatible transcription model and directory for cached transcriptions by setting MP4GREP_MODEL and MP4GREP_CACHE. You'll probably want to export them in your .bashrc or .zshrc as well: `export MP4GREP_MODEL=/full/path/of/model`, `export MP4GREP_CACHE=/full/path/of/cache/dir`.

### mp4grep-convert
The mp4grep executable only takes single-channel, 16000 Hz wav files as input. Running `make install` also provides you with `mp4grep-convert`, which is a Bash script that will take directories or audio files as its arguments, extract audio files from directories, and convert them to wav files using ffmpeg.

### Dependencies
OCaml 4.12.0+domains+effects, parmap 1.2.4, and ffmpeg. The Makefile assumes that you have installed parmap using Opam, and looks under OPAM_INSTALL_PREFIX for it. You will have to modify the topmost `ocamlc` command in the Makefile if you have installed it another way. 

### OCaml
mp4grep was previously written in Java, and later in C++. Although we learned a lot from using those languages, we've moved to OCaml because we think its robustness will help mp4grep to survive and improve with time. The OCaml community is still new, so it's important to be aware of which compiler you are using and where dependencies are stored. We recommend following the installation instructions above, and using Opam. 

### Why mp4grep-convert?
Prior versions of mp4grep came bundled with a ffmpeg executable or made calls to ffmpeg in the user's shell. Both caused compatibility issues and hidden transcription errors; this method was also insecure and unpredictable. Although it's inconvenient to convert files before transcribing them, we thought that the alternative was worse. New versions will separate these concerns until we can find a better option.

### Pull requests
Pull requests are welcome. Please open a pull request if you have a bug to fix or a cool idea.

### Platforms
mp4grep currently supports Linux.

