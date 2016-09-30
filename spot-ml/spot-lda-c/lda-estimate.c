// (C) Copyright 2004, David M. Blei (blei [at] cs [dot] cmu [dot] edu)

// This file is part of LDA-C.

// LDA-C is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License as published by the Free
// Software Foundation; either version 2 of the License, or (at your
// option) any later version.

// LDA-C is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
// for more details.

// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
// USA

#include "lda-estimate.h"
#include "mpi.h"
#include "stdlib.h"
/*
 * perform inference on a document and update sufficient statistics
 *
 */

double doc_e_step(document* doc, double* gamma, double** phi,
                  lda_model* model, lda_suffstats* ss)
{
    double likelihood;
    int n, k;

    // posterior inference

    likelihood = lda_inference(doc, model, gamma, phi);

    // update sufficient statistics

    double gamma_sum = 0;
    for (k = 0; k < model->num_topics; k++)
    {
        gamma_sum += gamma[k];
        ss->alpha_suffstats += digamma(gamma[k]);
    }
    ss->alpha_suffstats -= model->num_topics * digamma(gamma_sum);

    for (n = 0; n < doc->length; n++)
    {
        for (k = 0; k < model->num_topics; k++)
        {
            ss->class_word[k][doc->words[n]] += doc->counts[n]*phi[n][k];
            ss->class_total[k] += doc->counts[n]*phi[n][k];
        }
    }

    ss->num_docs = ss->num_docs + 1;

    return(likelihood);
}


/*
 * writes the word assignments line for a document to a file
 *
 */

void write_word_assignment(FILE* f, document* doc, double** phi, lda_model* model)
{
    int n;

    fprintf(f, "%03d", doc->length);
    for (n = 0; n < doc->length; n++)
    {
        fprintf(f, " %04d:%02d",
                doc->words[n], argmax(phi[n], model->num_topics));
    }
    fprintf(f, "\n");
    fflush(f);
}


/*
 * saves the gamma parameters of the current dataset
 *
 */

void save_gamma(char* filename, double** gamma, int num_docs, int num_topics)
{
    FILE* fileptr;
    int d, k;
    fileptr = fopen(filename, "w");

    for (d = 0; d < num_docs; d++)
    {
	fprintf(fileptr, "%5.10f", gamma[d][0]);
	for (k = 1; k < num_topics; k++)
	{
	    fprintf(fileptr, " %5.10f", gamma[d][k]);
	}
	fprintf(fileptr, "\n");
    }
    fclose(fileptr);
}


/*
 * run_em
 *
 */

