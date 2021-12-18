#ifndef MEDIAGREP_TRANSCRIBE_H
#define MEDIAGREP_TRANSCRIBE_H

#include <sys/stat.h> // TODO: replace with #ifdef for different platforms. stat.h should be fine for unix.
#include <cstdlib>
#include <string>

const int PATH_LENGTH = 512; // hardcoded for now. TODO: replace with #ifdef for different platforms.

const bool SHOW_FFMPEG_OUTPUT = false; // TODO: move these to config.h file
const bool SHOW_VOSK_OUTPUT = false;

/* key to uniquely identify different transcriptions. */
struct transcript_cache_key {
    ino_t model_inode;
    ino_t media_inode;
    time_t media_modified;
};

/* Cache Constants */
#define TRANSCRIBE_CACHE_TEXT "_text"
#define TRANSCRIBE_CACHE_TIMESTAMPS "_time"

const int CACHE_PATH_SIZE = 5000; 

/* struct to group the paths corresponding to a given transcription call. */
struct transcript_location {
    char text[CACHE_PATH_SIZE];
    char timestamp[CACHE_PATH_SIZE];
};

/* Feeds media file to vosk and records output to returned files. */
int transcribe(const char* model_path, const char* media_path, transcript_location* output);

#endif /* MEDIAGREP_TRANSCRIBE_H */
