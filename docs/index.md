# A library for managing experiments

If you are doing research in Computer Science, it is very likely that every now and then, you need to run experiments on your computer. LabPal is a Java library that allows you to quickly setup an environment for running these experiments, collating their results and processing them in various ways: generating tables, data files, plots, etc.

## Features

- All your experimental setup (including your code, its input files and library dependencies) can be bundled into a **single, runnable JAR** file, making it easy for anybody to download and re-run your experiments.
- The runnable JAR acts as a **web server**; when launched, a user can see all the experiments and control their execution using a web browser.
- Automated generation of PDF and PNG **plots** (using [GRAL](http://trac.erichseifert.de/gral) or [Gnuplot](http://gnuplot.info)) and **tables** (in beautified LaTeX, HTML or CSV).
- Each running experiment can update a visual **progress bar**; LabPal can even estimate and show the time remaining before they complete.
- A set of partially executed experiments can be saved to disk, then **reloaded and resumed** at a later time (or even on a different machine).

All these features are available in less than **30 lines** of Java code. See the example below!

## Why use LabPal?

To run experiments on a computer, you probably already write command-line scripts for various tasks: generating your data, saving it into text files, process and display them as plots or tables to include in a paper or a presentation. But soon enough, your handful of "quick and dirty" batch files becomes a bunch of arcane, poorly documented scripts that generate and pass around various kinds of obscure temporary files. This situation brings two important problems in terms of research methodology:

- **Problem 1: no one can reproduce your experiments.** Too much cleaning up would be required to your setup before anybody else could understand how it works, so chances are you'll never make your scripts and data publicly available.

- **Problem 2: you waste your time.** Most of your batch and data files are so specific to your experiments that even yourself are unlikely to reuse them on your next project; you'll start from scratch instead. This is not a very productive use of your time.

## Learn LabPal in five minutes

LabPal is easy enough to use that you can get up and running in a couple of minutes. Below is a minimal, but **complete** set of experiments for LabPal in 28 lines of code:

{% highlight java %}  
class MyLaboratory extends Laboratory {

  public void setup() {
    ExperimentTable t = new ExperimentTable("Number", "Time");
    for (long n : new long[]{42053447l, 1502050343l, 22602052667l})
      add(new MyExperiment(n), t);
    add(t).add(new Scatterplot(t));
  }

  class MyExperiment extends Experiment {
    public MyExperiment(long n) {
      setInput("Number", n);
    }
    
    public void execute() {
      long n = readLong("Number");
      long start = System.currentTimeMillis();
      // Do something with n...
      long end = System.currentTimeMillis();
      write("Time", end - start);
    }
  }
  
  public static void main(String[] args) {
    initialize(args, MyLaboratory.class);
  }
}
{% endhighlight %}

In a nutshell:

- Method `setup` creates a new *laboratory*, where we create a few *experiments* with different input parameters, assocaite them to a data *table*, and create a *plot* which will be drawn from the table.
- When instantiated, our experiments declare a number as an input parameter. When executed, the experiment reads this number, does something with it, and writes as an output parameter the time it has taken to do that.

What do we get in exchange for these 28 lines of code? Compile and run this file using the `--web` command-line argument. You should see a message telling you to visit `http://localhost:21212/index` in your browser.

If you go there, you'll see something like this:

<a href="index.png"><img src="index.png" alt="Home page" width="400" /></a>

The web interface contains a few pages, which you can access through the buttons at the top. Go to the *Experiments* page:



There you see the list of the experiments that have been added to the lab

<!-- :wrap=soft:mode=markdown: -->