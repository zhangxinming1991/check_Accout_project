#!/bin/bash
#if [ ! -n "$1" ];then
#	echo "i"
#	exit -1
#fi
git remote rm origin 
git remote add origin git@119.29.235.201:/home/git/check_Accout_project.git
git remote set-url origin ssh://git@119.29.235.201:20029/home/git/check_Accout_project.git
git remote show origin
