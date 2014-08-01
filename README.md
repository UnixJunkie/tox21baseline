The Tox21 Challenge baseline models
===================================

This repository contains a simple implementation of the [naive
Bayes](http://en.wikipedia.org/wiki/Naive_Bayes_classifier) classifier
to provide baseline models for the 12 datasets in the [Tox21
Challenge](https://tripod.nih.gov/tox21/challenge). The 
implementation makes use of [LyChI](https://github.com/ncats/lychi)
for structure standardization and
[PCFP](https://bitbucket.org/caodac/pcfp) for descriptor/feature
extraction. Please note that this code is for demonstration
purposes only; we make no claims as to its validity or correctness. We
do, however, encourage you to apply the models as-is (see below for
instructions) and submit the results to get a feel for what to
expect.


Compiling the code
==================

The code is self-contained in that all you need is a recent version of
[Java](http://www.oracle.com/technetwork/java/) development kit (e.g.,
JDK 6+) and [Apache ant](http://ant.apache.org). To compile the code,
simply type ```ant``` at the command line.


Training the classifier
=======================

The code comes bundled with prebuilt models under the ```models```
directory. However, if you want to build new set of models based on
tweaks you've made to the code, simply type the following on the
command line:

```
ant train
```

If all goes well, this command will generate the following 12 files:

```
NR-AHR.props
NR-AR-LBD.props
NR-AR.props
NR-AROMATASE.props
NR-ER-LBD.props
NR-ER.props
NR-PPAR-GAMMA.props
SR-ARE.props
SR-ATAD5.props
SR-HSE.props
SR-MMP.props
SR-P53.props
```

one for each respective model. To test these models, simply move them
to the ```models``` directory (thereby replacing the old ones).


Generating predictions
======================

To apply the models to test dataset, simply run the following command:

```
ant predict
```

Upon successful execution, the following files are generated:

```
NR-AHR.txt
NR-AR-LBD.txt
NR-AR.txt
NR-AROMATASE.txt
NR-ER-LBD.txt
NR-ER.txt
NR-PPAR-GAMMA.txt
SR-ARE.txt
SR-ATAD5.txt
SR-HSE.txt
SR-MMP.txt
SR-P53.txt
```

Each file is in the proper format for submission
[here](https://tripod.nih.gov/tox21/challenge/submission.jsp).


Contact
=======

For questions and/or problems with the code, please feel free to
contact the Tox21 team at <ncats9800tox21challenge@mail.nih.gov>. Good
luck with the competition!
