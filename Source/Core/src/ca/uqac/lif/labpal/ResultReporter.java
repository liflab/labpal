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
package ca.uqac.lif.labpal;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import ca.uqac.lif.labpal.server.MergeCallback;

public class ResultReporter implements Runnable
{
	private Thread m_thread;

	private volatile boolean m_running = false;

	/**
	 * The interval at which the lab's results will be reported
	 */
	private long m_reportInterval = 5000;

	/**
	 * The host to which experimental results should be reported by HTTP 
	 */
	private transient String m_reportHost = "";

	/**
	 * The path on the host corresponding to the endpoint of the request.
	 * It must start with a slash.
	 */
	private transient String m_reportPath = MergeCallback.s_path;

	/**
	 * The laboratory whose results will be reported
	 */
	private transient Laboratory m_lab;

	/**
	 * A list of exceptions generated when trying to report the results
	 */
	private transient List<ReporterException> m_exceptions = new LinkedList<ReporterException>();

	/**
	 * Creates a new result reporter
	 * @param lab The 
	 * @param host The host to which experimental results should be 
	 *   reported by HTTP
	 * @param interval The interval (in ms) at which the lab's 
	 *   results will be reported. Don't set it too small, as reporting
	 *   involves serializing the lab, sending an HTTP request and
	 *   waiting for the response. Set it to a negative value to 
	 *   disable reporting. 
	 */
	public ResultReporter(Laboratory lab, String host)
	{
		super();
		m_lab = lab;
		m_reportHost = host;
	}

	/**
	 * Creates a new result reporter
	 * @param lab The laboratory whose results will be reported
	 */
	public ResultReporter(Laboratory lab)
	{
		this(lab, null);
	}

	/**
	 * Sets the host to which experimental results should be reported
	 * @param host The host
	 */
	public void reportTo(String host)
	{
		m_reportHost = host;
	}

	/**
	 * Starts the results reporter
	 */
	public void start()
	{
		m_thread = new Thread(this);
		m_thread.start();
	}

	@Override
	public void run()
	{
		if (m_reportHost == null || m_reportHost.isEmpty() || m_reportInterval < 0)
		{
			// Do nothing
			return;
		}
		m_running = true;
		while (m_running)
		{
			try
			{
				Thread.sleep(m_reportInterval);
			} 
			catch (InterruptedException e)
			{
				// Nothing to do
			}
			try
			{
				reportResults();				
			}
			catch (ReporterException e)
			{
				m_exceptions.add(e);
			}
		}
	}

	/**
	 * Stops the results reporter
	 */
	public synchronized void stop()
	{
		m_running = false;
	}

	/**
	 * Gets the list of exceptions generated when trying to report results
	 * @return The list of exceptions
	 */
	public List<ReporterException> getExceptions()
	{
		return m_exceptions;
	}

	/**
	 * Reports the results of the lab. This is done by sending an
	 * HTTP POST request with the zipped contents of the lab.
	 * @return The HTTP response obtained from the request
	 */
	public synchronized String reportResults() throws ReporterException
	{
		HttpURLConnection connection = null;
		StringBuilder response = new StringBuilder();
		try
		{
			byte[] zip_lab = m_lab.saveToZip();
			//Create connection
			URL url = new URL("http://" + m_reportHost + m_reportPath);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", Laboratory.s_mimeType);
			connection.setRequestProperty("Content-Length", Integer.toString(zip_lab.length));
			connection.setUseCaches(false);
			connection.setDoOutput(true);
			InputStream is = null;

			//Send request
			OutputStream os = connection.getOutputStream();
			os.write(zip_lab);
			os.close();
			// Get Response
			is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));

			String line;
			while ((line = rd.readLine()) != null)
			{
				response.append(line);
				response.append('\r');
			}
			rd.close();
		}
		catch (FileNotFoundException e)
		{
			throw new ReporterException(e);
		}
		catch (ConnectException e)
		{
			throw new ReporterException(e);
		}
		catch (IOException e)
		{
			throw new ReporterException(e);
		}
		finally 
		{
			if (connection != null) 
			{
				connection.disconnect();
			}
		}
		return response.toString();		
	}

	public static class ReporterException extends LabException
	{
		/**
		 * Dummy UID
		 */
		private static final long serialVersionUID = 1L;

		public ReporterException(String message)
		{
			super(message);
		}

		public ReporterException(Throwable t)
		{
			super(t);
		}
	}

	/**
	 * Gets the URL of the host to report to
	 * @return The URL
	 */
	public String getUrl()
	{
		return m_reportHost;
	}

	/**
	 * Sets the interval at which results will be reported
	 * @param interval The time interval (in ms)
	 */
	public void setInterval(int interval)
	{
		m_reportInterval = interval;
	}
}
