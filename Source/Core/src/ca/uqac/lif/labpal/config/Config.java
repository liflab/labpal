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
package ca.uqac.lif.labpal.config;

import java.util.HashMap;

/**
 * Singleton class meant to hold configuration parameters for LabPal
 * 
 * @author Chafik Meniar
 */
public class Config
{
  /**
   * A list of possible environments where LabPal can be running
   */
  public static enum ENV
  {
    WINDOWS_LINUX, CODEOCEAN, STATIC, WEB, ALL
  }

  /**
   * The environment in which the lab is running
   */
  public static ENV env = ENV.WEB;

  protected static final HashMap<String, String> props = new HashMap<String, String>()
  {

    /**
     * Dummy UID
     */
    private static final long serialVersionUID = 1L;

    {
      put("namefileTempExp", "Experiments");
      put("pathInput", "../data/");
      put("pathOutput", "../results/");
      put("pdfName", "labpal-plots");
      put("imageName", "img");
      put("zipName", "LabpalStatic");
    }
  };

  public static void setProperty(String key, String value)
  {
    props.put(key, value);
  }

  public static String getProperty(String key)
  {
    return props.get(key);
  }
}
