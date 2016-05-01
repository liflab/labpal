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
package ca.uqac.lif.parkbench;

import java.util.HashSet;
import java.util.Set;

import ca.uqac.lif.azrael.SerializerException;
import ca.uqac.lif.azrael.json.JsonListHandler;
import ca.uqac.lif.azrael.json.JsonSerializer;
import ca.uqac.lif.azrael.json.JsonSetHandler;
import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonParser;
import ca.uqac.lif.json.JsonParser.JsonParseException;
import ca.uqac.lif.parkbench.CliParser.Argument;
import ca.uqac.lif.parkbench.CliParser.ArgumentMap;
import ca.uqac.lif.tui.AnsiPrinter;

/**
 * A set of experiments and plots. The lab is controlled by a
 * lab assistant, which is responsible for running the experiments
 * and drawing the plots.
 * 
 * @author Sylvain Hallé
 */
public abstract class Laboratory
{
	public static transient int ERR_OK = 0;
	public static transient int ERR_LAB = 1;
	public static transient int ERR_REQUIREMENTS = 2;
	public static transient int ERR_IO = 3;
	
	/**
	 * The set of experiments this lab has access to
	 */
	private HashSet<Experiment> m_experiments;
	
	/**
	 * The set of plots associated with this lab
	 */
	private HashSet<Plot> m_plots;
	
	/**
	 * The title given to this lab
	 */
	private String m_title = "Untitled";
	
	/**
	 * The version string of this lab
	 */
	public static final String s_versionString = "v2.0";
	
	/**
	 * The default file extension to save experiment results
	 */
	public static final String s_fileExtension = "labo";
	
	/**
	 * The dispatcher that currently executes an experiment (if any)
	 */
	private transient LabAssistant m_assistant;
	
	/**
	 * The thread that runs this assistant
	 */
	private transient Thread m_thread;
	
	/**
	 * The number of parkmips
	 * @see {@link #countParkMips()}
	 */
	public transient static float s_parkMips = countParkMips(); 
	
	/**
	 * A counter for auto-incrementing experiment IDs
	 */
	private static transient int s_idCounter = 0;
	
	/**
	 * The serializer used to save/load the assistant's status
	 */
	private static transient JsonSerializer s_serializer;
	
	static
	{
		// Serializer setup
		s_serializer = new JsonSerializer();
		s_serializer.addObjectHandler(0, new JsonSetHandler(s_serializer));
		s_serializer.addObjectHandler(0, new JsonListHandler(s_serializer));
	}

