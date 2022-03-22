while [ $# -gt 0 ] ; do
    case $1 in
	--prefix) prefix="$2";
		  shift;;
	*) echo "Argument \"${1}\" unrecognized";
    esac
    shift
done

echo "Setting MP4GREP_INSTALL_PREFIX=${prefix}"
export MP4GREP_INSTALL_PREFIX="${prefix}"
		  
    
