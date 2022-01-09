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

    std::string model_inode_str;
    std::string media_inode_str;
    std::string media_modified_str;
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

struct progress_bar_wrapper {
    int current;
    int total;
    
    std::string filename;
    int index_into_vector_transcript_location;
};

/* Feeds media file to vosk and records output to returned files. */
int transcribe(const std::string &model_str, const std::string &media_str, struct transcript_location* output, struct progress_bar_wrapper* pbar);



#endif /* MEDIAGREP_TRANSCRIBE_H */
