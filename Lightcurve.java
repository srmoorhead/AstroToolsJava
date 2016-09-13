/**
 * A class that holds the data of a light curve, which can then be manipulated in various ways
 * @author S.R. Moorhead
 * @version 05/28/14
 *
 */

//import statements:
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;


public class Lightcurve {
	//instance variables
	private ArrayList<DataPoint> data;
	
	/**
	 * Zero argument constructor to create an empty Lightcurve
	 */
	public Lightcurve()	{
		data = new ArrayList<DataPoint>();
	}
	
	/**
	 * Create a Lightcurve from a file of relevant data
	 * @param f is a file with data in the format:  time flux
	 * @throws FileNotFoundException if the file isn't found
	 */
	public Lightcurve(File f) throws FileNotFoundException	{
		data = new ArrayList<DataPoint>();
		
		Scanner readIn = new Scanner(f);
		while(readIn.hasNext())	{
			DataPoint temp = new DataPoint(readIn.nextLine());
			data.add(temp);
		}
	}
	
	/**
	 * @return the number of DataPoint objects in the Lightcurve
	 */
	public int size()	{
		return data.size();
	}
	
	/**
	 * Return a string containing all of the data in the format:<br>
	 * time flux<br>
	 * time flux<br>
	 * time flux<br>
	 * ...
	 */
	public String toString()	{
		String result = "";
		int fin = data.size() - 1;
		for(int i = 0; i < fin; i++)	{
			result += data.get(i).toString() + "\n";
		}
		if(data.size() > 0)
			result += data.get(data.size() - 1).toString();
		
		return result;
	}
	
