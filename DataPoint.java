/**
 * Class to represent a single data point of light curve data
 * @author S.R. Moorhead
 * @version 05/28/14
 *
 */

//import statements:
import java.util.Scanner;

public class DataPoint {
	//instance variables
	private double time;
	private double flux;
	private double weight;
	
	/**
	 * Zero argument constructor.
	 * Automatically sets time and flux to zero and weight to 1
	 * Probably unnecessary.
	 */
	public DataPoint()	{
		time = 0;
		flux = 0;
		weight = 1;
	}
	
	/**
	 * Given time t and flux f and weight w, create a data point.
	 * @param t is a double representing the time of the data point
	 * @param f is a double representing the flux level of the data point
	 */
	public DataPoint(double t, double f)	{
		time = t;
		flux = f;
		weight = 1;
	}
	
	/**
	 * Given time t and flux f and weight w, create a data point.
	 * @param t is a double representing the time of the data point
	 * @param f is a double representing the flux level of the data point
	 * @param w is a double representing the weight of the point in comparison to all other points
	 */
	public DataPoint(double t, double f, double w)	{
		time = t;
		flux = f;
		weight = w;
	}
	
	/**
	 * Given a single line from a data file, create a data point.
	 * @param line is a line containing data in the order:  time flux weight
	 */
	public DataPoint(String line)	{
		if(line.length() == 0)
			throw new IllegalArgumentException("The passed string does not contain any data.");
		
		Scanner s = new Scanner(line);
		
		time = Double.parseDouble(s.next());
		flux = Double.parseDouble(s.next());
		if(s.hasNext())
			weight = Double.parseDouble(s.next());
		else
			weight = 1;
	}
	
	/**
	 * @return time
	 */
	public double getTime()	{
		return time;
	}
	
	/**
	 * @return flux
	 */
	public double getFlux()	{
		return flux;
	}
	
	/**
	 * @return weight
	 */
	public double getWeight()	{
		return weight;
	}
	
	/**
	 * Change the time value of this DataPoint
	 * @param newTime is the value of the new time for this DataPoint
	 */
	public void changeTime(double newTime)	{
		time = newTime;
	}
	
	/**
	 * Change the flux value of this DataPoint
	 * @param newFlux is the value of the new flux for this DataPoint
	 */
	public void changeFlux(double newFlux)	{
		flux = newFlux;
	}
	
	/**
	 * Change the weight value of this DataPoint
	 * @param newWeight is the value of the new weight for this DataPoint
	 */
	public void changeWeight(double newWeight)	{
		weight = newWeight;
	}
	
	/**
	 * Overrides toString() method in Object class.
	 * Returns DataPoint to string in format:  #TIME# #FLUX# #WEIGHT# (no labels)
	 */
	public String toString()	{
		return time + "\t" + flux + "\t" + weight;
	}
	
	/**
	 * Print DataPoint to string in format: Time: #### Flux: #### Weight: ####
	 */
	public String toLabeledString()	{
		return "Time: " + time + " Flux: " + flux + " Weight: " + weight;
	}
	
	/**
	 * Returns true if data points are at same time and have same flux and same weight.
	 * False otherwise.
	 */
	public boolean equals(Object obj)	{
		if(obj instanceof DataPoint)	{
			DataPoint temp = (DataPoint) obj;
			if(temp.getFlux() == flux && temp.getTime() == time && temp.getWeight() == weight)
				return true;
		}
		return false;
	}
	
	/**
	 * Perform a deep copy of a data point.
	 * @return a deep copy of a data point.
	 */
	public DataPoint deepCopy()	{
		DataPoint returnCopy = new DataPoint();
		returnCopy.time = this.time;
		returnCopy.flux = this.flux;
		returnCopy.weight = this.weight;
		return returnCopy;
	}
}
