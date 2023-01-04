BIN := bin
LIB := lib

.PHONY: defs bindings all

current_dir_no_prefix:= $(notdir $(shell pwd))
current_dir:= $(shell pwd)
MP4GREP_LIBS:=${MP4GREP_INSTALL_PREFIX}/mp4grep-libs

# -ccopt is how you pass options to gcc from ocamlc
prebuilt-binary-gcc-options := -ccopt -I$(current_dir)/include -ccopt -L$(current_dir)/lib -cclib -lvosk -ccopt -Wl,-rpath=$(LIB)
installation-gcc-options := -ccopt -I$(current_dir)/include -ccopt -L$(current_dir)/lib -cclib -lvosk -ccopt -Wl,-rpath=$(MP4GREP_LIBS)

$(info mp4grep is being installed to MP4GREP_INSTALL_PREFIX=${MP4GREP_INSTALL_PREFIX})
$(info mp4grep libraries are being installed to ${MP4GREP_LIBS})


prebuilt-binary: defs bindings mp4grep.ml
	ocamlc -custom -o $(BIN)/mp4grep unix.cma -I $(OPAM_SWITCH_PREFIX)/lib/parmap/ $(OPAM_SWITCH_PREFIX)/lib/parmap/parmap.cma str.cma defs.cmo mp4grep.ml vosk_bindings.o $(prebuilt-binary-gcc-options)

install: check-env copy-dynlibs copy-convertscript defs bindings mp4grep.ml
	ocamlc -custom -o $(MP4GREP_INSTALL_PREFIX)/mp4grep unix.cma -I $(OPAM_SWITCH_PREFIX)/lib/parmap/ $(OPAM_SWITCH_PREFIX)/lib/parmap/parmap.cma str.cma defs.cmo mp4grep.ml vosk_bindings.o $(installation-gcc-options)

copy-dynlibs:
	if [ -a $(MP4GREP_LIBS) ]; then rm -r $(MP4GREP_LIBS); fi;
	mkdir $(MP4GREP_LIBS)
	cp lib/libvosk.so $(MP4GREP_LIBS)

copy-convertscript: mp4grep-convert
	cp mp4grep-convert $(MP4GREP_INSTALL_PREFIX)

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
	rm $(MP4GREP_INSTALL_PREFIX)/mp4grep-convert	
	rm -r $(MP4GREP_LIBS)



