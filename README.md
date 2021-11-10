mp4grep
-------
mp4grep is a search tool that transcribes and searches audio and video files for a regex pattern.
By default, mp4grep will locate searchable files inside of directories. Also, mp4grep does not just work on mp4 files! Supported extensions are mp2, mp3, mp4, ogg, webm, mov, and wav.

### Screenshot of search results

### Compatible transcription neural networks
mp4grep depends on [Vosk](https://alphacephei.com/vosk/) to transcribe audio,
which has an [official list](https://alphacephei.com/vosk/models) of supported models.
By default, mp4grep ships with a 40 MB lightweight English model. If you want to transcribe 
dialects of English or other languages with accuracy, you will need to use a different model.

### Why should I use mp4grep?
* It allows you to search audio and video, instead of just text.
* Files can be pre-cached, so transcribe your videos overnight and you'll be able to search them lightning fast in the morning.
* mp4grep is multithreaded: you can transcribe many audio files quickly.

### Installation
1. Download the latest JRE, if you don't already have JRE 11+ installed:
`sudo apt install openjdk-17-jre`

2. [Download](https://github.com/o-oconnell/mp4grep/releases) mp4grep and unzip it in the location that you want to install it: `unzip mp4grep-v0.1.0.zip`

3. Add mp4grep to your PATH, and set its environment variables: 

`cd mp4grep-v0.1.0`

`source install.sh`

This script adds mp4grep to your path and sets three environment variables:

MP4GREP_CACHE: transcription cache.

MP4GREP_CONVERTED: converted audio files - Vosk requires single-channel, 16 KHz sampling rate audio input.

MP4GREP_MODEL: transcription model directory.

5. Use mp4grep to search! `mp4grep "the birch canoe" harvard_sentences.mp4`

### Pull requests
Pull requests are welcome. Please open a pull request if you have a bug or code smell to fix.
