#include <iostream>
#include <fstream>
#include <cstdio>
#include <string>
#include <cstring>
#include <cmath>

/* Libs */
#include "include/vosk_api.h"
#include "include/jsmn.h"

using namespace std;

// hardcoded values, TODO: remove, make configurable or input
#define INPUT_WAV "./test_files/OSR_us_000_0061_8k.wav"
#define VOSK_MODEL "./models/vosk-model-small-en-us-0.15"
#define VOSK_SAMPLING_RATE (16000.0)
#define VOSK_AUDIO_BUFFER_SIZE (4096)

// structs for vosk, TODO move somewhere else
struct transcript_location {
    const char* text;
    const char* timestamp;
};

struct transcript_streams {
    ofstream text;
    ofstream timestamp;
};

void write_vosk_json_to_files(transcript_streams* output, const char* vosk_json);

int main(int argc, const char** argv) {
    // Parse arguments
    // * flags -> need global vars to store feature flags and options
    // * mode
    // * files to grip

    transcript_location cache_paths = {
        .text = "./bin/output/text",
        .timestamp  = "./bin/output/timestamps",
    };

    /* TRANSCRIPTION BLOCK */ {
        // OPEN TRANSCRIPTION STREAMS
        transcript_streams cache_streams;
        cache_streams.text.open(cache_paths.text, ios::out);
        cache_streams.timestamp.open(cache_paths.timestamp, ios::out);

        // INIT VOSK
        vosk_set_log_level(-1); // disable logging
        VoskModel* model = vosk_model_new(VOSK_MODEL);
        VoskRecognizer* recognizer = vosk_recognizer_new(model, VOSK_SAMPLING_RATE);
        vosk_recognizer_set_words(recognizer, true);

        FILE* fin = fopen(INPUT_WAV, "r"); if (!fin) return -1;
        char audio_buffer[VOSK_AUDIO_BUFFER_SIZE+1];

        size_t bytes_read;
        const char* vosk_json;
        int is_final; // variable used by recognizer to indicate this is the final part of a transcription.
        while(true) {
            // READ TO BUFFER //
            bytes_read = fread(audio_buffer, sizeof(char), VOSK_AUDIO_BUFFER_SIZE, fin);
            if (!(bytes_read > 0))
                break; // EOF
            ///////////////////

            // FEED BUFFER TO VOSK //
            is_final = vosk_recognizer_accept_waveform(recognizer, audio_buffer, bytes_read);

            if (!is_final) {
                continue;
                // vosk_json = vosk_recognizer_result(recognizer);
            } else {
                vosk_json = vosk_recognizer_final_result(recognizer);
            }
            /////////////////////////

            // GET TEXT OUT OF JSON //
            write_vosk_json_to_files(&cache_streams, vosk_json);
        }

        // CLEANUP VOSK //
        fclose(fin);
        vosk_recognizer_free(recognizer);
        vosk_model_free(model);

        // CLEANUP STREAMS //
        cache_streams.text.close();
        cache_streams.timestamp.close();
    }

    //// Normal mode -> Transcribe, Search, Print ////
    // Transcription step
        // * convert files to format vosk understands
        // * transcribe files
        //

    // Search step
        // * Feed transcription outputs to searcher
        // * Search

    // Print step
        // Format search output and print

    return 0;
}

static int jsoneq(const char *json, jsmntok_t *tok, const char *s) {
    int ret = 0;
    if (tok->type == JSMN_STRING && (int)strlen(s) == tok->end - tok->start &&
        strncmp(json + tok->start, s, tok->end - tok->start) == 0) {
        ret = 1;
    }
    // printf("%.*s == %s? %d\n", tok->end-tok->start,json+tok->start, s, ret);
    return ret;
}
// Input like this:
/*
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
void set_buffer(char buffer[], const char* start, int count) {
    strncpy(buffer, start, count);
    buffer[count] = '\0';
}

string pad_left(int x) {
    if (x < 10) {
        return "0" + to_string(x);
    } else {
        return to_string(x);
    }
}

string make_timestamp(char buffer[]) {
    static const int MINUTE = 60;
    static const int HOUR = 60*MINUTE;

    float raw = atof(buffer);
    int nat = floor(raw);

    string out = "";

    if (nat > HOUR) {
        out += pad_left(nat/HOUR) + ":";
        nat = nat % HOUR;
    }

    if (nat > MINUTE) {
        out += pad_left(nat/MINUTE) + ":";
        nat = nat % MINUTE;
        out += pad_left(nat);
    } else {
        out += "00:"+pad_left(nat);
    }

    return out;
}

void write_vosk_json_to_files(transcript_streams* output, const char* vosk_json) {
    #define VOSK_JSON_MAX_TOKENS (1024)
    jsmntok_t tokens[VOSK_JSON_MAX_TOKENS];
    int tokens_read;
    char buffer[64];

    jsmn_parser parser;
    jsmn_init(&parser);
    tokens_read = jsmn_parse(&parser, vosk_json, strlen(vosk_json), tokens, VOSK_JSON_MAX_TOKENS);

    if (tokens_read <= 0) return; // TODO: log failure
    if (tokens[0].type != JSMN_OBJECT) {
        return; // TODO: log failure
    }

    for (int i = 1; i < tokens_read; i++) {
        if (jsoneq(vosk_json, &tokens[i], "result")) {
            if (tokens[i+1].type != JSMN_ARRAY) {
                return; // TODO: log failure
            }

            // loop over objects
            for (int j = 0, obj = 0; obj < tokens[i + 1].size; j++) {
                if (tokens[i + 1 + j].type != JSMN_OBJECT) {
                    continue;
                }

                for (int k = 0, field = 0; field < tokens[i + 1 + j].size; k++) {
                    // write word
                    if (jsoneq(vosk_json, &tokens[i + 1 + j + 1 + k], "word")) {
                        jsmntok_t* value = &tokens[i + 1 + j + 1 + k + 1];
                        set_buffer(buffer, vosk_json+value->start, value->end-value->start);
                        output->text << buffer;
                        output->text << "\n";
                    }

                    if (jsoneq(vosk_json, &tokens[i + 1 + j + 1 + k], "start")) {
                        jsmntok_t* value = &tokens[i + 1 + j + 1 + k + 1];
                        set_buffer(buffer, vosk_json+value->start, value->end-value->start);
                        output->timestamp << make_timestamp(buffer);
                        output->timestamp << "\n";
                    }

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
