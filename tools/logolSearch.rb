#!/usr/bin/ruby

require 'open3'

# args: Err,Min,Sequence,MotifFileName,Mute,Max
#$1 = nb error
#minStart=$2
# If maxStart is equal to 0, do not take it into account
#maxStart=$6
#muteOnly=$5
#motifFileName=$4
#$3 sequence , add .index to name

nbError = ARGV[0]
minStart = ARGV[1]
sequence = ARGV[2]
motifFileName = ARGV[3]
muteOnly = ARGV[4]
maxStart = ARGV[5]


DIR=File.expand_path(File.dirname(__FILE__))

if muteOnly.to_i == 1
  cmd = "ruby -rubygems "+DIR+"/logolIndex.rb --search "+sequence+".index --min "+minStart+" --max "+maxStart+" --pattern "+motifFileName+" --error "+nbError+" --hamming"
else
  cmd = "ruby -rubygems "+DIR+"/logolIndex.rb --search "+sequence+".index --min "+minStart+" --max "+maxStart+" --pattern "+motifFileName+" --error "+nbError
end

Open3.popen3(cmd) do |stdin,stdout,stderr, wait_thr|
  while line=stdout.gets
    $stdout.puts line
	$stdout.flush
  end
end