void run_em(char* start, char* directory, corpus* corpus)
{

    int d, k, n;
    lda_model *model = NULL;
    double **var_gamma, **var_gamma_local, **phi;
    double wordp[corpus->num_terms], wordp_local[corpus->num_terms];

    // allocate variational parameters

    var_gamma = malloc(sizeof(double*)*(corpus->num_docs));
    for (d = 0; d < corpus->num_docs; d++)
	var_gamma[d] = malloc(sizeof(double) * NTOPICS);

    var_gamma_local = malloc(sizeof(double*)*(corpus->num_docs));
    for (d = 0; d < corpus->num_docs; d++)
	var_gamma_local[d] = malloc(sizeof(double) * NTOPICS);

    int max_length = max_corpus_length(corpus);
    phi = malloc(sizeof(double*)*max_length);
    for (n = 0; n < max_length; n++)
	phi[n] = malloc(sizeof(double) * NTOPICS);

    for (n = 0; n < corpus->num_terms; n++)
    {
        wordp[n] = 0.0;
        wordp_local[n] = 0.0;
    }

    // initialize model

    char filename[100];

    lda_suffstats* ss = NULL;
    lda_suffstats* ss_local = NULL;
    if (strcmp(start, "seeded")==0)
    {
        model = new_lda_model(corpus->num_terms, NTOPICS);
        ss = new_lda_suffstats(model);
        corpus_initialize_ss(ss, model, corpus);
        lda_mle(model, ss, 0);
        model->alpha = INITIAL_ALPHA;
    }
    else if (strcmp(start, "random")==0)
    {
        model = new_lda_model(corpus->num_terms, NTOPICS);
        ss = new_lda_suffstats(model);
        random_initialize_ss(ss, model);
        lda_mle(model, ss, 0);
        model->alpha = INITIAL_ALPHA;
    }
    else
    {
        model = load_lda_model(start);
        ss = new_lda_suffstats(model);
    }

    sprintf(filename,"%s/000",directory);
    save_lda_model(model, filename);

    // run expectation maximization

    int i = 0;
    double likelihood, likelihood_old = 0, converged = 1;
    double likelihood_local = 0;
     double gamma_local = 0.0; double gamma_global= 0.0;

    sprintf(filename, "%s/likelihood.dat", directory);
    FILE* likelihood_file = fopen(filename, "w");
    
    int myid, pnum, tag;

    MPI_Comm_rank(MPI_COMM_WORLD, &myid);
    MPI_Comm_size(MPI_COMM_WORLD, &pnum);
        
        
    while (((converged < 0) || (converged > EM_CONVERGED) || (i <= 2)) && (i <= EM_MAX_ITER))
    {
        i++;
        if (myid < 1 ) printf("**** em iteration %d ****\n", i);
        likelihood = 0; likelihood_local = 0;
       // int nstep = 0;

        //int min = 0;
        double alphass = 0.0; double alphass_local = 0.0;
        int numdocs = 0.0; int numdocs_local = 0.0;
        double wordn = 0.0; double wordn_local = 0.0;

        zero_initialize_ss(ss, model);
        ss_local = ss;

        for (d = myid; d < corpus->num_docs; d += pnum)
        {
            if ((d % 1000000) == 0) printf("document %d\n",d);
            likelihood_local += doc_e_step(&(corpus->docs[d]),
                                     var_gamma_local[d],
                                     phi,
                                     model,
                                     ss_local);
        }
        // may need to do the same thing with sufficient stats
        MPI_Barrier(MPI_COMM_WORLD);
         if (myid < 1) printf("**** reducing suffstats %d ****\n", myid);
        alphass_local = ss_local->alpha_suffstats;
 		MPI_Allreduce(&alphass_local, &alphass, 1, MPI_DOUBLE,
              MPI_SUM, MPI_COMM_WORLD);
        ss->alpha_suffstats = alphass;

        numdocs_local = ss_local->num_docs;
 		MPI_Allreduce(&numdocs_local, &numdocs, 1, MPI_INT,
              MPI_SUM, MPI_COMM_WORLD);
         ss->num_docs = numdocs;
	     // still a problem with word/topic matrix
	     //printf("**** reducing word matrix %d ****\n", myid);
		for (k = 0; k < model->num_topics; k++)
		{
			 if (myid < 1) printf("**** reducing word matrix topic %d , %d ****\n", k,myid);

		 	//for (n = 0; n < corpus->num_terms; n++)
		 	//{
			  //printf("**** reducing word matrix topic %d , %d, %d ****\n", k,n,myid);
			for (n = 0; n < corpus->num_terms; n++)
		 	{
				  wordp_local[n] = ss_local->class_word[k][n] ;
			 }
				  MPI_Allreduce(&wordp_local, &wordp,corpus->num_terms, MPI_DOUBLE,
				  MPI_SUM, MPI_COMM_WORLD);

			for (n = 0; n < corpus->num_terms; n++)
			{
					ss->class_word[k][n] = wordp[n];
			 }
		      //}

			  wordn_local = ss_local->class_total[k];
              MPI_Allreduce(&wordn_local, &wordn, 1, MPI_DOUBLE,
              MPI_SUM, MPI_COMM_WORLD);
              ss->class_total[k] = wordn;

		 }

		MPI_Allreduce(&likelihood_local, &likelihood, 1, MPI_DOUBLE,
              MPI_SUM, MPI_COMM_WORLD);
	    if (myid < 1) printf("**** reducing likelihood %10.10f ****\n", likelihood);
        // m-step

        lda_mle(model, ss, ESTIMATE_ALPHA);

        // check for convergence

        converged = (likelihood_old - likelihood) / (likelihood_old);
        if (converged < 0) VAR_MAX_ITER = VAR_MAX_ITER * 2;
        likelihood_old = likelihood;

        // output model and likelihood

        fprintf(likelihood_file, "%10.10f\t%5.5e\n", likelihood, converged);
        fflush(likelihood_file);

		MPI_Barrier(MPI_COMM_WORLD);
	}

    // output the final model

    sprintf(filename,"%s/final",directory);
    save_lda_model(model, filename);
    sprintf(filename,"%s/final.gamma",directory);
    // gather gammas across workers
    for (k = 0; k < model->num_topics; k++)
    {
        if (myid == 0) printf("**** saving gamma matrix topic %d  ****\n", k);
        if (myid > 0)
        {
            for (d = myid; d < corpus->num_docs; d += pnum)
            {
                //MPI_Request request;
                gamma_local = var_gamma_local[d][k];
                MPI_Send(&gamma_local,1,MPI_DOUBLE,0, d, MPI_COMM_WORLD);
                if (d >  corpus->num_docs - 5) printf("**** source %d  sent****\n",d);
            }
        }
        else
        {
            for (d=0 ; d < corpus->num_docs; d++) {
                if (d % pnum != 0)  // these documents were handled on other workers and must be gathered
                {
                    MPI_Status status;
                    MPI_Recv(&gamma_global, 1, MPI_DOUBLE, MPI_ANY_SOURCE, MPI_ANY_TAG, MPI_COMM_WORLD, &status);
                    if (status.MPI_TAG > corpus->num_docs - 5) printf("**** source %d  received****\n", status.MPI_TAG);
                    var_gamma[status.MPI_TAG][k] = gamma_global;
                }
                else
                {
                    gamma_local = var_gamma_local[d][k];
                    var_gamma[d][k] = gamma_local;
                }
            }
        }
        if (myid < 1) printf("*****%d hit barrier*****\n",myid);
        MPI_Barrier(MPI_COMM_WORLD);

	}
	// only the first worker on each machine should do this!
		if (myid < 1)
		{
			printf("*****saving gamma*****\n");
			save_gamma(filename, var_gamma, corpus->num_docs, model->num_topics);
		}
	MPI_Barrier(MPI_COMM_WORLD);

	fclose(likelihood_file);
}




