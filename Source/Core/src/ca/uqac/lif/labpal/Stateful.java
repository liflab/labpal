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
package ca.uqac.lif.labpal;

/**
 * Interface implemented by objects whose internal state follows a lifecycle.
 * @author Sylvain Hallé
 * @since 3.0
 */
public interface Stateful 
{
	public enum Status {UNINITIALIZED, RUNNING, RUNNING_PREREQ, READY, DONE, DONE_WARNING, TIMEOUT, CANCELLED, FAILED}
	
	public Status getStatus();
	
	public static Status getLowestStatus(Status s1, Status s2)
	{
		if (s1 == Status.FAILED || s2 == Status.FAILED || s1 == Status.CANCELLED || s2 == Status.CANCELLED || s1 == Status.TIMEOUT || s2 == Status.TIMEOUT)
		{
			// Failures are lower than everything else
			return Status.FAILED;
		}
		if (s1 == Status.RUNNING || s1 == Status.RUNNING_PREREQ || s2 == Status.RUNNING || s2 == Status.RUNNING_PREREQ)
		{
			// Running is lower than not started yet
			return Status.RUNNING;
		}
		if (s1 == Status.UNINITIALIZED || s2 == Status.UNINITIALIZED)
		{
			// Uninitialized is lower than ready
			return Status.UNINITIALIZED;
		}
		if (s1 == Status.READY || s2 == Status.READY)
		{
			// Ready is lower than done
			return Status.READY;
		}
		if (s1 == Status.DONE_WARNING || s2 == Status.DONE_WARNING)
		{
			// Done with warnings is lower than done
			return Status.DONE_WARNING;
		}
		return Status.DONE;
	}
}