	/**
	 * Creates a new lab assistant from the contents of a JSON string
	 * @param s The JSON string with the assistant's state
	 * @return The lab assistant, or null if some error occurred
	 */
	public static Laboratory loadFromString(String s)
	{
		JsonParser jp = new JsonParser();
		JsonElement je;
		try
		{
			je = jp.parse(s);
			return loadFromJson(je);
		}
		catch (JsonParseException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
		
	/**
	 * Creates a new lab assistant from the contents of a JSON element
	 * @param je The JSON element with the assistant's state
	 * @return The lab assistant, or null if some error occurred
	 */
	public static Laboratory loadFromJson(JsonElement je)
	{
		Laboratory a = null;
		try
		{
			a = (Laboratory) s_serializer.deserializeAs(je, Laboratory.class);
		}
		catch (SerializerException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return a;		
	}

	/**
	 * Saves the state of a lab assistant to a JSON string
	 * @param a The assistant
	 * @return The JSON string with the assistant's state, or null
	 *   if some error occurred
	 */
	public static String saveToString(Laboratory a)
	{
		JsonElement je = saveToJson(a);
		if (je != null)
		{
			return je.toString();
		}
		return null;
	}

	/**
	 * Saves the state of a lab assistant to a JSON element
	 * @param a The assistant
	 * @return The JSON element with the assistant's state, or null
	 *   if some error occurred
	 */
	public static JsonElement saveToJson(Laboratory a)
	{
		try
		{
			JsonElement js_out = s_serializer.serialize(a);
			return js_out;
		}
		catch (SerializerException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Creates a new lab assistant
	 */
	public Laboratory()
	{
		super();
		m_experiments = new HashSet<Experiment>();
		m_plots = new HashSet<Plot>();
		m_assistant = null;
	}
	
	public Laboratory setAssistant(LabAssistant a)
	{
		m_assistant = a;
		return this;
	}
	
	/**
	 * Adds an experiment to the lab
	 * @param id The experiment
	 * @return This assistant
	 */
	public Laboratory add(Experiment e)
	{
		e.setId(s_idCounter++);
		m_experiments.add(e);
		return this;
	}
	
	/**
	 * Adds an experiment to the lab and queues it to the
	 * assistant
	 * @param e The experiment
	 * @return This lab
	 */
	public Laboratory addAndQueue(Experiment e)
	{
		add(e);
		m_assistant.queue(e);
		return this;
	}
	
	/**
	 * Assigns a plot to this lab
	 * @param p The plot
	 * @return This lab
	 */
	public Laboratory add(Plot p)
	{
		m_plots.add(p);
		return this;
	}
	
	/**
	 * Gets the IDs of all the plots for this lab assistant
	 * @return The set of IDs
	 */
	public Set<Integer> getPlotIds()
	{
		Set<Integer> ids = new HashSet<Integer>();
		for (Plot p : m_plots)
		{
			ids.add(p.getId());
		}
		return ids;
	}
	
	/**
	 * Gets the plot with given ID
	 * @param id The ID
	 * @return The plot, null if not found
	 */
	public Plot getPlot(int id)
	{
		for (Plot p : m_plots)
		{
			if (p.getId() == id)
			{
				return p;
			}
		}
		return null;
	}

	
	/**
	 * Gets the title given to this assistant
	 * @return The title
	 */
	public String getTitle()
	{
		return m_title;
	}
	
	/**
	 * Sets the title for this lab assistant
	 * @param t The title
	 * @return This assistant
	 */
	public Laboratory setTitle(String t)
	{
		m_title = t;
		return this;
	}
	
	/**
	 * Fetches an experiment based on its ID
	 * @param id The ID
	 * @return The experiment if found, null otherwise
	 */
	public synchronized Experiment getExperiment(int id)
	{
		for (Experiment e : m_experiments)
		{
			if (id == e.getId())
			{
				return e;
			}
		}
		return null;
	}
	
	/**
	 * Gets the IDs of all the experiments for this lab assistant
	 * @return The set of IDs
	 */
	public Set<Integer> getExperimentIds()
	{
		Set<Integer> ids = new HashSet<Integer>();
		for (Experiment e : m_experiments)
		{
			ids.add(e.getId());
		}
		return ids;
	}
	
	public Laboratory start()
	{
		m_thread = new Thread(m_assistant);
		m_thread.start();
		return this;
	}
	
	public static final void initialize(String[] args, Class<? extends Laboratory> clazz)
	{
		initialize(args, clazz, new LinearAssistant());
	}
	
	public static final void initialize(String[] args, Class<? extends Laboratory> clazz, LabAssistant assistant)
	{
		CliParser parser = new CliParser();
		parser.addArgument(new Argument()
				.withLongName("color")
				.withDescription("Enables color and Unicode in text interface"));
		Laboratory new_lab = null;
		try
		{
			new_lab = clazz.newInstance();
		} 
		catch (InstantiationException e)
		{
			e.printStackTrace();
			System.exit(ERR_LAB);
		} 
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
			System.exit(ERR_LAB);
		}
		if (new_lab == null)
		{
			System.exit(ERR_LAB);
		}
		new_lab.setAssistant(assistant);
		new_lab.setupCli(parser);
		ArgumentMap map = parser.parse(args);
		new_lab.setupExperiments(map);
		AnsiPrinter stdout = new AnsiPrinter(System.out);
		stdout.resetColors();
		stdout.print("ParkBench " + s_versionString + " - A versatile environment for running experiments\n");
		stdout.print("(C) 2015-2016 Laboratoire d'informatique formelle\n");
		ParkbenchTui tui = new ParkbenchTui(new_lab, assistant, stdout, map);
		int code = tui.run();		
		stdout.close();
		System.exit(code);
	}
		
	/**
	 * Checks whether the environment for running the experiments in this lab
	 * is appropriate. Override this method if you want to check that some
	 * <em>general</em> conditions on the environment are met (such as: presence
	 * of some executable in the path, existence of a network connection, etc.).
	 * <p>
	 * As a rule, returning false means that something <em>external</em> to the
	 * lab must be fixed before running the experiments; hence ParkBench will
	 * simply quit. If your experiments
	 * have prerequisites they can generate, don't use this method.
	 *  
	 * @return true if the tests can be run, false otherwise.
	 */
	public boolean isEnvironmentOk()
	{
		return true;
	}
	
	/**
	 * Sets up the command-line parser to accept arguments specific to this
	 * lab. Your lab should override this method if you wish to receive custom
	 * CLI arguments. In such a case, call the methods on the <tt>parser</tt>
	 * variable to add new command-line switches.
	 * 
	 * @param parser The command-line parser
	 */
	public void setupCli(CliParser parser)
	{
		return;
	}
	
	/**
	 * Sets up the experiments and plots that this lab will contain. You
	 * <em>must</em> implement this method and add at least one experiment
	 * (otherwise there won't be anything to do with your lab).
	 * 
	 * @param map A map of arguments and values parsed from the command line.
	 *   If you specified custom command-line arguments in
	 *   {@link #setupCli(CliParser)}, this is where you can retrieve them
	 *   and read their values.
	 */
	public abstract void setupExperiments(ArgumentMap map);
	
	/**
	 * Counts the number of "parkmips" of this system. This is a very rough
	 * indicator of the system's speed, measured in the number of increments
	 * of a long variable during a third of a second.
	 * <p>
	 * ParkMips are designed to equal roughly 1 when run on an Asus Transformer
	 * Book T200 (yes, this is totally arbitrary).
	 * @return The number of parkmips
	 */
	public static float countParkMips()
	{
		long start = System.currentTimeMillis();
		long i = 0;
		while (System.currentTimeMillis() - start < 333)
		{
			i++;
		}
		return i / 5061974;
	}
	
}
