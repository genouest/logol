#include <stdio.h>
#include <sicstus/sicstus.h>

/*
 * Pre-analyse module. Calls logolMain with logol pre-analyse file to load and dummy other parameters. 
 * 
 * 
 */

     
     int user_main(int argc, char **argv)
     {
       int rval;

       int i=1;
       
       SP_pred_ref pred;
       SP_qid goal;
  
       SP_term_ref LogolFile,Res;

       
       if(argc!=3) {
    	   fprintf(stderr,"Error in arguments. Usage is:\n#myprogram logolFile logolSavFile\n");
    	   //exit(1);
           return 1;
       }
       
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
       rval = SP_restore(argv[2]);
     
       if (rval == SP_ERROR || rval == SP_FAILURE)
         {
           fprintf(stderr, "Could not restore \"logol.sav\".\n");
           //exit(1);
           return 1;
         }
       
       
  	// Call predicate logolMain
       if (!(pred = SP_predicate("logolMain",5,"user")))
         {
           fprintf(stderr, "Could not find logolMain/5.\n");
           //exit(1);
           return 1;
         }
     
       /* Create the three arguments to connected/4. */
       SP_put_string(LogolFile = SP_new_term_ref(), argv[1]);
       SP_put_variable(Res=SP_new_term_ref());
       /* Open the query. the query looks like:
        *
        * | ?- logolMain('myfastasequencefile','myresultfile',Offset,Res,'mylogolfile').
        */
       if (!(goal = SP_open_query(pred,LogolFile,LogolFile,LogolFile,LogolFile,Res)))
         {
           fprintf(stderr, "Failed to open query.\n");
           //exit(1);
           return 1;
         }
     
       /*
        * Loop through all the solutions.
        */
       
        printf("Looking for matches\n ");
        
       
       while (SP_next_solution(goal)==SP_SUCCESS && i <= 1)
         {
	       i=i+1;
         }
     
       SP_close_query(goal);

       printf("Pre-analyse done\n ");
       
       //exit(0);
       return 0;
     }
