# Experiments

In the [5-minute tutorial](quick-tutorial.html), we have seen how to create a simple experiment that sorts an array and calculates the time it takes. We will modify this example in various ways to show the functionalities you can add to an experiment.

## Using multiple experiment classes

Suppose first that we would like to compare multiple sorting algorithms. We already created the `GnomeSort` experiment, so we could simply create other classes that use a different sorting algorithm, such as Bubble sort or Quick sort.

However, there are lots of things in common with these experiments: apart from the actual sorting, everything else is similar. It is therefore wise, in the object-oriented tradition, to factor out these functionalities into a common ancestor. Let us call it the `SortExperiment`:

{% highlight java %}
public abstract class SortExperiment extends Experiment {
  public SortExperiment(int n) {
    setInput("Size", n);
  }
  
  public void execute() {
    // Generate random array of given size
    Random rand = new Random();
    int n = readInt("Size");
    int[] array = new int[n];
    for (int i = 0; i < n; i++)
      array[i] = rand.nextInt();
    // Sort
    long start = System.currentTimeMillis();
    sort(array);
    long end = System.currentTimeMillis();
    write("Time", end - start);
  }
  
  public abstract void sort(int[] array);
}
{% endhighlight %}

Each specific experiment now only needs to implement the `sort` method, which performs the actual sorting. Hence our GnomeSort experiment becomes:

{% highlight java %}
public class GnomeSort extends SortExperiment {
  public GnomeSort(int n) {
    super(n);
    setInput("Algorithm", "Gnome Sort");
  }
  
  public void sort(int[] array) {
    int i = 0;
    while (i < array.length) {
      if (i == 0 || array[i-1] <= array[i]) i++;
      else {int tmp = array[i]; array[i] = array[i-1]; array[--i] = tmp;}
	}
  }
}
{% endhighlight %}

Note that our experiment now has an additional input parameter, which contains the name of the algorithm used for sorting.

