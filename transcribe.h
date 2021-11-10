#ifndef PERG_TRANSCRIBE_H
#define PERG_TRANSCRIBE_H

#include <cstdio>

/* struct to group the paths corresponding to a given transcription call */
struct transcript_location {
    const char* text;
    const char* timestamp;
};

/* struct to group the file pointers correspondingto a given transcription call */
struct transcript_streams {
    FILE* text;
    FILE* timestamp;
};

/* Feeds audio file to vosk and records output to cache_paths. Returns 0 for success. */
int do_transcription(const char* model_path, const char* audio_path, transcript_location cache_paths);

#endif /* PERG_TRANSCRIBE_H */
