package simple;

import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.plot.gral.Scatterplot;
import ca.uqac.lif.labpal.table.ExperimentTable;

class MyLaboratory extends Laboratory {

  public void setup() {
    ExperimentTable t = new ExperimentTable("Number", "Time");
    for (long n = 10; n <= 10000; n *= 10)
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