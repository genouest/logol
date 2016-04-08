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
       
		 if(argc!=3) {
    	   fprintf(stderr,"Error in arguments.\nUsage is:\n#myprogram logolFile logolSavFile\n");
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
		 
		 term_t logolMainterms = PL_new_term_refs(5);
		 term_t LogolFile1 = logolMainterms;
		 PL_put_string_chars(LogolFile1,argv[1]);
		 term_t LogolFile2 = logolMainterms+1;
		 PL_put_string_chars(LogolFile2,argv[1]);		 
		 term_t LogolFile3 = logolMainterms+2;
		 PL_put_string_chars(LogolFile3,argv[1]);	
		 term_t LogolFile4 = logolMainterms+3;
		 PL_put_string_chars(LogolFile4,argv[1]);	
		 term_t Res = logolMainterms+4;
		 PL_put_variable(Res);
		 
		 pred = PL_predicate("logolMain",5,"user");

		 query = PL_open_query(NULL, PL_Q_NORMAL, pred, logolMainterms);
		 
		 /*
		  * Loop through all the solutions.
		  */
		 
		 printf("Looking for matches\n ");
		 
		 while(PL_next_solution(query) && i <= 1)
		 {
			 i+=1;
		 }
		 PL_close_query(query);
		 printf("Pre-analyse done\n ");
		 
		 
		 PL_halt(0);
		 return 0;
     }
