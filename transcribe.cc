#include <cstdio>
#include <string>
#include <cstring>

/* Libs */
#include "transcribe.h"
#include "include/vosk_api.h"
#include "include/jsmn.h"

void write_vosk_json_to_files(const char* vosk_json, transcript_streams* cache_files);

/* Vosk models are trained on this sampling rate, so it's a hard requirement
that audio inputs have exactly this same rate. */
const float VOSK_SAMPLING_RATE = 16000.0;

int do_transcription(const char* model_path, const char* audio_path, transcript_location cache_paths) {
    /* CONSTANT CONFIGS FOR VOSK */
    const int VOSK_AUDIO_BUFFER_SIZE = 4096;

    /* OPEN TRANSCRIPTION STREAMS */
    const char* CACHE_FILES_OPEN_MODE = "w";
    transcript_streams cache_streams;
    cache_streams.text = fopen(cache_paths.text, CACHE_FILES_OPEN_MODE);
    cache_streams.timestamp = fopen(cache_paths.timestamp, CACHE_FILES_OPEN_MODE);

    /* INIT VOSK */
    vosk_set_log_level(-1); // disables vosk's logging, has to be done before things are initialized.
    VoskModel* model = vosk_model_new(model_path); // NOTE: only one model needed for multiple recognizers. When multi-threading pull this out and pass each recognizer the same model.

    VoskRecognizer* recognizer = vosk_recognizer_new(model, VOSK_SAMPLING_RATE);
    vosk_recognizer_set_words(recognizer, true);
    int is_final; // variable used by recognizer to indicate this is the final part of a transcription.

    /* OPEN INPUT AUDIO FILE */
    const char* AUDIO_FILE_OPEN_MODE = "r";
    FILE* audio = fopen(audio_path, AUDIO_FILE_OPEN_MODE); if (!audio) return -1;

    /* BUFFER TO FEED AUDIO INPUT TO VOSK */
    char audio_buffer[VOSK_AUDIO_BUFFER_SIZE];
    size_t bytes_read;

    /* POINTER FOR VOSK TO OUTPUT TO */
    const char* vosk_json;

    while(true) {
        /* READ TO BUFFER */
        bytes_read = fread(audio_buffer, sizeof(char), VOSK_AUDIO_BUFFER_SIZE, audio);
        if (!(bytes_read > 0))
            break; // EOF, exit loop

        /* FEED BUFFER TO VOSK */
        is_final = vosk_recognizer_accept_waveform(recognizer, audio_buffer, bytes_read);

        /* WRITE TRANSCRIPTION RESULTS TO CACHE */
        if (!is_final) {
            vosk_json = vosk_recognizer_partial_result(recognizer);
            write_vosk_json_to_files(vosk_json, &cache_streams);
        }
    }

    /* WRITE FINAL TRANSCRIPTION RESULT */
    vosk_json = vosk_recognizer_final_result(recognizer);
    write_vosk_json_to_files(vosk_json, &cache_streams);

    /* CLEANUP VOSK RESOURCES */
    fclose(audio);
    vosk_recognizer_free(recognizer);
    vosk_model_free(model);

    /* CLOSE FILE STREAMS */
    fclose(cache_streams.text);
    fclose(cache_streams.timestamp);
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
void write_vosk_json_to_files(const char* vosk_json, transcript_streams* cache_files) {
    const int VOSK_JSON_MAX_TOKENS = 1024;
    jsmntok_t tokens[VOSK_JSON_MAX_TOKENS];
    int tokens_read;

    {
        jsmn_parser parser;
        jsmn_init(&parser);
        tokens_read = jsmn_parse(&parser, vosk_json, strlen(vosk_json), tokens, VOSK_JSON_MAX_TOKENS);
        if ((tokens_read <= 0) || (tokens[0].type != JSMN_OBJECT)) {
            return; // TODO: log failure
        }
    }

    auto write_text = [&](jsmntok_t* token) {
        const char* WRITE_FORMAT = "%.*s\n";
        fprintf(cache_files->text, WRITE_FORMAT, token->end-token->start, vosk_json+token->start);
    };

    char conversion_buffer[16];  // small buffer to put floats from vosk into while converting them to timestamp format.
    auto write_timestamp = [&](jsmntok_t* token) {
        /* SLICE OUT NUMBER FROM VOSK OUTPUT */
        strncpy(conversion_buffer, vosk_json+token->start, token->end-token->start);
        conversion_buffer[token->end-token->start] = '\0';

        /* DEFINITIONS FOR TIME UNITS */
        const int SECOND = 1;
        const int MINUTE = 60*SECOND;
        const int HOUR = 60*MINUTE;


        /* GET TIME UNITS FROM FLOAT */
        float raw = atof(conversion_buffer);
        int nat = int(raw); if (nat > raw) nat--; // fast replacement for floor()
        auto get = [&](int unit) {
            int out = nat/unit;
            nat = nat % unit;
            return out;
        };
        
        int hours = get(HOUR);
        int minutes = get(MINUTE);
        int seconds = get(SECOND);

        /* FORMAT TO TIMESTAMP */
        if (hours > 0) {
            fprintf(cache_files->timestamp, "%.2i:%.2i:%.2i\n", hours, minutes, seconds);
        } else {
            fprintf(cache_files->timestamp, "%.2i:%.2i\n", minutes, seconds);

        }
    };

    auto jsoneq = [&](jsmntok_t *tok, const char *s) {
        if ((tok->type == JSMN_STRING)
        && ((int)strlen(s) == tok->end - tok->start)
        && (strncmp(vosk_json + tok->start, s, tok->end - tok->start) == 0)) {
            return 1;
        }
        return 0;
    };

    for (int i = 1; i < tokens_read; i++) {
        /* SKIP IRRELEVANT FIELDS */
        if (jsoneq(&tokens[i], "result")) {
            if (tokens[i+1].type != JSMN_ARRAY) {
                return; // shouldn't happen
            }
            /* LOOP OVER WORD STRUCTS */
            for (int j = 0, obj = 0; obj < tokens[i + 1].size; j++) {
                if (tokens[i + 1 + j].type != JSMN_OBJECT) {
                    continue; // shouldn't happen
                }

                /* LOOP THROUGH STRUCT FIELDS */
                for (int k = 0, field = 0; field < tokens[i + 1 + j].size; k++) {
                    if (jsoneq(&tokens[i + 1 + j + 1 + k], "word")) {
                        jsmntok_t* value = &tokens[i + 1 + j + 1 + k + 1];
                        write_text(value);
                    }

                    if (jsoneq(&tokens[i + 1 + j + 1 + k], "start")) {
                        jsmntok_t* value = &tokens[i + 1 + j + 1 + k + 1];
                        write_timestamp(value);
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
}
