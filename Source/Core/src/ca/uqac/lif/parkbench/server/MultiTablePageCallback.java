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
package ca.uqac.lif.parkbench.server;

import java.util.Map;

import ca.uqac.lif.parkbench.LabAssistant;
import ca.uqac.lif.parkbench.Laboratory;
import ca.uqac.lif.parkbench.table.ExperimentMultidimensionalTable;
import ca.uqac.lif.parkbench.table.MultidimensionalTable;
import ca.uqac.lif.parkbench.table.TableNode;
import ca.uqac.lif.parkbench.table.rendering.HtmlTableNodeRenderer;
import ca.uqac.lif.parkbench.table.rendering.LatexTableRenderer;
import ca.uqac.lif.parkbench.table.rendering.TableNodeRenderer;

/**
 * Callback producing an image from one of the lab's plots, in various
 * formats.
 * <p>
 * The HTTP request accepts the following parameters:
 * <ul>
 * <li><tt>dl=1</tt>: to download the image instead of displaying it. This
 *   will prompt the user to save the file in its browser</li>
 * <li><tt>id=x</tt>: mandatory; the ID of the plot to display</li>
 * <li><tt>format=x</tt>: the requested image format. Currenly supports
 *   pdf, dumb (text), png and gp (raw data file for Gnuplot).
 * </ul>
 * 
 * @author Sylvain Hallé
 *
 */
public class MultiTablePageCallback extends TemplatePageCallback
{
	public MultiTablePageCallback(Laboratory lab, LabAssistant assistant)
	{
		super("/multitable", lab, assistant);
	}

	@Override
	public String fill(String s, Map<String,String> params)
	{
		int tab_id = Integer.parseInt(params.get("id"));
		ExperimentMultidimensionalTable tab = m_lab.getMultiTable(tab_id);
		if (tab == null)
		{
			return null;
		}
		s = s.replaceAll("\\{%TITLE%\\}", tab.getTitle());
		MultidimensionalTable mtt = tab.getTable();
		TableNode node = mtt.getTree();
		String format = params.get("format");
		if (format == null || format.compareToIgnoreCase("html") == 0)
		{
			TableNodeRenderer renderer = new HtmlTableNodeRenderer();
			String rendered_table = renderer.render(node, tab.getDimensions());
			s = s.replaceAll("\\{%TABLE%\\}", rendered_table);
		}
		else if (format.compareToIgnoreCase("latex") == 0)
		{
			TableNodeRenderer renderer = new LatexTableRenderer();
			String rendered_table = renderer.render(node, tab.getDimensions());
			rendered_table = rendered_table.replaceAll("\\\\", "\\\\\\\\");
			s = s.replaceAll("\\{%TABLE%\\}", "<pre>" + rendered_table + "</pre>");
		}
		String desc = tab.getDescription();
		if (desc != null && !desc.isEmpty())
		{
			s = s.replaceAll("\\{%DESCRIPTION%\\}", desc);
		}
		else
		{
			s = s.replaceAll("\\{%DESCRIPTION%\\}", "");
		}
		s = s.replaceAll("\\{%FAVICON%\\}", getFavicon(IconType.TABLE));
		return s;
	}
}
