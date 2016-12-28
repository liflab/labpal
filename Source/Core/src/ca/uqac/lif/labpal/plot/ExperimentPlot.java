package ca.uqac.lif.labpal.plot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ca.uqac.lif.labpal.FileHelper;
import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.table.ExperimentTable;
import de.erichseifert.gral.data.DataSource;
import de.erichseifert.gral.io.plots.DrawableWriter;
import de.erichseifert.gral.io.plots.DrawableWriterFactory;
import de.erichseifert.gral.plots.Plot;

public class ExperimentPlot
{
	/**
	 * The terminal used for displaying the plot
	 */
	public static enum ImageType {PNG, DUMB, PDF};
	
	/**
	 * The table this plot is based on
	 */
	protected ExperimentTable m_table;
	
	/**
	 * The plot's title
	 */
	protected String m_title;

	/**
	 * The plot's ID
	 */
	protected int m_id;

	/**
	 * A counter for auto-incrementing plot IDs
	 */
	private static int s_idCounter = 1;
	
	/**
	 * A lock for accessing the counter
	 */
	private static Lock s_counterLock = new ReentrantLock(); 
	
	/**
	 * The symbol used to separate data values in a file
	 */
	public static final transient String s_datafileSeparator = ",";
	
	/**
	 * The symbol used to represent missing values in a file
	 */
	public static final transient String s_datafileMissing = "?";

	/**
	 * The lab that contains the experiments this plot is
	 * drawing
	 */
	protected transient Laboratory m_lab;
	
	/**
	 * The path to launch GnuPlot
	 */
	protected static transient String s_path = "gnuplot";

	/**
	 * Whether gnuplot is present on the system
	 */
	protected static transient final boolean s_gnuplotPresent = FileHelper.commandExists(s_path);
	
	
	/**
	 * Creates a new wrapped plot from a plot
	 * @param p The plot
	 */
	public ExperimentPlot(ExperimentTable t)
	{
		super();
		m_table = t;
		m_title = "Untitled";
		s_counterLock.lock();
		m_id = s_idCounter++;
		s_counterLock.unlock();
	}
	
	/**
	 * Creates a new plot
	 * @param title
	 * @param a
	 */
	ExperimentPlot(ExperimentTable t, String title, Laboratory a)
	{
		this(t);
		m_title = title;
		m_lab = a;
		s_counterLock.lock();
		m_id = s_idCounter++;
		s_counterLock.unlock();
	}
		
	/**
	 * Sets the plot's caption
	 * @param t The caption
	 * @return This plot
	 */
	public ExperimentPlot setCaption(String t)
	{
		m_title = t;
		return this;
	}

	/**
	 * Gets the plot's caption
	 * @return The caption
	 */
	public String getCaption()
	{
		return m_title;
	}
	
	/**
	 * Gets the plot's ID
	 * @return The ID
	 */
	public int getId()
	{
		return m_id;
	}
	
	/**
	 * Assigns this plot to a laboratory
	 * @param a The assistant
	 * @return This plot
	 */
	public ExperimentPlot assignTo(Laboratory a)
	{
		m_lab = a;
		a.add(this);
		return this;
	}

	/**
	 * Gets the MIME type string associated to an image format
	 * @param t The format
	 * @return The MIME string
	 */
	public static String getTypeName(ImageType t)
	{
		switch (t)
		{
		case PNG:
			return "image/png";
		case PDF:
			return "application/pdf";
		case DUMB:
			return "text/plain";
		}
		return "image/png";
	}
	
	/**
	 * Runs GnuPlot on a file and returns the resulting graph
	 * @param term The terminal (i.e. PNG, etc.) to use for the image
	 * @return The (binary) contents of the image produced by Gnuplot
	 */
	public final byte[] getImage(ImageType term)
	{
		Plot plot = getPlot(m_table);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    String type_string = getTypeName(term);
    DrawableWriter wr = DrawableWriterFactory.getInstance().get(type_string);
    try
		{
			wr.write(plot, baos, 640, 480);
	    baos.flush();
	    byte[] bytes = baos.toByteArray();
	    baos.close();
	    return bytes;
		}
    catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    return null;
	}
	
	/**
	 * Generates a stand-alone Gnuplot file for this plot
	 * @param term The terminal used to display the plot
	 * @return The Gnuplot file contents
	 */
	public final String toGnuplot(ImageType term)
	{
		return toGnuplot(term, "");
	}

	/**
	 * Generates a stand-alone Gnuplot file for this plot
	 * @param term The terminal used to display the plot
	 * @param lab_title The title of the lab. This is only used in the 
	 *   auto-generated comments in the file's header
	 * @return The Gnuplot file contents
	 */
	public String toGnuplot(ImageType term, String lab_title)
	{
		// TODO
		return "";
	}

	/**
	 * Checks if Gnuplot is present in the system
	 * @return true if Gnuplot is present, false otherwise
	 */
	public static boolean isGnuplotPresent()
	{
		return s_gnuplotPresent;
	}
	
	/**
	 * Gets a Plot object from this LabPlot
	 * @return The plot
	 */
	public final Plot getPlot()
	{
		return getPlot(m_table);
	}

	/**
	 * Gets a Plot object from a data source
	 * @return The plot
	 */
	public Plot getPlot(DataSource source)
	{
		return null;
	}

}
