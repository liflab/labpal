/*
  ParkBench, a versatile benchmark environment
  Copyright (C) 2015-2016 Sylvain Hallé
  
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
package ca.uqac.lif.parkbench.plot;

/**
 * Palette with a fixed number of colors
 */
public class DiscretePalette extends Palette 
{
	protected String[] m_colors;
	
	public DiscretePalette(String ... colors)
	{
		super();
		m_colors = colors;
	}

	@Override
	public String getDeclaration() 
	{
		StringBuilder out = new StringBuilder();
		out.append("# line styles\n");
		for (int i = 0; i < m_colors.length; i++)
		{
			out.append("set style line ").append(i + 1).append(" lc rgb \"").append(m_colors[i]).append("\"\n");
		}
		out.append("set palette maxcolors ").append(m_colors.length).append("\n");
		out.append("set palette defined (");
		for (int i = 0; i < m_colors.length; i++)
		{
			if (i > 0)
			{
				out.append(", ");
			}
			out.append(i).append(" \"").append(m_colors[i]).append("\"");
		}
		out.append(")\n");
		return out.toString();
	}

	@Override
	public String getHexColor(int color_nb) 
	{
		return m_colors[color_nb % m_colors.length];
	}

}
