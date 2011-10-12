#!/bin/bash

cp ~/work/map/Project01/src/project01/*.java ./java
cp ~/work/map/Project01/src/com/bruceeckel/swing/Console.java ./java

flip -u ./java/*.java

for file in `ls ./java`
do
  lgrind -i -ljava ./java/$file > ./$file.tex
  echo "\subsection{$file}" >> src-include.tex
  echo "\begin{lgrind}" >> src-include.tex
  echo "\input src/$file.tex" >> src-include.tex
  echo "\end{lgrind}" >> src-include.tex
  echo "" >> src-include.tex
done
