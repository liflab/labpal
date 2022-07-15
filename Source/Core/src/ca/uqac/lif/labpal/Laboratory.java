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

import ca.uqac.lif.azrael.PrintException;
import ca.uqac.lif.azrael.ReadException;
import ca.uqac.lif.azrael.json.JsonPrinter;
import ca.uqac.lif.azrael.json.JsonReader;
import ca.uqac.lif.dag.LabelledNode;
import ca.uqac.lif.fs.FileSystemException;
import ca.uqac.lif.fs.WriteZipFile;
import ca.uqac.lif.jerrydog.Server;
import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonParser;
import ca.uqac.lif.json.JsonParser.JsonParseException;
import ca.uqac.lif.labpal.Stateful.Status;
import ca.uqac.lif.labpal.assistant.Assistant;
import ca.uqac.lif.labpal.assistant.QueuedThreadPoolExecutor;
import ca.uqac.lif.labpal.assistant.SingleThreadExecutor;
import ca.uqac.lif.labpal.claim.Claim;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.labpal.experiment.ExperimentException;
import ca.uqac.lif.labpal.experiment.ExperimentGroup;
import ca.uqac.lif.labpal.macro.Macro;
import ca.uqac.lif.labpal.plot.Plot;
import ca.uqac.lif.labpal.provenance.LaboratoryPart;
import ca.uqac.lif.labpal.provenance.LaboratoryPart.ClaimNumber;
import ca.uqac.lif.labpal.provenance.LaboratoryPart.ExceptionNumber;
import ca.uqac.lif.labpal.provenance.LaboratoryPart.MacroNumber;
import ca.uqac.lif.labpal.provenance.LaboratoryPart.PlotNumber;
import ca.uqac.lif.labpal.provenance.LaboratoryPart.TableNumber;
import ca.uqac.lif.labpal.provenance.TrackedValue;
import ca.uqac.lif.labpal.server.HttpUtilities;
import ca.uqac.lif.labpal.server.LabPalServer;
import ca.uqac.lif.labpal.server.LaboratoryCallback;
import ca.uqac.lif.labpal.table.Table;
import ca.uqac.lif.labpal.util.AnsiPrinter;
import ca.uqac.lif.labpal.util.CliParser;
import ca.uqac.lif.labpal.util.CommandRunner;
import ca.uqac.lif.labpal.util.FileHelper;
import ca.uqac.lif.labpal.util.Stopwatch;
import ca.uqac.lif.petitpoucet.ComposedPart;
import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.Part;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.ExplanationQueryable;
import ca.uqac.lif.petitpoucet.function.FunctionException;
import ca.uqac.lif.petitpoucet.function.NthOutput;
import ca.uqac.lif.petitpoucet.function.vector.NthElement;
import ca.uqac.lif.spreadsheet.Cell;
import ca.uqac.lif.labpal.util.CliParser.Argument;
import ca.uqac.lif.labpal.util.CliParser.ArgumentMap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

/**
 * An organized collection of experiments, tables, and plots. The lab is
 * controlled by a lab assistant, which is responsible for running the
 * experiments, populating the tables and drawing the plots.
 * 
 * @since 1.0
 * 
 * @author Sylvain Hallé
 */
