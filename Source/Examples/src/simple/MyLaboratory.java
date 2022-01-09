/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2022 Sylvain Hallé

  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package simple;

import java.math.BigInteger;

import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.plot.Plot;
import ca.uqac.lif.labpal.table.ExperimentTable;
import ca.uqac.lif.spreadsheet.chart.gnuplot.GnuplotScatterplot;

public class MyLaboratory extends Laboratory {

  public void setup() {
    ExperimentTable t = new ExperimentTable("Number", "Time");
    for (long n : new long[]{22602052667l, 42602051897l, 63612552733l, 84612554431l})
      add(new MyExperiment(n), t);
    add(t).add(new Plot(t, new GnuplotScatterplot()));
  }

  class MyExperiment extends Experiment {
    public MyExperiment(long n) {
      setInput("Number", n);
    }
    
    public void execute() {
      BigInteger n = new BigInteger(Long.toString(readLong("Number")));
      long start = System.nanoTime();
      n.isProbablePrime(1);
      write("Time", System.nanoTime() - start);
    }
  }
  
  public static void main(String[] args) {
    initialize(args, MyLaboratory.class);
  }
}