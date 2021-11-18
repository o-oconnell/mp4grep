#ifndef MEDIAGREP_TRANSCRIBE_H
#define MEDIAGREP_TRANSCRIBE_H

#include <cstdio>

const int PATH_LENGTH = 256; // hardcoded for now. TODO: replace with #ifdef for different platforms.
const bool SHOW_FFMPEG_OUTPUT = false; // TODO: move this to config.h file

/* struct to group the paths corresponding to a given transcription call */
struct transcript_location {
    const char* text;
    const char* timestamp;
};

/* struct to group the file pointers corresponding to a given transcription call */
struct transcript_streams {
    FILE* text;
    FILE* timestamp;
};

/* Feeds audio file to vosk and records output to files in cache_paths. Returns 0 for success. */
int transcribe_audio(const char* model_path, const char* media_path, transcript_location cache_paths);

#endif /* MEDIAGREP_TRANSCRIBE_H */
