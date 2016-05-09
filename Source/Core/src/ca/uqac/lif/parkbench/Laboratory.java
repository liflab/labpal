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
import ca.uqac.lif.azrael.json.JsonSerializer;
import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonParser;
import ca.uqac.lif.json.JsonParser.JsonParseException;
import ca.uqac.lif.parkbench.CliParser.Argument;
import ca.uqac.lif.parkbench.CliParser.ArgumentMap;
import ca.uqac.lif.parkbench.plot.Plot;
import ca.uqac.lif.parkbench.server.ParkbenchServer;
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
	private transient HashSet<Plot> m_plots;

	/**
	 * The title given to this lab
	 */
	private String m_title = "Untitled";
	
	/**
	 * The lab's author
	 */
	private String m_author= "Fred Filntstone";

	/**
	 * The version string of this lab
	 */
	public static final transient String s_versionString = "v2.0";

	/**
	 * The default file extension to save experiment results
	 */
	public static final transient String s_fileExtension = "labo";

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
	private static transient int s_idCounter = 1;

	/**
	 * The serializer used to save/load the assistant's status
	 */
	private transient JsonSerializer m_serializer;

	/**
	 * Creates a new lab assistant
	 */
	public Laboratory()
	{
		super();
		m_experiments = new HashSet<Experiment>();
		m_plots = new HashSet<Plot>();
		m_assistant = null;
		m_serializer = new JsonSerializer();
		m_serializer.addClassLoader(ca.uqac.lif.parkbench.Laboratory.class.getClassLoader());
	}

	public Laboratory setAssistant(LabAssistant a)
	{
		m_assistant = a;
		return this;
	}
	
	/**
	 * Sets the lab's author
	 * @param author The author's name
	 * @return This lab
	 */
	public final Laboratory setAuthorName(String author)
	{
		m_author = author;
		return this;
	}
	
	/**
	 * Gets the lab's author
	 * @return The author's name
	 */
	public final String getAuthorName()
	{
		return m_author;
	}

	/**
	 * Adds an experiment to the lab
	 * @param e The experiment
	 * @param plots Optional: a number of plots this experiment should be
	 *   associated with
	 * @return This lab
	 */
	public Laboratory add(Experiment e, Plot ... plots)
	{
		e.setId(s_idCounter++);
		m_experiments.add(e);
		for (Plot p : plots)
		{
			p.add(e);
		}
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

	/**
	 * Creates a new lab assistant from the contents of a JSON string
	 * @param s The JSON string with the assistant's state
	 * @return The lab assistant, or null if some error occurred
	 */
	public Laboratory loadFromString(String s)
	{
		JsonParser jp = new JsonParser();
		JsonElement je;
		try
		{
			je = jp.parse(s);
			Laboratory lab = loadFromJson(je);
			// Don't forget to transplant the plots
			for (Plot p : m_plots)
			{
				p.assignTo(lab);
			}
			lab.m_plots = m_plots;
			return lab;
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
	public Laboratory loadFromJson(JsonElement je)
	{
		Laboratory a = null;
		try
		{
			a = (Laboratory) m_serializer.deserializeAs(je, this.getClass());
		}
		catch (SerializerException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return a;		
	}

	/**
	 * Saves the state of the lab assistant to a JSON string
	 * @return The JSON string with the assistant's state, or null
	 *   if some error occurred
	 */
	public String saveToString()
	{
		JsonElement je = saveToJson();
		if (je != null)
		{
			return je.toString();
		}
		return null;
	}

	/**
	 * Saves the state of the lab to a JSON element
	 * @return The JSON element with the assistant's state, or null
	 *   if some error occurred
	 */
	public JsonElement saveToJson()
	{
		try
		{
			JsonElement js_out = m_serializer.serializeAs(this, this.getClass());
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
	 * Adds a class that must be serialized with the benchmark
	 * @param clazz The class
	 * @return This lab
	 */
	public Laboratory addClassToSerialize(Class<?> clazz)
	{
		if (clazz != null)
		{
			m_serializer.addClassLoader(clazz.getClassLoader());
		}
		return this;
	}

	public static final void initialize(String[] args, Class<? extends Laboratory> clazz)
	{
		initialize(args, clazz, new LinearAssistant());
	}

	public static final void initialize(String[] args, Class<? extends Laboratory> clazz, final LabAssistant assistant)
	{
		CliParser parser = new CliParser();
		parser.addArgument(new Argument()
		.withLongName("color")
		.withDescription("Enables color and Unicode in text interface"));
		parser.addArgument(new Argument()
		.withLongName("web")
		.withDescription("Start ParkBench as a web server"));
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
		final ArgumentMap map = parser.parse(args);
		new_lab.setupExperiments(map);
		final AnsiPrinter stdout = new AnsiPrinter(System.out);
		stdout.resetColors();
		int code = ERR_OK;
		// Properly close print streams when closing the program
		// https://www.securecoding.cert.org/confluence/display/java/FIO14-J.+Perform+proper+cleanup+at+program+termination
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				assistant.stop();
				stdout.close();
			}
		}));
		stdout.print(getCliHeader());
		if (map.hasOption("web"))
		{
			// Start ParkBench's web interface
			ParkbenchServer server = new ParkbenchServer(map, new_lab, assistant);
			stdout.print("Server started on " + server.getServerName() + ":" + server.getServerPort() + "\n");
			server.startServer();
			while (true)
			{
				Experiment.wait(10000);
			}
		}
		else
		{
			// Start ParkBench's text interface
			ParkbenchTui tui = new ParkbenchTui(new_lab, assistant, stdout, map);
			code = tui.run();
		}
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
		return (float) i / 5061974f;
	}

	public static void main(String[] args)
	{
		System.out.println(getCliHeader());
		System.out.println("You are running parkbench.jar, which is only a library to create\nyour own test suites. As a result nothing will happen here. Read the \nonline documentation to learn how to use ParkBench.");
	}
	
	protected static String getCliHeader()
	{
		String out = "";
		out += "ParkBench " + s_versionString + " - A versatile environment for running experiments\n";
		out += "(C) 2015-2016 Laboratoire d'informatique formelle\n";
		return out;
	}

}
