# spot-lda-c

Parallel implementation of latent Dirchlet allocation for Apache Spot (incubating).

spot-lda-c is a program for *topic modelling*:  From a collection of documents and integer k, infer k topics and assign to each document a mixture of topics and to each topic a mixture of words. 

Topic modelling is used in the Apache Spot suspicious connections analysis to generate profiles of typical traffic on the network and estimate
the probability of a given communication occurring between two IPs.  Network traffic on a particular channel corresponds to a "document" and
summaries of the traffic are the "words". 

For example, in a netflow analysis the document is communication between two IP addresses and
and the "words" are summaries of the netflow records between the two. The
"topics" are then profiles of common traffic on the network, the mixture of topic to the documents
is a decomposition of the traffic between two IPs into these common traffic profiles.  For given communication, we can estimate its probablity of occurrence between a given IP pair and flag it as "suspicious".  Similarly, in an analysis based on the DNS protocols, the documents are the DNS queries
coming from a given client IP address, and the words are summaries of the DNS responses.


To learn more about topic modelling, we suggest that you start with [these materials](https://www.cs.princeton.edu/~blei/topicmodeling.html).

**On the shoulders of giants**: spot-lda-c is an MPI parallelization of [the BleiLab's single processor lda-c program](https://github.com/blei-lab/lda-c).

## Getting Started

These instructions are for installing spot-lda-c as a standalone topic modelling tool. If you wish to install spot-lda-c as part of Apache Spot, please follow the instructions [here]. 
Even if you wish only to use spot-lda-c as a part of oni, the material in this README file may be helpful for configuring and understanding spot-lda-c.

To install spot-lda-c standalone, see the accompanying [INSTALL.md] file.

### Command line

The command line used to invoke lda will vary with MPI implementation. Here is a sample command line:

```
mpiexec -n PROCESS_COUNT -f MACHINEFILE ./lda est ALPHA TOPIC_COUNT SETTINGS PROCESS_COUNT INPUT_FILE random outputdir
```

where:

- **PROCESS_COUNT** is the total number of processes running on the MPI hosts (the sum of the process counts in the machinefile)
- **MACHINEFILE** is a file containing hosts and their process counts, in the format desecribed in the configuration section above
- **est** Tells lda to estimate the topic model.
- **ALPHA** The alpha parameter for LDA.
- **TOPIC_COUNT** is the number of topics to be used to profile the data.
- **SETTINGS** is a text file containing further settings for LDA, see configuration above
- **INPUT_FILE** contains a representation of the corpus in the format described in **Prepare data for input**
- **outputdir** is the name of a directory in which to store output files

Yes, PROCESS_COUNT appears twice right now (once for MPI and once for LDA).

Example:
```
[user@system ~]$ cd spot-lda-c 
[user@system ~]$  mpiexec -n 20 -f machinefile ./lda est 2.5  12 settings.txt 20 corpus20151031.txt random results20151031
```

### spot-lda-c input 

The corpus of documents and words must be preprocessed into a single file for ingestion by spot-lda-c
In this representation, each document is represented as a sparse vector of word
counts. That is, the data is a text file where each line is of the form:

     [M] [term_1]:[count] [term_2]:[count] ...  [term_N]:[count]

where [M] is the number of unique terms in the document, and the
[count] associated with each term is how many times that term appeared
in the document.  Note that [term_1] is an integer which indexes the
term; it is not a string.

This file must be loaded onto each MPI host in the working directories which MPI will use (typically, by default this
is a directory on the host with the same name as the directory which MPI is invoked).
## spot-lda-c output


There are two files that will appear in the output directory:

**final.beta** in which each row corresponds to a topic and each row contains the natural logarithm of the probability of each word given that topic.

For example,  if in topic 1, the probability of word 1 were 0.1, the probability of word 2 were 0.89 and the probability of word 3 were 0.01,
then the first line of final.beta would be  −2.302585093 −0.116533816 −4.605170186

**final.gamma** in which each row corresponds to a document and each row contains the unnormalized probabilities of the topics for that document.

For example, if the first line of final.gamma were
0.0224561925 0.0224561925 0.0224561925 4.0224561925 4.0224561925
then document 1 would a topic mix of approximately 0.3% topic 1, 0.3% topic 2, 0.3% topic 3, 49.6% topic 4 and 49.6% topic 5.


## Licensing

spot-lda-c is licensed under the GNU Lesser Public License, version 2.1

## Contributing

Because of the critical position that spot-lda-c holds in the Apache Spot system, contributions to spot-lda-c will be carefully vetted.

## Issues

Report issues at the [Apache Spot issues page](https://github.com/Apache Spot/open-network-insight/issues).

## Maintainers


[Ricardo Barona](https://github.com/rabarona)

[Nathan Segerlind](https://github.com/NathanSegerlind)

[Everardo Lopez Sandoval](https://github.com/EverLoSa)

