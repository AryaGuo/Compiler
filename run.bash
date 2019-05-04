#!/bin/sh
set -e
prefix=`pwd`

osascript closeWindow.scpt
cp testcase/testcase_$1.txt program.c
open -a TextEdit program.c