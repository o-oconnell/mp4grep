BIN := bin

.PHONY: defs bindings all

nothing:=$(echo "hello world")

current_dir_no_prefix:= $(notdir $(shell pwd))
current_dir:= $(shell pwd)
OPAM_SWITCH:=${OPAM_SWITCH_PREFIX}

MP4GREP_LIBS:=${MP4GREP_INSTALL_PREFIX}/mp4grep-libs
$(info mp4grep is being installed to ${MP4GREP_INSTALL_PREFIX})
$(info mp4grep libraries are being installed to ${MP4GREP_LIBS})
# $(info $$current_dir is [${current_dir}])


install: check-env copy-dynlibs defs bindings mp4grep.ml
	ocamlc -custom -o $(MP4GREP_INSTALL_PREFIX)/mp4grep unix.cma -I $(OPAM_SWITCH_PREFIX)/lib/parmap/ $(OPAM_SWITCH_PREFIX)/lib/parmap/parmap.cma str.cma defs.cmo mp4grep.ml vosk_bindings.o -ccopt -I$(current_dir)/include -ccopt -L$(current_dir)/lib -cclib -lvosk -ccopt -Wl,-rpath=$(MP4GREP_LIBS)

copy-dynlibs:
	mkdir $(MP4GREP_LIBS)
	cp lib/libvosk.so $(MP4GREP_LIBS)

check-env:
ifndef MP4GREP_INSTALL_PREFIX
	$(error MP4GREP_INSTALL_PREFIX is undefined)
endif

defs: defs.ml
	ocamlc -c defs.ml

bindings: vosk_bindings.c
	ocamlc -c vosk_bindings.c

clean:
	rm mp4grep.cmo mp4grep.cmi defs.cmi defs.cmo vosk_bindings.o

remove: check-env
	$(info mp4grep is being removed from ${MP4GREP_INSTALL_PREFIX})
	rm $(MP4GREP_INSTALL_PREFIX)/mp4grep
	rm -r $(MP4GREP_LIBS)



