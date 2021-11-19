## Setup
# include compilation config:
include config.mk

# output dir for executable
EXECUTABLE_NAME =mediagrep
SRC_DIR=.
BIN_DIR=bin
INC_DIR=include

# list all source files here
FILES = mediagrep transcribe
SRC = ${FILES:%=$(SRC_DIR)/%.cc}
OBJ = ${FILES:%=$(BIN_DIR)/%.o}

# Includes
IFLAGS= -I$(INC_DIR)

# Linking (LDDIRS -> directories to search, LDLIBS -> library names)
LDLIBS = -lvosk -ldl
LDDIRS = lib
LDFLAGS= -L$(LDDIRS)

## Rules
all: $(BIN_DIR)/$(EXECUTABLE_NAME)

# show environment vars
environment: config.mk
	@echo "${EXECUTABLE_NAME} build options: (modify in config.mk)"
	@echo "CXX = ${CXX}"
	@echo "CXXFLAGS = ${CXXFLAGS}"
	@echo "LDFLAGS = ${LDFLAGS}"
	@echo "LDLIBS = ${LDLIBS}"
	@echo ""

# override for transcribe.cc because it needs ffmpeg headers included during compile
MEOW_FLAGS= -O3 -mavx2 -maes
$(BIN_DIR)/transcribe.o: $(SRC_DIR)/transcribe.cc environment dirs
	$(CXX) $(CXXFLAGS) $(IFLAGS) $(MEOW_FLAGS) -c $< -o $@

# compile object files with configuration from config.mk
${BIN_DIR}/%.o: ${SRC_DIR}/%.cc environment dirs
	$(CXX) $(CXXFLAGS) -c $< -o $@

# link all objects, produce final executable and output to BIN_DIR
${BIN_DIR}/${EXECUTABLE_NAME}: ${OBJ} dirs
	$(CXX) ${OBJ} -o $@ $(IFLAGS) $(LDFLAGS) -Wl,-rpath $(LDDIRS) $(LDLIBS)

# make output directories
dirs:
	mkdir -p $(BIN_DIR)

# clean output dir
clean:
	rm -rf $(BIN_DIR)

# set env variables for dynamic libraries
#	NOTE: (each makefile line runs as seperate shell), so '\'s needed to keep
#	commands in the same environment.
run: all
	LD_LIBRARY_PATH=${LDDIRS}; \
	export LD_LIBRARY_PATH; \
	${BIN_DIR}/$(EXECUTABLE_NAME);

# put everything in distributable archive
dist:

# compile and install for execution from anywhere #TODO: make this actually install mediagrep
install: all

# specify which rules do not actually create the file they are named after
.PHONY: all bin_dir clean run environment

# ignores suffix rules (deprecated) to save time
.SUFFIXES:
