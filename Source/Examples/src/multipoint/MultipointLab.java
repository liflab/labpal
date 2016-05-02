/*
    ParkBench, a versatile benchmark environment
    Copyright (C) 2015-2016 Sylvain Hallé

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package multipoint;

import ca.uqac.lif.parkbench.CliParser.ArgumentMap;
import ca.uqac.lif.parkbench.Laboratory;
import ca.uqac.lif.parkbench.Scatterplot;

public class MultipointLab extends Laboratory 
{
	public static void main(String[] args)
	{
		initialize(args, MultipointLab.class);
	}

	@Override
	public void setupExperiments(ArgumentMap map) 
	{
		Scatterplot plot = new Scatterplot();
		plot.useForX("a").useForY("b").assignTo(this);
		add(new MultipointExperiment(), plot);
	}

}
