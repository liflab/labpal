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
package ca.uqac.lif.labpal.claim;

import ca.uqac.lif.labpal.Dependent;
import ca.uqac.lif.labpal.Identifiable;
import ca.uqac.lif.labpal.Stateful;
import ca.uqac.lif.labpal.latex.LatexExportable;
import ca.uqac.lif.petitpoucet.NodeFactory;
import ca.uqac.lif.petitpoucet.PartNode;
import ca.uqac.lif.petitpoucet.function.ExplanationQueryable;

/**
 * A description expressed over the contents of lab elements, and which can be
 * take one of three possible truth values. The meaning of each of these truth
 * values is broadly expected to follow these rules:
 * <ul>
 * <li>A claim evaluates to <em>true</em> or <em>false</em> if it depends on
 * lab elements that have all been successfully run (i.e. ended in the
 * <em>done</em> state), or if the completion of the remaining elements cannot
 * change the outcome of the claim.</li>  
 * <li>A claim evaluates to <em>inconclusive</em> if it is evaluated on lab
 * elements where at least one of them is not in the <em>done</em> state, and
 * the outcome of the claim may turn out to be either true or false depending
 * on its successful completion.</li>
 * </ul>
 * The three truth values have a special type called {@link Troolean}, and
 * there are functions to combine Troolean values together, which correspond to
 * the classical rules of three-valued logic.
 * 
 * @author Sylvain Hallé
 * @since 2.10
 *
 */
public abstract class Claim implements ExplanationQueryable, Dependent<Stateful>, Identifiable, LatexExportable
{
	/**
	 * A counter to provide unique IDs to claims.
	 */
	private int s_idCounter = 1;
	
	/**
	 * The unique ID given to a claim instance.
	 */
	private int m_id;
	
	/**
	 * A nickname given to this claim.
	 */
	/*@ non_null @*/ private String m_nickname;
	
	/**
	 * A one-sentence description describing the meaning of the claim.
	 */
	/*@ non_null @*/ private String m_statement;
	
	/**
	 * A longer description of the claim and how it is calculated.
	 */
	/*@ non_null @*/ private String m_description;
	
	/**
	 * Creates a new claim.
	 */
	public Claim()
	{
		super();
		m_id = s_idCounter++;
		m_statement = "";
		m_description = "";
		m_nickname = LatexExportable.latexify("Claim" + m_id);
	}
	
	/**
	 * Sets the claim's description.
	 * @param s A one-sentence description describing the meaning of the claim
	 * @return This claim
	 */
	/*@ non_null @*/ public final Claim setStatement(/*@ non_null @*/ String s)
	{
		m_statement = s;
		return this;
	}
	
	/**
	 * Gets the claim's description.
	 * @return A one-sentence description describing the meaning of the claim
	 */
	/*@ non_null @*/ public final String getStatement()
	{
		return m_statement;
	}
	
	/**
	 * Sets the claim's description.
	 * @param s A description describing the meaning of the claim
	 * @return This claim
	 */
	/*@ non_null @*/ public final Claim setDescription(/*@ non_null @*/ String s)
	{
		m_description = s;
		return this;
	}
	
	/**
	 * Gets the claim's description.
	 * @return A description describing the meaning of the claim
	 */
	/*@ non_null @*/ public final String getDescription()
	{
		return m_description;
	}
	
	/**
	 * Sets the claim's nickname.
	 * @param name The nickname for this claim. It must be a valid LaTeX
	 * identifier
	 * @return This claim
	 * @see LatexExportable#latexify(String)
	 */
	/*@ non_null @*/ public final Claim setNickname(/*@ non_null @*/ String name)
	{
		m_nickname = name;
		return this;
	}
	
	@Override
	public final int getId()
	{
		return m_id;
	}
	
	@Override
	public String getNickname()
	{
		return m_nickname;
	}
	
	@Override
	public String toLatex()
	{
		StringBuilder out = new StringBuilder();
		out.append("\\newcommand{\\").append(getNickname()).append("}");
		out.append("{").append(getStatement()).append("}");
		return out.toString();
	}
	
	protected void continueExplanation(PartNode from, NodeFactory f)
	{
		
	}
	
	/**
	 * Evaluates the claim.
	 * @return The result of evaluating the claim on the current contents of the
	 * lab.
	 */
	/*@ non_null @*/ public abstract Troolean.Value evaluate();
}
