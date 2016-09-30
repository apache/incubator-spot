# spot-lda-c

Parallel implementation of latent Dirchlet allocation using MPI for Apache Spot (incubating).

## Getting Started

These instructions are for installing spot-lda-c as a standalone topic modelling tool. If you wish to install spot-lda-c as part of Apache Spot, please follow the instructions [here]. 

Even if you wish only to use spot-lda-c as a part of Apache Spot, the material in this README file may be helpful for configuring and understanding spot-lda-c.

### Prerequisites

spot-lda-c requires an MPI installation on the cluster. It has been validated with [mpich](http://www.mpich.org/) and [intel-mpi](https://software.intel.com/en-us/intel-mpi-library). You must install your favorite MPI on your cluster following the instructions provided by that
distribution.

### Compilation

Compilation requires that MPI libraries have been installed on the machine where spot-lda-c will be compiled. Other than that, just make in the top spot-lda-c directory:
```
[user@system  ~]$ cd spot-lda-c/
[user@system  spot-lda-c]$ make clean ; make
rm -f *.o
mpicc -O3 -Wall -g   -c -o lda-data.o lda-data.c
mpicc -O3 -Wall -g   -c -o lda-estimate.o lda-estimate.c
lda-estimate.c: In function ‘run_em’:
lda-estimate.c:181: warning: unused variable ‘tag’
mpicc -O3 -Wall -g   -c -o lda-model.o lda-model.c
mpicc -O3 -Wall -g   -c -o lda-inference.o lda-inference.c
mpicc -O3 -Wall -g   -c -o utils.o utils.c
mpicc -O3 -Wall -g   -c -o cokus.o cokus.c
mpicc -O3 -Wall -g   -c -o lda-alpha.o lda-alpha.c
mpicc -O3 -Wall -g lda-data.o lda-estimate.o lda-model.o lda-inference.o utils.o cokus.o lda-alpha.o -o lda -lm
[user@system spot-lda-c]$ ls lda
lda
```
###  Configuration

There are two files that must be configured to run spot-lda-c: a *machine file* that tells MPI what the hosts are and how many processes to run on each host,
and a *settings* file that contains settings that control the behavior of LDA.

**Configure the machine file** 
There is a sample machinefile in `spot-lda-c/machinefile`. Edit your machine file as follows:
```
[user@system ~]]$ cat spot-lda-c/machinefile 
Host_1:p_1
Host_2:p_2
Host_3:p_3
...
Host_N:p_N 
```

where ```Host_i``` is the name of  the i-th MPI host and `p_i` is the number of processes to run on the i-th host.  

For example, consider four worker nodes running 5 processes each:
```
[user@system ~]$ cat spot-lda-c/machinefile 
mynode1:5
mynode2:5
mynode3:5
mynode4:5 
```

**Configure LDA settings** 

See `spot-lda-c/settings.txt` for an example.

This is of the following form:

     var max iter [integer e.g., 10 or -1]
     var convergence [float e.g., 1e-8]
     em max iter [integer e.g., 100]
     em convergence [float e.g., 1e-5]
     alpha [fit/estimate]

where the settings are

     [var max iter]

     The maximum number of iterations of coordinate ascent variational
     inference for a single document.  A value of -1 indicates "full"
     variational inference, until the variational convergence
     criterion is met.

     [var convergence]

     The convergence criteria for variational inference.  Stop if
     (score_old - score) / abs(score_old) is less than this value (or
     after the maximum number of iterations).  Note that the score is
     the lower bound on the likelihood for a particular document.

     [em max iter]

     The maximum number of iterations of variational EM.

     [em convergence]

     The convergence criteria for varitional EM.  Stop if (score_old -
     score) / abs(score_old) is less than this value (or after the
     maximum number of iterations).  Note that "score" is the lower
     bound on the likelihood for the whole corpus.

     [alpha]

     If set to [fixed] then alpha does not change from iteration to
     iteration.  If set to [estimate], then alpha is estimated along
     with the topic distributions.

## Distribute code and settings about the cluster

All nodes specified in the machinefile need to have the ``spot-lda-c`` directory (containing the executable, machinefile and settings file)
copied to the same pathname as it has one the master node.

## Execution

The lda command line is:

    lda est [alpha] [k] [settings] [#processes] [data] [random/seeded/*] [output directory]

Exact invocation will vary with MPI implementation. Here is an example where the total number of process is 20:

    mpiexec -n 20 -f machinefile ./lda est 2.5 12 settings.txt 20 input.dat random outputdir
 


