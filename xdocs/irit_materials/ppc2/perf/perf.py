#!/usr/bin/python

import sys
import os
import subprocess
import re
from optparse import OptionParser

parser = OptionParser()
parser.add_option("-i", "--int", action="store_true", dest="int", default=False, help="use integer benches")
parser.add_option("-f", "--float", action="store_true", dest="float", default=False, help="use float benches")
(options, args) = parser.parse_args()


rate_re = re.compile("Rate = ([0-9]+\\.[0-9]+) Mips\n")

print "Performance Analysis\n"
print "BENCH\tRATE (Mips)"
root = "tests_loop"
sim  = "../sim/ppc-sim -s -fast %s"

fp_dirs = [ "fft1", "qurt", "ludcmp", "all_malardalen", "minver", "lms", "st", "select" ]
if options.float:
	dirs = fp_dirs
elif options.int:
	dirs = [t for t in os.listdir(root) if t not in fp_dirs]
else:
	dirs = os.listdir(root)

rate_total = 0
rate_cnt = 0
rate_max = -1
rate_min = -1

for dir in dirs:
	sys.stdout.flush()
	file = "%s/%s/%s.elf" % (root, dir, dir)
	rate_sum = 0

	for i in xrange(0, 1):
		proc = subprocess.Popen(sim % file, shell=True, stdout=subprocess.PIPE)
		for line in proc.stdout.xreadlines():
			match = rate_re.match(line)
			if match:
				rate_sum = rate_sum + float(match.group(1))
			#else:
			#	sys.stdout.write(line)

	rate_avg = rate_sum / 1
	print "%s\t%f" % (dir, rate_avg )
	rate_total = rate_total + rate_avg
	rate_cnt = rate_cnt + 1
	if rate_max < 0:
		rate_max = rate_avg
		rate_min = rate_avg
	else:
		if rate_max < rate_avg:
			rate_max = rate_avg
		if rate_min > rate_avg:
			rate_min = rate_avg

print "AVERAGE\t%f" % (rate_total / rate_cnt)
print "MAX\t%f" % (rate_max )
print "MIN\t%f" % (rate_min )

