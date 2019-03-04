/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2014-2017 Sylvain Hallé

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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ca.uqac.lif.jerrydog.CachedRequestCallback;
import ca.uqac.lif.jerrydog.InnerFileServer;
import ca.uqac.lif.jerrydog.RequestCallback;
import ca.uqac.lif.labpal.CliParser;
import ca.uqac.lif.labpal.CliParser.ArgumentMap;
import ca.uqac.lif.labpal.FileHelper;
import ca.uqac.lif.labpal.LabAssistant;
import ca.uqac.lif.labpal.Laboratory;

/**
 * Server supporting LabPal's web GUI
 * 
 * @author Sylvain Hallé
 *
 */
public class LabPalServer extends InnerFileServer
{
  /**
   * The default port
   */
  public static final transient int s_defaultPort = 21212;

  /**
   * The time (in seconds) during which the client is allowed to cache static
   * resources (such as images or JS files) locally
   */
  protected static final transient int s_cacheInterval = 600;

  /**
   * A number identifying the color scheme used in the GUI
   */
  protected int m_colorScheme = 0;

  /**
   * The predefined color schemes used for the GUI
   */
  protected static final List<String[]> s_colorSchemes = loadSchemes();

  /**
   * The instance of lab this server is responsible for
   */
  protected transient Laboratory m_lab;

  /**
   * The callback for the individual experiment page
   */
  protected ExperimentPageCallback m_experimentPageCallback;

  /**
   * The callback for the dynamic CSS file
   */
  private CssCallback m_cssCallback;

  /**
   * Creates a new LabPal server
   * 
   * @param args The arguments passed from the command line
   * @param lab The lab that this server will manage
   * @param assistant The asssitant used to coordinate the execution
   * of the lab's experiments
   */
  public LabPalServer(ArgumentMap args, Laboratory lab, LabAssistant assistant)
  {
    super(LabPalServer.class, true, s_cacheInterval);
    m_lab = lab;
    setUserAgent("LabPal " + Laboratory.s_versionString);
    if (args.hasOption("port"))
    {
      setServerPort(Integer.parseInt(args.getOptionValue("port")));
    }
    else
    {
      setServerPort(s_defaultPort);
    }
    registerCallback(0, new HomePageCallback(lab, assistant));
    m_cssCallback = new CssCallback(this, lab, assistant);
    CachedRequestCallback css_callback = new CachedRequestCallback(m_cssCallback);
    css_callback.setCachingEnabled(true);
    css_callback.setCachingInterval(s_cacheInterval);
    registerCallback(0, css_callback);
    registerCallback(0, new MergeCallback(lab, assistant));
    registerCallback(0, new ReportResultsCallback(lab, assistant));
    registerCallback(0, new StatusPageCallback(lab, assistant));
    registerCallback(0, new EditParametersCallback(lab, assistant));
    registerCallback(0, new ExperimentPageCallback(lab, assistant));
    registerCallback(0, new ExperimentsPageCallback(lab, assistant));
    registerCallback(0, new AssistantPageCallback(lab, assistant, this));
    registerCallback(0, new PlotsPageCallback(lab, assistant));
    registerCallback(0, new PlotImageCallback(lab, assistant));
    registerCallback(0, new DownloadCallback(lab, assistant));
    registerCallback(0, new UploadCallback(this, lab, assistant));
    registerCallback(0, new HelpPageCallback(lab, assistant));
    registerCallback(0, new AllPlotsCallback(lab, assistant));
    registerCallback(0, new AllPlotsLatexCallback(lab, assistant));
    registerCallback(0, new TablesPageCallback(lab, assistant));
    registerCallback(0, new TablePageCallback(lab, assistant));
    registerCallback(0, new TableExportCallback(lab, assistant));
    registerCallback(0, new AllTablesCallback(lab, assistant));
    registerCallback(0, new ExplainCallback(lab, assistant));
    registerCallback(0, new ExplainImageCallback(lab, assistant));
    registerCallback(0, new FindFormCallback(lab, assistant));
    registerCallback(0, new MacrosPageCallback(lab, assistant));
    registerCallback(0, new AllMacrosLatexCallback(lab, assistant));
    registerCallback(0, new ExportStaticCallback(lab, assistant, this));
    registerCallback(0, new EditParametersFormCallback(lab, assistant));
    registerCallback(0, new ComputeClaimsCallback(lab, assistant));
    registerCallback(0, new ClaimCallback(lab, assistant));
    registerCallback(0, new UnavailableCallback(lab, assistant));
  }

  /**
   * Sets the the color scheme used in the GUI
   * 
   * @param c
   *          A number identifying the color scheme
   */
  public void setColorScheme(int c)
  {
    m_colorScheme = c % s_colorSchemes.size();
  }

