BIN := bin

.PHONY: defs bindings all

nothing:=$(echo "hello world")


current_dir:= $(shell pwd)
OPAM_SWITCH:=${OPAM_SWITCH_PREFIX}

$(info $$current_dir is [${current_dir}])

all: defs bindings mp4grep.ml
	ocamlc -custom -o $(OPAM_SWITCH_PREFIX)/$(BIN)/mp4grep unix.cma -I $(OPAM_SWITCH_PREFIX)/lib/parmap/ $(OPAM_SWITCH_PREFIX)/lib/parmap/parmap.cma str.cma defs.cmo mp4grep.ml vosk_bindings.o -ccopt -I$(current_dir)/include -ccopt -L$(current_dir)/lib -cclib -lvosk -ccopt -Wl,-rpath=$(current_dir)/lib

defs: defs.ml
	ocamlc -c defs.ml

bindings: vosk_bindings.c
	ocamlc -c vosk_bindings.c

clean:
	rm mp4grep.cmo mp4grep.cmi defs.cmi defs.cmo vosk_bindings.o

# install:
# 	cp bin/mp4grep $(HOME)/.local/bin


