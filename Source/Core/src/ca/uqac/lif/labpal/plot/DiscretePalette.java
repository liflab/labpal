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
package ca.uqac.lif.labpal.plot;

import java.awt.Color;

import ca.uqac.lif.labpal.FileHelper;

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
		out.append("# line styles").append(FileHelper.CRLF);
		for (int i = 0; i < m_colors.length; i++)
		{
			out.append("set style line ").append(i + 1).append(" lc rgb \"").append(m_colors[i]).append("\"").append(FileHelper.CRLF);
		}
		out.append("set palette maxcolors ").append(m_colors.length).append(FileHelper.CRLF);
		out.append("set palette defined (");
		for (int i = 0; i < m_colors.length; i++)
		{
			if (i > 0)
			{
				out.append(", ");
			}
			out.append(i).append(" \"").append(m_colors[i]).append("\"");
		}
		out.append(")").append(FileHelper.CRLF);
		return out.toString();
	}

	@Override
	public String getHexColor(int color_nb) 
	{
		return m_colors[color_nb % m_colors.length];
	}
	
	@Override
	public Color getPaint(int color_nb)
	{
		String color_hex = getHexColor(color_nb);
		return Color.decode(color_hex);
	}

}
