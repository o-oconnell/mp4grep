#include <cstdio>
#include <string>
#include <cstring>
#include <climits>
#include <mutex>
#include <iostream>
#include "transcribe.h"

/* Libs */
#include "include/vosk_api.h"
#include "include/jsmn.h"

/* struct to group the file pointers corresponding to a given transcription call */
struct transcript_streams {
    FILE* text;
    FILE* timestamp;
};

std::mutex rip_lock;

/* Subroutines */
int make_cache_directory();
int rip_audio_from_media(const char* input, char** output_path);
int transcribe_audio(const char* model_path, const char* vosk_audio_path, transcript_location* cache_paths, struct progress_bar_wrapper* pbar);
int write_vosk_json_to_files(const char* vosk_json, transcript_streams* cache_files, struct progress_bar_wrapper* pbar);


/* Vosk Constants
 * Audio format used by vosk assumed to be single channel, mono audio, sampled at 16000hz, with signed 16 bit samples
 */
const float VOSK_SAMPLING_RATE = 16000.0;
const char* VOSK_SAMPLE_CODEC = "pcm_s16le";
const int VOSK_CHANNEL_COUNT = 1;
const int VOSK_AUDIO_BUFFER_SIZE = 4096;


/* TEMPORARY SOLUTION: DEFINED IN BOTH TRANSCRIBE.CC AND MEDIAGREP.CC */
const char* TRANSCRIBE_CACHE_DIRECTORY = ((getenv("MEDIAGREP_CACHE")) ? (getenv("MEDIAGREP_CACHE")) : (".cache/"));

int transcribe(const std::string &model_str, const std::string &media_str, struct transcript_location* output, struct progress_bar_wrapper* pbar) {
    
    const char* model_path = model_str.c_str();
    const char* media_path = media_str.c_str();

    /* BUILD CACHE KEY */
    transcript_cache_key this_call;
    {
        struct stat model_info;
        if (stat(model_path, &model_info) != 0) {
            // Error: failed to stat model. TODO: handle
        }

        struct stat media_info;
        if (stat(media_path, &media_info) != 0) {
            // Error: failed to stat media. TODO: handle
        }
	
	this_call.model_inode_str = std::to_string(static_cast<long int>(media_info.st_ino));
	this_call.media_inode_str = std::to_string(static_cast<long int>(media_info.st_ino));
	this_call.media_modified_str = std::to_string(static_cast<long int> (media_info.st_mtime));
	
        this_call.model_inode = model_info.st_ino;
        this_call.media_inode = media_info.st_ino;
        this_call.media_modified = media_info.st_mtime;
    }

    /* SET OUTPUT CACHE PATHS */
    {
        auto create_path = [&](char* path, const char* extension) {
	    sprintf(path, "%s%zu%s",
		    TRANSCRIBE_CACHE_DIRECTORY,
		    std::hash<std::string>{}(this_call.media_inode_str + this_call.model_inode_str + this_call.media_modified_str),
		    extension);
	};

        create_path(output->text, TRANSCRIBE_CACHE_TEXT);
        create_path(output->timestamp, TRANSCRIBE_CACHE_TIMESTAMPS);
    }

    /* CHECK IF FILE IS IN CACHE */
    {
        make_cache_directory();
        struct stat not_used;
        if ((stat(output->text, &not_used) == 0)
        && (stat(output->timestamp, &not_used)) == 0) {

	    /* SET PROGRESS BAR FINISHED */
	    pbar->current = pbar->total;
            return 0; // files exist, don't need to transcribe again.
        }
    }

    /* RIP AND RESAMPLE AUDIO FROM MEDIA */
    char vosk_audio_path_[PATH_LENGTH];
    char* vosk_audio_path = vosk_audio_path_;

    rip_lock.lock();
    rip_audio_from_media(media_path, &vosk_audio_path); // TODO: don't ignore error code
    rip_lock.unlock();

    transcribe_audio(model_path, vosk_audio_path, output, pbar); // TODO: don't ignore error code

    /* DELETE CONVERTED AUDIO */
    remove(vosk_audio_path);

    /* SET PROGRESS BAR TO FINISHED */
    pbar->current = pbar->total;
    
    return 0;
}


