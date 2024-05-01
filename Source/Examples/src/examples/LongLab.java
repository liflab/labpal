package examples;

import java.math.BigInteger;

import ca.uqac.lif.labpal.Laboratory;
import ca.uqac.lif.labpal.experiment.Experiment;
import ca.uqac.lif.units.si.Second;

public class LongLab extends Laboratory
{
	@Override
	public void setup()
	{
		for (int i = 1; i <= 10; i++)
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
			if (id == 2)
			{
				setTimeout(new Second(5));
			}
		}
		
		@Override
		public void execute() throws InterruptedException
		{
		// Do something that takes a long time
			boolean interrupted = false;
			BigInteger i = BigInteger.ZERO;
			BigInteger j = new BigInteger("999999");
			if (m_id == 2)
			{
				j = new BigInteger("9999999");
			}
			while (i.compareTo(j) < 0)
			{
				i = i.add(BigInteger.ONE);
				i.isProbablePrime(256);
				if (Thread.interrupted())
				{
					System.out.println("Interrupted " + m_id);
					throw new InterruptedException();
				}
			}
			/*
			if (!interrupted)
			{
				System.out.println("Done " + m_id);
			}
			*/
		}
	}
}
