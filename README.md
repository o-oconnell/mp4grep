mp4grep
-------
mp4grep is a tool that transcribes and searches audio files, caching the results for fast repeated searches. Out of the box, it only supports single-channel, 16000 Hz wav files. mp4grep ships with `mp4grep-convert` which converts mp3, mp4, ogg, webm, mov, wav, and avi to the correct format.

### Screenshots
![all](https://github.com/o-oconnell/mp4grep/blob/main/screenshots/mp4grep-example.png)
![help](https://github.com/o-oconnell/mp4grep/blob/main/screenshots/mp4grep-help.png)

### Compatible transcription models
mp4grep depends on [Vosk](https://alphacephei.com/vosk/) to transcribe audio. You can download models from Vosk's [official list](https://alphacephei.com/vosk/models).

### Installation
The [latest release](https://github.com/o-oconnell/mp4grep/releases) provides a pre-built executable for x86 Linux. You can also refer to the most current [build instructions](https://github.com/o-oconnell/mp4grep/releases/tag/0.1.3-linux), which require installing the OCaml compiler.

### mp4grep-convert
The mp4grep executable only takes single-channel, 16000 Hz wav files as input. Running `make install` also provides you with `mp4grep-convert`, which is a Bash script that will take directories or audio files as its arguments, extract audio files from directories, and convert them to wav files using ffmpeg.

### Dependencies
If you only want to install, the [latest release](https://github.com/o-oconnell/mp4grep/releases) provides a pre-built executable for x86 Linux. You'll need `ffmpeg` and `gcc` in this case. If you want to build from source, you'll need more dependencies. The latest build instructions are [here](https://github.com/o-oconnell/mp4grep/releases/tag/0.1.3-linux).

### OCaml
mp4grep was previously written in Java, and later in C++. Although we learned a lot from using those languages, we've moved to OCaml because we think its robustness will help mp4grep to survive and improve with time. Since mp4grep wraps Vosk, which wraps Kaldi, OCaml has helped to solidify mp4grep's logic and isolate confusing errors to a small portion of C code. The OCaml ecosystem is unfamiliar to most people: if you're building from source, it's important to be aware of which compiler you are using and where dependencies are stored. We recommend following the latest build instructions and using Opam.

### Why mp4grep-convert?
Prior versions of mp4grep came bundled with a ffmpeg executable or made calls to ffmpeg in the user's shell. Both caused compatibility issues and hidden transcription errors; this method was also insecure and unpredictable. Although it's inconvenient to convert files before transcribing them, we thought that the alternative was worse. New versions will separate these concerns until we can find a better option.

### Pull requests
Pull requests are welcome. Please open a pull request if you have a bug to fix or a cool idea.

### Platforms
mp4grep currently supports Linux.