	/**
	 * Prints a string representation of this light curve to a file
	 * @param fName is the name of a file.  It can already exist, or not exist.  Both are handled.
	 * @throws IOException
	 */
	public void toFile(String fName) throws IOException	{
		File output = new File(fName);
		output.createNewFile();
		
		//write this Lightcurve to the specified file
		FileWriter fw = new FileWriter(output.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(this.toString());
		bw.close();
	}
	
	/**
	 * Returns a light curve with the specified subsection, starting at the first value
	 * greater than or equal to start, and ending at the last value smaller than or equal to end.
	 * @param startTime is a double denoting the start time of the subsection
	 * @param endTime is a double denoting the end time of the subsection
	 * @return a Lightcurve of the subsection
	 */
	public Lightcurve subsection(double startTime, double endTime)	{
		//ensure all time values are valid, and correct accordingly
		checkTimeExceptions(startTime, endTime);
		startTime = correctStartTime(startTime);
		endTime = correctEndTime(endTime);
		
		//get the indices of the start of the substring and the end of the substring
		//in the current Lightcurve
		int[] indices = getIndices(startTime, endTime);

		//add all the DataPoint objects between the two indices to subCurve
		Lightcurve subCurve = new Lightcurve();
		int fin = indices[1];
		for(int i = indices[0]; i <= fin; i++)
			subCurve.add(data.get(i));
		return subCurve;
	}
	
	/**
	 * Add a DataPoint to the end of the Lightcurve
	 * @param p is a DataPoint to be added to the end of the Lightcurve
	 */
	public void add(DataPoint p)	{
		data.add(p);
	}
	
	/**
	 * Add another Lightcurve to the end of the current Lightcurve
	 * @param l is the Lightcurve to be added to the end
	 * @return the combined Lightcurve
	 */
	public Lightcurve append(Lightcurve l)	{
		Lightcurve result = new Lightcurve();
		for(int i = 0; i < this.data.size(); i++)
			result.add(this.data.get(i));
		for(int i = 0; i < l.data.size(); i++)
			result.add(l.data.get(i));
		return result;
	}
	
	/**
	 * Remove sections of the Lightcurve based on their times.  Points removed
	 * are all points >= startTime and <= endTime
	 * @param startTime is the time of the first point to be removed
	 * @param endTime is the time of the last point to be removed
	 */
	public void remove(double startTime, double endTime)	{
		//ensure all time values are valid, and correct accordingly
		checkTimeExceptions(startTime, endTime);
		startTime = correctStartTime(startTime);
		endTime = correctEndTime(endTime);
		
		//get the indices of the start of the section of Lightcurve to be removed, and the end
		//of the section of Lightcurve to be removed
		int[] indices = getIndices(startTime, endTime);
		
		//remove this section of the light curve
		int fin = indices[1];
		for(int i = indices[0]; i < fin; i++)	{
			data.remove(i); 
			fin--;
		}
	}

	/**
	 * Remove method to remove all data starting at startTime until the end
	 * @param startTime is the time <= the first data point to remove
	 */
	public void remove(double startTime)	{
		remove(startTime, data.get(data.size()-1).getTime());
	}
	
	/**
	 * Generate a normalized Lightcurve using the given limits as the parameters
	 * @param lowLimit is the lower bound of normalization
	 * @param highLimit is the upper bound of normalization
	 * @return a normalized Lightcurve
	 */
	public Lightcurve normalizeTime(double lowLimit, double highLimit)	{
		Lightcurve normal = deepCopy(this);
		
		//initialize BigDecimal objects for limits for precision of arithmetic
		BigDecimal lowerBound = new BigDecimal(lowLimit);
		BigDecimal upperBound = new BigDecimal(highLimit);
		
		//Check for special cases in BigDecimal world
		if(lowLimit == 0)
			lowerBound = BigDecimal.ZERO;
		else if(lowLimit == 1)
			lowerBound = BigDecimal.ONE;
		else if(lowLimit == 10)
			lowerBound = BigDecimal.TEN;
		if(highLimit == 0)
			upperBound = BigDecimal.ZERO;
		else if(highLimit == 1)
			upperBound = BigDecimal.ONE;
		else if(highLimit == 10)
			upperBound = BigDecimal.TEN;
		
		//store some numbers to make math more efficient
		BigDecimal min = new BigDecimal(normal.data.get(0).getTime()).setScale(10, RoundingMode.CEILING);
		BigDecimal max = new BigDecimal(normal.data.get(data.size()-1).getTime()).setScale(10, RoundingMode.CEILING);
		BigDecimal timeDiff = max.subtract(min).setScale(10, RoundingMode.CEILING);
		BigDecimal limitDiff = upperBound.subtract(lowerBound).setScale(10, RoundingMode.CEILING);
		BigDecimal division = limitDiff.divide(timeDiff, 10, RoundingMode.CEILING);
		int size = data.size();
		
		for(int i = 0; i < size; i++)	{
			BigDecimal element = new BigDecimal(normal.data.get(i).getTime()).setScale(10, RoundingMode.CEILING);
			normal.data.get(i).changeTime((lowerBound.add(element.subtract(min)).multiply(division)).setScale(10, RoundingMode.FLOOR).doubleValue());
		}
		
		return normal;
	}

	/**
	 * Normalized the flux of the Lightcurve between 0 and 1
	 * @return a Lightcurve with flux normalized between 0 and 1
	 */
	public Lightcurve normalizeTime()	{
		return normalizeTime(0, 1);
	}
	
	/**
	 * Generate a normalized Lightcurve using the given limits as the parameters
	 * @param lowLimit is the lower bound of normalization
	 * @param highLimit is the upper bound of normalization
	 * @return a normalized Lightcurve
	 */
	public Lightcurve normalizeFlux(double lowLimit, double highLimit)	{
		Lightcurve normal = deepCopy(this);
		
		//initialize BigDecimal objects for limits for precision of arithmetic
		BigDecimal lowerBound = new BigDecimal(lowLimit);
		BigDecimal upperBound = new BigDecimal(highLimit);
		
		//Check for special cases in BigDecimal world
		if(lowLimit == 0)
			lowerBound = BigDecimal.ZERO;
		else if(lowLimit == 1)
			lowerBound = BigDecimal.ONE;
		else if(lowLimit == 10)
			lowerBound = BigDecimal.TEN;
		if(highLimit == 0)
			upperBound = BigDecimal.ZERO;
		else if(highLimit == 1)
			upperBound = BigDecimal.ONE;
		else if(highLimit == 10)
			upperBound = BigDecimal.TEN;
		
		//store some numbers to make math more efficient
		BigDecimal min = new BigDecimal(normal.getMinFlux()).setScale(10, RoundingMode.CEILING);
		BigDecimal max = new BigDecimal(normal.getMaxFlux()).setScale(10, RoundingMode.CEILING);
		BigDecimal fluxDiff = max.subtract(min).setScale(10, RoundingMode.CEILING);
		BigDecimal limitDiff = upperBound.subtract(lowerBound).setScale(10, RoundingMode.CEILING);
		BigDecimal division = limitDiff.divide(fluxDiff, 10, RoundingMode.CEILING);
		int size = data.size();
		
		for(int i = 0; i < size; i++)	{
			BigDecimal element = new BigDecimal(normal.data.get(i).getFlux()).setScale(10, RoundingMode.CEILING);
			normal.data.get(i).changeFlux((lowerBound.add(element.subtract(min)).multiply(division)).setScale(10, RoundingMode.FLOOR).doubleValue());
		}
			
		return normal;
	}

	/**
	 * Normalized the flux of the Lightcurve between 0 and 1
	 * @return a Lightcurve with flux normalized between 0 and 1
	 */
	public Lightcurve normalizeFlux()	{
		return normalizeFlux(0, 1);
	}
	
	/**
	 * Method to return a boxcar smoothed version of the current Lightcurve, where the boxcar is
	 * defined as 1/2 binSize behind the current point to 1/2 binSize ahead of the current point.
	 * @param binSize is the number of points to include in the boxcar.  If the binSize is even, it will
	 * be increased by 1.
	 * @return a Lightcurve that is a boxcar smoothed version of the current Lightcurve
	 */
	public Lightcurve boxcarSmooth(int binSize)	{
		//ensure binSize is odd, and correct accordingly
		if(binSize % 2 != 1)
			binSize += 1;
		
		Lightcurve smooth = deepCopy(this);
		
		//the below integers are save for efficiency-sake
		int halfBin = binSize / 2;
		int end = smooth.size() - halfBin;
		
		for(int i = halfBin; i < end; i++)	{
			BigDecimal sum = new BigDecimal(data.get(i - halfBin).getFlux()).setScale(10, BigDecimal.ROUND_CEILING);
			int fin = i + halfBin;	//for efficiency
			for(int j = i - halfBin + 1; j <= fin; j++)	{
				BigDecimal toAdd = new BigDecimal(data.get(j).getFlux()).setScale(10, BigDecimal.ROUND_CEILING);
				sum = sum.add(toAdd);
				
			}
			BigDecimal binSizeBD = new BigDecimal(binSize).setScale(10, BigDecimal.ROUND_CEILING);
			BigDecimal avg = sum.divide(binSizeBD, 10, RoundingMode.CEILING);
			
			//assign new point
			smooth.data.get(i).changeFlux(avg.doubleValue());
		}
		
		return smooth;
	}
	
	
	
	/**
	 * @return the value of the maximum flux in the Lightcurve
	 */
	public double getMaxFlux()	{
		BigDecimal maximum = new BigDecimal(data.get(0).getFlux()).setScale(11, RoundingMode.FLOOR);
		int size = data.size();
		for(int i = 1; i < size; i++)	{
			if(data.get(i).getFlux() > maximum.doubleValue())
				maximum = new BigDecimal(data.get(i).getFlux()).setScale(11, RoundingMode.FLOOR);
		}
		
		return maximum.doubleValue();
	}
	
	/**
	 * @param startTime is the starting time of the subregion of the Lightcurve to investigate
	 * @param endTime is the ending time of the subregion of the Lightcurve to investigate
	 * @return the value of the maximum flux in the subregion of the Lightcurve specified
	 */
	public double getMaxFlux(double startTime, double endTime)	{
		//generate the subcurve
		Lightcurve subregion = subsection(startTime, endTime);
		return subregion.getMaxFlux();
	}
	
	/**
	 * @return the value of the minimum flux in the Lightcurve
	 */
	public double getMinFlux()	{
		BigDecimal minimum = new BigDecimal(data.get(0).getFlux()).setScale(11, RoundingMode.CEILING);
		int size = data.size();
		for(int i = 1; i < size; i++)	{
			if(data.get(i).getFlux() < minimum.doubleValue())
				minimum = new BigDecimal(data.get(i).getFlux()).setScale(11, RoundingMode.CEILING);
		}
		return minimum.doubleValue();
	}
	
	/**
	 * @param startTime is the starting time of the subregion of the Lightcurve to investigate
	 * @param endTime is the ending time of the subregion of the Lightcurve to investigate
	 * @return the value of the maximum flux in the subregion of the Lightcurve specified
	 */
	public double getMinFlux(double startTime, double endTime)	{
		//generate the subcurve
		Lightcurve subregion = subsection(startTime, endTime);
		
		return subregion.getMinFlux();
	}
	
	/**
	 * Copy all elements of one Lightcurve into another Lightcurve
	 * @param curve is the Lightcurve to be copied
	 * @return
	 */
	public Lightcurve deepCopy(Lightcurve curve)	{
		Lightcurve copy = new Lightcurve();
		for(int i = 0; i < curve.size(); i++)
			copy.add(curve.data.get(i).deepCopy());
		return copy;
	}
	
	/**
	 * Method to ensure startTime and endTime are within the bounds of the Lightcurve
	 * @param startTime is the given time to be used as the start of some subcurve
	 * @param endTime is the given time to be used as the end of some subcurve
	 */
	private void checkTimeExceptions(double startTime, double endTime)	{
		//check that startTime is before the end
		double lastTime = data.get(data.size() - 1).getTime();
		if(startTime > lastTime)
			throw new IllegalArgumentException("The start time is beyond the range of the Lightcurve."
					+ "\nstartTime must be less than or equal to " + lastTime);
		//check that endTime is after the start
		double firstTime = data.get(0).getTime();
		if(endTime < firstTime)
			throw new IllegalArgumentException("The end time is before the range of the Lightcurve."
					+ "\nendTime must be greater than or equal to " + firstTime);
	}
	
	/**
	 * Corrects the startTime if it is less than the value of the start
	 * @param startTime is a given time to be used as the start of some subcurve
	 */
	private double correctStartTime(double startTime)	{
		double firstTime = data.get(0).getTime();
		if(startTime < firstTime)
			return firstTime;
		return startTime;
	}
	
	/**
	 * Corrects endTime if it is greater than the value of the end
	 * @param endTime is a given time to be used as the end of some subcurve
	 */
	private double correctEndTime(double endTime)	{
		double lastTime = data.get(data.size() - 1).getTime();
		if(endTime > lastTime)
			return lastTime;
		return endTime;
	}
	
	/**
	 * Retrieve the indices of the startTime and the endTime within the Lightcurve such that
	 * the first index is the first time >= startTime and the last index is the last time <= endTime
	 * @param startTime is a double representing the first time in a sequence
	 * @param endTime is a double representing the end time in a sequence
	 * @return a two-element array containing the index of startTime in position 0 and the index
	 * of endTime in position 1.
	 */
	private int[] getIndices(double startTime, double endTime)	{
		//find start
		double time = 0;
		int index = -1;
		while(time < startTime)	{
			index++;
			time = data.get(index).getTime();
		}
		//store first index
		int[] result = new int[2];
		result[0] = index;
		
		//find end
		if(endTime == data.get(data.size()-1).getTime())
			result[1] = data.size()-1;
		else	{
			while(time <= endTime)	{
				index++;
				time = data.get(index).getTime();
			}
			result[1] = index;
		}
		return result;
	}
	
	public double averageFlux()	{
		double fluxSum = 0;
		int pointCount = 0;
		for(DataPoint d : data)	{
			fluxSum += d.getFlux();
			pointCount++;
		}
		return fluxSum / pointCount;
	}
	
	/**
	 * Take a weighted average of the flux of the light curve
	 * @return weighted average
	 */
	public double weightedAverageFlux() {
		double fluxSum = 0;
		double totalCount = 0;
		for(DataPoint d : data)	{
			fluxSum += (d.getFlux() * d.getWeight());
			totalCount += d.getWeight();
		}
		return fluxSum / totalCount;
	}
}
	