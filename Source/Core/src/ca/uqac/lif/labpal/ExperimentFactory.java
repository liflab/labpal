/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2019 Sylvain Hallé

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

import java.util.Collection;

/**
 * Factory object that creates instances of experiments based on regions,
 * and adds them to an existing lab. A factory is used through its method
 * {@link #get(Region)}, which fetches an existing instance of an experiment
 * matching a given region, or creates a new one if such experiment does
 * not exist. To create a concrete factory, one must implement method
 * {@link #createExperiment(Region)}, which creates a new experiment instance
 * based on the region's contents.
 * 
 * @author Sylvain Hallé
 *
 * @param <L> The class of the laboratory
 * @param <T> The class of the experiments that are to be created
 * 
 * @since 2.11
 */
public abstract class ExperimentFactory<L extends Laboratory,T extends Experiment>
{
  /**
   * The lab in which the experiments are to be added
   */
  /*@ non_null @*/ protected L m_lab;

  /**
   * The class of the experiments that are to be created
   */
  /*@ non_null @*/ protected Class<T> m_class;

  /**
   * Creates a new experiment factory
   * @param lab The lab in which the experiments are to be added
   * @param c The class of the experiments that are to be created
   */
  public ExperimentFactory(/*@ non_null @*/ L lab, /*@ non_null @*/ Class<T> c)
  {
    super();
    m_lab = lab;
    m_class = c;
  }

  /**
   * Gets an experiment based on a region, or creates a new one if no matching
   * experiment exists.
   * @param r The region that describes the parameters of the experiment. If
   * no experiment matches <tt>r</tt> in the lab, a new experiment will be
   * created (through a call to {@link #createExperiment(Region)} and added to
   * the lab.
   * @return The experiment
   */
  /*@ non_null @*/ @SuppressWarnings("unchecked")
  public T get(/*@ non_null @*/ Region r)
  {
    T exp = null;
    Collection<Experiment> col = m_lab.filterExperiments(r, m_class);
    if (col.isEmpty())
    {
      // Experiment does not exist
      exp = createExperiment(r);
      if (exp != null)
      {
        m_lab.add(exp);
      }
    }
    else
    {
      for (Experiment e : col)
      {
        exp = (T) e;
      }
    }
    return exp;
  }

  /**
   * Creates a new instance of the experiment based on the parameters
   * of a region
   * @param r The region
   * @return The new experiment
   */
  protected abstract T createExperiment(Region r);
}