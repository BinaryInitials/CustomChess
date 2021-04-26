#!/bin/bash
classpath="com.ozone.main.ChessAPI"
directories=`ls`
src="src"
bin="bin"
libs="libs"
if [[ " ${directories[@]} " =~ $libs ]]; then
	echo "Including Libraries..."
	libraries_for_compiler=`echo $(ls libs/) | perl -pe 's/jar/jar:/g' | perl -pe 's/ //g' | perl -pe 's/:$//g' | perl -pe 's/:/:libs\//g' | perl -pe 's/^/libs\//g'`
	libraries_for_manifest=`echo $(ls libs/)`
	echo $libraries_for_compiler
	echo $libraries_for_manifest
	if [[ " ${directories[@]} " =~ $bin ]]; then
		echo "Recompiling..."
		rm -r $bin/
	else 
		echo "Compiling..."
	fi

	if [[ " ${directories[@]} " =~ $src ]]; then
		mkdir bin && javac -cp $libraries_for_compiler -d bin $(find . -name "*.java" | egrep -v "^\./test/") && echo "Class-Path: " > manifest.txt && for i in $libraries_for_manifest; do echo " libs/"$i" " >> manifest.txt ; done && echo "Main-Class: "$classpath >> manifest.txt && cd bin && jar -cvfm ../chessAPI.jar ../manifest.txt *
		echo "Build succeeded. To run, use: java -jar chessAPI.jar"
	else
		echo "build-script is not in a java project. Please move the script directly in a java project."
		exit 1
	fi
	exit 1
fi

if [[ " ${directories[@]} " =~ $bin ]]; then
	echo "Recompiling..."
	rm -r $bin/
else 
	echo "Compiling..."
fi

if [[ " ${directories[@]} " =~ $src ]]; then
	mkdir bin && javac -d bin $(find . -name "*.java" | egrep -v "^\./test/") && cd bin && echo "Main-Class: "$classpath > manifest.txt && jar -cvfm chessAPI.jar manifest.txt * && mv chessAPI.jar ../
else
	echo "build-script is not in a java project. Please move the script directly in a java project."
	exit 1
fi