public class Laboratory implements ExplanationQueryable, Persistent
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
	private static final transient int s_minorVersionNumber = 99;

	/**
	 * The revision version number
	 */
	private static final transient int s_revisionVersionNumber = 0;

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
	 * The seed used to initialize the random number generator
	 */
	private int m_seed = 0;

	/**
	 * A (possibly long) textual description for what the lab does.
	 */
	private transient String m_description;

	/**
	 * The number of parkmips (see {@link #countParkMips()})
	 */
	public transient static float s_parkMips = countParkMips();

	/**
	 * The number of loops that equal to 1 parkmip
	 */
	protected static final transient float s_parkMipsDivider = 5061974f;

	/**
	 * The hostname of the machine running the lab
	 */
	private String m_hostName = guessHostName();

	/**
	 * A map associating experiment IDs to experiment instances.
	 */
	/*@ non_null @*/ private Map<Integer,Experiment> m_experiments;

	/**
	 * A list of experiment groups.
	 */
	/*@ non_null @*/ private transient List<ExperimentGroup> m_experimentGroups;

	/**
	 * A map associating plot IDs to plot instances.
	 */
	/*@ non_null @*/ private transient Map<Integer,Plot> m_plots;

	/**
	 * A map associating table IDs to table instances.
	 */
	/*@ non_null @*/ private transient Map<Integer,Table> m_tables;

	/**
	 * A map associating macro IDs to macro instances.
	 */
	/*@ non_null @*/ private transient Map<Integer,Macro> m_macros;

	/**
	 * A map associating macro IDs to claim instances.
	 */
	/*@ non_null @*/ private transient Map<Integer,Claim> m_claims;

	/**
	 * An assistant instance used to run experiments inside the lab.
	 */
	/*@ non_null @*/ private transient Assistant m_assistant;

	/**
	 * The name of this lab.
	 */
	/*@ non_null @*/ private String m_name;

	/**
	 * The author of this lab.
	 */
	/*@ non_null @*/ private String m_author;

	/**
	 * The DOI of this lab, if any.
	 */
	/*@ non_null @*/ private String m_doi;

	/**
	 * The default filename assumed for the HTML description
	 */
	private static transient final String s_descriptionDefaultFilename = "description.html";

	/**
	 * A list of environments in which a lab can be running
	 */
	public static enum Environment {STANDALONE, CODEOCEAN}

	/**
	 * The environment in which the lab is running
	 */
	private transient Environment m_environment = Environment.STANDALONE;

	/**
	 * The arguments parsed from the command line
	 */
	private transient ArgumentMap m_cliArguments = null;

	/**
	 * Creates a new empty laboratory instance.
	 */
	public Laboratory()
	{
		super();
		m_experiments = new HashMap<Integer,Experiment>();
		m_plots = new HashMap<Integer,Plot>();
		m_tables = new HashMap<Integer,Table>();
		m_macros = new HashMap<Integer,Macro>();
		m_claims = new HashMap<Integer,Claim>();
		m_assistant = new Assistant();
		m_experimentGroups = new ArrayList<ExperimentGroup>();
		m_doi = "";
		m_author = "Fred Flintstone";
		m_name = "Untitled";
	}

	/**
	 * Creates a new empty laboratory instance.
	 */
	public Laboratory(ArgumentMap args)
	{
		this();
		m_experiments = new HashMap<Integer,Experiment>();
		m_assistant = new Assistant();
	}

	/**
	 * Sets up the command-line parser to accept arguments specific to this lab.
	 * Your lab should override this method if you wish to receive custom CLI
	 * arguments. In such a case, call the methods on the <tt>parser</tt> variable
	 * to add new command-line switches.
	 * 
	 * @param parser
	 *          The command-line parser
	 */
	public void setupCli(CliParser parser)
	{
		return;
	}

	/**
	 * Sets up the experiments and plots that this lab will contain. You
	 * <em>must</em> override this method and add at least one experiment
	 * (otherwise there won't be anything to do with your lab).
	 */
	public void setup()
	{
		// Do nothing
	}

	/**
	 * Adds callbacks to the LabPal web server that will be launched for this
	 * lab.
	 * @param server The server to which the callbacks will be added
	 * @param callbacks An empty list of callbacks. The method can instantiate
	 * new callbacks and add them to this list. 
	 */
	public void setupCallbacks(LabPalServer server, List<LaboratoryCallback> callbacks)
	{
		// Do nothing
	}

	/**
	 * Gets the command-line arguments parsed when launching this lab.
	 * 
	 * @return A map of arguments and values parsed from the command line. If you
	 *         specified custom command-line arguments in
	 *         {@link #setupCli(CliParser)}, this is where you can retrieve them and
	 *         read their values.
	 */
	public final ArgumentMap getCliArguments()
	{
		return m_cliArguments;
	}

	/*@ non_null @*/ private static String getCliHeader()
	{
		String out = "";
		out += "LabPal " + formatVersion() + " - A versatile environment for running experiments\n";
		out += "(C) 2014-2022 Laboratoire d'informatique formelle\nUniversité du Québec à Chicoutimi, Canada\n";
		out += "https://liflab.github.io/labpal\n";
		return out;
	}

	/**
	 * Sets the seed of the lab's random number generator
	 * 
	 * @param seed
	 *          The seed
	 * @return This lab
	 */
	/*@ non_null @*/ public final Laboratory setRandomSeed(int seed)
	{
		m_seed = seed;
		return this;
	}

	/**
	 * Gets the seed of the lab's random number generator
	 * 
	 * @return The seed
	 */
	public final int getRandomSeed()
	{
		return m_seed;
	}

	/**
	 * Gets the lab's random seed.
	 * @return The seed
	 */
	/*@ pure non_null @*/ public final int getSeed()
	{
		return m_seed;
	}

	/**
	 * Gets the name of this lab.
	 * @return The lab name
	 */
	/*@ pure non_null @*/ public final String getName()
	{
		return m_name;
	}

	/**
	 * Sets the name of this lab.
	 * @param name The lab name
	 * @return This lab
	 */
	/*@ non_null @*/ public final Laboratory setName(String name)
	{
		m_name = name;
		return this;
	}

	/**
	 * Gets the author of this lab.
	 * @return The author name
	 */
	/*@ pure non_null @*/ public final String getAuthor()
	{
		return m_author;
	}

	/**
	 * Sets the author of this lab.
	 * @param author The author name
	 * @return This lab
	 */
	/*@ non_null @*/ public final Laboratory setAuthor(String author)
	{
		m_author = author;
		return this;
	}

	/**
	 * Gets the DOI of this lab.
	 * @return The DOI
	 */
	/*@ pure non_null @*/ public final String getDoi()
	{
		return m_doi;
	}

	/**
	 * Sets the DOI of this lab.
	 * @param doi The DOI
	 * @return This lab
	 */
	/*@ non_null @*/ public final Laboratory setDoi(String doi)
	{
		m_doi = doi;
		return this;
	}

	/**
	 * Retrieves the total number of "data points" contained within this lab.
	 * @return The number of points
	 */
	/*@ pure @*/ public int countDataPoints()
	{
		int total = 0;
		for (Experiment e : m_experiments.values())
		{
			total += e.countDataPoints();
		}
		return total;
	}

	/**
	 * Gets the list of all nicknames shared by two or more lab objects.
	 * Exporting these objects to LaTeX produce compilation errors, as LaTeX
	 * expects each identifier to be defined exactly once. 
	 * @return
	 */
	/*@ pure non_null @*/ public List<String> getNonUniqueNicknames()
	{
		List<String> duplicates = new ArrayList<String>();
		Set<String> names = new HashSet<String>();
		for (Plot p : m_plots.values())
		{
			String n = p.getNickname();
			if (names.contains(n) && !duplicates.contains(n))
			{
				duplicates.add(n);
			}
			names.add(n);
		}
		for (Table t : m_tables.values())
		{
			if (!t.showsInList())
			{
				continue; // This table won't be exported anyway
			}
			String n = t.getNickname();
			if (names.contains(n) && !duplicates.contains(n))
			{
				duplicates.add(n);
			}
			names.add(n);
		}
		return duplicates;
	}

	/**
	 * Gets the list of experiment groups contained in this lab.
	 * @return The list of experiment groups, including the group of
	 * orphan experiments
	 * {@see getOrphanExperiments()}
	 */
	public final List<ExperimentGroup> getExperimentGroups()
	{
		List<ExperimentGroup> all_groups = new ArrayList<ExperimentGroup>(m_experimentGroups.size() + 1);
		all_groups.add(getOrphanExperiments());
		all_groups.addAll(m_experimentGroups);
		return all_groups;
	}

	/**
	 * Gets an experiment group made of all experiments that do not belong to
	 * any other group.
	 * @return The group of experiments
	 */
	/*@ pure non_null @*/ protected ExperimentGroup getOrphanExperiments()
	{
		Set<Experiment> in_group = new HashSet<Experiment>();
		for (Group<Experiment> g : m_experimentGroups)
		{
			in_group.addAll(g.getObjects());
		}
		Collection<Experiment> s_orphans = new HashSet<Experiment>();
		s_orphans.addAll(m_experiments.values());
		s_orphans.removeAll(in_group);
		ExperimentGroup g = new ExperimentGroup("Ungrouped experiments");
		g.add(s_orphans);
		g.setId(0); // 0 is always the ID of the "orphan" group
		return g;
	}

	/**
	 * Adds a group to the lab and assigns it a unique ID.
	 * @param g The group
	 * @return This lab
	 */
	public Laboratory add(ExperimentGroup g)
	{
		g.setId(m_experimentGroups.size() + 1);
		m_experimentGroups.add(g);
		return this;
	}

	/**
	 * Gives a textual description to the laboratory.
	 * 
	 * @param description
	 *          The description. This string must be valid HTML; in particular, HTML
	 *          special characters (<tt>&amp;</tt>, <tt>&lt;</tt>, etc.)
	 *          <em>must</em> be escaped; otherwise there may be problems displaying
	 *          the description in the web interface.
	 * @return This lab
	 */
	public final Laboratory setDescription(String description)
	{
		m_description = description;
		return this;
	}

	/**
	 * Gets the lab's textual description. This description is either the text given
	 * in a previous call to {@link #setDescription(String)}, or, if no such call
	 * was made, the contents of a file called <tt>description.html</tt> that
	 * resides beside the lab. If no such file exists, the empty string is returned.
	 * 
	 * @return The lab's description
	 */
	public final String getDescription()
	{
		if (m_description != null)
		{
			return m_description;
		}
		String s = FileHelper.internalFileToString(getClass(), s_descriptionDefaultFilename);
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
	 * Attempts to retrieve the host name of the machine running this lab. This is
	 * done by running the "hostname" command at the command and fetching its
	 * resulting string.
	 * 
	 * @return A string with the hostname. If launching the command resulted in an
	 *         error, returns an empty string or {@code null}
	 */
	public String getHostName()
	{
		return m_hostName;
	}

	/**
	 * Guesses the host name by running the <tt>hostname</tt> command at
	 * the command line.
	 * 
	 * @return The host name
	 */
	protected static String guessHostName()
	{
		byte[] bytes = CommandRunner.runAndGet("hostname", null);
		String name = new String(bytes);
		return name.trim();
	}

	/*@ pure @*/ public float getParkMips()
	{
		return s_parkMips;
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
	/*@ pure null @*/ public final Experiment getExperiment(int id)
	{
		if (m_experiments.containsKey(id))
		{
			return m_experiments.get(id);
		}
		return null;
	}

	/**
	 * Returns a claim instance with given ID, if it exists.
	 * @param id The claim ID
	 * @return The claim instance, or <tt>null</tt> if the claim
	 * does not exist in the lab
	 */
	/*@ pure null @*/ public final Claim getClaim(int id)
	{
		if (m_claims.containsKey(id))
		{
			return m_claims.get(id);
		}
		return null;
	}

	/**
	 * Returns a table instance with given ID, if it exists.
	 * @param id The table ID
	 * @return The table instance, or <tt>null</tt> if the table
	 * does not exist in the lab
	 */
	/*@ pure null @*/ public final Table getTable(int id)
	{
		if (m_tables.containsKey(id))
		{
			return m_tables.get(id);
		}
		return null;
	}

	/**
	 * Returns a plot instance with given ID, if it exists.
	 * @param id The plot ID
	 * @return The plot instance, or <tt>null</tt> if the plot
	 * does not exist in the lab
	 */
	/*@ pure null @*/ public final Plot getPlot(int id)
	{
		if (m_plots.containsKey(id))
		{
			return m_plots.get(id);
		}
		return null;
	}

	/**
	 * Returns a macro instance with given ID, if it exists.
	 * @param id The macro ID
	 * @return The macro instance, or <tt>null</tt> if the macro
	 * does not exist in the lab
	 */
	/*@ pure null @*/ public final Macro getMacro(int id)
	{
		if (m_macros.containsKey(id))
		{
			return m_macros.get(id);
		}
		return null;
	}

	/**
	 * Returns a list of all experiments in the lab, sorted by ID.
	 * @return The list of experiments
	 */
	/*@ pure non_null @*/ public final List<Experiment> getExperiments()
	{
		List<Integer> l_ids = new ArrayList<Integer>();
		l_ids.addAll(m_experiments.keySet());
		Collections.sort(l_ids);
		List<Experiment> exps = new ArrayList<Experiment>();
		for (int id : l_ids)
		{
			exps.add(m_experiments.get(id));
		}
		return exps;
	}

	/**
	 * Returns a list of all plots in the lab, sorted by ID.
	 * @return The list of plots
	 */
	/*@ pure non_null @*/ public final List<Plot> getPlots()
	{
		List<Integer> l_ids = new ArrayList<Integer>();
		l_ids.addAll(m_plots.keySet());
		Collections.sort(l_ids);
		List<Plot> exps = new ArrayList<Plot>();
		for (int id : l_ids)
		{
			exps.add(m_plots.get(id));
		}
		return exps;
	}

	/**
	 * Returns a list of all claims in the lab, sorted by ID.
	 * @return The list of claims
	 */
	/*@ pure non_null @*/ public final List<Claim> getClaims()
	{
		List<Integer> l_ids = new ArrayList<Integer>();
		l_ids.addAll(m_claims.keySet());
		Collections.sort(l_ids);
		List<Claim> exps = new ArrayList<Claim>();
		for (int id : l_ids)
		{
			exps.add(m_claims.get(id));
		}
		return exps;
	}

	/**
	 * Returns a list of all tables in the lab, sorted by ID.
	 * @return The list of tables
	 */
	/*@ pure non_null @*/ public final List<Table> getTables()
	{
		List<Integer> l_ids = new ArrayList<Integer>();
		l_ids.addAll(m_tables.keySet());
		Collections.sort(l_ids);
		List<Table> exps = new ArrayList<Table>();
		for (int id : l_ids)
		{
			exps.add(m_tables.get(id));
		}
		return exps;
	}

	/**
	 * Adds a list of experiments to the lab.
	 * @param experiments The experiments to add
	 * @return This lab
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
	 * Adds a list of tables to the lab.
	 * @param tables The tables to add
	 * @return The first table in the arguments
	 */
	/*@ non_null @*/ public Table add(Table ... tables)
	{
		for (Table t : tables)
		{
			m_tables.put(t.getId(), t);
		}
		if (tables.length > 0)
		{
			return tables[0];
		}
		return null;
	}

	/**
	 * Adds a list of claims to the lab.
	 * @param claims The claims to add
	 * @return The first claim in the arguments
	 */
	/*@ null @*/ public Claim add(Claim ... claims)
	{
		for (Claim c : claims)
		{
			m_claims.put(c.getId(), c);
		}
		if (claims.length > 0)
		{
			return claims[0];
		}
		return null;
	}

	/**
	 * Adds a list of macros to the lab.
	 * @param macros The macros to add
	 * @return The first macro in the arguments
	 */
	/*@ null @*/ public Macro add(Macro ... macros)
	{
		for (Macro m : macros)
		{
			m_macros.put(m.getId(), m);
		}
		if (macros.length > 0)
		{
			return macros[0];
		}
		return null;
	}

	/**
	 * Adds a list of plots to the lab.
	 * @param experiments The plots to add
	 * @return The first plot in the arguments
	 */
	/*@ null @*/ public Plot add(Plot ... plots)
	{
		for (Plot p : plots)
		{
			m_plots.put(p.getId(), p);
		}
		if (plots.length > 0)
		{
			return plots[0];
		}
		return null;
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

	/**
	 * Determines if an experiment is waiting to be executed.
	 * @param e The experiment
	 * @return <tt>true</tt> if the experiment is queued, <tt>false</tt>
	 * otherwise
	 */
	public final boolean isQueued(Experiment e)
	{
		return m_assistant.isQueued(e);
	}

	/**
	 * Gets the environment in which the lab is running
	 * @return The environment
	 */
	public Environment getEnvironment()
	{
		return m_environment;
	}

	/**
	 * Gets the major version number
	 * 
	 * @return The number
	 */
	public static final int getMajor()
	{
		return s_majorVersionNumber;
	}

	/**
	 * Gets the minor version number
	 * 
	 * @return The number
	 */
	public static final int getMinor()
	{
		return s_minorVersionNumber;
	}

	/**
	 * Gets the revision version number
	 * 
	 * @return The number
	 */
	public static final int getRevision()
	{
		return s_revisionVersionNumber;
	}

	/**
	 * Formats the version number into a string
	 * 
	 * @return The version string
	 */
	/*@ non_null @*/ public static String formatVersion()
	{
		if (getRevision() == 0)
		{
			return s_majorVersionNumber + "." + s_minorVersionNumber;
		}
		return s_majorVersionNumber + "." + s_minorVersionNumber + "." + s_revisionVersionNumber;
	}

	/**
	 * Counts the number of "parkmips" of this system. This is a very rough
	 * indicator of the system's speed, measured in the number of increments of a
	 * long variable during a third of a second.
	 * <p>
	 * ParkMips are designed to equal roughly 1 when run on an Asus Transformer Book
	 * T200 (yes, this is totally arbitrary).
	 * 
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

	public TrackedValue getPart(String datapoint_id)
	{
		String[] parts = datapoint_id.split(":");
		int id = Integer.parseInt(parts[0].substring(1));
		if (parts[0].startsWith("T"))
		{
			// Table
			Table subject = getTable(id);
			if (parts.length == 1)
			{
				return new TrackedValue(null, Part.all, subject);
			}
			int row = Integer.parseInt(parts[1]);
			int col = Integer.parseInt(parts[2]);
			return new TrackedValue(null, Cell.get(col, row), subject);
		}
		else if (parts[0].startsWith("P"))
		{
			// Plot
			Plot subject = getPlot(id);
			return new TrackedValue(null, Part.all, subject);
		}
		else if (parts[0].startsWith("M"))
		{
			// Macro
			Macro subject = getMacro(id);
			if (parts.length == 1)
			{
				return new TrackedValue(null, Part.all, subject);
			}
			int index = Integer.parseInt(parts[1]);
			return new TrackedValue(null, new NthElement(index), subject);
		}
		else if (parts[0].startsWith("C"))
		{
			// Claim
			Claim subject = getClaim(id);
			return new TrackedValue(null, NthOutput.FIRST, subject);
		}
		else if (parts[0].startsWith("X"))
		{
			// Exception thrown by an experiment
			Experiment e = getExperiment(id);
			if (e == null)
			{
				return null;
			}
			Exception subject = e.getException();
			return new TrackedValue(null, NthOutput.FIRST, subject);
		}
		return null;
	}

	public PartNode getExplanation(String datapoint_id)
	{
		TrackedValue tv = getPart(datapoint_id);
		if (tv == null)
		{
			return null;
		}
		NodeFactory f = NodeFactory.getFactory();
		Part root_part = null;
		Part p = tv.getPart();
		Object o = tv.getSubject();
		LabelledNode child = null;
		if (o instanceof Table)
		{
			child = ((Table) o).getExplanation(p);
			root_part = ComposedPart.compose(p, new TableNumber(((Table) o).getId()));
		}
		else if (o instanceof Macro)
		{
			child = ((Macro) o).getExplanation(p);
			root_part = ComposedPart.compose(p, new MacroNumber(((Macro) o).getId()));
		}
		else if (o instanceof Plot)
		{
			child = ((Plot) o).getExplanation(p);
			root_part = ComposedPart.compose(p, new PlotNumber(((Plot) o).getId()));
		}
		else if (o instanceof Claim)
		{
			child = ((Claim) o).getExplanation(p);
			root_part = ComposedPart.compose(p, new ClaimNumber(((Claim) o).getId()));
		}
		else if (o instanceof ExperimentException && o instanceof ExplanationQueryable)
		{
			child = ((ExplanationQueryable) o).getExplanation(p);
			root_part = ComposedPart.compose(p, new ExceptionNumber(((ExperimentException) o).getId()));
		}
		else
		{
			child = f.getUnknownNode();
		}
		if (root_part != null)
		{
			PartNode root = f.getPartNode(root_part, this);
			root.addChild(child);
			return root;
		}
		return null;
	}

	@Override
	public PartNode getExplanation(Part d, NodeFactory f)
	{
		PartNode root = f.getPartNode(d, this);
		Part head = d.head();
		if (!(head instanceof LaboratoryPart))
		{
			root.addChild(f.getUnknownNode());
			return root;
		}
		int id = ((LaboratoryPart) head).getId();
		if (head instanceof PlotNumber)
		{
			Plot o = getPlot(id);
			if (o != null)
			{
				root.addChild(o.getExplanation(d.tail(), f));
			}
		}
		else if (head instanceof TableNumber)
		{
			Table o = getTable(id);
			if (o != null)
			{
				root.addChild(o.getExplanation(d.tail(), f));
			}
		}
		else if (head instanceof MacroNumber)
		{
			Macro o = getMacro(id);
			if (o != null)
			{
				root.addChild(o.getExplanation(d.tail(), f));
			}
		}
		else if (head instanceof ClaimNumber)
		{
			Claim o = getClaim(id);
			if (o != null)
			{
				root.addChild(o.getExplanation(d.tail(), f));
			}
		}
		else
		{
			root.addChild(f.getUnknownNode());
		}
		return root;
	}

	@Override
	public PartNode getExplanation(Part d)
	{
		return getExplanation(d, NodeFactory.getFactory());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadState(Object o_map) throws PersistenceException
	{
		setup();
		if (!(o_map instanceof Map))
		{
			throw new PersistenceException("Unexpected data structure");
		}
		Map<?,?> map = (Map<?,?>) o_map;
		if (!map.containsKey("name") || !map.containsKey("author") || !map.containsKey("doi") || !map.containsKey("experiments") || !map.containsKey("seed"))
		{
			throw new PersistenceException("Unexpected data structure");
		}
		m_name = (String) map.get("name");
		m_author = (String) map.get("author");
		m_doi = (String) map.get("doi");
		m_seed = ((Number) map.get("seed")).intValue();
		Object o_exps = map.get("experiments");
		if (!(o_exps instanceof Map))
		{
			throw new PersistenceException("Unexpected data structure");
		}
		for (Map.Entry<Integer,?> entry : ((Map<Integer,?>) o_exps).entrySet())
		{
			int exp_id = entry.getKey();
			Object o_e = entry.getValue();
			Experiment e = getExperiment(exp_id);
			if (e == null)
			{
				throw new PersistenceException("Experiment " + exp_id + " does not exist in lab");
			}
			e.loadState(o_e);
		}
	}

	@Override
	public Object saveState() throws PersistenceException
	{
		Map<String,Object> contents = new HashMap<String,Object>();
		contents.put("name", m_name);
		contents.put("author", m_author);
		contents.put("doi", m_doi);
		contents.put("seed", m_seed);
		Map<Integer,Object> exps = new HashMap<Integer,Object>(m_experiments.size());
		for (Experiment e : m_experiments.values())
		{
			Status s = e.getStatus();
			if (s == Status.DONE || s == Status.INTERRUPTED || s == Status.FAILED)
			{
				// Only print the state of experiments that are completed
				exps.put(e.getId(), e.saveState());
			}
		}
		contents.put("experiments", exps);
		return contents;
	}

	/**
	 * Sets up a command line parser with the arguments that are common to
	 * all lab instances.
	 * 
	 * @return A parser
	 */
	private static CliParser setupParser()
	{
		CliParser parser = new CliParser();
		parser.addArgument(
				new Argument().withLongName("codeocean").withDescription("Indicates lab is running inside CodeOcean capsule"));
		parser.addArgument(
				new Argument().withLongName("save-to").withDescription("Save files to dir").withArgument("dir"));
		parser.addArgument(new Argument().withLongName("seed").withArgument("x")
				.withDescription("Sets the seed for the random number generator to x"));
		parser.addArgument(
				new Argument().withLongName("help").withDescription("Prints command line usage"));
		parser.addArgument(new Argument().withLongName("autostart")
				.withDescription("Queues all experiments and starts the assistant"));
		parser.addArgument(new Argument().withLongName("preload")
				.withDescription("Loads an internal lab file on startup"));
		parser.addArgument(new Argument().withLongName("port").withArgument("x")
				.withDescription("Starts server on port x"));
		parser
		.addArgument(new Argument().withLongName("version").withDescription("Shows version info"));
		parser.addArgument(new Argument().withLongName("color-scheme").withArgument("c")
				.withDescription("Use GUI color scheme c (0-3)"));
		parser.addArgument(new Argument().withLongName("filter").withArgument("exp")
				.withDescription("Filter experiments according to expression exp"));
		parser.addArgument(new Argument().withLongName("threads").withDescription("Uses n threads to run experiments").withArgument("n"));
		return parser;
	}

	private static void showVersionInfo(AnsiPrinter out)
	{
		out.append(getCliHeader()).append("\n");
	}

	public static final int initialize(String[] args, Class<? extends Laboratory> clazz)
	{
		// Open a print stream to the terminal
		final AnsiPrinter stdout = new AnsiPrinter(System.out);
		stdout.resetColors();
		stdout.print(getCliHeader());
		// Properly close print streams when closing the program
		// https://www.securecoding.cert.org/confluence/display/java/FIO14-J.+Perform+proper+cleanup+at+program+termination
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				stdout.close();
			}
		}));

		// Create an empty lab instance and ask for any custom CLI switches
		CliParser parser = setupParser();
		Laboratory new_lab = getInstance(clazz);
		if (new_lab == null)
		{
			stdout.println("Cannot create lab instance");
			return ERR_LAB;
		}		
		new_lab.setupCli(parser);
		ArgumentMap argument_map = parser.parse(args);
		if (argument_map == null)
		{
			// Something went wrong when parsing
			stdout.print(getCliHeader());
			System.err.println("Error parsing command-line arguments. Run the lab with --help to see\nthe syntax.");
			return ERR_ARGUMENTS;
		}
		/*
		// Are we loading a lab from an internal file?
		if (argument_map.hasOption("preload"))
		{
			new_lab = preloadLab(new_lab, stdout);
		}
		 */
		// Are we loading a lab file? If so, this overrides the
		// lab loaded from an internal file (if any)
		String filename = "";
		List<String> names = argument_map.getOthers();
		for (int i = 0; i < names.size(); i++)
		{
			filename = names.get(i);
			new_lab.loadFromFilename(filename);
		}
		new_lab.m_cliArguments = argument_map;
		if (new_lab.m_cliArguments.hasOption("help"))
		{
			parser.printHelp("", System.out);
			stdout.close();
			return ERR_OK;
		}
		if (new_lab.m_cliArguments.hasOption("version"))
		{
			showVersionInfo(stdout);
			stdout.close();
			return ERR_OK;
		}
		if (new_lab.m_cliArguments.hasOption("seed"))
		{
			// Sets random seed
			int seed = Integer.parseInt(new_lab.m_cliArguments.getOptionValue("seed"));
			new_lab.setRandomSeed(seed);
		}
		/*if (!filename.isEmpty())
		{
			stdout.println("Loading lab from " + filename);
		}*/
		new_lab.setup();

		// Setup assistant
		Assistant lab_assistant = null;
		if (new_lab.m_cliArguments.hasOption("threads"))
		{
			int num_threads = Integer.parseInt(new_lab.m_cliArguments.getOptionValue("threads"));
			if (num_threads == 1)
			{
				new Assistant(new SingleThreadExecutor());
			}
			if (num_threads < 1)
			{
				stdout.println("Invalid number of threads");
				return ERR_ARGUMENTS;
			}
			lab_assistant = new Assistant(new QueuedThreadPoolExecutor(num_threads));
		}
		else
		{
			lab_assistant = new Assistant(new QueuedThreadPoolExecutor(2));
		}
		new_lab.setAssistant(lab_assistant);

		// Auto-start
		if (argument_map.hasOption("autostart"))
		{
			lab_assistant.enqueue(new_lab.getExperiments(), null);
		}

		/*
		// Sets an experiment filter
		String filter_params = "";
		if (argument_map.hasOption("filter"))
		{
			filter_params = argument_map.getOptionValue("filter");
		}
		new_lab.m_filter = new_lab.createFilter(filter_params);
		BatchRunner br = null;
		if (new_lab.m_cliArguments.hasOption("batch") || new_lab.m_cliArguments.hasOption("codeocean"))
		{
			if (new_lab.m_cliArguments.hasOption("codeocean"))
			{
				br = new CodeOceanRunner(new_lab, assistant, stdout);
				new_lab.m_environment = Environment.CODEOCEAN;
			}
			else
			{
				if (!new_lab.m_cliArguments.hasOption("save-to"))
				{
					System.err.println("Batch mode selected; parameter save-to required");
					return ERR_ARGUMENTS;
				}
				String to_path = new_lab.m_cliArguments.getOptionValue("save-to");
				br = new LocalBatchRunner(new_lab, assistant, stdout, to_path);
			}
			if (br != null)
			{
				// Batch mode
				br.run();
			}
		}
		 */

		// Start LabPal's web interface
		LabPalServer server = new LabPalServer(new_lab);
		List<LaboratoryCallback> callbacks = new ArrayList<>();
		new_lab.setupCallbacks(server, callbacks);
		if (callbacks != null)
		{
			// Register custom callbacks, if any
			for (LaboratoryCallback cb : callbacks)
			{
				server.registerCallback(0, cb);
			}
		}
		stdout.print("Visit http://" + server.getServerName() + ":" + server.getServerPort()
		+ " in your browser\n");
		stdout.print("Hit Ctrl+C in this window to stop\n");
		try
		{
			server.startServer();
		}
		catch (IOException e)
		{
			System.err.println("Cannot start server on port " + server.getServerPort()
			+ ". Is another lab already running?");
			stdout.close();
			return ERR_SERVER;
		}
		if (new_lab.m_cliArguments.hasOption("color-scheme"))
		{
			int scheme = Integer.parseInt(new_lab.m_cliArguments.getOptionValue("color-scheme"));
			server.setColorScheme(scheme);
		}
		if (new_lab.m_cliArguments.hasOption("autostart"))
		{
			lab_assistant.enqueueCurrent();
		}
		// Infinite loop
		while (true)
		{
			Stopwatch.sleep(10000);
		}
	}

	/**
	 * Saves the content of a lab as a zip file. The zip is made of a single
	 * JSON file containing the lab's internal state.
	 * 
	 * @return The byte array with the contents of the zip file
	 * @throws IOException
	 *           Thrown if the creation of the zip failed for some reason
	 * @throws PrintException
	 *           Thrown if the serialization of the lab failed for some reason
	 */
	public byte[] saveToZip() throws FileSystemException, IOException, PrintException, PersistenceException
	{
		String filename = Server.urlEncode(getName());
		Object o = saveState();
		String lab_contents = new JsonPrinter().print(o).toString();		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		WriteZipFile zip = new WriteZipFile(bos);
		PrintStream ps = new PrintStream(zip.writeTo(filename + ".json"));
		ps.write(lab_contents.getBytes());
		ps.close();
		zip.close();
		return bos.toByteArray();
	}

	/**
	 * Attempts to get an instance of a laboratory class by calling its no-args
	 * constructor.
	 * @param clazz The lab class to get an instance of
	 * @return A lab instance, or <tt>null</tt> if the lab cannot not be
	 * instantiated
	 */
	/*@ null @*/ private static Laboratory getInstance(/*@ null @*/ Class<? extends Laboratory> clazz)
	{
		try
		{
			Constructor<? extends Laboratory> c = clazz.getConstructor();
			return c.newInstance();
		}
		catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			return null;
		}
	}

	public final void loadFromFilename(String filename)
	{
		try
		{
			// Substitute current lab for one loaded from the file
			if (filename.endsWith(".zip") || filename.endsWith("." + s_fileExtension))
			{
				loadFromZipFile(new FileInputStream(new File(filename)));
			}
			else
			{
				loadFromJsonString(FileHelper.readToString(new File(filename)));
			}
		}
		catch (FileNotFoundException e)
		{
			System.err.println(
					"WARNING: file " + filename + " not found. An empty lab will be started instead.");
		}
		catch (IOException e)
		{
			System.err.println("WARNING: file " + filename
					+ " could not be read. An empty lab will be started instead.");
		}
		catch (PersistenceException e)
		{
			System.err
			.println("WARNING: a lab could not be loaded from the contents of " + filename + " .");
		}
	}

	/**
	 * Loads a laboratory from an input stream open on a zip file. The file is
	 * expected to contain a single JSON document with the lab's internal state.
	 * @param is The input stream
	 * @throws IOException Thrown if stream cannot be read
	 * @throws ReadException Thrown if input stream does not contain
	 * a valid lab instance
	 * @throws JsonParseException Thrown if the file contained within the
	 * zip is not a valid JSON document
	 */
	public final void loadFromZipFile(InputStream is) throws IOException, PersistenceException
	{
		ZipInputStream zis = new ZipInputStream(is);
		ZipEntry entry;
		entry = zis.getNextEntry();
		byte[] contents = null;
		while (entry != null)
		{
			// String name = entry.getName();
			contents = HttpUtilities.extractFile(zis);
			// We assume the zip contains a single file
			break;
		}
		String json = new String(contents);
		loadFromJsonString(json);
	}

	/**
	 * Loads the lab's state from a JSON string.
	 * @param s The JSON string with the assistant's state
	 * @throws PersistenceException If the deserialization or JSON parsing could
	 * not be done
	 */
	public final void loadFromJsonString(String s) throws PersistenceException
	{
		JsonParser jp = new JsonParser();
		try
		{
			JsonElement je = jp.parse(s);
			Object o = new JsonReader().read(je);
			loadState(o);	
		}
		catch (JsonParseException | ReadException e)
		{
			throw new PersistenceException(e);
		}
	}

	public static void main(String[] args)
	{
		System.out.println(getCliHeader());
		System.out.println(
				"You are running labpal.jar, which is only a library to create\nyour own test suites. As a result nothing will happen here. Read the \nonline documentation to learn how to use LabPal.");
	}
}