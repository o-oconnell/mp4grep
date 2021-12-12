/* Libs */
#include "mediagrep.h"
#include "transcribe.h"
#include <map>
#include <string>
#include <fstream>

using namespace std;

// hardcoded values, TODO: remove, make configurable or input
const char* INPUT_WAV = "./test_files/harvardsentences.mp4";
const char* VOSK_MODEL = "./models/vosk-model-small-en-us-0.15";

int main(int argc, const char** argv) {
    // Parse arguments
    // * flags -> need global vars to store feature flags and options
    // * mode
    // * files to grep

    //// Normal mode -> Transcribe, Search, Print ////
    transcript_location cache_paths;
    if (transcribe(VOSK_MODEL, INPUT_WAV, &cache_paths) != 0) {
        // TODO handle error
    }


    /* READ ENTIRE TRANSCRIPT INTO MEMORY, MAP STARTING INDEX OF EACH WORD TO A TIMESTAMP*/
    std::ifstream text(cache_paths.text),  timestamp(cache_paths.timestamp);
    std::string transcript, line, timestampLine;
    std::map<int, std::string> indexToTimestamp;

    while (getline(text, line) && getline(timestamp, timestampLine)) {
	indexToTimestamp[transcript.length()] = timestampLine;
	transcript += line + ' ';
    }

    std::string SEARCH_STRING = "the";
    std::size_t found, findstart = 0;
    const int NUM_WORDS_BEFORE = 5;
    const int NUM_WORDS_AFTER = 5;

    /* GENERATE AND PRINT EACH MATCH LINE */
    while ((found = transcript.find(SEARCH_STRING, findstart)) != std::string::npos) {
	std::map<int, std::string>::const_iterator first_word = --indexToTimestamp.upper_bound(found);
	for (int i = 0; i < NUM_WORDS_BEFORE && first_word != indexToTimestamp.begin(); ++i) {
	    first_word--;
	}

	std::map<int, std::string>::const_iterator last_word = indexToTimestamp.lower_bound(found + SEARCH_STRING.length());
	for (int i = 0; i < NUM_WORDS_AFTER && last_word != indexToTimestamp.end(); ++i) {
	    last_word++;
	}

	std::string timestamp = first_word->second;
	std::string before_highlight = transcript.substr(firstWord->first, found - firstWord->first);
	std::string highlight = transcript.substr(found, SEARCH_STRING.length());
	// TODO: gives an extra space after match when the last word in the file is matched
	std::string after_highlight = transcript.substr(found + SEARCH_STRING.length(), last_word->first - (found + SEARCH_STRING.length()) - 1);
	
	printf("match:%s:%s||%s||%s:done\n", matchingTimestamp.c_str(), before_highlight.c_str(), highlight.c_str(), after_highlight.c_str());
	findstart = found + 1;
    }

    /* CLOSE TRANSCRIPT AND TIMESTAMP CACHE FILES */
    text.close();
    timestamp.close();
    
    // Search step
        // * Feed transcription outputs to searcher
        // * Search

    // Print step
        // Format search output and print

    return 0;
}
