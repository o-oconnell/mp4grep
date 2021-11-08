# Configures Compilation options

# Compilation options
CC=g++
CPPFLAGS= -g -pedantic -Wall -O0 # For Development

# Includes
IDIRS=-I ./include

# Linking (LDDIRS -> directories to search, LDLIBS -> library names)
LDLIBS += -lvosk
LDLIBS += -ldl

LDDIRS =./lib
LDFLAGS= -L $(LDDIRS)
