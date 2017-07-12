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

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.ResultReporter;
import ca.uqac.lif.labpal.ResultReporter.ReporterException;

import com.sun.net.httpserver.HttpExchange;

/**
 * Triggers the sending of experimental data by the results
 * reported associated to the lab.
 * @author Sylvain Hallé
 */
public class ReportResultsCallback extends WebCallback
{
	public ReportResultsCallback(Laboratory lab, LabAssistant assistant)
	{
		super("/report-results", lab, assistant);
	}

	@Override
	public CallbackResponse process(HttpExchange t)
	{
		CallbackResponse cbr = new CallbackResponse(t);
		cbr.setHeader("Location", "/status");
		ResultReporter reporter = m_lab.getReporter();
		if (reporter != null)
		{
			try
			{
				reporter.reportResults();
				cbr.setCode(CallbackResponse.HTTP_OK);
			} 
			catch (ReporterException e)
			{
				cbr.setCode(CallbackResponse.HTTP_BAD_REQUEST);
				cbr.setContents(e.getMessage());
			}
		}
		return cbr;
	}
}
