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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import ca.uqac.lif.jerrydog.CallbackResponse;
import ca.uqac.lif.jerrydog.CallbackResponse.ContentType;
import ca.uqac.lif.jerrydog.Server;
import ca.uqac.lif.labpal.CommandRunner;
import ca.uqac.lif.labpal.FileHelper;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.plot.Plot;
import ca.uqac.lif.labpal.plot.gnuplot.GnuPlot;

import com.sun.net.httpserver.HttpExchange;

/**
 * Callback to download all plots as a single, multi-page PDF file.
 * This callback makes use of both Gnuplot and pdftk in the background.
 * It will return a 404 response if these programs cannot be found.
 * 
 * @author Sylvain Hallé
 *
 */
public class AllPlotsCallback extends WebCallback
{
	/**
	 * The path to launch pdftk
	 */
	protected static transient String s_path = "pdftk";
	
	/**
	 * Whether pdftk is present on the system
	 */
	protected static transient final boolean s_pdftkPresent = FileHelper.commandExists(s_path);

	public AllPlotsCallback(Laboratory lab, LabAssistant assistant)
	{
		super("/all-plots", lab, assistant);
	}

	@Override
	public CallbackResponse process(HttpExchange t)
	{
		CallbackResponse response = new CallbackResponse(t);
		if (!GnuPlot.isGnuplotPresent() || !s_pdftkPresent)
		{
			// Can't do this without gnuplot and pdftk
			response.setCode(CallbackResponse.HTTP_NOT_FOUND);
			return response;
		}
		List<String> filenames = new LinkedList<String>();
		for (int id : m_lab.getPlotIds())
		{
			Plot plot = m_lab.getPlot(id);
			// Get plot's image and write to temporary file
			byte[] image = plot.getImage(Plot.ImageType.PDF);
			try
			{
				if (image.length > 0)
				{
					// Do something only if pdftk produced a non-zero-sized file
					File tmp_file = File.createTempFile("plot", ".pdf");
					tmp_file.deleteOnExit();
					FileOutputStream fos = new FileOutputStream(tmp_file);
					fos.write(image, 0, image.length);
					fos.flush();
					fos.close();
					String filename = tmp_file.getPath();
					filenames.add(filename);
				}
			}
			catch (FileNotFoundException e)
			{
				response.setCode(CallbackResponse.HTTP_BAD_REQUEST);
				return response;
			}
			catch (IOException e)
			{
				response.setCode(CallbackResponse.HTTP_BAD_REQUEST);
				return response;
			}
		}
		// Now run pdftk to merge all the plots into a single PDF
		List<String> command = new LinkedList<String>();
		command.add("pdftk");
		command.addAll(filenames);
		command.add("cat");
		command.add("output");
		command.add("-");
		CommandRunner runner = new CommandRunner(command, null);
		runner.start();
		while (runner.isAlive())
		{
			// Wait 0.1 s and check again
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException e)
			{
				// This happens if the user cancels the command manually
				runner.stopCommand();
				runner.interrupt();
				return null;
			}
		}
		// pdftk is done; read the output
		byte[] file_contents = runner.getBytes();
		response.setContentType(ContentType.PDF);
		String filename = Server.urlEncode(m_lab.getTitle()) + ".pdf";
		response.setAttachment(filename);
		response.setContents(file_contents);
		return response;
	}

}
