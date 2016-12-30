/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2017 Sylvain Hallé

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
package ca.uqac.lif.labpal.table;

import java.util.HashMap;

/**
 * An entry in a data table
 */
public class TableEntry extends HashMap<String,Object>
{
	/**
	 * Dummy UID
	 */
	private static final transient long serialVersionUID = 1L;
	
	public TableEntry()
	{
		super();
	}
	
	public TableEntry(TableEntry e)
	{
		super();
		putAll(e);
	}
	
	@Override
	public int hashCode()
	{
		int x = 0;
		for (Object o : values())
		{
			if (o != null)
			{
				x += o.hashCode();
			}
		}
		return x;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof TableEntry))
		{
			return false;
		}
		if (o == this)
		{
			return true;
		}
		TableEntry te = (TableEntry) o;
		if (size() != te.size())
		{
			return false;
		}
		for (Entry<String,Object> entry : entrySet())
		{
			if (!te.get(entry.getKey()).equals(entry.getValue()))
			{
				return false;
			}
		}
		return true;
	}
}