  /**
   * Changes the laboratory associated with each registered callback. This occurs
   * when the user loads a new lab from a file.
   * 
   * @param lab
   *          The new laboratory
   */
  public void changeLab(Laboratory lab)
  {
    m_lab = lab;
    for (RequestCallback cb : m_callbacks)
    {
      if (cb instanceof WebCallback)
      {
        ((WebCallback) cb).changeLab(lab);
      }
    }
  }

  /**
   * Loads the set of color schemes from an internal file
   * 
   * @return A list of arrays with hex colors
   */
  protected static List<String[]> loadSchemes()
  {
    Scanner scanner = new Scanner(LabPalServer.class.getResourceAsStream("schemes.csv"));
    List<String[]> lines = new ArrayList<String[]>();
    while (scanner.hasNextLine())
    {
      String line = scanner.nextLine().trim();
      if (line.isEmpty() || !line.startsWith("#"))
        continue;
      String[] parts = line.split(",");
      lines.add(parts);
    }
    scanner.close();
    return lines;
  }

  /**
   * Gets the array of hex colors corresponding to the current color scheme
   * 
   * @return The array
   */
  public String[] getColorScheme()
  {
    return s_colorSchemes.get(m_colorScheme);
  }

  /**
   * Adds options to the command line parser. Currently this method does nothing
   * when called.
   * 
   * @param parser
   *          The parser
   */
  public static void setupCli(CliParser parser)
  {
    // Currently the server receives nothing from the CLI
  }

  /**
   * Exports the whole contents of the lab as a zipped bundle of static web pages.
   * 
   * @return An array of bytes, with the contents of the zip file generated
   */
  public byte[] exportToStaticHtml()
  {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    outputStream(bos);
    return bos.toByteArray();
  }

  /**
   * Exports the whole contents of the lab as a zipped bundle of static web pages
   * and writes that bundle to the disk.
   * 
   * @param path
   *          The path and filename where the file must be written
   */
  public void exportToStaticHtml(String path)
  {

    try
    {
      FileOutputStream fos = new FileOutputStream(path);
      outputStream(fos);
    }
    catch (FileNotFoundException e)
    {
      Logger.getAnonymousLogger().log(Level.WARNING, e.getMessage());
    }

  }

  /**
   * Exports the contents of a lab and writes its content into an output stream
   * 
   * @param fos
   *          The output stream to write to
   */
  void outputStream(OutputStream fos)
  {
    String file_contents, filename;
    ZipOutputStream zos = new ZipOutputStream(fos);
    try
    {
      {
        // All the non-HTML pages in the root
        String root_path = "ca/uqac/lif/labpal/server/";
        String folder = "resource/";
        List<String> internal_files = FileHelper.getResourceListing(LabPalServer.class,
            root_path + folder, ".*", ".*html|.*~|images|screen\\.css$");
        for (String in_filename : internal_files)
        {
          byte[] contents = FileHelper.internalFileToBytes(LabPalServer.class,
              folder + in_filename);
          ZipEntry ze = new ZipEntry(in_filename);
          zos.putNextEntry(ze);
          zos.write(contents);
          zos.closeEntry();
        }
      }
      {
        // The screen.css file requires special attention
        file_contents = m_cssCallback.exportToStaticHtml("");
        filename = "screen.css";
        ZipEntry ze = new ZipEntry(filename);
        zos.putNextEntry(ze);
        zos.write(file_contents.getBytes());
        zos.closeEntry();
      }
      {
        // Everything in images
        String root_path = "ca/uqac/lif/labpal/server/";
        String folder = "resource/images/";
        List<String> internal_files = FileHelper.getResourceListing(LabPalServer.class,
            root_path + folder, ".*", ".*html|.*~$");
        for (String in_filename : internal_files)
        {
          byte[] contents = FileHelper.internalFileToBytes(LabPalServer.class,
              folder + in_filename);
          ZipEntry ze = new ZipEntry("images/" + in_filename);
          zos.putNextEntry(ze);
          zos.write(contents);
          zos.closeEntry();
        }
      }
      // Then all the callbacks
      for (RequestCallback wc : m_callbacks)
      {
        if (wc instanceof WebCallback)
        {
          ((WebCallback) wc).addToZipBundle(zos);
        }
      }
    }
    catch (IOException e)
    {
      Logger.getAnonymousLogger().log(Level.WARNING, e.getMessage());
      e.printStackTrace();
    }
    finally
    {
      try
      {
        zos.close();
      }
      catch (IOException e)
      {
        Logger.getAnonymousLogger().log(Level.WARNING, e.getMessage());
      }
    }
  }
}
