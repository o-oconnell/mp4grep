## Setup
# include compilation config:
include config.mk

# output dir for executable
BIN_DIR=./bin

# list all source files here
SRC = perg.cc

OBJ = ${SRC:.c=.o}

## Rules
all: perg

# show environment vars
environment:
	@echo "\tgrep_star build options: (modify in config.mk)"
	@echo "\tCC = ${CC}"
	@echo "\tCPPFLAGS = ${CPPFLAGS}"
	@echo "\tLDFLAGS = ${LDFLAGS}"
	@echo "\tLDLIBS = ${LDLIBS}"
	@echo ""

# make sure configuration and environment are set before compiling objects
${OBJ}: config.mk environment

# compile object files with configuration from config.mk
%.o: %.c

# link all objects, produce final executable and output to BIN_DIR
perg: ${OBJ} bin_dir
	${CC} -o $(BIN_DIR)/$@ ${OBJ} $(IDIRS) ${LDFLAGS} -Wl,-rpath ${LDDIRS} ${LDLIBS}

# make binary output directory
bin_dir:
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
	${BIN_DIR}/grep_star;

# put everything in distributable archive
dist:

# compile and install for execution from anywhere
install: all

# specify which rules do not actually create the file they are named after
.PHONY: all bin_dir clean run environment
