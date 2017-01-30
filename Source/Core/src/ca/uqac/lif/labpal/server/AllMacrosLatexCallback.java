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
package ca.uqac.lif.labpal.server;

import java.util.Calendar;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.CallbackResponse.ContentType;
import ca.uqac.lif.jerrydog.Server;
import ca.uqac.lif.labpal.FileHelper;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.macro.Macro;

import com.sun.net.httpserver.HttpExchange;

/**
 * Callback to download a LaTeX file defining a unique command for each
 * macro of the lab.
 * 
 * @author Sylvain Hallé
 *
 */
public class AllMacrosLatexCallback extends WebCallback
{
	public AllMacrosLatexCallback(Laboratory lab, LabAssistant assistant)
	{
		super("/all-macros-latex", lab, assistant);
	}

	@Override
	public CallbackResponse process(HttpExchange t)
	{
		CallbackResponse response = new CallbackResponse(t);
		StringBuilder out = new StringBuilder();
		out.append("% ----------------------------------------------------------------").append(FileHelper.CRLF);
		out.append("% File generated by LabPal ").append(Laboratory.s_versionString).append(FileHelper.CRLF);
		out.append("% Date:     ").append(String.format("%1$te-%1$tm-%1$tY", Calendar.getInstance())).append(FileHelper.CRLF);
		out.append("% Lab name: ").append(m_lab.getTitle()).append(FileHelper.CRLF);
		out.append("% ----------------------------------------------------------------").append(FileHelper.CRLF).append(FileHelper.CRLF);
		for (Macro m : m_lab.getMacros())
		{
			String latex_def = m.toLatex(true);
			out.append(latex_def);
		}
		response.setContentType(ContentType.LATEX);
		String filename = Server.urlEncode("labpal-macros.tex");
		response.setAttachment(filename);
		response.setContents(out.toString());
		return response;
	}

}
