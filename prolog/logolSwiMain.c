#include <stdio.h>
#include <SWI-Prolog.h>
#include <string.h>

// This program call the logolMain function defined in logol.pro
// It sends as argument a prolog file to load dynamically then executes entry point of this file (via logolMain).

     int main(int argc, char **argv)
     {
       int rval;

       int i=1;
       int maxSolutions=0;
       int offset=0;
       int header=0;
       int solutions = 0;
       
		 predicate_t pred;
		 qid_t query,goal;
         
		 if(argc!=8) {
    	   fprintf(stderr,"Error in arguments.\nUsage is:\n#myprogram inputfile outputfile maxresults offset inputHeaderSize logolFile logolSavFile\n");
    	   //exit(1);
           return 1;
		}
		 
		 
		 char *program = argv[0];
		 
		 char *av[10];
		 int ac = 0;
		 /* make the argument vector for Prolog */
		 av[ac++] = program;
		 av[ac] = NULL;
		 
		 if ( !PL_initialise(ac, av) )
			 PL_halt(1);
		 
		 
		 
		 maxSolutions=atoi(argv[3]);
       
		 offset=atoi(argv[4]);
       
	
       
       // Write header of result file.
		 pred = PL_predicate("initResultFile",1,"user");
		 term_t ResultFile = PL_new_term_ref();
		 PL_put_string_chars(ResultFile,argv[2]);
		 query = PL_open_query(NULL, PL_Q_NORMAL, pred, ResultFile);
		 PL_next_solution(query);
		 PL_close_query(query);
       
		 
		 term_t terms = PL_new_term_refs(2);
		 term_t File = terms;
		 PL_put_string_chars(File,argv[1]);
		 //Size of header in bytes
		 header=atoi(argv[5]);
		 term_t HeaderSize = terms+1;
		 PL_put_integer(HeaderSize, header);
		 
		 
		 pred = PL_predicate("openSequenceStream",2,"user"); 
		 query = PL_open_query(NULL, PL_Q_NORMAL, pred, terms);
		 PL_next_solution(query);
		 PL_close_query(query);
		 
		 
		 
		
		 
		 term_t logolMainterms = PL_new_term_refs(5);
		 term_t LogolFile = logolMainterms;
		 PL_put_string_chars(LogolFile,argv[6]);
		 term_t InFile = logolMainterms+1;
		 PL_put_string_chars(InFile,argv[1]);
		 term_t OutFile = logolMainterms+2;
		 PL_put_string_chars(OutFile,argv[2]);
		 term_t Offset = logolMainterms+3;
		 PL_put_integer(Offset,offset);
		 term_t Res = logolMainterms+4;
		 PL_put_variable(Res);
		 
		 pred = PL_predicate("logolMain",5,"user");

		 query = PL_open_query(NULL, PL_Q_NORMAL, pred, logolMainterms);
		 
		 while(PL_next_solution(query) && (i <= (maxSolutions-1) || maxSolutions==-1))
		 {
			 i+=1;
			 solutions++;
			 printf(".");
		 }
		 PL_close_query(query);
		 printf("\nSearch is over, found %d solutions\n",solutions);
		 
		 
		 pred = PL_predicate("closeSequenceStream",0,"user");
		 query = PL_open_query(NULL, PL_Q_NORMAL, pred, PL_new_term_refs(0));
		 PL_next_solution(query);
		 PL_close_query(query);
		 
		 
		 pred = PL_predicate("writeFinalMatches",0,"user");
		 query = PL_open_query(NULL, PL_Q_NORMAL, pred, PL_new_term_refs(0));
		 
		 solutions = 0;

         while (PL_next_solution(query)) {
			 solutions++;
		 }		 
		 
		 PL_close_query(query);
		 printf("\nOptimization filter: found %d solutions\n",solutions);
		 
		 pred = PL_predicate("closeResultFile",1,"user");
		 ResultFile = PL_new_term_ref();
		 PL_put_string_chars(ResultFile,argv[2]);
		 query = PL_open_query(NULL, PL_Q_NORMAL, pred, ResultFile);
		 PL_next_solution(query);
		 PL_close_query(query);	 
		 
 
		 // Now exit
		 
		 PL_halt(0);
		 return 0;
     }
