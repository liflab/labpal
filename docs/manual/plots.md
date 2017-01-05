# Creating plots

LabPal provides various ways of displaying the results of experiments graphically, using plots. The way of creating plots is generally this:

1. An experiment table is created and associated to experiments from the lab
2. An instance of the [Plot](/doc/ca/uqac/lif/labpal/plot/Plot.html) object is created and given a table --along with an optional [TableTransformation](/doc/ca/uqac/lif/labpal/table/TableTransformation.html) to apply before plotting.

The creation and transformation of tables is covered in its [own section](tables.html). Here we focus on the creation and customization of plots from existing tables.

## Plot types

Currently, LabPal supports the following types of plots:

- [Scatterplots](#scatterplot)
- [Pie charts](#piechart)
- [Clustered bar plots](#clusteredhistogram)

## Plot libraries

LabPal uses two programs to generate plots.

- The first is the [GRAL](http://trac.erichseifert.de/gral/) graphing library, which comes bundled with it. When using GRAL-based plots, LabPal is stand-alone and does not require any external programs to display the generated pictures.
- The second is [Gnuplot](http://gnuplot.info), a powerful plotting program. Contrarily to GRAL, Gnuplot is an external software that does not come with LabPal. In order to display Gnuplot-based plots, the host system must have Gnuplot installed and executable from the command line. When run on a machine that does not have Gnuplot, the corresponding plots can be created, but cannot be shown.

Some types of plots (such as scatterplots) are available in both "flavours"; others can only be created by one of the tools. For example, Gnuplot cannot create pie charts, while GRAL cannot create clustered bar plots.

## Two-dimensional plots

Every two-dimensional plot has a few common methods that can be used to customize it:

- `setCaption` can give a caption to either the *x* or the *y* axis of the graph
- `setTitle` can give a title to the plot (which is generally displayed above it). The title is also used when showing the list of plots in the web and the text console. By default, a plot takes the same name as the table it is given.
- `setLogscale`: can instruct to plot an axis using a logarithmic, instead of a linear scale

### Scatterplots

A scatterplot is a two-dimensional plot of points. Given a table, the *(x,y)* coordinates of the points are defined as follows:

- The first column of the table is the set of *x* values
- The remaining columns are data series, each defining a value of *y*. Normally, the points of each data series are painted in a different color (see Palettes).

For example, when given to a scatterplot, the following table:

<table border="1">
<tr><th>n</th><th>A</th><th>B</th></tr>
<tr><td>0</td><td>1</th><td>0</td></tr>
<tr><td>1</td><td>2</th><td>3</td></tr>
<tr><td>2</td><td>3</th><td>4</td></tr>
</table>

will generate a plot with the points (0,1), (1,2), (2,3) painted in one color (corresponding to data series "A"), and the points (0,0), (1,3), (2,4) painted in another color (corresponding to data series "B").

A scatterplot is created like this:

{% highlight java %}
Table t = ...
ca.uqac.lif.labpal.plot.Scatterplot plot = new Scatterplot(t);
{% endhighlight %}

The first occurrence of `Scatterplot` refers to the *interface*, while the second occurrence refers to a specific implementation of this interface. Normally, this is either `ca.uqac.lif.labpal.gral.Scatterplot` to use the GRAL version, or `ca.uqac.lif.labpal.gnuplot.Scatterplot` for the Gnuplot version.

### Clustered histograms

TODO

### Pie charts

TODO

## Customizing GRAL plots

If these basic plot customization functions are not sufficient, it is possible to access to the complete GRAL library by directly manipulating the underlying Plot object. There are two ways of doing this.

### Light customization

If the basic plot is correct, but one wishes simply to perform some "touching up", a simple way is to override the `customize` method. For example, the Scatterplot class does not provide a way of changing the border width of the plot; however, this can be done by overriding `customize` and setting it using GRAL primitives:

{% highlight java %}
Table t = ...
ca.uqac.lif.labpal.plot.Scatterplot plot = new Scatterplot(t) {
  public void customize(de.erichseifert.gral.plots.Plot plot) {
    plot.setInsets(new Insets2D.Double(20d, 60d, 60d, 40d));
  }
}
{% endhighlight %}

### Heavier customization

For heavier customization, or to create a plot that GRAL supports but for which there exists no corresponding LabPal object, one can simply create a class that extends [GralPlot](/doc/ca/uqac/lif/labpal/plot/gral/GralPlot.html), and implement the `getPlot` method. This allows the user to create and setup any plot provided by GRAL and associate it with a DataSource corresponding to the table being passed to the plot. For example, the following code creates a GRAL RasterPlot object and changes the font used for displaying it.

{% highlight java %}
GralPlot plot = new GralPlot(t) {
  public void getPlot(DataSource source) {
    DataSource rasterdata = RasterPlot.createRasterData(source);
    RasterPlot p = new RasterPlot(rasterdata);
    p.setFont(new Font("Jokerman", Font.PLAIN, 35));
    return p;
  }
}
add(plot);
{% endhighlight %}

Once created, this plot can be managed by LabPal like any other plot.

## Palettes

The colours associated to each data series in a plot can be customized using a [Palette](/doc/ca/uqac/lif/labpal/plot/Palette.html). A palette is simply a map between numbers and colours; when drawing data series, a plot will use the first colour of the palette for the first series, the second colour for the second series, and so on. (If there are more series than colours in the palette, the palette index restarts from the beginning.)

The [Plot](/doc/ca/uqac/lif/labpal/plot/Plot.html) object contains a few predefined palettes, which you can refer statically (for example, `Palette.QUANTITATIVE_1` or `Palette.EGA`). To instruct a plot to use a specific palette, it can be passed by a call to method `setPalette`.

One can also create their own palettes by creating a descendent of the `Palette` class, and defining their own set of colours.

<!-- :wrap=soft:mode=markdown: -->