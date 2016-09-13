/**
 * Class of tools to manipulate light curve data
 * @author S.R. Moorhead
 * @version 05/28/14
 *
 */

import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Iterator;
import java.io.IOException;

//A collection of tools needed to do things with data
public class AstroTools {
	
	private static final double KEPLER_dT = 58.84876;
	
	/**
	 * meanEventProfile is specifically for use on the Kepler DAV object to determine a mean event shape and values
	 * based on an averaging of all events
	 * @param f is a file containing the start time, end time, peak time, and weight of each event
	 * @param l is a light curve containing data of which @param f is a subset of
	 * @throws FileNotFoundException should File f not exist
	 * @throws IOException for scanner issues
	 */
	public static void meanEventProfile(File f, Lightcurve l, Boolean w) throws FileNotFoundException, IOException		{
		Scanner s = new Scanner(f);
		s.nextLine(); //skip header line
		//read in file
		ArrayList<Double> peaks = new ArrayList<Double>();
		ArrayList<Double> starts = new ArrayList<Double>();
		ArrayList<Double> ends = new ArrayList<Double>();
		ArrayList<Double> weights = new ArrayList<Double>();
			
		while(s.hasNext())	{
			starts.add(Double.valueOf(s.next()));
			ends.add(Double.valueOf(s.next()));
			peaks.add(Double.valueOf(s.next()));
			if(w)	{
				weights.add(Double.valueOf(s.next()));
			}
			else	{
				s.next();
			}
		}
		//determine longest event
		Double max = Double.valueOf(0);
		for(int i = 0; i < peaks.size(); i++)	{
			if(ends.get(i) - peaks.get(i) > max)
				max = ends.get(i) - peaks.get(i);
			if(peaks.get(i) - starts.get(i) > max)
				max = peaks.get(i) - starts.get(i);
		}
		//make all events same length based on longest
		for(int i = 0; i < peaks.size(); i++)	{
			starts.set(i, peaks.get(i) - max);
			ends.set(i, peaks.get(i) + max);
		}
		//bin the points in each dT and keep count of how many points get placed in each bin
		//average each bin
		ArrayList<Double> bins = new ArrayList<Double>(peaks.size());
		boolean add = true;
		for(int i = 0; i < peaks.size(); i++)	{
			double time = starts.get(i);
			double weight = 1.0;
			if(w) {
				weight = weights.get(i);
			}
			
			int step = 0;
			while(time < ends.get(i))	{
				Lightcurve temp = l.subsection(time, time + KEPLER_dT + 0.000001);
				if(add)	{
					if(w)	{
						bins.add(temp.averageFlux() * weight);
					}
					else	{
						bins.add(temp.averageFlux());
					}
				}
				else
					if(w)	{
						bins.set(step, bins.get(step) + (temp.averageFlux() * weight));
					}
					else	{
						bins.set(step, bins.get(step) + (temp.averageFlux()));
					}	
				step++;
				time += KEPLER_dT;
			}
			add = false;
		}
		
		if(w)	{
			//sum all weights
			double weightSum = 0;
			for(double wVal : weights)	{
				weightSum += wVal;
			}
			for(int i = 0; i < bins.size(); i++)	{
				bins.set(i, bins.get(i) / weightSum);
			}
		}	
		
		//add each bins flux to a model event LC with the peak centered at zero
		Lightcurve result = new Lightcurve();
		Iterator<Double> it = bins.iterator();
		double time = 0 - (peaks.get(0) - starts.get(0));
		while(it.hasNext())	{
			if(w)	{
				result.add(new DataPoint(time, it.next()));
			}
			else	{
				result.add(new DataPoint(time, it.next() / peaks.size()));
			}
			time += KEPLER_dT;
		}
		
//		result.normalizeTime(0 - (peaks.get(0) - starts.get(0)), ends.get(0) - peaks.get(0));
//		result.normalizeFlux();
		
		result.toFile("../20150616/meanEventShape20150616_noWeight.dat");
		result.boxcarSmooth(15).toFile("../20150616/meanEventShape_smooth15_20150616_noWeight.dat");
		result.boxcarSmooth(5).toFile("../20150616/meanEventShape_smooth5_20150616_noWeight.dat");
	}
}
