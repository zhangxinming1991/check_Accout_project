#!/bin/bash
if [ ! -n "$*" ];then
	echo "input the commit"
	exit -1
fi
git add run_git_add_remote.sh
git add run_git_upload.sh
git add run_git_download.sh
git add src/
git add .settings
git add README.md
git add .classpath
git add .deployment
git add .project
git add pom.xml
git commit -m "$*"
git rm --cache src/main/java/hibernate.cfg.xml
git push -u origin master
