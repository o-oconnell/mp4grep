SCRIPT_DIRECTORY=$(dirname "$(readlink -f "$0")")

echo "# mediagrep environment variables -------" >> ~/.bashrc
echo "export MEDIAGREP_CACHE='$SCRIPT_DIRECTORY/.mediagrep_cache'" >> ~/.bashrc
echo "export MEDIAGREP_MODEL='$SCRIPT_DIRECTORY/vosk-model-small-en-us-0.15'" >> ~/.bashrc

echo -n 'export PATH="$PATH:' >> ~/.bashrc
echo "$SCRIPT_DIRECTORY/bin\"" >> ~/.bashrc
echo "# --------------------------------------" >> ~/.bashrc


echo "Completed environment setup for mediagrep: "
echo "MEDIAGREP_CACHE=$SCRIPT_DIRECTORY/.mediagrep_cache"
echo "MEDIAGREP_MODEL=$SCRIPT_DIRECTORY/vosk-model-small-en-us-0.15"
echo -n 'PATH=$PATH:'
echo "$SCRIPT_DIRECTORY/bin"
echo ""
echo "Variables exported in ~/.bashrc"


source ~/.bashrc
