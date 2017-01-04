# Are your experimental results independently reproducible?

If you are doing research in Computer Science, it is very likely that every now and then, you need to run experiments on your computer. You'll probably write command-line scripts for various tasks: generating your data, saving it into text files, process and display them as plots or tables to include in a paper or a presentation. But soon enough, your handful of "quick and dirty" batch files becomes a bunch of arcane, poorly documented scripts that generate and pass around various kinds of obscure temporary files. This brings two important problems:

- Too much cleaning up would be required to your setup before anybody else could understand how it works, so chances are you'll never make your scripts and data publicly available. This is bad: science should be all about being able to reproduce someone's experiments; working in such a way is arguably not very scientific.

- Most of your batch and data files are so specific to your experiments that you are likely to reuse none of them on your next project. In the long term, you end up repeatedly writing code that does the same few things: crunching text files, piping data to graphing software, generating LaTeX tables from them, etc. This is not a very productive use of your time.

## LabPal, a library for managing experiments

LabPal is a Java library that allows you to quickly setup an environment for running experiments, collating their results and processing them in various ways: generating tables, data files, plots, etc. All your experimental setup can be bundled into a single, stand-alone JAR file. Using LabPal, anybody can easily get a copy of your experimental setup; LabPal. It even provides a **web interface** so that they can run experiments on their own computer and examine their results directly from a web browser.

## How does it work?

- Take the [five minute tutorial](quick-tutorial.html) to understand how to easily setup an experimental environment with LabPal.

<!-- :wrap=soft:mode=markdown: -->