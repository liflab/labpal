package simple;

import java.math.BigInteger;

import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.plot.gral.Scatterplot;
import ca.uqac.lif.labpal.table.ExperimentTable;

public class MyLaboratory extends Laboratory {

  public void setup() {
    ExperimentTable t = new ExperimentTable("Number", "Time");
    for (long n : new long[]{22602052667l, 42602051897l, 63612552733l, 84612554431l})
      add(new MyExperiment(n), t);
    add(t).add(new Scatterplot(t));
  }

  class MyExperiment extends Experiment {
    public MyExperiment(long n) {
      setInput("Number", n);
    }
    
    public void execute() {
      BigInteger n = new BigInteger(Long.toString(readLong("Number")));
      long start = System.nanoTime();
      boolean b = n.isProbablePrime(1);
      long end = System.nanoTime();
      write("Time", end - start);
    }
  }
  
  public static void main(String[] args) {
    initialize(args, MyLaboratory.class);
  }
}