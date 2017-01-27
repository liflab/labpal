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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.uqac.lif.azrael.SerializerException;
import ca.uqac.lif.azrael.json.JsonSerializer;
import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonParser;
import ca.uqac.lif.json.JsonParser.JsonParseException;
import ca.uqac.lif.labpal.CliParser.Argument;
import ca.uqac.lif.labpal.CliParser.ArgumentMap;
import ca.uqac.lif.labpal.server.HomePageCallback;
import ca.uqac.lif.labpal.server.WebCallback;
import ca.uqac.lif.labpal.server.LabPalServer;
import ca.uqac.lif.labpal.table.ExperimentTable;
import ca.uqac.lif.labpal.table.Table;
import ca.uqac.lif.labpal.table.TransformedTable;
import ca.uqac.lif.labpal.plot.Plot;
import ca.uqac.lif.labpal.provenance.DataTracker;
import ca.uqac.lif.tui.AnsiPrinter;

/**
 * A set of experiments, tables and plots. The lab is controlled by a
 * lab assistant, which is responsible for running the experiments,
 * populating the tables and drawing the plots.
 * 
 * @author Sylvain Hallé
 */
public abstract class Laboratory
{
	/* Return codes */
	public static transient int ERR_OK = 0;
	public static transient int ERR_LAB = 1;
	public static transient int ERR_REQUIREMENTS = 2;
	public static transient int ERR_IO = 3;
	public static transient int ERR_SERVER = 4;
	public static transient int ERR_ARGUMENTS = 5;

	/**
	 * The major version number
	 */
	private static final int s_majorVersionNumber = 2;

	/**
	 * The minor version number
	 */
	private static final int s_minorVersionNumber = 7;

	/**
	 * The revision version number
	 */
	private static final int s_revisionVersionNumber = 0;

	/**
	 * The set of experiments this lab has access to
	 */
	private HashSet<Experiment> m_experiments;

	/**
	 * The set of plots associated with this lab
	 */
	private transient HashSet<Plot> m_plots;

	/**
	 * The set of tables associated to this lab
	 */
	private transient HashSet<Table> m_tables;

	/**
	 * The set of groups associated with this lab
	 */
	private transient HashSet<Group> m_groups;

	/**
	 * The hostname of the machine running the lab
	 */
	private String m_hostName = guessHostName();

	/**
	 * A data tracker for generating provenance info
	 */
	private transient DataTracker m_dataTracker;
	
	/**
	 * A DOI assigned to this lab artifact, if any
	 */
	private transient String m_doi = "";

	/**
	 * The title given to this lab
	 */
	private String m_title = "Untitled";

	/**
	 * The lab's author
	 */
	private String m_author = "Fred Filntstone";

