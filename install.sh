SCRIPT_DIRECTORY=$(dirname "$(readlink -f "$0")")

echo "# mp4grep environment variables -------" >> ~/.bashrc
echo "export MP4GREP_CACHE='$SCRIPT_DIRECTORY/.mp4grep_cache'" >> ~/.bashrc
echo "export MP4GREP_MODEL='$SCRIPT_DIRECTORY/model'" >> ~/.bashrc

echo -n 'export PATH="$PATH:' >> ~/.bashrc
echo "$SCRIPT_DIRECTORY/bin\"" >> ~/.bashrc
echo "# --------------------------------------" >> ~/.bashrc


echo "Completed environment setup for mp4grep: "
echo "MP4GREP_CACHE=$SCRIPT_DIRECTORY/.mp4grep_cache"
echo "MP4GREP_MODEL=$SCRIPT_DIRECTORY/model"
echo -n 'PATH=$PATH:'
echo "$SCRIPT_DIRECTORY/bin"
echo ""
echo "Variables exported in ~/.bashrc"


source ~/.bashrc
