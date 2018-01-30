/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2018 Sylvain Hallé

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import ca.uqac.lif.azrael.GenericSerializer;
import ca.uqac.lif.azrael.SerializerException;
import ca.uqac.lif.azrael.json.JsonSerializer;
import ca.uqac.lif.jerrydog.Server;
import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonMap;
import ca.uqac.lif.json.JsonParser;
import ca.uqac.lif.json.JsonParser.JsonParseException;
import ca.uqac.lif.labpal.CliParser.Argument;
import ca.uqac.lif.labpal.CliParser.ArgumentMap;
import ca.uqac.lif.labpal.Experiment.QueueStatus;
import ca.uqac.lif.labpal.Experiment.Status;
import ca.uqac.lif.labpal.macro.Macro;
import ca.uqac.lif.labpal.server.HomePageCallback;
import ca.uqac.lif.labpal.server.HttpUtilities;
import ca.uqac.lif.labpal.server.WebCallback;
import ca.uqac.lif.labpal.server.LabPalServer;
import ca.uqac.lif.labpal.table.ExperimentTable;
import ca.uqac.lif.mtnp.table.Table;
import ca.uqac.lif.mtnp.table.TransformedTable;
import ca.uqac.lif.petitpoucet.OwnershipManager;
import ca.uqac.lif.mtnp.DataFormatter;
import ca.uqac.lif.mtnp.plot.Plot;
import ca.uqac.lif.labpal.provenance.DataTracker;
import ca.uqac.lif.tui.AnsiPrinter;

/**
 * A set of experiments, tables and plots. The lab is controlled by a
 * lab assistant, which is responsible for running the experiments,
 * populating the tables and drawing the plots.
 * 
 * @author Sylvain Hallé
 */
