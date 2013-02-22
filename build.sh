#!/bin/sh

export CLASSPATH=./jars/ant-antlr3.jar:./jars/antlr-3.4-complete.jar:./jars/commons-cli-1.2.jar
ant $1