/* Make directory for cache if it doesnt exist. */
int make_cache_directory() {
    /* CALL MKDIR
     * Note: This code is not very portable. An #ifdef solution for different
     * platforms could be added once we have those figured out.
     */
    const char* mkdir_command_template = "mkdir -p %s";
    char command[16+PATH_LENGTH];
    sprintf(command, mkdir_command_template, TRANSCRIBE_CACHE_DIRECTORY);
    return system(command);
}


/* Gets the audio from the input media file, and formats it correctly for vosk. Returns success status. */
int rip_audio_from_media(const char* input, char** output_path) {
    /* SET PATH OF CONVERTED FILE */
    std::string s(input);
    std::size_t last_slash = s.find_last_of("/");
    
    sprintf(*output_path, "%s%s%s",
	    TRANSCRIBE_CACHE_DIRECTORY,
	    (last_slash < (s.length() - 1) ? s.substr(last_slash + 1).c_str() : s.c_str()),
	    "_converted.wav");

    /* CALL FFMPEG WITH CORRECT PARAMETERS FOR VOSK
     * Note: This code is not very portable. An #ifdef solution for different
     * platforms could be added once we have those figured out.
     */
    char command[64+PATH_LENGTH+PATH_LENGTH];
    // TODO: CROSS PLATFORM SUPPRESSION OF ERROR/OUTPUT
    const char* ffmpeg_command_template = "ffmpeg%s -i %s -acodec %s -ac %d -ar %f %s > /dev/null 2>/dev/null";
    const char* ffmpeg_show_output = (SHOW_FFMPEG_OUTPUT)? "" : " -loglevel quiet";
    sprintf(command, ffmpeg_command_template, ffmpeg_show_output, input, VOSK_SAMPLING_RATE, VOSK_SAMPLE_CODEC, VOSK_CHANNEL_COUNT, *output_path);
    //    printf("%s\n",command);
    return system(command);
}


/* Transcribes contents of vosk_audio using the model, outputs text and timestamps to cache. */
int transcribe_audio(const char* model_path, const char* vosk_audio_path, transcript_location* cache_paths, struct progress_bar_wrapper* pbar) {
    /* OPEN TRANSCRIPTION STREAMS */
    transcript_streams cache_streams;
    cache_streams.text = fopen(cache_paths->text, "w");
    cache_streams.timestamp = fopen(cache_paths->timestamp, "w");

    /* INIT VOSK */
    if (!SHOW_VOSK_OUTPUT) vosk_set_log_level(-1); // disables vosk's logging, has to be done before things are initialized.
    VoskModel* model = vosk_model_new(model_path); // NOTE: only one model needed for multiple recognizers. When multi-threading pull this out and pass each recognizer the same model.
    VoskRecognizer* recognizer = vosk_recognizer_new(model, VOSK_SAMPLING_RATE);
    vosk_recognizer_set_words(recognizer, true);
    int is_final; // variable used by recognizer to indicate this is the final part of a transcription.

    /* OPEN CONVERTED AUDIO FILE */
    FILE* audio = fopen(vosk_audio_path, "r");
    if (!audio) {
        return -1; // Error: failed to open converted audio file. TODO: logging
    }

    /* BUFFER TO FEED AUDIO INPUT TO VOSK */
    char audio_buffer[VOSK_AUDIO_BUFFER_SIZE+1];
    size_t bytes_read;

    /* GET OUTPUT FROM VOSK  */
    auto write_vosk_result = [&]() {
        const char* vosk_json = vosk_recognizer_final_result(recognizer);
        write_vosk_json_to_files(vosk_json, &cache_streams, pbar);
    };

    while(true) {
        /* READ TO BUFFER */
        bytes_read = fread(audio_buffer, sizeof(char), VOSK_AUDIO_BUFFER_SIZE, audio);
        if (!(bytes_read > 0))
            break; // EOF, exit loop

        /* FEED BUFFER TO VOSK */
        is_final = vosk_recognizer_accept_waveform(recognizer, audio_buffer, bytes_read);

        /* WRITE TRANSCRIPTION RESULTS TO CACHE */
        if (is_final) {
            write_vosk_result();
        }
    }

    /* WRITE THE LAST TRANSCRIPTION RESULT */
    write_vosk_result();

    /* CLEANUP VOSK RESOURCES */
    vosk_recognizer_free(recognizer);
    vosk_model_free(model);

    /* CLOSE FILE STREAMS */
    fclose(audio);
    fclose(cache_streams.text);
    fclose(cache_streams.timestamp);

    return 0; // success
}


