package ca.uqac.lif.labpal.latex;

/**
 * Interface implemented by objects that can export their contents as a LaTeX
 * code snippet.
 * @author Sylvain Hallé
 * @since 3.0
 */
public interface LatexExportable
{
	public String toLatex();
	
	/**
   * Replaces illegal characters in a LaTeX string by legal ones. Since
   * identifiers in LaTeX commands can only have letters, numbers and other
   * symbols must be replaced into something else to create a valid
   * identifier. The basic replacement scheme is to change every occurrence
   * of a digit into a unique letter that somehow "looks" like the digit.
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
    return s;
  }
}