	/**
	 * The version string of this lab
	 */
	public static final transient String s_versionString = formatVersion();

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
	 * The number of parkmips (see {@link #countParkMips()})
	 */
	public transient static float s_parkMips = countParkMips();

	/**
	 * The number of loops that equal to 1 parkmip
	 */
	protected static final transient float s_parkMipsDivider = 5061974f;

	/**
	 * A (possibly long) textual description for what the lab does
	 */
	protected transient String m_description = null;

	/**
	 * A random number generator associated with this lab
	 */
	private transient Random m_random = new Random();

	/**
	 * The seed used to initialize the random number generator
	 */
	private int m_seed = 0;

	/**
	 * A counter for auto-incrementing experiment IDs
	 */
	private static transient int s_idCounter = 1;

	/**
	 * The serializer used to save/load the assistant's status
	 */
	private transient JsonSerializer m_serializer;

	/**
	 * The arguments parsed from the command line
	 */
	private transient ArgumentMap m_cliArguments = null;

	/**
	 * Creates a new lab assistant
	 */
	public Laboratory()
	{
		super();
		m_experiments = new HashSet<Experiment>();
		m_dataTracker = new DataTracker(this);
		m_plots = new HashSet<Plot>();
		m_tables = new HashSet<Table>();
		m_groups = new HashSet<Group>();
		m_assistant = null;
		m_serializer = new JsonSerializer();
		m_serializer.addClassLoader(ca.uqac.lif.labpal.Laboratory.class.getClassLoader());
	}

	/**
	 * Sets the assistant for this lab
	 * @param a The assistant
	 * @return This lab
	 */
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
	 * Assigns a DOI to this laboratory
	 * @param doi The DOI. If this is the null string, the argument is
	 * simply ignored.
	 * @return This lab
	 */
	public final Laboratory setDoi(String doi)
	{
		if (doi != null)
		{
			m_doi = doi;
		}
		return this;
	}
	
	/**
	 * Gets the DOI associated to this lab, if any
	 * @return The DOI or an empty string
	 */
	public final String getDoi()
	{
		return m_doi;
	}

	/**
	 * Adds an experiment to the lab
	 * @param e The experiment
	 * @param group The group to add this experiment to
	 * @param tables Optional: a number of tables this experiment should be
	 *   associated with
	 * @return This lab
	 */
	public Laboratory add(Experiment e, Group group, ExperimentTable ... tables)
	{
		int exp_id = s_idCounter++;
		e.setId(exp_id);
		m_experiments.add(e);
		addClassToSerialize(e.getClass());
		e.m_random = m_random;
		for (ExperimentTable p : tables)
		{
			p.add(e);
		}
		if (group != null)
		{
			group.add(e);
		}
		return this;
	}

	/**
	 * Adds an experiment to the lab
	 * @param e The experiment
	 * @param tables Optional: a number of tables this experiment should be
	 *   associated with
	 * @return This lab
	 */
	public Laboratory add(Experiment e, ExperimentTable ... tables)
	{
		return add(e, null, tables);
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
	 * Assigns plots to this lab
	 * @param plots The plots
	 * @return This lab
	 */
	public Laboratory add(Plot ... plots)
	{
		for (Plot p : plots)
		{
			m_plots.add(p);
		}
		return this;
	}

	/**
	 * Assigns one or more tables to this lab
	 * @param tables The tables
	 * @return This lab
	 */
	public Laboratory add(Table ... tables)
	{
		for (Table t : tables)
		{
			m_tables.add(t);
			if (t instanceof TransformedTable)
			{
				addInternalTable((TransformedTable) t);
			}
		}
		return this;
	}

	/**
	 * Adds the arguments of a transformed table 
	 * @param table
	 */
	protected void addInternalTable(TransformedTable table)
	{
		Set<Integer> table_ids = getTableIds();
		for (Table t : table.getInputTables())
		{
			if (!table_ids.contains(t.getId()))
			{
				// Add table to lab but make it invisible
				t.setShowInList(false);
				add(t);
			}
		}
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
	 * Gets the IDs of all the tables for this lab assistant
	 * @return The set of IDs
	 */
	public Set<Integer> getTableIds()
	{
		return getTableIds(false);
	}

	/**
	 * Gets the IDs of all the tables for this lab assistant
	 * @param including_invisible Set to {@code true} to also
	 * show invisible tables
	 * @return The set of IDs
	 */
	public Set<Integer> getTableIds(boolean including_invisible)
	{
		Set<Integer> ids = new HashSet<Integer>();
		for (Table p : m_tables)
		{
			if (including_invisible || p.showsInList())
			{
				ids.add(p.getId());
			}
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
	 * @return The lab, or null if some error occurred
	 * @throws SerializerException If the deserialization could not be done
	 * @throws JsonParseException If the JSON parsing could not be done
	 */
	public Laboratory loadFromString(String s) throws SerializerException, JsonParseException
	{
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(s);
		Laboratory lab = loadFromJson(je);
		return lab;
	}

	/**
	 * Creates a new lab assistant from the contents of a JSON element
	 * @param je The JSON element with the assistant's state
	 * @return The lab, or null if some error occurred
	 * @throws SerializerException If the deserialization could not be done
	 */
	public Laboratory loadFromJson(JsonElement je) throws SerializerException
	{
		Laboratory lab = (Laboratory) m_serializer.deserializeAs(je, this.getClass());
		// Don't forget to transplant the plots
		for (Plot p : m_plots)
		{
			p.assignTo(lab);
		}
		lab.m_plots = m_plots;
		// Don't forget to transplant the tables
		for (Table t : m_tables)
		{
			t.assignTo(lab);
		}
		lab.m_tables = m_tables;		
		// Don't forget to transplant the RNG
		for (Experiment e : lab.m_experiments)
		{
			e.m_random = lab.m_random;
		}
		return lab;		
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
		/* NOTE: this method should instead throw the exception and let
		 * higher levels of the GUI handle it, rather than silently fail
		 */
		try
		{
			JsonElement js_out = m_serializer.serializeAs(this, this.getClass());
			return js_out;
		}
		catch (SerializerException e)
		{
			// Do nothing
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

	/**
	 * Adds groups to this lab
	 * @param groups The groups
	 * @return This lab
	 */
	public Laboratory add(Group ... groups)
	{
		for (Group g : groups)
		{
			m_groups.add(g);
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
				.withLongName("console")
				.withDescription("Start LabPal in console mode"));
		parser.addArgument(new Argument()
				.withLongName("seed")
				.withArgument("x")
				.withDescription("Sets the seed for the random number generator to x"));
		parser.addArgument(new Argument()
				.withLongName("help")
				.withDescription("Prints command line usage"));
		parser.addArgument(new Argument()
				.withLongName("autostart")
				.withDescription("Queues all experiments and starts the assistant"));
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
		final AnsiPrinter stdout = new AnsiPrinter(System.out);		
		new_lab.setAssistant(assistant);
		new_lab.setupCli(parser);
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

		new_lab.m_cliArguments = parser.parse(args);
		if (new_lab.m_cliArguments == null)
		{
			// Could not parse command line arguments
			parser.printHelp(getCliHeader(), System.out);
			stdout.close();
			System.exit(ERR_ARGUMENTS);
		}
		if (new_lab.m_cliArguments.hasOption("help"))
		{
			parser.printHelp(getCliHeader(), System.out);
			stdout.close();
			System.exit(ERR_OK);
		}
		if (new_lab.m_cliArguments.hasOption("seed"))
		{
			// Sets random seed
			int seed = Integer.parseInt(new_lab.m_cliArguments.getOptionValue("seed"));
			new_lab.setRandomSeed(seed);
		}
		List<WebCallback> callbacks = new ArrayList<WebCallback>();
		new_lab.setupCallbacks(callbacks);
		stdout.resetColors();
		stdout.print(getCliHeader());
		int code = ERR_OK;
		new_lab.setup();
		if (!new_lab.m_cliArguments.hasOption("console"))
		{
			// Start ParkBench's web interface
			LabPalServer server = new LabPalServer(new_lab.m_cliArguments, new_lab, assistant);
			if (callbacks != null)
			{
				// Register custom callbacks, if any
				for (WebCallback cb : callbacks)
				{
					server.registerCallback(0, cb);
				}				
			}
			stdout.print("Visit http://" + server.getServerName() + ":" + server.getServerPort() + HomePageCallback.URL + " in your browser\n");
			stdout.print("Hit Ctrl+C in this window to stop\n");
			try
			{
				server.startServer();
			}
			catch (IOException e)
			{
				System.err.println("Cannot start server on port " + server.getServerPort() +". Is another lab already running?");
				stdout.close();
				System.exit(ERR_SERVER);
			}
			if (new_lab.m_cliArguments.hasOption("autostart"))
			{
				new_lab.startAll();
			}
			while (true)
			{
				Experiment.wait(10000);
			}
		}
		else
		{
			// Start LabPal's text interface
			LabPalTui tui = new LabPalTui(new_lab, assistant, stdout, new_lab.m_cliArguments);
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
	 * @return null if the tests <em>can</em> be run, a String with an
	 *   explanation otherwise.
	 */
	public String isEnvironmentOk()
	{
		return null;
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
	 */
	public abstract void setup();

	/**
	 * Sets the custom callbacks to the web server. 
	 * This allows a lab to include custom pages in its web interface.
	 * @param callbacks An empty list. The method should add new
	 *   callbacks to this list.
	 *   These callbacks will be added to the server if the {@code --web}
	 *   option is used at startup.
	 */
	public void setupCallbacks(List<WebCallback> callbacks)
	{
		// Do nothing
	}

	/**
	 * Gets the command-line arguments parsed when launching this lab.
	 * @return A map of arguments and values parsed from the command line.
	 *   If you specified custom command-line arguments in
	 *   {@link #setupCli(CliParser)}, this is where you can retrieve them
	 *   and read their values.
	 */
	public final ArgumentMap getCliArguments()
	{
		return m_cliArguments;
	}

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
		return (float) i / s_parkMipsDivider;
	}

	public static void main(String[] args)
	{
		System.out.println(getCliHeader());
		System.out.println("You are running labpal.jar, which is only a library to create\nyour own test suites. As a result nothing will happen here. Read the \nonline documentation to learn how to use LabPal.");
	}

	protected static String getCliHeader()
	{
		String out = "";
		out += "LabPal " + formatVersion() + " - A versatile environment for running experiments\n";
		out += "(C) 2015-2017 Laboratoire d'informatique formelle\nUniversité du Québec à Chicoutimi, Canada\n";
		return out;
	}

	/**
	 * Sets the seed of the lab's random number generator
	 * @param seed The seed
	 * @return This lab
	 */
	public final Laboratory setRandomSeed(int seed)
	{
		m_random.setSeed(seed);
		m_seed = seed;
		return this;
	}

	/**
	 * Gets the seed of the lab's random number generator
	 * @return The seed
	 */
	public final int getRandomSeed()
	{
		return m_seed;
	}

	/**
	 * Gets a reference to the lab's random number generator
	 * @return The generator
	 */
	public final ca.uqac.lif.labpal.Random getRandom()
	{
		return m_random;
	}

	/**
	 * Gives a textual description to the laboratory.
	 * @param description The description. This string must be
	 *  valid HTML; in particular, HTML special characters
	 *  (<tt>&amp;</tt>, <tt>&lt;</tt>, etc.) <em>must</em> be escaped;
	 *  otherwise there may be problems displaying the description in
	 *  the web interface.
	 * @return This lab
	 */
	public final Laboratory setDescription(String description)
	{
		m_description = description;
		return this;
	}

	/**
	 * Gets the lab's textual description. This description is either the
	 * text given in a previous call to {@link #setDescription(String)}, or,
	 * if no such call was made, the contents of a file called
	 * <tt>description.html</tt> that resides beside the lab. If no such file
	 * exists, the empty string is returned.
	 *  
	 * @return The lab's description
	 */
	public final String getDescription()
	{
		if (m_description != null)
		{
			return m_description;
		}
		String s = FileHelper.internalFileToString(getClass(), "description.html");
		if (s != null)
		{
			// Get only body
			Pattern pat = Pattern.compile("<body.*?>(.*?)</body", Pattern.DOTALL);
			Matcher mat = pat.matcher(s);
			if (mat.find())
			{
				return mat.group(1);
			}			
		}
		return "";
	}

	/**
	 * Gets the set of all groups in this lab
	 * @return The set of groups
	 */
	public final Set<Group> getGroups()
	{
		return m_groups;
	}

	/**
	 * Gets the set of all experiment IDs that don't belong to any
	 * group.
	 * @return The set of IDs
	 */
	public final Set<Integer> getOrphanExperiments()
	{
		HashSet<Integer> out = new HashSet<Integer>();
		out.addAll(getExperimentIds());
		for (Group g : m_groups)
		{
			out.removeAll(g.getExperimentIds());
		}
		return out;
	}

	/**
	 * Fetches the groups an experiment belongs to
	 * @param id The experiment's ID
	 * @return The groups this experiment belongs to
	 */
	public final Set<Group> getGroups(int id)
	{
		HashSet<Group> groups = new HashSet<Group>();
		for (Group g : m_groups)
		{
			if (g.belongsTo(id))
			{
				groups.add(g);
			}
		}
		return groups;
	}

	public final Set<Integer> getGroupIds()
	{
		HashSet<Integer> ids = new HashSet<Integer>();
		for (Group g : m_groups)
		{
			ids.add(g.getId());
		}
		return ids;
	}

	/**
	 * Fetches the groups an experiment belongs to
	 * @param e The experiment
	 * @return The groups this experiment belongs to
	 */
	public final Set<Group> getGroup(Experiment e)
	{
		return getGroups(e.getId());
	}

	/**
	 * Attempts to get a reference to one of the classes defined in this
	 * lab
	 * @param name The fully qualified name of the class
	 * @return The class, or null if the class could not be found
	 * @throws ClassNotFoundException
	 */
	public final Class<?> findClass(String name) throws ClassNotFoundException
	{
		return m_serializer.findClass(name);
	}

	/**
	 * Gets the table with given ID
	 * @param table_id The table ID
	 * @return The table, <tt>null</tt> if not found
	 */
	public Table getTable(int table_id)
	{
		for (Table t : m_tables)
		{
			if (t.getId() == table_id)
			{
				return t;
			}
		}
		return null;
	}

	/**
	 * Gets a table by its nickname
	 * @param nickname The nickname
	 * @return The table, {@code null} if not found
	 */
	public Table getTable(String nickname)
	{
		for (Table t : m_tables)
		{
			if (t.getNickname().compareTo(nickname) == 0)
			{
				return t;
			}
		}
		return null;
	}

	/**
	 * Fetches a group based on its ID
	 * @param id The group's id
	 * @return The group, or <tt>null</tt> if not found
	 */
	public Group getGroupById(int id)
	{
		for (Group g : m_groups)
		{
			if (g.getId() == id)
			{
				return g;
			}
		}
		return null;
	}

	/**
	 * Creates a new group and adds it to the current lab
	 * @see Group#Group(String)
	 * @param name The group's name
	 * @return The group
	 */
	public Group newGroup(String name)
	{
		Group g = new Group(name);
		add(g);
		return g;
	}

	/**
	 * Gets the major version number
	 * @return The number
	 */
	public static final int getMajor()
	{
		return s_majorVersionNumber;
	}

	/**
	 * Gets the minor version number
	 * @return The number
	 */
	public static final int getMinor()
	{
		return s_minorVersionNumber;
	}

	/**
	 * Gets the revision version number
	 * @return The number
	 */
	public static final int getRevision()
	{
		return s_revisionVersionNumber;
	}

	protected static String formatVersion()
	{
		if (getRevision() == 0)
		{
			return s_majorVersionNumber + "." + s_minorVersionNumber;
		}
		return s_majorVersionNumber + "." + s_minorVersionNumber + "." + s_revisionVersionNumber;
	}

	/**
	 * Gets the number of "data points" generated by this lab. This can be
	 * used to get an estimate "size" of the lab.
	 * @return The number of points
	 */
	public int countDataPoints()
	{
		int pts = 0;
		for (Experiment e : m_experiments)
		{
			pts += e.countDataPoints();
		}
		return pts;
	}

	/**
	 * Attempts to retrieve the host name of the machine running this lab.
	 * This is done by running the "hostname" command at the command and
	 * fetching its resulting string.
	 * @return A string with the hostname. If launching the command
	 * resulted in an error, returns an empty string or {@code null}
	 */
	public String getHostName()
	{
		return m_hostName;
	}

	/**
	 * Guesses the host name
	 * @return The host name
	 */
	protected static String guessHostName()
	{
		byte[] bytes = CommandRunner.runAndGet("hostname", null);
		String name = new String(bytes);
		return name.trim();
	}

	/**
	 * Gets the instance of the data tracker used for provenance
	 * information
	 * @return The data tracker
	 */
	public DataTracker getDataTracker()
	{
		return m_dataTracker;
	}

	/**
	 * Queues all the experiments and starts the assistant
	 */
	public void startAll()
	{
		for (Experiment e : m_experiments)
		{
			m_assistant.queue(e);
		}
		start();
	}

}
