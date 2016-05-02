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

import ca.uqac.lif.json.JsonList;
import ca.uqac.lif.parkbench.Experiment;

public class MultipointExperiment extends Experiment 
{
	public MultipointExperiment()
	{
		super();
	}

	@Override
	public Status execute()
	{
		JsonList list_x = new JsonList();
		JsonList list_y = new JsonList();
		for (int i = 0; i < 10; i++)
		{
			list_x.add(i);
			list_y.add(2*i);
		}
		write("a", list_x);
		write("b", list_y);
		return Status.DONE;
	}
}
