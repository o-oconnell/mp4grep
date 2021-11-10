mp4grep
-------
mp4grep is a search tool that transcribes and searches audio and video files for a regex pattern. mp4grep isn't just for mp4 files! It also supports mp3, mp4, ogg, webm, mov, and wav.

### Screenshot of search results

### Compatible transcription models
mp4grep depends on [Vosk](https://alphacephei.com/vosk/) to transcribe audio.
By default, mp4grep ships with a 40 MB lightweight English model. If you want to transcribe 
dialects of English or other languages with accuracy, you will need to use a different model.
You can download other models from Vosk's [official list](https://alphacephei.com/vosk/models).

### Installation
1. Download the latest JRE, if you don't already have JRE 11+ installed:
`sudo apt install openjdk-17-jre`

2. [Download](https://github.com/o-oconnell/mp4grep/releases) mp4grep and unzip it in the location that you want to install it: `unzip mp4grep-v0.1.0.zip`

3. Add mp4grep to your PATH, and set its environment variables: 

`cd mp4grep-v0.1.0`

`source install.sh`

This script adds mp4grep to your path and sets some environment variables.

5. Use mp4grep to search! `mp4grep "the birch canoe" harvard_sentences.mp4`

### Pull requests
Pull requests are welcome. Please open a pull request if you have a bug to fix or a cool idea.
