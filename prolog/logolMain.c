#include <stdio.h>
#include <sicstus/sicstus.h>

// This program call the logolMain function defined in logol.pro
// It sends as argument a prolog file to load dynamically then executes entry point of this file (via logolMain).

     int user_main(int argc, char **argv)
     {
       int rval;

       int i=1;
       int maxSolutions=0;
       int offset=0;
       int header=0;
       int solutions=0;
       
       SP_pred_ref pred;
       SP_qid goal;
  
       SP_term_ref ResultFile, File, OutFile, Res, Offset, HeaderSize, LogolFile;

       
       if(argc!=8) {
    	   fprintf(stderr,"Error in arguments. Usage is:\n#myprogram inputfile outputfile maxresults offset inputHeaderSize logolFile logolSavFile\n");
    	   //exit(1);
           return 1;
       }
     
       maxSolutions=atoi(argv[3]);
       
       offset=atoi(argv[4]);
       
       /* Initialize Prolog engine. The third arg to SP_initialize is
          reserved and should always be NULL */
       if (SP_FAILURE == SP_initialize(argc, argv, NULL))
         {
           fprintf(stderr, "SP_initialize failed: %s\n",
                   SP_error_message(SP_errno));
           //exit(1);
           return 1;
         }
       /* take logol.sav from arguments */
       rval = SP_restore(argv[7]);
     
       if (rval == SP_ERROR || rval == SP_FAILURE)
         {
           fprintf(stderr, "Could not restore \"logol.sav\".\n");
           //exit(1);
           return 1;
         }
       
       // Write header of result file.
       
       if (!(pred = SP_predicate("initResultFile",1,"user")))
         {
           fprintf(stderr, "Could not find initResultFile/1.\n");
           //exit(1);
           return 1;
         }
       SP_put_string(ResultFile = SP_new_term_ref(), argv[2]);
       
       SP_query(pred, ResultFile);
       
       SP_put_string(File = SP_new_term_ref(), argv[1]);
       /*
        * Open stream on sequence file 
        */  
       
       //Size of header in bytes
       header=atoi(argv[5]);
       
       SP_put_integer(HeaderSize = SP_new_term_ref(), header);
       
       if (!(pred = SP_predicate("openSequenceStream",2,"user")))
         {
           fprintf(stderr, "Could not find openSequenceStream/2.\n");
           //exit(1);
           return 1;
         }              
       SP_query(pred, File,HeaderSize);       
       
       
  	// Call predicate logolMain
       if (!(pred = SP_predicate("logolMain",5,"user")))
         {
           fprintf(stderr, "Could not find logolMain/5.\n");
           //exit(1);
           return 1;
         }
     
       /* Create the three arguments to connected/4. */
       //SP_put_string(File = SP_new_term_ref(), argv[1]);
       SP_put_string(OutFile = SP_new_term_ref(), argv[2]);
       SP_put_integer(Offset = SP_new_term_ref(), offset);
       SP_put_variable(Res = SP_new_term_ref());
       SP_put_string(LogolFile = SP_new_term_ref(), argv[6]);
     
       /* Open the query. the query looks like:
        *
        * | ?- logolMain('myfastasequencefile','myresultfile',Offset,Res,'mylogolfile').
        */
       if (!(goal = SP_open_query(pred,LogolFile,File,OutFile,Offset,Res)))
         {
           fprintf(stderr, "Failed to open query.\n");
           //exit(1);
           return 1;
         }
     
       /*
        * Loop through all the solutions.
        */
       
        printf("Looking for matches\n ");
        
       
       while (SP_next_solution(goal)==SP_SUCCESS && (i <= (maxSolutions-1) || maxSolutions==-1))
         {
	       i=i+1;
	       solutions++;
               printf(".");
         }
        printf("\nSearch is over, found %d solutions\n",solutions);
     
       SP_close_query(goal);

       /*
        * Close stream on sequence file 
        */
       if (!(pred = SP_predicate("closeSequenceStream",0,"user")))
         {
           fprintf(stderr, "Could not find closeSequenceStream/0.\n");
           //exit(1);
           return 1;
         }              
       SP_query(pred);        
       

       /*
        * If optimization was set, write the matches now
        */
       if (!(pred = SP_predicate("writeFinalMatches",0,"user")))
         {
           fprintf(stderr, "Could not find writeFinalMatches/0.\n");
           //exit(1);
           return 1;
        }

       if (!(goal = SP_open_query(pred)))
         {
           fprintf(stderr, "Failed to open query.\n");
           //exit(1);
           return 1;
         }
         solutions=0;
         while (SP_next_solution(goal)==SP_SUCCESS) {
              solutions++;
          }
          SP_close_query(goal);
         printf("\nOptimization filter: found %d solutions\n",solutions);
    
       
       
       // Write closure of result file.
       
       if (!(pred = SP_predicate("closeResultFile",1,"user")))
         {
           fprintf(stderr, "Could not find closeResultFile/1.\n");
           //exit(1);
           return 1;
         }
       SP_put_string(ResultFile = SP_new_term_ref(), argv[2]);
       
       SP_query(pred, ResultFile);
       
       //exit(0);
       return 0;
     }
