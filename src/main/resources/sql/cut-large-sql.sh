#!/bin/sh
####################
#   Usage:
#       ./cut-large-sql.sh large-file.sql 15000
#####################
wc=$(wc -l $1)

lines=`echo "$wc" | awk -F[" "] '{print $1}'`

echo "********* Total lines: $lines, split by $2"

fileCount=1
start=1
while [ $start -lt $lines ]
do  
  end=$(($start+$2))
  
  if [ $end -gt $lines ]
    then
	end=$lines
    fi

  sedCmd="sed -n '$start,$end""p' $1 > $1__"$fileCount"__"$start"-"$end
  eval $sedCmd
#  sed -n '3,6p' /path/to/file
  start=$(($end+1))
  fileCount=$(($fileCount+1))
#  sleep 1
done


