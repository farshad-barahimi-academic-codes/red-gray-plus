/**
 * This file belongs to the Red Gray Plus projection tool project.
 * 
 * Code author: Farshad Barahimi
 * Research contributors: Farshad Barahimi and Dr. Fernando Paulovich
 * 
 * © 2019-2021 Dr. Fernando Paulovich and Farshad Barahimi. Licensed under the Academic Free License version 3.0 (https://opensource.org/licenses/AFL-3.0).
 * 
 * The Red Gray Plus projection tool project is an academic research project reflecting the Red Gray Projection method which is described in the following preprint on arXiv:
 * Farshad Barahimi and Fernando Paulovich, “Multi-point dimensionality reduction to improve projection layout reliability.” , arXiv preprint (2021).
 */

package CompactRedGrayPlusProjection;

import java.util.ArrayList;

public class CompactBox
{
	private int numberOfDimentions_;
	private ArrayList<Double> corner1_;
	private ArrayList<Double> corner2_;
	
	public ArrayList<Double> GetCorner1()
	{
		return corner1_;
	}
	
	public ArrayList<Double> GetCorner2()
	{
		return corner2_;
	}
	
	public int GetNumberOfDimentions()
	{
		return numberOfDimentions_;
	}
	
	public CompactBox(int numberOfDimentions)
	{
		numberOfDimentions_=numberOfDimentions;
		corner1_=new ArrayList<Double>();
		corner2_=new ArrayList<Double>();
		
		for(int i=0;i<numberOfDimentions;i++)
		{
			corner1_.add(0.0);
			corner2_.add(1.0);
		}
	}
	
	public double GetSize(int dimention)
	{
		return Math.abs(corner2_.get(dimention)-corner1_.get(dimention));
	}
}

