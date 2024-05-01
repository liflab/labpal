package examples;

import java.math.BigInteger;

import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.assistant.AssistantRun;
import ca.uqac.lif.labpal.experiment.Experiment;

public class LongLab extends Laboratory
{
	@Override
	public void setup()
	{
		for (int i = 0; i < 10; i++)
		{
			add(new LongExperiment(i));
		}
		//AssistantRun run = this.getAssistant().enqueue(getExperiments());
		//this.getAssistant().
	}
	
	public static void main(String[] args)
	{
		initialize(args, LongLab.class);
	}
	
	protected static class LongExperiment extends Experiment
	{
		protected final int m_id;
		
		public LongExperiment(int id)
		{
			super();
			m_id = id;
		}
		
		@Override
		public void execute()
		{
		// Do something that takes a long time
			boolean interrupted = false;
			BigInteger i = BigInteger.ZERO;
			BigInteger j = new BigInteger("999999");
			System.out.println("Experiment in thread " + Thread.currentThread().hashCode());
			while (i.compareTo(j) < 0)
			{
				i = i.add(BigInteger.ONE);
				i.isProbablePrime(256);
				if (Thread.currentThread().isInterrupted())
				{
					System.out.println("Interrupted " + m_id);
					interrupted = true;
					break;
				}
			}
			if (!interrupted)
			{
				System.out.println("Done " + m_id);
			}
		}
	}
}
