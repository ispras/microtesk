#!/bin/bash

root="/home/casse/Benchs/malardalen/ppc-eabi/gcc-4.4.2"
benchs="
	adpcm
	bs
	bsort100
	cnt
	compress
	cover
	crc
	duff
	edn
	expint
	fac
	fdct
	fibcall
	fir
	insertsort
	janne_complex
	jfdctint
	lcdnum
	matmult
	ndes
	ns
	nsichneu
	prime
	qsort-exam
	recursion
	select
	statemate
	ud
"

removed="
	fft1
	lms
	ludcmp
	minver
	qurt
	st

"

for b in $benchs; do
	file="$root/$b/$b.elf"
	echo "PROCESSING $file : ./comp $file"
	if ./comp $file 2> $b.out; then
		true
	else
		#cat $b.out
		echo "FAILED: content in $b.out"
		exit 1
	fi
done
