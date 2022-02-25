package ca.uqac.lif.labpal;

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
