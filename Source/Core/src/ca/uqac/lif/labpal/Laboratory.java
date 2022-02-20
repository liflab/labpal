/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2022 Sylvain Hallé

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

import ca.uqac.lif.jerrydog.Server;
import ca.uqac.lif.labpal.CliParser.ArgumentMap;
import ca.uqac.lif.labpal.assistant.Assistant;
import ca.uqac.lif.labpal.experiment.Experiment;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ca.uqac.lif.jerrydog.RequestCallback.Method;

public class Laboratory extends Server
{
	/**
	 * 
	 */
	private transient String m_description;
	
	/**
	 * A map associating experiment IDs to experiment instances.
	 */
	/*@ non_null @*/ private Map<Integer,Experiment> m_experiments;
	
	/**
	 * An assistant instance used to run experiments inside the lab.
	 */
	/*@ non_null @*/ private transient Assistant m_assistant;  
	
	/**
	 * Creates a new empty laboratory instance.
	 */
	public Laboratory()
	{
		super();
		m_experiments = new HashMap<Integer,Experiment>();
		m_assistant = new Assistant();
	}
	
	/**
	 * Creates a new laboratory instance by setting its configuration based on
	 * external arguments. By default, this constructor does nothing more than
	 * the no-args constructor {@link #Laboratory()}. It should be overridden
	 * if lab instances depend on parameters obtained from another source, such
	 * as command line arguments. 
	 * @param arguments A map between strings and objects defining the
	 * configuration parameters to instantiate the lab.
	 */
	public Laboratory(Map<String,Object> arguments)
	{
		this();
	}
	
	/**
	 * Sets the assistant used to run experiments inside the lab.
	 * @param a The assistant
	 * @return This lab
	 */
	/*@ non_null @*/ public Laboratory setAssistant(/*@ non_null @*/ Assistant a)
	{
		m_assistant = a;
		return this;
	}
	
	/**
	 * Gets the assistant used to run experiments inside the lab.
	 * @return The assistant
	 */
	/*@ pure non_null @*/ public Assistant getAssistant()
	{
		return m_assistant;
	}
	
	/**
	 * Returns an experiment instance with given ID, if it exists.
	 * @param id The experiment ID
	 * @return The experiment instance, or <tt>null</tt> if the experiment
	 * does not exist in the lab
	 */
	/*@ pure null @*/ public Experiment getExperiment(int id)
	{
		if (m_experiments.containsKey(id))
		{
			return m_experiments.get(id);
		}
		return null;
	}
	
	/**
	 * Adds a list of 
	 * @param experiments
	 * @return
	 */
	/*@ non_null @*/ public Laboratory add(Experiment ... experiments)
	{
		for (Experiment e : experiments)
		{
			m_experiments.put(e.getId(), e);
		}
		return this;
	}

	/**
	 * Checks if an experiment exists inside the lab.
	 * @param e The experiment
	 * @return <tt>true</tt> if the experiment exists, <tt>false</tt>
	 * otherwise
	 */
	/*@ pure @*/ public boolean contains(Experiment e)
	{
		return m_experiments.containsKey(e.getId());
	}
	
	public static void main(String[] args)
	{
		CliParser p = new CliParser();
		ArgumentMap map = p.parse(args);
		Laboratory lab = new Laboratory();
		lab.setupServer();
		lab.setServerPort(8080);
		try 
		{
			lab.startServer();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	protected void setupServer()
	{
		registerCallback(new FreemarkerCallback(Method.GET, "/test", "test.ftlh").setTitle("Foo"));
	}
	
	public class Bla
	{
		
	}
}