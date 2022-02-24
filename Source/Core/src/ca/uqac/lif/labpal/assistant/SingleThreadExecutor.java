/*
  LabPal, a versatile environment for running experiments on a computer
  Copyright (C) 2015-2022 Sylvain Hallé

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
package ca.uqac.lif.labpal.assistant;

import java.util.concurrent.Executors;

/**
 * An executor service that dispatches tasks sequentially on a single thread.
 * This class works exactly in the same way as Java's single thread executor.
 * In a single-thread situation, method {@link #shutdownAtEnd()} has the same
 * effect as {@link #shutdown()}.
 *   
 * @author Sylvain Hallé
 *
 */
public class SingleThreadExecutor extends LabPalExecutorService
{
	public SingleThreadExecutor()
	{
		super();
		m_executor = Executors.newSingleThreadExecutor();
	}

	@Override
	public SingleThreadExecutor newInstance() 
	{
		return new SingleThreadExecutor();
	}
	
	@Override
	public String toString()
	{
		return "ST";
	}
}
