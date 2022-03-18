/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2014-2022 Sylvain Hallé

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
package ca.uqac.lif.labpal.latex;

/**
 * Interface implemented by objects that can export their contents as a LaTeX
 * code snippet. Each object is also assigned an identifier (macro name, or
 * box name), which is called the object's "nickname".
 * @author Sylvain Hallé
 * @since 3.0
 */
public interface LatexExportable
{
	/**
	 * Exports an object as a LaTeX snippet.
	 * @return A string containing the LaTeX snippet for the object
	 */
	public String toLatex();
	
	/**
	 * Gets the object's LaTeX nickname.
	 * @return The nickname. This string is assumed to contain only valid
	 * characters for a LaTeX identifier.
	 * @see #latexify(String)
	 */
	public String getNickname();
	
	/**
   * Replaces illegal characters in a LaTeX string by legal ones. Since
   * identifiers in LaTeX commands can only have letters, numbers and other
   * symbols must be replaced into something else to create a valid
   * identifier. The basic replacement scheme is to change every occurrence
   * of a forbidden character into a unique letter that somehow "relates" to
   * that character (with some imagination).
   * @param s The string to replace
   * @return The replaced string
   */
  public static String latexify(String s)
  {
    s = s.replaceAll("0", "Z");
    s = s.replaceAll("1", "O");
    s = s.replaceAll("2", "W");
    s = s.replaceAll("3", "R");
    s = s.replaceAll("4", "F");
    s = s.replaceAll("5", "V");
    s = s.replaceAll("6", "X");
    s = s.replaceAll("7", "S");
    s = s.replaceAll("8", "H");
    s = s.replaceAll("9", "N");
    s = s.replaceAll("\\.", "D");
    s = s.replaceAll("-", "T");
    s = s.replaceAll(",", "C");
    s = s.replaceAll(" ", "P");
    s = s.replaceAll("_", "U");
    s = s.replaceAll("=", "E");
    s = s.replaceAll(":", "L");
    return s;
  }
  
  /**
   * Escapes LaTeX characters in a string.
   * @param s The string
   * @return The escaped string
   */
  public static String escape(String s)
  {
  	s = s.replaceAll("&", "\\&");
  	s = s.replaceAll("\\$", "\\$");
  	s = s.replaceAll("_", "\\_");
  	return s;
  }
}
