/* Libs */
#include "mediagrep.h"
#include "transcribe.h"
#include "include/termcolor.hpp"
#include <dirent.h>
#include <map>
#include <unordered_map>
#include <unordered_set>
#include <string>
#include <fstream>
#include <algorithm>
#include <vector>
#include <set>
#include <iostream>
#include <sstream>
#include <filesystem>

using namespace std;

struct parsed_args {
    unordered_map<string, string> args;
    unordered_set<string> flags;
    vector<string> params;
};

const string DEFAULT_MODEL = getenv("MEDIAGREP_MODEL") ? getenv("MEDIAGREP_MODEL") : "model";

const vector<string> AUDIO_TYPES{"mp3", "mp4", "ogg", "webm", "mov", "wav"};

void parse_args(int argc, const char** argv, struct parsed_args& pargs);
void search(int argc, const char** argv);
void transcribe_only(int argc, const char** argv);
void transcribe_to_files(int argc, const char** argv);
vector<string> get_audio_files(vector<string>& files);
int file_is_valid(const string& filename);

int main(int argc, const char** argv) {

    auto option_exists = [&](const std::string& option) {
	return std::find(argv, argv + argc, option) != (argv + argc);
    };

    /* SET THE WORKFLOW */
    if (option_exists("--help")) {
	printf("Help message goes here\n");
	exit(0);
    } else if (option_exists("--clear-cache")) {
	printf("Clearing cache\n");
	// clear_cache();
	exit(0);
    } else if (option_exists("--transcribe")) { 
	transcribe_only(argc, argv);
    } else if (option_exists("--transcribe-to-files")) {
	transcribe_to_files(argc, argv);
    } else {
	search(argc, argv);
    }

    return 0;
}

void transcribe_to_files(int argc, const char** argv) {

    /* SET DEFAULT ARGUMENTS */
    unordered_map<string, string> valid_args({
	    { "--words", "5"},
	    { "--model", DEFAULT_MODEL},
	});
    unordered_set<string> valid_flags{"--transcribe-to-files"};

    /* PARSE ARGUMENTS */
    struct parsed_args pargs;
    pargs.flags = valid_flags;
    pargs.args = valid_args;
    parse_args(argc, argv, pargs);

    /* ERROR CHECK ARGUMENTS (TODO: inexhaustive) */
    if (pargs.params.empty()) {
	printf("no files provided\n");
	exit(0);
    }
    
    for (string file : pargs.params) {

	/* CREATE OR RETRIEVE TRANSCRIPT AND TIMESTAMPS */
	transcript_location cache_paths;
	if (transcribe(pargs.args["--model"].c_str(), file.c_str(), &cache_paths) != 0) {
	    // TODO handle error
	}
	
	/* SETUP TRANSCRIPT AND TIMESTAMP INPUT */
	ifstream text(cache_paths.text),  timestamp(cache_paths.timestamp);
	int word_number = 0, words_per_line = atoi(pargs.args["--words"].c_str());
	string word_tmp, timestamp_tmp, line, line_timestamp;
	
	/* SETUP OUTPUT FILE */
	string output_file = file + "_transcript.txt";
	ofstream outstream;
	outstream.open(output_file, ios_base::app);
	
	/* READ FROM TRANSCRIPTS AND TIMESTAMPS */
	while (getline(text, word_tmp) && getline(timestamp, timestamp_tmp)) {

	    /* APPEND LINES WITH words_per_line WORDS TO FILE */
	    if (word_number % words_per_line == 0 && word_number < words_per_line) {
		line_timestamp = timestamp_tmp;
	    } else if (word_number % words_per_line == 0 && word_number >= words_per_line) {
		outstream << "[" << line_timestamp << "]:" << line << '\n';
		line.clear();
		line_timestamp = timestamp_tmp;
	    }

	    word_number++;
	    line += word_tmp + ' ';
	}

	outstream << "[" << line_timestamp << "]:" << line << '\n';
	outstream.close();

	/* CLOSE TRANSCRIPT AND TIMESTAMP CACHE FILES */
	text.close();
	timestamp.close();
    }
}

