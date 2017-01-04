# A quick tutorial

Suppose you want to measure empirically how long it takes to check if a given number *n* is prime, for various values of *n*. 

### Creating an experiment

To do so, you first need to create an **experiment**. An experiment is an object that can take input *parameters*, can be *run*, and produces one or more output *values*. In LabPal, experiments all descend from the class... [Experiment](doc/ca/uqac/lif/labpal/Experiment.html). In our example:

- Our experiment will take a single input parameter, which is the number we wish to check for primality. Setting an input parameter is done by calling the method `setInput`, which associates to a parameter *name* a particular *value*. 
- When being run, the actual primality test should be executed. An experiment must implement the method `execute`: this is where that code should be written.
- Our experiment produces a single output value, which corresponds to the time it takes to check that particular number. Writing an output value is done by calling the method `write`, which associates to a *name* a particular *value*.

Therefore, a sensible way to create our experiment would be to write this:

{% highlight java %}
class MyExperiment extends Experiment {

  public MyExperiment(long n) {
    setInput("Number", n);
  }
  
  public void execute() {
    long n = read("Number");
    long start = System.currentTimeMillis();
    // Code for checking n...
    long end = System.currentTimeMillis();
    write("Time", end - start);
  }
}
{% endhighlight %}

The constructor receives a number, and sets its as an input parameter of the experiment with name "Number". Method `execute` first reads the input parameter (the number), then checks if this number is prime (not shown). This code is surrounded by two calls to get the current system time. Finally, the total duration is written as an output data and is given the name "Time".

### Creating a lab

We are now ready to create a **laboratory** ("lab" for short), which will be the environment in which many of these **experiments** will be run. In LabPal, a lab is a descendent of the [Laboratory](doc/ca/uqac/lif/labpal/Laboratory.html) class. Experiments are created in a method called `setup`, and are added to the lab by a call to method `add`. Our lab could hence look like this:

{% highlight java %}
class MyLab extends Laboratory {

  public void setup() {
    add(new MyExperiment(10));
    add(new MyExperiment(100));
    add(new MyExperiment(1000));
  }
  
  public static void main(String[] args) {
    initialize(args, MyLab.class);
  }
}
{% endhighlight %}

<!-- :wrap=soft:mode=markdown: -->