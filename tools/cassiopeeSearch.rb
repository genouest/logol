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

nbSub = ARGV[0]
nbError = ARGV[1]
minStart = ARGV[2]
sequence = ARGV[3]
motifFileName = ARGV[4]
muteOnly = ARGV[5]
maxStart = ARGV[6]
# DNA/RNA/PROTEIN, no ambiguity for PROTEIN
type = " -m "+ARGV[7]
if ARGV[7].to_i != 2
  type += " -a "
end

#or_condition.logol.b61b7cbe-42b3-4c7a-9bf4-1b46eff4173f.1-41.fasta.tmp_486.fsa
# => or_condition.logol.b61b7cbe-42b3-4c7a-9bf4-1b46eff4173f.1-41.cass

DIR="${cassiopee_path}"

cmd = DIR+"cassiopeeknife -s "+sequence+" -o "+sequence+".cass"

#File.open('/tmp/debug.out', 'a') { |file| file.write(cmd+"\n") }

if ! File.exists?(sequence+".cass")
  Open3.popen3(cmd) do |stdin,stdout,stderr, wait_thr|
#    while line=stdout.gets
#        File.open('/tmp/debug.out', 'a') { |file| file.write(line+"\n") }
#    end
  end
#else
#    File.open('/tmp/debug.out', 'a') { |file| file.write("Index sequence already exists\n") }
end

index = ""
if ! File.exists?(sequence+".cass.cass.idx")
  index = " -u "
end

outfile = " -o "+sequence+".cass.out"

if muteOnly.to_i == 1
  cmd = DIR+"cassiopee -r -s "+sequence+".cass -f "+motifFileName+" -e "+nbSub+" -x "+minStart+" -y "+maxStart + index + outfile +type
else
  cmd = DIR+"cassiopee -r -s "+sequence+".cass -f "+motifFileName+" -i "+nbError+" -e "+nbSub+" -x "+minStart+" -y "+maxStart + index + outfile +type
end

#File.open('/tmp/debug.out', 'a') { |file| file.write(cmd+"\n") }

Open3.popen3(cmd) do |stdin,stdout,stderr, wait_thr|
    #stdout.each {||}
    pid = wait_thr.pid # pid of the started process.
    exit_status = wait_thr.value # Process::Status object returned.
end

motifout = File.open(motifFileName+".cass.out","w")

File.open(sequence+".cass.out", "r").each_line do |line|
   vals = line.split(/\t/)
   #$stdout.puts vals[0]
   #$stdout.puts vals[1]
   errs = vals[2].to_i+vals[3].to_i
   #$stdout.puts errs.to_s
   #$stdout.flush
   motifout.puts vals[0]
   motifout.puts vals[1]
   motifout.puts errs.to_s
end

motifout.close

File.delete(sequence+".cass.out")

