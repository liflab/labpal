package ca.uqac.lif.labpal;

import ca.uqac.lif.json.JsonBoolean;
import ca.uqac.lif.json.JsonElement;
import ca.uqac.lif.json.JsonNumber;
import ca.uqac.lif.json.JsonString;
import ca.uqac.lif.labpal.plot.Plot;
import ca.uqac.lif.labpal.table.Table;

/**
 * Object that automatically builds a description for a table or plot object,
 * based on the parameters that have been used to create it.
 * @author Sylvain Hallé
 * @since 2.11
 */
public abstract class Namer
{
  /**
   * Gives a nickname to a table
   * @param t The table to name
   * @param r The region that describes the table
   * @param prefix A prefix to append at the beginning of the name
   * @param suffix A suffix to append at the end of the name
   */
  public void setNickname(Table t, Region r, String prefix, String suffix)
  {
    t.setNickname(buildName(r, prefix, suffix));
  }
  
  /**
   * Gives a title to a table
   * @param t The table to name
   * @param r The region that describes the table
   * @param prefix A prefix to append at the beginning of the name
   * @param suffix A suffix to append at the end of the name
   */
  public void setTitle(Table t, Region r, String prefix, String suffix)
  {
    t.setTitle(buildName(r, prefix, suffix));
  }
  
  /**
   * Gives a nickname to a plot
   * @param p The plot to name
   * @param r The region that describes the table
   * @param prefix A prefix to append at the beginning of the name
   * @param suffix A suffix to append at the end of the name
   */
  public void setNickname(Plot p, Region r, String prefix, String suffix)
  {
    p.setNickname(buildName(r, prefix, suffix));
  }
  
  /**
   * Gives a title to a plot
   * @param p The plot to name
   * @param r The region that describes the table
   * @param prefix A prefix to append at the beginning of the name
   * @param suffix A suffix to append at the end of the name
   */
  public void setTitle(Plot p, Region r, String prefix, String suffix)
  {
    p.setTitle(buildName(r, prefix, suffix));
  }
  
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
  
  /**
   * Builds a name based on a region
   * @param r The region that describes the object
   * @param prefix A prefix to append at the beginning of the name
   * @param suffix A suffix to append at the end of the name
   * @return The name
   */
  /*@ non_null @*/ protected String buildName(/*@ non_null @*/ Region r, /*@ non_null @*/ String prefix, /*@ non_null @*/ String suffix)
  {
    StringBuilder nickname = new StringBuilder();
    nickname.append(handlePrefix(prefix));
    for (String dim_name : r.getDimensions())
    {
      if (r.getAll(dim_name).size() > 1)
      {
        // We don't use variable parameters to give a name
        continue;
      }
      nickname.append(createFragment(dim_name, r.get(dim_name)));
    }
    nickname.append(handleSuffix(suffix));
    return postProcess(nickname.toString());
  }
  
  /**
   * Creates a fragment of a nickname based on a dimension and its associated
   * value 
   * @param dim_name The name of the dimension
   * @param json_value The value
   * @return The name fragment
   */
  /*@ non_null @*/ protected final String createFragment(String dim_name, JsonElement json_value)
  {
    if (json_value instanceof JsonNumber)
    {
      return createFragment(dim_name, ((JsonNumber) json_value).numberValue());
    }
    else if (json_value instanceof JsonBoolean)
    {
      return createFragment(dim_name, ((JsonBoolean) json_value).toString());
    }
    else
    {
      return createFragment(dim_name, ((JsonString) json_value).stringValue());
    }
  }
  
  /**
   * Applies a transformation to the prefix
   * @param prefix The prefix
   * @return The transformed prefix
   */
  protected String handlePrefix(String prefix)
  {
    return prefix;
  }
  
  /**
   * Applies a transformation to the suffix
   * @param suffix The suffix
   * @return The transformed suffix
   */
  protected String handleSuffix(String suffix)
  {
    return suffix;
  }
  
  /**
   * Performs a last post-processing of the name
   * @param s The string
   * @return The post-processed string
   */
  protected String postProcess(String s)
  {
    return s;
  }
  
  /**
   * Creates a fragment of a nickname based on a dimension and its associated
   * numerical value 
   * @param dim_name The name of the dimension
   * @param n The numerical value
   * @return The name fragment
   */
  /*@ non_null @*/ protected abstract String createFragment(String dim_name, Number n);
  
  /**
   * Creates a fragment of a nickname based on a dimension and its associated
   * string value 
   * @param dim_name The name of the dimension
   * @param s The string value
   * @return The name fragment
   */
  /*@ non_null @*/ protected abstract String createFragment(String dim_name, String s);
}
