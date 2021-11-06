mp4grep
-------
mp4grep is a search tool that transcribes and searches audio and video files for a regex pattern.
By default, mp4grep will locate searchable files in directories that are provided as arguments.
Currently, mp4grep only supports Linux and requires JRE 11+ (sudo apt install openjdk-11-jre on Linux). 
mp4grep is written in Java, so I plan to 
make use of the JVM to support macOS and Windows in future.

### Screenshot of search results

### Compatible transcription neural networks
mp4grep depends on [Vosk](https://alphacephei.com/vosk/) to transcribe audio,
which has an [official list](https://alphacephei.com/vosk/models) of supported models.
By default, mp4grep ships with a 40 MB lightweight English model. If you want to transcribe 
dialects of English or other languages with accuracy, you will need to use a different model.

### Why should I use mp4grep?
* It allows you to search audio and video, instead of just text.
* Some of the use cases provided by other search tools like pdfgrep can be served better with mp4grep:
for example, instead of searching a textbook for an example problem, you can search a recording of your class (if your professor is alright with it).
* It caches transcription results, so if you transcribe a video once (which may take some time),
searching that video again will be instantaneous. mp4grep hashes the contents of transcribed files, so you can move or rename the file and you will still be able to quickly search its contents.
* Although transcription takes some time, mp4grep is multithreaded on separate inputs. You can transcribe multiple audio files more quickly if you transcribe them simultaneously. 

### Why should I not use mp4grep?
* You aren't technically allowed to download a copy of the audio/video you want to search.
* If you are using a language that is not supported well by a Vosk model, or you need high
transcription accuracy for an English accent that a neural network does not exist for
(mp4grep still finds keywords pretty well, so it could still be useful)
* mp4grep doesn't work in your Linux environment (please open an issue!)
* Transcription accuracy is too low (please raise an issue with [Vosk on Github](https://github.com/alphacep/vosk-api)!)

### Installation

### User guide
