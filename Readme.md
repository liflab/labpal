# Easily run experiments on a computer

If you are doing research in Computer Science, it is very likely that every now and then, you need to run experiments on your computer. LabPal is a Java library that allows you to quickly setup an environment for running these experiments, collating their results and processing them in various ways: generating tables, data files, plots, etc.

## Features

- All your experimental setup (including your code, its input files and library dependencies) can be bundled into a **single, runnable JAR** file, making it easy for anybody to download and re-run your experiments.
- The runnable JAR acts as a **web server**; when launched, a user can see all the experiments and control their execution using a web browser.
- For a few more lines of code, the input/output parameters of an experiment can come with a small **textual description** that is displayed in the web interface, so that a user can understand the meaning of each data element. You can do the same with the lab itself, and each experiment it contains.
- Automated generation of PDF and PNG **plots** (using [GRAL](http://trac.erichseifert.de/gral) or [Gnuplot](http://gnuplot.info)) and **tables** (in beautified LaTeX, HTML or CSV). You can perform **transformations** to the tables before plotting them, and also **customize** the display of your plots (scales, colors, labels) very easily.
- Each running experiment can update a visual **progress bar**; LabPal can even estimate and show the time remaining before they complete.
- A set of partially executed experiments can be saved to disk, then **reloaded and resumed** at a later time (or even on a different machine).

## Why use LabPal?

To run experiments on a computer, you probably already write command-line scripts for various tasks: generating your data, saving it into text files, process and display them as plots or tables to include in a paper or a presentation. But soon enough, your handful of "quick and dirty" batch files becomes a bunch of arcane, poorly documented scripts that generate and pass around various kinds of obscure temporary files. This situation brings two important problems in terms of research methodology:

- **Problem 1: no one can reproduce your experiments.** Too much cleaning up would be required to your setup before anybody else could understand how it works, so chances are you'll never make your scripts and data publicly available.

- **Problem 2: you waste your time.** Most of your batch and data files are so specific to your experiments that even yourself are unlikely to reuse them on your next project; you'll start from scratch instead. This is not a very productive use of your time.

## I want to use LabPal!

- Just [download the latest release](https://github.com/liflab/labpal/releases/latest) and include it in your classpath.
- Or download our [template project](https://github.com/liflab/labpal-project) that includes a few extra features (like a boilerplate Readme and Ant build script).

If you want to know more about LabPal's features:

- Read the short [tutorial](http://liflab.github.io/labpal/quick-tutorial.html)
- Look at our [lab examples](https://github.com/liflab/labpal/tree/master/Source/Examples/src) to learn how to use various features
- Look at our slightly longer [instructions](http://liflab.github.io/labpal/instructions/)
- Consult the online [API documentation](http://liflab.github.io/labpal/doc/)

## About LabPal

LabPal was developed by [Sylvain Hallé](http://leduotang.ca/sylvain), Associate Processor at [Université du Québec à Chicoutimi](http://www.uqac.ca), Canada. Pr. Hallé is also the head of [LIF](http://liflab.ca), a research lab, where LabPal is extensively used for the processing of experimental results.