/*
 * read settings.
 *
 */

void read_settings(char* filename)
{
    FILE* fileptr;
    char alpha_action[100];
    fileptr = fopen(filename, "r");
    fscanf(fileptr, "var max iter %d\n", &VAR_MAX_ITER);
    fscanf(fileptr, "var convergence %f\n", &VAR_CONVERGED);
    fscanf(fileptr, "em max iter %d\n", &EM_MAX_ITER);
    fscanf(fileptr, "em convergence %f\n", &EM_CONVERGED);
    fscanf(fileptr, "alpha %s", alpha_action);
    if (strcmp(alpha_action, "fixed")==0)
    {
	ESTIMATE_ALPHA = 0;
    }
    else
    {
	ESTIMATE_ALPHA = 1;
    }
    fclose(fileptr);
}


/*
 * inference only
 *
 */

void infer(char* model_root, char* save, corpus* corpus)
{
    FILE* fileptr;
    char filename[100];
    int i, d, n;
    lda_model *model;
    double **var_gamma, likelihood, **phi;
    document* doc;

    model = load_lda_model(model_root);
    var_gamma = malloc(sizeof(double*)*(corpus->num_docs));
    for (i = 0; i < corpus->num_docs; i++)
	var_gamma[i] = malloc(sizeof(double)*model->num_topics);
    sprintf(filename, "%s-lda-lhood.dat", save);
    fileptr = fopen(filename, "w");
    for (d = 0; d < corpus->num_docs; d++)
    {
	if (((d % 100) == 0) && (d>0)) printf("document %d\n",d);

	doc = &(corpus->docs[d]);
	phi = (double**) malloc(sizeof(double*) * doc->length);
	for (n = 0; n < doc->length; n++)
	    phi[n] = (double*) malloc(sizeof(double) * model->num_topics);
	likelihood = lda_inference(doc, model, var_gamma[d], phi);

	fprintf(fileptr, "%5.5f\n", likelihood);
    }
    fclose(fileptr);
    sprintf(filename, "%s-gamma.dat", save);
    save_gamma(filename, var_gamma, corpus->num_docs, model->num_topics);
}


/*
 * update sufficient statistics
 *
 */



/*
 * main
 *
 */

int main(int argc, char* argv[])
{
    // (est / inf) alpha k settings machinefile data (random / seed/ model) (directory / out)

    const int OPERATION_ARG = 1;


    const char* operation = argv[OPERATION_ARG];

    corpus* corpus;

    long t1;
    (void) time(&t1);
    seedMT(t1);

    if (argc > 1)
    {
        if (strcmp(operation, "est")==0)
        {
            // usage: lda est alpha k settings data random/seeded/* output_directory [prg seed]

            float alpha = atof(argv[2]); //should read alpha in as a vector instead of from args
            int ntopics = atoi(argv[3]);
            char* settings_path = argv[4];

            char* corpus_path = argv[5];
            char* initialization_option = argv[6];
            char* output_directory = argv[7];
           
            
            if (argc > 8) 
            {
                long seed = atol(argv[8]);
                seedMT(seed);
            }
            

            NTOPICS = ntopics; // TODO: get rid of global non-constants
            INITIAL_ALPHA = alpha; // TODO: get rid of global non-constants

            read_settings(settings_path);
            corpus = read_data(corpus_path);
            make_directory(output_directory);

            MPI_Init(&argc, &argv);
            run_em(initialization_option, output_directory, corpus);
            MPI_Finalize();
        }
        if (strcmp(operation, "inf")==0)
        {
            // usage: lda inf settings model data output_filename

            char* settings_path = argv[2];
            char* model_path = argv[3];
            char* corpus_path = argv[4];
            char* output_name = argv[5];

            read_settings(settings_path);
            corpus = read_data(corpus_path);

            MPI_Init(&argc, &argv);
            infer(model_path, output_name, corpus);
            MPI_Finalize();
        }
    }
    else
    {
        printf("usage : lda est alpha k settings_file data random/seeded/* output_directory [PRG seed]\n");
        printf("        lda inf settings_file model data output_filename\n");
    }
    return(0);
}
