#!/bin/sh
set -e
prefix=`pwd`

osascript closeWindow.scpt
cp testcase_codegen/testcase_$1.txt program.c
open -a TextEdit program.c
/Library/Java/JavaVirtualMachines/jdk-10.0.1.jdk/Contents/Home/bin/java -ea "-javaagent:/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar=51057:/Applications/IntelliJ IDEA.app/Contents/bin" -Dfile.encoding=UTF-8 -classpath /Users/guowenxuan/虚拟机/share/reCompiler/out/production/reCompiler:/Users/guowenxuan/虚拟机/share/reCompiler/lib/antlr-4.7.1-complete.jar Compiler.Main