void transcribe_only(int argc, const char** argv) {

    /* SET DEFAULT ARGUMENTS */
    unordered_map<string, string> valid_args({
	    { "--words", "5"},
	    { "--model", DEFAULT_MODEL},
	});
    unordered_set<string> valid_flags{"--transcribe"};

    /* PARSE ARGUMENTS */
    struct parsed_args pargs;
    pargs.flags = valid_flags;
    pargs.args = valid_args;
    parse_args(argc, argv, pargs);

    /* ERROR CHECK ARGUMENTS (TODO: inexhaustive) */
    if (pargs.params.empty()) {
	printf("No files provided.\n");
	exit(0);
    }

    for (string file : pargs.params) {

	/* CREATE OR RETRIEVE TRANSCRIPT AND TIMESTAMPS */
	transcript_location cache_paths;
	if (transcribe(pargs.args["--model"].c_str(), file.c_str(), &cache_paths) != 0) {
	    // TODO handle error
	}

	/* SETUP TRANSCRIPT AND TIMESTAMP INPUT */
	ifstream text(cache_paths.text),  timestamp(cache_paths.timestamp);
	int word_number = 0, words_per_line = atoi(pargs.args["--words"].c_str());
	string word_tmp, timestamp_tmp, line, line_timestamp;
	
	/* READ FROM TRANSCRIPTS AND TIMESTAMPS */
	while (getline(text, word_tmp) && getline(timestamp, timestamp_tmp)) {

	    /* PRINT LINES WITH words_per_line WORDS */
	    if (word_number % words_per_line == 0 && word_number < words_per_line) {
		line_timestamp = timestamp_tmp;
	    } else if (word_number % words_per_line == 0 && word_number >= words_per_line) {
		printf("[%s]:%s\n", line_timestamp.c_str(), line.c_str());
		line.clear();
		line_timestamp = timestamp_tmp;
	    }

	    word_number++;
	    line += word_tmp + ' ';
	}

	printf("[%s]:%s\n", line_timestamp.c_str(), line.c_str());

	/* CLOSE TRANSCRIPT AND TIMESTAMP CACHE FILES */
	text.close();
	timestamp.close();
    }

}    

void parse_args(int argc, const char** argv, struct parsed_args& pargs) {
    vector<string> args(argv, argv + argc);
    set<int> used_args{0}; 
    unordered_map<string, string>& valid_args = pargs.args;
    const unordered_set<string>& valid_flags = pargs.flags;

    // Skip the first arg, the command.
    for (unsigned i = 1; i < args.size(); ++i) {

	/* VALID ARGUMENTS WITH VALUES */
	if (valid_flags.find(args[i]) == valid_flags.end() 
	    && valid_args.find(args[i]) != valid_args.end()
	    && i + 1 < args.size()
	    && used_args.find(i + 1) == used_args.end()) {
	    
	    valid_args[args[i]] = args[i + 1];
	    
	    ++i; // SKIP THE VALUE
	}
	
	/* VALID ARGUMENTS WITHOUT VALUES */
	else if (valid_flags.find(args[i]) == valid_flags.end()
		 && valid_args.find(args[i]) != valid_args.end()
		 && (i + 1 >= args.size() || used_args.find(i + 1) != used_args.end())) {
	    printf("Missing value for argument %s\n", args[i].c_str());
	    exit(1);
	}

	/* VALID FLAGS */
	else if (valid_flags.find(args[i]) != valid_flags.end()) {
	    used_args.insert(i);
	}

	/* TREAT EVERYTHING ELSE AS A PARAM */ 
	else {
	    pargs.params.push_back(args[i]);
	}
    }
}


struct progress_bar {
    int current;
    int max;
    
};

