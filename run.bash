#!/bin/sh
set -e
prefix=`pwd`

osascript closeWindow.scpt
cp testcase_codegen/testcase_$1.txt program.c
open -a TextEdit program.c
