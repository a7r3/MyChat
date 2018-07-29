NO_OF_COMMITS=$(git rev-list --count HEAD);

UPDATE_VERSION="0.1.${NO_OF_COMMITS}";

echo -e "\nBumping MyChat to version ${UPDATE_VERSION}";

echo -e "\nProvide a changelog for the update [Press ENTER]"

read -r;

nano update_message.txt;

echo -e "\nThe Provided Update Message is: \n";

cat update_message.txt;

echo -e "\nSign the Tag ? [y/n]";

read -r SIGN_TAG;

ARGS=" ";
if [[ "${SIGN_TAG}" == [Yy] ]]; then
	ARGS=" -s";
fi

COMMAND="git tag -a${ARGS} -F update_message.txt v${UPDATE_VERSION}"
echo -e "> $COMMAND";
if $($COMMAND); then
	echo -e "\nTag Created Successfully\n";
	echo -e "\nRun: git push origin master && git push origin v${UPDATE_VERSION}";
	echo -e "Sayonara";
	rm -f update_message.txt;
fi
