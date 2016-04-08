#!/usr/bin/ruby
require 'open3'

DIR=File.expand_path(File.dirname(__FILE__))


args = ""

nbErrors = ARGV[0]
minStart = ARGV[1].to_i
sequenceIndex = ARGV[2]
motifFileName = ARGV[3]
muteOnly = ARGV[4].to_i
maxStart = ARGV[5].to_i

mode = 0
if nbErrors.to_i == 0
  mode = 0
else
  if muteOnly == 1
    mode = 1
  else
    mode = 2
  end
end

cmd = "${vmatch_path}vmatch "

case mode
when 0
  cmd += "-q "+motifFileName+"  -complete -absolute -nodist -noevalue -noscore -noidentity "+sequenceIndex
when 1
  cmd += "-q "+motifFileName+" -h "+nbErrors.to_s+" -complete -absolute  -noevalue -noscore -noidentity  "+sequenceIndex
when 2
  cmd += "-q "+motifFileName+" -e "+nbErrors.to_s+" -complete -absolute  -noevalue -noscore -noidentity "+sequenceIndex
end

Open3.popen3(cmd) do |stdin,stdout,stderr, wait_thr|

  while line=stdout.gets
    if line[/^#/]
	next
    end
    line.strip!
    vals = line.split(/\s+/)
    case mode
    when 0
      if vals[1].to_i >= minStart and (vals[1].to_i <= maxStart or maxStart=0)
        $stdout.puts vals[1]
        $stdout.puts vals[0]
        $stdout.puts "0"
      end
    when 1
      if vals[1].to_i >= minStart and (vals[1].to_i <= maxStart or maxStart=0)
        $stdout.puts vals[1]
        $stdout.puts vals[0]
        if vals[5].to_i == 0
          $stdout.puts "0"
        else
          errs = vals[5].to_i * -1
          $stdout.puts errs.to_s
        end
      end
    when 2
      if vals[1].to_i >= minStart and (vals[1].to_i <= maxStart or maxStart=0)
        $stdout.puts vals[1]
        $stdout.puts vals[0]
        $stdout.puts vals[5]
      end
    end

    $stdout.flush

  end

end

