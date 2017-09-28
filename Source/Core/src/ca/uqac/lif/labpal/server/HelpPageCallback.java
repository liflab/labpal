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
package ca.uqac.lif.labpal.server;

import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;

/**
 * Callback for the help page
 * 
 * @author Sylvain Hallé
 *
 */
public class HelpPageCallback extends TemplatePageCallback
{
	
	public HelpPageCallback(Laboratory lab, LabAssistant assistant)
	{
		super("/help", lab, assistant);
	}
	
	@Override
	public String fill(String page, Map<String,String> params, boolean is_offline)
	{
		String out = page.replaceAll("\\{%TITLE%\\}", "Help");
		out = out.replaceAll("\\{%SEL_HELP%\\}", "selected");
		out = out.replaceAll("\\{%FAVICON%\\}", getFavicon(IconType.HELP));
		out = out.replaceAll("\\{%HEADER_PROGRESS_BAR%\\}", getHeaderBar());
		return out;
	}
	
	@Override
	public void addToZipBundle(ZipOutputStream zos) throws IOException
	{
		String file_contents = exportToStaticHtml("");
		String filename = "help.html";
		ZipEntry ze = new ZipEntry(filename);
		zos.putNextEntry(ze);
		zos.write(file_contents.getBytes());
		zos.closeEntry();
	}
}
