package filtering;

import java.util.HashSet;
import java.util.Set;

import ca.uqac.lif.labpal.Experiment;
import ca.uqac.lif.labpal.ExperimentFilter;

/**
 * A simple filter that reads a string of the form
 * <code>u<sub>1</sub>,u<sub>2</sub>,&hellip;,u<sub><i>m</i></sub>;v<sub>1</sub>,v<sub>2</sub>,&hellip;,v<sub><i>n</i></sub></code>
 * and selects all experiments whose input parameter {@code a} matches
 * one of the u<sub><i>i</i></sub>, and {@code b}
 * matches one of v<sub><i>i</i></sub>.
 */
public class MyFilter extends ExperimentFilter
{
	/**
	 * The values for {@code a}
	 */
	protected Set<Integer> m_valuesA = new HashSet<Integer>();
	
	/**
	 * The values for {@code b}
	 */
	protected Set<Integer> m_valuesB = new HashSet<Integer>();
	
	/**
	 * Creates a new filter instance based on a command-line parameter
	 * @param parameters The string with the parameter value
	 */
	public MyFilter(String parameters)
	{
		super(parameters);
		if (!parameters.isEmpty())
		{
			String[] parts = parameters.split(";");
			for (String s : parts[0].split(","))
			{
				m_valuesA.add(Integer.parseInt(s.trim()));
			}
			for (String s : parts[1].split(","))
			{
				m_valuesB.add(Integer.parseInt(s.trim()));
			}
		}
	}

	@Override
	public boolean include(Experiment e)
	{
		return (m_valuesA.isEmpty() || m_valuesA.contains(e.readInt("a"))) && 
				(m_valuesB.isEmpty() || m_valuesB.contains(e.readInt("b")));
	}
}