Structured in this way, it is easy to create new classes that would use other sorting algorithms (we won't show them here; look at the [code examples](https://github.com/liflab/labpal/tree/master/Source/Examples/src/sorting)). Our lab can then include experiments of various kinds:

{% highlight java %}
...
add(new GnomeSort(size), t);
add(new QuickSort(size), t);
add(new BubbleSort(size), t);
...
{% endhighlight %}

We also need to change the table, since now, there are multiple experiments for the same value of parameter *Size*. Let us add column "Algorithm" to the table:

{% highlight java %}
ExperimentTable t = new ExperimentTable("Algorithm", "Size", "Duration");
{% endhighlight %}

If you run the experiments of this new lab, the table it produces will now look like this:

<table border="1">
<tr><th>Algorithm</th><th>Size</th><th>Duration</th></tr>
<tr><td rowspan="3">Gnome Sort</td><td>100</td><td></td></tr>
<tr> <td>200</td><td></td></tr>
<tr> <td>...</td><td></td></tr>
<tr><td rowspan="3">Quick Sort</td><td>100</td><td></td></tr>
<tr> <td>200</td><td></td></tr>
<tr> <td>...</td><td></td></tr>
<tr><td rowspan="3">Bubble Sort</td><td>100</td><td></td></tr>
<tr> <td>200</td><td></td></tr>
<tr> <td>...</td><td></td></tr>
</table>

The table automatically groups all cells with the same value of "Algorithm". (The way cells are grouped depends on the order in which the names are enumerated when creating it. See [Tables](tables.html).)

## Transforming the table

However, with such a table, our scatterplot will not make much sense: it will use the value of "Algorithm" for its *x* value, and use "Size" and "Duration" as two data series for the *y* value. We rather need a table like this:

<table border="1">
<tr><th>Size</th><th>Gnome Sort</th><th>Quick Sort</th><th>Bubble Sort</th></tr>
<tr><td>100</td><td></td><td></td><td></td></tr>
<tr><td>200</td><td></td><td></td><td></td></tr>
<tr><td>...</td><td></td><td></td><td></td></tr>
</table>

The *x* column is the value of "Size", followed by as many columns as there are values for the "Algorithm" parameter in the experiments. Each cell should contain the value of the "Duration" parameter for the experiment with the corresponding value of "Size" and "Algorithm".

We could redesign our experiments to produce such a table. An easier way is to apply a *transformation* to the existing table before plotting it. LabPal defines objects called [TableTransformation](doc/ca/uqac/lif/labpal/table/TableTransformation.html)s, which produce an output table from an input table. There are various transformations available; the one we are looking for here is called [ExpandAsColumns](doc/ca/uqac/lif/labpal/table/ExpandAsColumns.html). When creating a plot, we can pass a table transformation in addition to a table; so our plot declaration becomes:

{% highlight java %}
Scatterplot plot = new Scatterplot(t, new ExpandAsColumns("Algorithm", "Duration"));
{% endhighlight %}

See [tables](tables.html) for more information about tables and transformations.

## Prerequisites

Our new lab has one slight problem: the array is randomly generated by each experiment. This means that for a given size, the experiments do not sort the same array! We could fix this by making the random number generator deterministic (by giving the same seed every time), but a better way would be to generate the arrays of each size in advance, save them to files called `array-100.txt`, `array-200.txt`, etc., and simply have the experiments read these files when asked.

Suppose we already have these files. We could change `SortExperiment` so that it reads the array from the corresponding file:

{% highlight java %}
public abstract class SortExperiment extends Experiment {
  public SortExperiment(int n) {
    setInput("Size", n);
  }
  
  public void execute() {
    // Read array from file
    int n = readInt("Size");
    String filename = "array-" + n + ".txt";
    String[] elements = FileHelper.readToString(new File(filename)).split(",");
    int[] array = new int[n];
    for (int i = 0; i < n; i++)
      array[i] = Integer.parseInt(elements[i].trim());
    // Sort
    long start = System.currentTimeMillis();
    sort(array);
    long end = System.currentTimeMillis();
    write("Time", end - start);
  }
  
  public abstract void sort(int[] array);
}
{% endhighlight %}

Now, our experiment depends on an external resource to run successfully; this is called a **prerequisite**. Clearly, we do not want to run an experiment if the corresponding file does not exist. It is possible to signal this to our lab by implementing a method of class `Experiment` called `prerequisitesFulfilled`. This method returns `true` by default, indicating that the experiment is ready to run. We can override this behaviour so that it returns `false` if we can't find the input file:

{% highlight java %}
public boolean prerequisitesFulfilled() {
  int n = readInt("Size");
  String filename = "array-" + n + ".txt";
  return FileHelper.fileExists(filename);
}
{% endhighlight %}

If you compile and run this lab, you will see that an experiment will have the status "Needing prerequisites", instead of "Ready", if it is missing the file it is looking for.

## Generating prerequisites

Instead of creating the file by some external means, it would be even better if our lab could *generate* the files by itself. A first possibility would be to include code that creates the files in the beginning of its `setup` method, before the experiments are actually created. However, all the files would require to be generated, even if you wish to run just a few of the experiments. Moreover, if you change the parameters of your experiments (e.g., using other values for size), you must make sure that your generation code follows suit.

Better still is to make each experiment responsible of creating its input file if it does not exist. This is done by implementing another method called `fulfillPrerequisites`. The process is as follows: when running an experiment, LabPal first calls `prerequisitesFulfilled`; if the experiment returns `true`, it calls `execute` right away. Otherwise, LabPal calls this experiment's `fulfillPrerequisites` method, and *then* executes it.

{% highlight java %}
public void fulfillPrerequisites() {
  Random rand = new Random(0);
  int n = readInt("Size");
  String filename = "array-" + n + ".txt";
  PrintStream ps = new PrintStream(new File(filename));
  for (int i = 0; i < n; i++) {
    if (i > 0) ps.print(",");
    ps.print(rand.nextInt());
  }
  ps.close();
}
{% endhighlight %}

This method presents several advantages:

- The dependency of each experiment on some external resource is made explicit.
- For a given array size, the corresponding input file is generated only once, by the first instance of the experiment that is executed. For other experiments with the same value of "Size", method `prerequisitesFulfilled` will return `true` since the file is already there.
- The lab does not need to include the input files; they can be generated on demand. This means that the resulting JAR file is much smaller. Moreover, since the random number generator is instantiated with a specific seed, the generated files will be identical on every machine.
- The code for generating the resources is placed near the code that uses them.

It is also possible to clean the environment of temporary files that belong to an experiment, by implementing the `cleanPrerequisites` method. In our example, this cleanup would involve deleting the corresponding input file if it exists:

{% highlight java %}
public void cleanPrerequisites() {
  int n = readInt("Size");
  String filename = "array-" + n + ".txt";
  if (FileHelper.fileExists(filename))
    FileHelper.deleteFile(filename);
}
{% endhighlight %}

## Setting a progress indicator

Some experiments can be very long to execute; in such cases, it may be desirable to have an idea of the progression of the experiment. To this end, each experiment has its own "progress bar" that it can update as it pleases. One can do so by simply calling method `setProgression`. This method takes a single value between 0 and 1, indicating the level of progression of the experiment (0 meaning not started, and 1 indicating completion).

For example, here is the use of a progress bar in the body of method `sort` for the ShellSort experiment class:

{% highlight java %}
public void sort(int[] array) {
  float num_iterations = 0, max_iterations = (Math.log(2, array.length) - 1);
  for (int gap = array.length / 2; gap > 0; gap /= 2 ) {
    setProgression(++num_iterations / max_iterations);
    for(int i = gap; i < array.length; i++) {
      int tmp = array[i];
      for (int j = i; j >= gap && tmp < array[ j - gap ]; j -= gap) {
        array[ j ] = array[ j - gap ];
      }
      array[ j ] = tmp;
    }
  }
}
{% endhighlight %}

The interesting bit is the call to `setProgression` in line 4. Since the value of `gap` is divided by 2 on every iteration, the loop executes log<sub>2</sub>(array.length / 2) times. The progress indicator is updated at every turn of the loop to the fraction of all iterations completed so far. In LabPal's web console, this progress indicator is shown as a blue bar beside the experiment that is currently running; refreshing the page will refresh the indicator.

Obviously, it is up to the experiment's designer to come up with a meaningful way of measuring progress. Doing so is optional, especially for experiments that are very short. If no call to `setProgression` is made during the execution of an experiment, the indicator will stay at 0 until the experiment is finished.

<!-- :wrap=soft:mode=markdown: -->