package ca.uqac.lif.labpal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ca.uqac.lif.labpal.plot.Plot;
import ca.uqac.lif.labpal.region.Region;
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
      if (r.getDimensions().length > 1)
      {
        // We don't use variable parameters to give a name
        continue;
      }
      nickname.append(createFragment(dim_name, r.getDomain(dim_name)));
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
  /*@ non_null @*/ protected final String createFragment(String dim_name, Set<Object> set)
  {
  	List<Object> list = new ArrayList<Object>(set.size());
  	list.addAll(set);
  	if (list.size() != 1)
  	{
  		return "";
  	}
  	Object o = list.get(0);
  	if (o == null)
  	{
  		return "null";
  	}
    if (o instanceof Number)
    {
      return createFragment(dim_name, (Number) o);
    }
    else
    {
      return createFragment(dim_name, o.toString());
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