/* Writes the json that vosk outputs to the cached files.
    Parses inputs formatted like this:
    {
      "result" : [{
          "conf" : 0.280911,
          "end" : 7.408000,
          "start" : 7.348000,
          "word" : "i"
        }, {
          "conf" : 0.280911,
          "end" : 7.557229,
          "start" : 7.408000,
          "word" : "think"
      }]
    }
*/
int write_vosk_json_to_files(const char* vosk_json, transcript_streams* cache_files, struct progress_bar_wrapper* pbar) {
    const int VOSK_JSON_MAX_TOKENS = 1024;
    jsmntok_t tokens[VOSK_JSON_MAX_TOKENS];

    int tokens_read;
    {
        jsmn_parser parser;
        jsmn_init(&parser);
        tokens_read = jsmn_parse(&parser, vosk_json, strlen(vosk_json), tokens, VOSK_JSON_MAX_TOKENS);
        if ((tokens_read <= 0) || (tokens[0].type != JSMN_OBJECT)) {
            return -1; // TODO: log json parse failure
        }
    }

    auto write_text = [&](jsmntok_t* token) {
        const char* WRITE_FORMAT = "%.*s\n";
        fprintf(cache_files->text, WRITE_FORMAT, token->end-token->start, vosk_json+token->start);
    };

    char conversion_buffer[16];  // small buffer to put strings from vosk into while converting them to timestamp format.
    auto write_timestamp = [&](jsmntok_t* token) {
        /* SLICE OUT NUMBER FROM VOSK OUTPUT */
        strncpy(conversion_buffer, vosk_json+token->start, token->end-token->start);
        conversion_buffer[token->end-token->start] = '\0';

        /* CONSTANTS FOR TIME UNITS */
        const int SECOND = 1;
        const int MINUTE = 60*SECOND;
        const int HOUR = 60*MINUTE;

        /* GET TIME UNITS FROM JSON STRING */
	/* UPDATE THE NUMBER OF SECONDS IN THE PROGRESS BAR STRUCT */
        float raw = atof(conversion_buffer);
        int total_seconds = int(raw); if (total_seconds > raw) total_seconds--; // fast replacement for floor()

	pbar->current = total_seconds;

        auto get = [&total_seconds](int unit) {
            int out = total_seconds/unit;
            total_seconds = total_seconds % unit;
            return out;
        };
        int hours = get(HOUR);
        int minutes = get(MINUTE);
        int seconds = total_seconds;

        /* FORMAT AS TIMESTAMP */
        if (hours > 0) {
            fprintf(cache_files->timestamp, "%.2i:%.2i:%.2i\n", hours, minutes, seconds);
        } else {
            fprintf(cache_files->timestamp, "%.2i:%.2i\n", minutes, seconds);
        }
    };

    auto jsoneq = [&](int tok_index, const char *s) {
        jsmntok_t tok = tokens[tok_index];
        if ((tok.type == JSMN_STRING)
        && ((int)strlen(s) == tok.end - tok.start)
        && (strncmp(vosk_json + tok.start, s, tok.end - tok.start) == 0)) {
            return 1;
        }
        return 0;
    };

    for (int i = 1; i < tokens_read; i++) {
        /* SKIP IRRELEVANT FIELDS */
        if (jsoneq(i, "result")) {
            if (tokens[i + 1].type != JSMN_ARRAY) {
                return -1; // shouldn't happen
            }
            /* LOOP OVER WORD STRUCTS */
            for (int j = 0, obj = 0; obj < tokens[i + 1].size; j++) {
                if (tokens[i + 1 + j].type != JSMN_OBJECT) {
                    continue; // shouldn't happen
                }

                /* LOOP THROUGH STRUCT FIELDS */
                for (int k = 0, field = 0; field < tokens[i + 1 + j].size; k++) {
                    if (jsoneq(i + 1 + j + 1 + k, "word")) {
                        jsmntok_t* word_token = &tokens[i + 1 + j + 1 + k + 1];
                        write_text(word_token);
                    }

                    if (jsoneq(i + 1 + j + 1 + k, "start")) {
                        jsmntok_t* start_timestamp_token = &tokens[i + 1 + j + 1 + k + 1];
                        write_timestamp(start_timestamp_token);
                    }

                    /* ignoring end time and confidence values for now... */

                    field++;
                    k++; // skip
                }
                obj++;
                j += tokens[i + 1 + j].size;
            }
            break; // break if we already looped over results;
        }
    }
    return 0; // success
}
