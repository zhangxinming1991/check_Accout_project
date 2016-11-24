#!/bin/bash
cd /var/tomcat/tomcat-7/webapps/check_Accout/backup_database
rm -rf run_restore.log
if [ ! -n "$1" ];then
  echo "file name is null!">run_restore.log
  exit -1
fi
Cur_Dir=$(pwd)
File_Name=$1
Dir_File=${Cur_Dir}"/"${File_Name}
echo "the database will be restore"
mysql -uroot -p1234 check_a_db<$Dir_File
echo "restore success">run_restore.log