void search(int argc, const char** argv) {

    /* SET DEFAULT ARGS */
    unordered_map<string, string> valid_args({
	    { "--before", "5"},
	    { "--after", "1"},
	    { "--model", DEFAULT_MODEL},
	});

    /* PARSE ARGS */
    struct parsed_args pargs;
    pargs.args = valid_args;
    parse_args(argc, argv, pargs);

    /* ERROR CHECK ARGS */
    string search;
    vector<string> files;
    
    for (unsigned i = 0; i < pargs.params.size(); ++i) {
	if (search.empty()) {
	    search = pargs.params[i];
	} else {
	    files.push_back(pargs.params[i]);
	}
    }
    
    if (search.empty()) {
	printf("No search string provided. Exiting.\n");
	exit(0);
    }

    if (files.empty()) {
	printf("No files to search provided. Exiting.\n");
	exit(0);
    }

    vector<string> all_audio_files = get_audio_files(files);
    
    for (string file : all_audio_files) {
    
	/* CREATE OR RETRIEVE TRANSCRIPT AND TIMESTAMPS */
	transcript_location cache_paths;
	if (transcribe(pargs.args["--model"].c_str(), file.c_str(), &cache_paths) != 0) {
	    // TODO handle error
	}
    
	/* SETUP TRANSCRIPT AND TIMESTAMP INPUT */
	ifstream text(cache_paths.text),  timestamp(cache_paths.timestamp);
	string transcript, line, timestampLine;
	map<int, string> indexToTimestamp;
	
	while (getline(text, line) && getline(timestamp, timestampLine)) {
	    /* MAP STARTING INDEX OF EACH WORD TO A TIMESTAMP */
	    indexToTimestamp[transcript.length()] = timestampLine;

	    /* READ TRANSCRIPT INTO MEMORY */
	    transcript += line + ' ';
	}

	size_t found, findstart = 0;
	int before = atoi(pargs.args["--before"].c_str());
	int after = atoi(pargs.args["--after"].c_str());

	/* FIND START INDEX OF EACH MATCHING WORD */
	while ((found = transcript.find(search, findstart)) != string::npos) {

	    /* FIND START INDEX OF FIRST WORD TO PRINT */
	    map<int, string>::const_iterator first_word = --indexToTimestamp.upper_bound(found);
	    for (int i = 0; i < before && first_word != indexToTimestamp.begin(); ++i) {
		first_word--;
	    }
	    int first_word_start = first_word->first;

	    /* TIMESTAMP OF FIRST WORD */
	    string timestamp = first_word->second;

	    /* START INDEX OF LAST WORD */
	    map<int, string>::const_iterator last_word = indexToTimestamp.lower_bound(found + search.length());
	    for (int i = 0; i < after && last_word != indexToTimestamp.end(); ++i) {
		last_word++;
	    }
	    int last_word_start = last_word->first;


	    /* PRINT MATCH LINE */
	    string before_highlight = transcript.substr(first_word_start, found - first_word_start);
	    string highlight = transcript.substr(found, search.length());
	    // TODO: gives an extra space after match when the last word in the file is matched
	    string after_highlight = transcript.substr(found + search.length(),
						       last_word_start - (found + search.length()) - 1);

	    std::cout << termcolor::green << file
		      << termcolor::reset << ":"
		      << termcolor::blue << "[" << timestamp << "]"
		      << termcolor::reset << ":" << before_highlight
		      << termcolor::red << highlight
		      << termcolor::reset << after_highlight
		      << '\n';
	    
	    findstart = found + 1;
	}

	/* CLOSE TRANSCRIPT AND TIMESTAMP CACHE FILES */
	text.close();
	timestamp.close();
    }
}

vector<string> get_audio_files(vector<string>& files) {
    
    vector<string> output;
    for (string file : files) {
	DIR *dir;
	struct dirent *ent;
	
	if ((dir = opendir(file.c_str())) != NULL) {
	    /* ADD EVERY AUDIO FILE IN DIRECTORY TO OUTPUT */
	    while ((ent = readdir(dir)) != NULL) {
		if (file_is_valid(string(ent->d_name))) {
		    output.push_back(string(ent->d_name));
		}
	    }
	    closedir(dir);
	} else if (file_is_valid(file)) {
	    output.push_back(file);
	} else {
	    printf("Invalid audio file: %s\n", file.c_str());
	}
    }
    return output;
}

int file_is_valid(const string& filename) {
    DIR *dir;

    /* DIRECTORIES ARE NOT VALID AUDIO FILES */
    if ((dir = opendir(filename.c_str())) != NULL) {
	return 0;
    }

    /* DETERMINE VALIDITY BASED ON FILE EXTENSION */
    istringstream iss(filename);
    vector<string> split_on_period;
    string tok;
    while (std::getline(iss, tok, '.')) {
	if (!tok.empty())
	    split_on_period.push_back(tok);
    }
	    
    for (string valid_ext : AUDIO_TYPES) {
	if (split_on_period[split_on_period.size() - 1] == valid_ext) {
	    return 1;
	}
    }
    return 0;
}