public abstract class Laboratory implements OwnershipManager
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
	private static final transient int s_majorVersionNumber = 2;

	/**
	 * The minor version number
	 */
	private static final transient int s_minorVersionNumber = 10;

	/**
	 * The revision version number
	 */
	private static final transient int s_revisionVersionNumber = 0;

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
	 * The set of macros associated to this lab
	 */
	private transient Set<Macro> m_macros;

	/**
	 * The set of groups associated with this lab
	 */
	private HashSet<Group> m_groups;

	/**
	 * The set of classes that are serialized with the lab
	 */
	private transient Set<Class<?>> m_serializableClasses;

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
	 * A filter for experiments
	 */
	private transient ExperimentFilter m_filter;

	/**
	 * The version string of this lab
	 */
	public static final transient String s_versionString = formatVersion();

	/**
	 * The default file extension to save experiment results
	 */
	public static final transient String s_fileExtension = "labo";

	/**
	 * The MIME type for LabPal files
	 */
	public static final transient String s_mimeType = "application/labpal";

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
	private transient int m_idCounter = 1;

	/**
	 * The serializer used to save/load the assistant's status
	 */
	private transient JsonSerializer m_serializer;

	/**
	 * The arguments parsed from the command line
	 */
	private transient ArgumentMap m_cliArguments = null;

	/**
	 * Whether the lab is currently being deserialized
	 */
	private transient boolean m_isDeserialized = false;

	/**
	 * A result reporter
	 */
	private transient ResultReporter m_reporter;
	
	/**
	 * A set of claims associated to this lab
	 */
	private transient List<Claim> m_claims;
	
	/**
	 * A map associating to each registered claim the last computed result
	 * for that claim
	 */
	private transient Map<Integer,Claim.Result> m_claimStatus;
	
	/**
	 * The default filename assumed for the HTML description
	 */
	private static transient final String s_descriptionDefaultFilename = "description.html"; 

	/**
	 * Creates a new lab assistant
	 */
	public Laboratory()
	{
		super();
		m_experiments = new HashSet<Experiment>();
		m_claims = new ArrayList<Claim>();
		m_claimStatus = new HashMap<Integer,Claim.Result>();
		m_dataTracker = new DataTracker(this);
		m_plots = new HashSet<Plot>();
		m_tables = new HashSet<Table>();
		m_groups = new HashSet<Group>();
		m_macros = new HashSet<Macro>();
		m_assistant = null;
		m_serializableClasses = new HashSet<Class<?>>();
		m_serializer = new JsonSerializer();
		m_serializer.addClassLoader(ca.uqac.lif.labpal.Laboratory.class.getClassLoader());
		m_reporter = new ResultReporter(this);
		if (FileHelper.internalFileExists(getClass(), s_descriptionDefaultFilename))
		{
			setDescription(FileHelper.internalFileToString(getClass(), "description.html"));
		}
	}

	/**
	 * Sets the assistant for this lab
	 * @param a The assistant
	 * @return This lab
	 */
	public Laboratory setAssistant(LabAssistant a)
	{
		m_assistant = a;
		a.setLaboratory(this);
		return this;
	}

	/**
	 * Sets the lab's author
	 * @param author The author's name
	 * @return This lab
	 */
	public final Laboratory setAuthor(String author)
	{
		m_author = author;
		return this;
	}

	/**
	 * Gets the results reporter responsible for reporting the
	 * experimental results
	 * @return The reporter
	 */
	public final ResultReporter getReporter()
	{
		return m_reporter;
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
		Experiment target_e = e;
		int exp_id = m_idCounter++;
		target_e.setId(exp_id);
		addClassToSerialize(target_e.getClass());
		target_e.m_random = m_random;
		if (!m_isDeserialized)
		{
			m_experiments.add(e);
		}
		else
		{
			target_e = getExperiment(exp_id);
			if (target_e == null)
			{
				// Not supposed to happen!
				throw new RuntimeException("Experiment #" + exp_id + " cannot be found in deserialized lab.");
			}
		}
		for (ExperimentTable p : tables)
		{
			p.add(target_e);
		}
		if (group != null && !m_isDeserialized)
		{
			group.add(target_e);
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
	 * Adds one or more macros to this lab
	 * @param macros The macros
	 * @return This lab
	 */
	public Laboratory add(Macro ... macros)
	{
		for (Macro m : macros)
		{
			m_macros.add(m);
		}
		return this;
	}
	
	/**
	 * Assigns claims to this lab
	 * @param claims The claims
	 * @return This lab
	 */
	public Laboratory add(Claim ... claims)
	{
		for (Claim c : claims)
		{
			m_claims.add(c);
			m_claimStatus.put(c.getId(), Claim.Result.UNKNOWN);
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
	
	/**
	 * Gets a claim with given ID
	 * @param id The ID of the claim to look for
	 * @return The claim if found, {@code null} otherwise
	 */
	public final Claim getClaim(int id)
	{
		for (Claim c : m_claims)
		{
			if (c.getId() == id)
			{
				return c;
			}
		}
		return null;
	}
	
	/**
	 * Gets the list of all claims associated to this lab
	 * @return The list of claims
	 */
	public List<Claim> getClaims()
	{
		return m_claims;
	}
	
	/**
	 * Gets the latest status of each claim associated to this lab
	 * @return A map associating claim IDs with their latest computed result
	 */
	public final Map<Integer,Claim.Result> getClaimResults()
	{
		return m_claimStatus;
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
	public synchronized Laboratory loadFromJson(JsonElement je) throws SerializerException
	{
		Laboratory lab = (Laboratory) m_serializer.deserializeAs(je, this.getClass());
		lab.m_isDeserialized = true;
		Table.resetCounter();
		Macro.resetCounter();
		Plot.resetCounter();
		lab.setup();
		lab.m_isDeserialized = false;
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
	 * @throws NoEmptyConstructorException If the class to be added
	 *   does not have a no-args constructor
	 */
	public Laboratory addClassToSerialize(Class<?> clazz)
	{
		if (clazz != null)
		{
			m_serializableClasses.add(clazz);
			m_serializer.addClassLoader(clazz.getClassLoader());
		}
		return this;
	}

	/**
	 * Gets the set of all classes that are serialized with this lab.
	 * This method is used by the web interface to display a warning to
	 * the user if one of the classes does not have a no-args constructor.
	 * @return The set of classes
	 */
	public final Set<Class<?>> getSerializableClasses()
	{
		return m_serializableClasses;
	}

	/**
	 * Adds groups to this lab
	 * @param groups The groups
	 * @return This lab
	 */
	public Laboratory add(Group ... groups)
	{
		if (!m_isDeserialized)
		{
			for (Group g : groups)
			{
				m_groups.add(g);
			}			
		}
		return this;
	}

	/**
	 * Loads a laboratory from an input stream containing a zip file
	 * @param is The input stream
	 * @return A new lab instance
	 * @throws IOException
	 * @throws SerializerException
	 * @throws JsonParseException
	 */
	public final Laboratory loadFromZip(InputStream is) throws IOException, SerializerException, JsonParseException
	{
		ZipInputStream zis = new ZipInputStream(is);
		ZipEntry entry;
		entry = zis.getNextEntry();
		byte[] contents = null;
		while (entry != null)
		{
			//String name = entry.getName();
			contents = HttpUtilities.extractFile(zis);
			// We assume the zip to contain a single file 
			break;
		}
		String json = new String(contents);
		return loadFromString(json);
	}

	/**
	 * Loads a laboratory from an input stream containing a JSON text document
	 * @param is The input stream
	 * @return A new lab instance
	 * @throws SerializerException
	 * @throws JsonParseException
	 */
	public final Laboratory loadFromJson(InputStream is) throws SerializerException, JsonParseException
	{
		String json = FileHelper.readToString(is);
		return loadFromString(json);
	}

	/**
	 * Attempts to load a laboratory from a local file, specified by a filename.
	 * This method prints error messages to the standard error if something goes
	 * wrong
	 * @param new_lab An instance of lab for the deserialization
	 * @param filename The name of the file to look for
	 * @return The deserialized lab
	 */
	protected static final Laboratory loadFromFilename(Laboratory new_lab, String filename)
	{
		if (filename.endsWith(".zip") || filename.endsWith("." + s_fileExtension))
		{
			try
			{
				// Substitute current lab for one loaded from the file
				new_lab = new_lab.loadFromZip(new FileInputStream(new File(filename)));
			}
			catch (FileNotFoundException e)
			{
				System.err.println("WARNING: file " + filename + " not found. An empty lab will be started instead.");
			}
			catch (IOException e)
			{
				System.err.println("WARNING: file " + filename + " could not be read. An empty lab will be started instead.");
			}
			catch (SerializerException e)
			{
				System.err.println("WARNING: a lab could not be loaded from the contents of " + filename + " .");
			}
			catch (JsonParseException e)
			{
				System.err.println("WARNING: a lab could not be loaded from the contents of " + filename + " .");
			}
		}
		else
		{
			// Substitute current lab for one loaded from the file
			try
			{
				new_lab = new_lab.loadFromJson(new FileInputStream(new File(filename)));
			} 
			catch (FileNotFoundException e)
			{
				System.err.println("WARNING: file " + filename + " not found. An empty lab will be started instead.");
			}
			catch (SerializerException e)
			{
				System.err.println("WARNING: a lab could not be loaded from the contents of " + filename + " .");
			}
			catch (JsonParseException e)
			{
				System.err.println("WARNING: a lab could not be loaded from the contents of " + filename + " .");
			}
		}
		return new_lab;
	}
	
	/**
	 * Sets up a command line parser
	 * @return A parser
	 */
	protected static CliParser setupParser()
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
		parser.addArgument(new Argument()
		.withLongName("preload")
		.withDescription("Loads an internal lab file on startup"));
		parser.addArgument(new Argument()
		.withLongName("port")
		.withArgument("x")
		.withDescription("Starts server on port x"));
		parser.addArgument(new Argument()
		.withLongName("version")
		.withDescription("Shows version info"));
		parser.addArgument(new Argument()
		.withLongName("color-scheme")
		.withArgument("c")
		.withDescription("Use GUI color scheme c (0-3)"));
		parser.addArgument(new Argument()
		.withLongName("report-to")
		.withArgument("host:port")
		.withDescription("Report results to host:port"));
		parser.addArgument(new Argument()
		.withLongName("name")
		.withArgument("x")
		.withDescription("Set assistant name to x"));
		parser.addArgument(new Argument()
		.withLongName("interval")
		.withArgument("x")
		.withDescription("Report results every x sec (works with report-to)"));
		parser.addArgument(new Argument()
		.withLongName("filter")
		.withArgument("exp")
		.withDescription("Filter experiments according to expression exp"));
		return parser;
	}

	public static final void initialize(String[] args, Class<? extends Laboratory> clazz)
	{
		initialize(args, clazz, new LinearAssistant());
	}

	public static final void initialize(String[] args, Class<? extends Laboratory> clazz, final LabAssistant assistant)
	{
		final AnsiPrinter stdout = new AnsiPrinter(System.out);
		CliParser parser = setupParser();
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
		stdout.resetColors();
		stdout.print(getCliHeader());
		new_lab.setupCli(parser);
		// Add lab-specific options and parse command line
		ArgumentMap argument_map = parser.parse(args);
		if (argument_map == null)
		{
			// Something went wrong when parsing
			stdout.print(getCliHeader());
			System.err.println("Error parsing command-line arguments. Run the lab with --help to see the syntax.");
			System.exit(ERR_ARGUMENTS);
		}
		// Are we loading a lab from an internal file?
		if (argument_map.hasOption("preload"))
		{
			new_lab = preloadLab(new_lab, stdout);
		}
		// Are we loading a lab file? If so, this overrides the
		// lab loaded from an internal file (if any)
		String filename = "";
		List<String> names = argument_map.getOthers();
		for (int i = 0; i < names.size(); i++)
		{
			filename = names.get(i);
			if (i == 0)
			{
				new_lab = loadFromFilename(new_lab, filename);
			}
			else
			{
				Laboratory lab_to_merge = loadFromFilename(new_lab, filename);
				new_lab.mergeWith(lab_to_merge);
			}
		}
		new_lab.setAssistant(assistant);
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
		new_lab.m_cliArguments = argument_map;
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
		if (new_lab.m_cliArguments.hasOption("version"))
		{
			showVersionInfo(stdout);
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
		int code = ERR_OK;
		if (!filename.isEmpty())
		{
			stdout.println("Loading lab from " + filename);
		}
		new_lab.setup();
		if (argument_map.hasOption("report-to"))
		{
			String host = argument_map.getOptionValue("report-to").trim();
			new_lab.getReporter().reportTo(host);
			stdout.println("Results will be reported to " + host);
			if (argument_map.hasOption("interval"))
			{
				new_lab.getReporter().setInterval(Integer.parseInt(argument_map.getOptionValue("interval")) * 1000);
			}
		}
		if (argument_map.hasOption("name"))
		{
			String assistant_name = argument_map.getOptionValue("name").trim();
			assistant.setName(assistant_name);
		}
		// Sets an experiment filter
		String filter_params = "";
		if (argument_map.hasOption("filter"))
		{
			filter_params = argument_map.getOptionValue("filter");
		}
		new_lab.m_filter = new_lab.createFilter(filter_params);
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
			if (new_lab.m_cliArguments.hasOption("color-scheme"))
			{
				int scheme = Integer.parseInt(new_lab.m_cliArguments.getOptionValue("color-scheme"));
				server.setColorScheme(scheme);
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
		out += "(C) 2014-2018 Laboratoire d'informatique formelle\nUniversité du Québec à Chicoutimi, Canada\n";
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
	 * Fetches the groups an experiment belongs to. Note that this is a set,
	 * since an experiment may belong to multiple groups.
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

	/**
	 * Gets the IDs of all groups within the lab
	 * @return A set of group IDs
	 */
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

	/**
	 * Formats the version number into a string
	 * @return The version string
	 */
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
			if (m_filter.include(e))
			{
				m_assistant.queue(e);
			}
		}
		start();
	}

	/**
	 * Gets the set of all macros defined in this lab
	 * @return The macros
	 */
	public Collection<Macro> getMacros() 
	{
		return m_macros;
	}

	/**
	 * Gets the set of all experiments in this lab
	 * @return The experiments
	 */
	public Collection<Experiment> getExperiments()
	{
		return m_experiments;
	}

	/**
	 * Gets a reference to the macro with given ID
	 * @param id The ID
	 * @return The macro, or {@code null} if no macro with such ID exists
	 */
	public Macro getMacro(int id) 
	{
		for (Macro m : m_macros)
		{
			if (m.getId() == id)
			{
				return m;
			}
		}
		return null;
	}

	@Override
	public Object getObjectWithId(String id)
	{
		if (id == null || id.isEmpty())
		{
			return null;
		}
		if (id.startsWith("T"))
		{
			int nb = Integer.parseInt(id.substring(1));
			return getTable(nb);
		}
		if (id.startsWith("P"))
		{
			int nb = Integer.parseInt(id.substring(1));
			return getPlot(nb);
		}
		if (id.startsWith("M"))
		{
			int nb = Integer.parseInt(id.substring(1));
			return getMacro(nb);
		}
		return null;
	}

	protected static void showVersionInfo(AnsiPrinter out)
	{
		out.append(getCliHeader()).append("\n");
		out.append("Azrael version:   ").append(GenericSerializer.getVersionString()).append("\n");
		out.append("Jerrydog version: ").append(Server.getVersionString()).append("\n");
		out.append("MTNP version:     ").append(DataFormatter.getVersionString()).append("\n");
	}

	/**
	 * Attempts to load a lab from an file saved internally. The method
	 * will attempt to load the first {@code .labo} or {@code .json} file
	 * it finds in the root folder of the lab's JAR file.
	 * @param new_lab The lab used to load the file
	 * @param stdout A print stream to write information messages about the
	 *   process
	 * @return A new lab, or {@code new_lab} if no loadable lab could be
	 *   found
	 */
	protected static Laboratory preloadLab(Laboratory new_lab, AnsiPrinter stdout)
	{
		String class_path = getClassPath(new_lab);
		List<String> lab_files = FileHelper.getResourceListing(new_lab.getClass(), class_path, ".*\\.labo$");
		if (!lab_files.isEmpty())
		{
			String filename = lab_files.get(0);
			stdout.println("Loading lab data from internal file " + filename);
			try 
			{
				return new_lab.loadFromZip(new_lab.getClass().getResourceAsStream(filename));
			} 
			catch (IOException e)
			{
				System.err.println("WARNING: lab data could not be loaded from internal file.\nAn empty lab will be started instead.");
			} catch (SerializerException e)
			{
				System.err.println("WARNING: lab data could not be loaded from internal file.\nAn empty lab will be started instead.");
			} 
			catch (JsonParseException e)
			{
				System.err.println("WARNING: lab data could not be loaded from internal file.\nAn empty lab will be started instead.");
			}
		}
		System.err.println("WARNING: lab data could not be loaded from an internal file.\nAn empty lab will be started instead.");
		return new_lab;
	}

	/**
	 * Gets the absolute path within the directory structure of the current
	 * instance of the lab
	 * @param lab The current lab
	 * @return The path
	 */
	protected static String getClassPath(Laboratory lab)
	{
		String[] parts = lab.getClass().getName().split("\\.");
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < parts.length - 1; i++)
		{
			out.append(parts[i]).append("/");
		}
		return out.toString();
	}

	/**
	 * Attempts to merge the results of a laboratory with the current lab. This
	 * is done by calling the
	 * {@link Experiment#mergeWith(Experiment, boolean) mergeWith()}
	 * method on experiments with matching IDs in both labs.
	 * 
	 * @param lab The lab to merge with the current lab
	 * @return {@code true} if the merger was done fully without error,
	 *   {@code false} otherwise.
	 */
	public boolean mergeWith(Laboratory lab)
	{
		boolean success = true;
		for (Experiment e : getExperiments())
		{
			Experiment e_to_merge = lab.getExperiment(e.getId());
			if (canMerge(e, e_to_merge))
			{
				success &= e.mergeWith(e_to_merge, true);
			}
		}
		return success;
	}
	
	/**
	 * Determines if an experiment {@code e2} can be merged to the
	 * results of another experiment {@code e1}
	 * @param e1 The experiment to be merged <em>to</em>
	 * @param e2 The experiment whose contents are to be merged
	 * @return {@code true} if the experiments can be merged,
	 *   {@code false} otherwise
	 */
	protected static boolean canMerge(Experiment e1, Experiment e2)
	{
		if (e1 == null || e2 == null)
		{
			return false;
		}
		Experiment.Status s2 = e2.getStatus();
		Experiment.QueueStatus q2 = e2.getQueueStatus();
		// We only overwrite if the source experiment is running
		return s2 == Status.RUNNING || s2 == Status.DONE || s2 == Status.DONE_WARNING || s2 == Status.FAILED || s2 == Status.INTERRUPTED || s2 == Status.TIMEOUT || q2 != QueueStatus.NOT_QUEUED;
	}

	/**
	 * Saves the content of a lab as a zip file
	 * @return The byte array with the contents of the zip file
	 * @throws IOException Thrown if the creation of the zip failed
	 *   for some reason
	 */
	public byte[] saveToZip() throws IOException
	{
		String filename = Server.urlEncode(getTitle());
		String lab_contents = saveToString();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ZipOutputStream zos = new ZipOutputStream(bos);
		String ZE = filename + ".json";
		ZipEntry ze = new ZipEntry(ZE);
		zos.putNextEntry(ze);
		zos.write(lab_contents.getBytes());
		zos.closeEntry();
		zos.close();
		return bos.toByteArray();
	}

	/**
	 * Creates a new lab instance from the contents of a zip file
	 * @param lab_file_contents
	 * @return The lab
	 * @throws IOException
	 * @throws SerializerException
	 * @throws JsonParseException
	 */
	public Laboratory getFromZip(byte[] lab_file_contents) throws IOException, SerializerException, JsonParseException
	{
		ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(lab_file_contents));
		ZipEntry entry;
		byte[] contents = null;
		entry = zis.getNextEntry();
		while (entry != null)
		{
			//String name = entry.getName();
			contents = HttpUtilities.extractFile(zis);
			// We assume the zip to contain a single file 
			break;
		}
		assert contents != null;
		String json = new String(contents);
		return loadFromString(json);
	}
	
	/**
	 * Creates a filter for the experiments in this lab. You should override
	 * this method if you want to actually filter experiments in a specific way
	 * @param parameters A string of parameters that can be used to instantiate
	 * the filter
	 * @return A filter
	 */
	public ExperimentFilter createFilter(String parameters)
	{
		return new ExperimentFilter.IdFilter(parameters);
	}

	/**
	 * Gets the experiment filter associated to this laboratory 
	 * @return The filter
	 */
	public final ExperimentFilter getFilter()
	{
		return m_filter;
	}

	/**
	 * Edit the parameters of an experiment
	 * @param exp_id
	 * @param new_parameters
	 */
	public void editParameters(int exp_id, JsonMap new_parameters)
	{
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Recomputes the value of all claims
	 */
	public void computeClaims()
	{
		for (Claim c : m_claims)
		{
			Claim.Result r = c.check();
			m_claimStatus.put(c.getId(), r);
		}
	}

	/**
	 * Recomputes the value of a claim
	 * @param claim_nb The claim ID
	 */
	public void computeClaim(int claim_nb)
	{
		Claim c = getClaim(claim_nb);
		if (c != null)
		{
			Claim.Result r = c.check();
			m_claimStatus.put(c.getId(), r);
		}
	}
	
	/**
	 * Gets all experiments of the lab that fit into a given region
	 * @param r The region to consider
	 * @return The subset of of all experiments that lie within the region
	 */
	public Collection<Experiment> filterExperiments(Region r)
	{
		return Region.filterExperiments(getExperiments(), r);
	}
	
	/**
	 * Returns the set of all experiments in the lab that are descendents of
	 * a given class
	 * @param in_set The set of experiments to filter from
	 * @param clazz Experiments must be descendents of this class to be kept
	 * @return The set of experiments that match these conditions
	 */
	public static Collection<Experiment> filterExperiments(Collection<? extends Experiment> in_set, Class<? extends Experiment> clazz)
	{
		Set<Experiment> set = new HashSet<Experiment>();
		for (Experiment e : in_set)
		{
			if (clazz.isAssignableFrom(e.getClass()))
				set.add(e);
		}
		return set;
	}
}
