#!/bin/bash
##################################################
#                                                #
# Script to test optirg and graphirg             #
# Generate 4 jpeg files :                        #
#      optimized vs non-optimized                #
#      canvas vs tree-displayed                  #
#                                                #
# Just call with ./test.sh                       #
#                                                #
##################################################
#                                                #
# IRIT - TRACES                                  #
#                                                #
# R. Dubot - dubot@irit.fr                       #
# Y. Ndongo - ndongo@irit.fr                     #
#                                                #
# July 2009                                      #
#                                                #
##################################################


# Path to gliss2 : 
GL=../..

# Path to the NMP file to treat : 
PNMP=.

# NMP file to treat : 
NMP=arm.nmp
N=arm
NOPT=$N"opt"

# Call make in gliss2 folder
(cd $GL ; make)

echo Preprocessing
$GL/gep/gliss-nmp2nml.pl $NMP > $N.nml

echo Make the irg file
$GL/irg/mkirg $N.nml $N.irg

echo Optimize the IRG
$GL/optirg/optirg $N.irg $NOPT.irg

echo Generate graphviz file
$GL/graphirg/graphirg $N.irg $N.dot
$GL/graphirg/graphirg $NOPT.irg $NOPT.dot
$GL/graphirg/graphirg -t $N.irg t$N.dot
$GL/graphirg/graphirg -t $NOPT.irg t$NOPT.dot

echo Generate JPEG files with dot.
dot -Tjpg -o $N.jpg $N.dot
dot -Tjpg -o $NOPT.jpg $NOPT.dot
dot -Tjpg -o t$N.jpg t$N.dot
dot -Tjpg -o t$NOPT.jpg t$NOPT.dot


