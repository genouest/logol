#!/usr/bin/env ruby
require 'rubygems'
require 'optparse'
require 'logger'
require 'cassiopee-mt'


 
options = {}
 
optparse = OptionParser.new do|opts|
   # Set a banner, displayed at the top
   # of the help screen.
  opts.banner = "Usage: logolIndex.rb [options]"

  opts.on( '-h', '--help', 'Display this screen' ) do
    puts opts
	exit
  end
  
   
  options[:verbose] = false
  opts.on('-V','--verbose','verbose mode') do
    options[:verbose] = true
  end
  
  options[:cache] = false
  opts.on('--cache','use cache') do
    options[:cache] = true
  end
   
  options[:pattern] = nil
  opts.on('-p', '--pattern FILE', 'Pattern file (raw)' ) do |file|
	options[:pattern] = file
  end 

   
  options[:min] = 0
  opts.on('--min VALUE', 'Minimal position' ) do |value|
	options[:min] = value
  end   

  options[:max] = 0
  opts.on('--max VALUE', 'Maximal position' ) do |value|
	options[:max] = value
  end
  
  options[:error] = 0
  opts.on('-e','--error VALUE', 'Max number of error' ) do |value|
	options[:error] = value
  end    

  options[:hamming] = false
  opts.on('-s','--hamming', 'Allow substitutions only (default: false)' ) do
	options[:hamming] = true
  end   
  

  options[:index] = false
  options[:file] = nil
  opts.on('--index FILE', 'Index sequence' ) do |file|
	options[:index] = true
	options[:search] = false
	options[:file] = file
  end 
  
  options[:out] = nil
  opts.on('-o','--out FILE', 'Output index file' ) do |file|
    if(options[:index])
	  options[:out] = file
          options[:log] = file+".log"
    end    
  end

  options[:search] = false
  opts.on('--search FILE', 'Search sequence with pattern (default)' ) do |file|
	options[:search] = true
	options[:out] = file
        options[:log] = file+".log"
  end   

end

optparse.parse!



$log = Logger.new(options[:log])
$log.level = Logger::INFO

if(!options[:index] && !options[:search])
  puts "Error, nor index or search options set"
  exit(1)
end

crawler = CassiopeeMt::CrawlerMt.new
crawler.setLogLevel(Logger::ERROR)
crawler.file_suffix = options[:out]
crawler.maxthread=2

if(options[:index])
  $log.info("Index file") unless !options[:verbose]
  crawler.comments = Array["#",">"]
  # Index
  if(!File.exists?(crawler.file_suffix+".sfx"))
    crawler.indexFile(options[:file])
  end
else
  # Search
    $log.info("Load index") unless !options[:verbose]
    crawler.loadIndex()

	if(options[:min].to_i>0 || options[:max].to_i>0)
	  $log.info("Apply position filter " << options[:min] << "-" << options[:max]) unless !options[:verbose]
	  crawler.filter_position(options[:min].to_i,options[:max].to_i)
    end

	pattern = ''
	file = File.new(options[:pattern], "r")
	# Skip first line (fasta)
	line = file.gets
	while (line = file.gets)
	  input = line.downcase.chomp
	  pattern << input
	end
	file.close
	if(pattern.length==0)
      puts "Error pattern file is empty"
	  exit(1) 
	end
  
  if(options[:cache])
    crawler.useCache=true
  end
  
  if(File.exists?(options[:out]+".dna"))
    $log.info("Load ambiguity map: ") unless !options[:verbose]
    crawler.loadAmbiguityFile(File.join(File.dirname(__FILE__), 'amb.map'))
  end
  
  if(options[:error].to_i==0)
    $log.info("Exact search") unless !options[:verbose]
    crawler.searchExact(pattern)
  else
    $log.info("Approximate search: " << options[:error]) unless !options[:verbose]
    errors = options[:error].to_i
	if(!options[:hamming])
		errors = errors * (-1)
	end
     crawler.searchApproximate(pattern,errors)
  end

  # Go through matches
  while((match = crawler.next())!=nil)
    #puts "Match: " << match.inspect
    i=0
    len = 0
    match[2].each do |m|
      if(i>0)
        puts m.to_s
        puts len.to_s
        puts match[1].to_s
        if(options[:verbose])
          $log.info(m.to_s << "\t" << len.to_s << "\t" << match[1].to_s << "\t" << crawler.extractSuffix(m,len))
        end
     else
       len = m
     end
     i += 1
    end
  end


end




