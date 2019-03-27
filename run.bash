#!/bin/sh
set -e
prefix=`pwd`

osascript closeWindow.scpt
cp testcase/testcase_$1.txt program.txt
open program.txt