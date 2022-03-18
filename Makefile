BIN := bin

.PHONY: defs bindings all

nothing:=$(echo "hello world")


current_dir_no_prefix:= $(notdir $(shell pwd))
current_dir:= $(shell pwd)
OPAM_SWITCH:=${OPAM_SWITCH_PREFIX}

$(info $$current_dir is [${current_dir}])

all: defs bindings libs mp4grep.ml
	ocamlc -custom -o $(OPAM_SWITCH_PREFIX)/$(BIN)/mp4grep unix.cma -I $(OPAM_SWITCH_PREFIX)/lib/parmap/ $(OPAM_SWITCH_PREFIX)/lib/parmap/parmap.cma str.cma defs.cmo mp4grep.ml vosk_bindings.o -ccopt -I$(current_dir)/include -ccopt -L$(current_dir)/lib -cclib -lvosk -ccopt -Wl,-rpath=$(OPAM_SWITCH_PREFIX)/lib

defs: defs.ml
	ocamlc -c defs.ml

bindings: vosk_bindings.c
	ocamlc -c vosk_bindings.c

libs:
	cp $(current_dir)/lib/libvosk.so $(OPAM_SWITCH_PREFIX)/lib

clean:
	rm mp4grep.cmo mp4grep.cmi defs.cmi defs.cmo vosk_bindings.o

remove:
	rm $(OPAM_SWITCH_PREFIX)/$(BIN)/mp4grep
	rm $(OPAM_SWITCH_PREFIX)/lib/libvosk.so

# install:
# 	cp bin/mp4grep $(HOME)/.local/bin


