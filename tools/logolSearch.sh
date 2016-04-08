#!/bin/bash

# args: Err,Min,Sequence,MotifFileName,Mute,Max
#$1 = nb error
#minStart=$2
# If maxStart is equal to 0, do not take it into account
#maxStart=$6
#muteOnly=$5
#motifFileName=$4
#$3 sequence , add .index to name

DIR="$( cd "$( dirname "$0" )" && pwd )"

if [ $5 -eq 1 ]; then
  ruby -rubygems $DIR/logolIndex.rb --search $3.index --min $2 --max $6 --pattern $4 --error $1 --hamming
else
  ruby -rubygems $DIR/logolIndex.rb --search $3.index --min $2 --max $6 --pattern $4 --error $1
fi
