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

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class CompactDataInstance
{
	private ArrayList<Double> features_;
	private ArrayList<Double> featuresForEvaluation_;
	private ArrayList<Integer> classes_;
	private ArrayList<CompactProjectedPoint> projectedPoints_;
	private ArrayList<CompactProjectedPoint> evaluationProjectedPoints_;
	private ArrayList<CompactDataInstance> neighbors_;
	private ArrayList<CompactDataInstance> neighborsForEvaluation_;
	private int indexInDataInstanceSet_;
	private CompactDataInstanceSet dataInstanceSet_;
	private Image digitImage_;
	private Image digitImageRed_;
	private Image digitImageGray_;
	
	public CompactDataInstance()
	{
		features_=new ArrayList<Double>();
		featuresForEvaluation_=new ArrayList<Double>();
		classes_=new ArrayList<Integer>();
		projectedPoints_=new ArrayList<CompactProjectedPoint>();
		evaluationProjectedPoints_=new ArrayList<CompactProjectedPoint>();
		neighbors_=new ArrayList<CompactDataInstance>();
		neighborsForEvaluation_=new ArrayList<CompactDataInstance>();
		indexInDataInstanceSet_=-1;
		dataInstanceSet_=null;
		digitImage_=null;
		digitImageRed_=null;
	}
	
	
	public ArrayList<Double> GetFeatures()
	{
		return features_;
	}
	
	public ArrayList<Integer> GetClasses()
	{
		return classes_;
	}
	
	public double GetFeature(int dimention)
	{
		return features_.get(dimention);
	}
	
	public int GetClass(int index)
	{
		return classes_.get(index);
	}
	
	public ArrayList<CompactProjectedPoint> GetProjectedPoints()
	{
		return projectedPoints_;
	}
	
	public ArrayList<CompactProjectedPoint> GetEvaluationProjectedPoints()
	{
		return evaluationProjectedPoints_;
	}
	
	public int GetIndexInDataInstanceSet()
	{
		return indexInDataInstanceSet_;
	}
	
	public void SetIndexInDataInstanceSet(int indexInDataInstanceSet)
	{
		indexInDataInstanceSet_=indexInDataInstanceSet;
	}
	
	public CompactDataInstance Clone()
	{
		var dataInstance=new CompactDataInstance();
		for(int i=0;i<features_.size();i++)
			dataInstance.GetFeatures().add(features_.get(i));
		
		for(int i=0;i<classes_.size();i++)
			dataInstance.GetClasses().add(classes_.get(i));
		
		for(int i=0;i<projectedPoints_.size();i++)
			dataInstance.GetProjectedPoints().add(projectedPoints_.get(i).Clone());
		
		dataInstance.SetIndexInDataInstanceSet(indexInDataInstanceSet_);
		
		return dataInstance;
	}
	
	public double DistanceTo(CompactDataInstance dataInstance)
	{
		if(dataInstanceSet_!=null)
		{
			if(dataInstanceSet_.IsDistancesPreComputed())
				return dataInstanceSet_.GetPreComputedDistance(indexInDataInstanceSet_, dataInstance.GetIndexInDataInstanceSet());
			else if(dataInstanceSet_.GetUseDissimilarityInsteadOfDistance())
				return dataInstanceSet_.GetDisSimiliraties()[indexInDataInstanceSet_][dataInstance.GetIndexInDataInstanceSet()];
		}
		
		double sum=0;
		for(int i=0;i<features_.size();i++)
			sum+=Math.pow(this.GetFeature(i)-dataInstance.GetFeature(i),2);
		
		return Math.sqrt(sum);
	}
	
	public double EvaluationDistanceTo(CompactDataInstance dataInstance)
	{
		if(dataInstanceSet_!=null)
		{
			if(dataInstanceSet_.IsDistancesForEvaluationPreComputed())
				return dataInstanceSet_.GetPreComputedDistanceForEvaluation(indexInDataInstanceSet_, dataInstance.GetIndexInDataInstanceSet());
			else if(dataInstanceSet_.GetUseDissimilarityInsteadOfDistance())
				return dataInstanceSet_.GetDisSimiliraties()[indexInDataInstanceSet_][dataInstance.GetIndexInDataInstanceSet()];
		}
		
		if(featuresForEvaluation_.size()==0)
			return this.DistanceTo(dataInstance);
		
		double sum=0;
		for(int i=0;i<featuresForEvaluation_.size();i++)
			sum+=Math.pow(this.GetFeaturesForEvaluation().get(i)-dataInstance.GetFeaturesForEvaluation().get(i),2);
		
		return Math.sqrt(sum);
	}
	
	public ArrayList<CompactDataInstance> GetNeighbors()
	{
		return neighbors_;
	}
	
	public ArrayList<CompactDataInstance> GetNeighborsForEvaluation()
	{
		return neighborsForEvaluation_;
	}
	
	public double GetProjectedDistanceExtended(CompactDataInstance dataInstance, double idealDistance, double scale)
	{
		double bestDistance=this.projectedPoints_.get(0).GetDistance(dataInstance.GetProjectedPoints().get(0));
		
		for(int i=0;i<projectedPoints_.size();i++)
		{
			for(int j=0;j<dataInstance.GetProjectedPoints().size();j++)
			{
				double distance=projectedPoints_.get(i).GetDistance(dataInstance.GetProjectedPoints().get(j));
				distance*=scale;
				if(Math.abs(distance-idealDistance)<Math.abs(bestDistance-idealDistance))
					bestDistance=distance;
			}
		}
		
		return bestDistance;
	}
	
	public void SetDataInstanceSet(CompactDataInstanceSet dataInstanceSet)
	{
		dataInstanceSet_=dataInstanceSet;
	}
	
	public ArrayList<Double> GetFeaturesForEvaluation()
	{
		return featuresForEvaluation_;
	}
	
	public void BuildDigitImage(int imageHeight)
	{
		var featuresForEvaluation=featuresForEvaluation_;
		if(featuresForEvaluation.size()==0)
			featuresForEvaluation=features_;
		
		int imageWidth=featuresForEvaluation.size()/imageHeight;
		
		var digitImage=new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_BYTE_GRAY);
		var digitImageRed=new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
		var digitImageGray=new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_BYTE_GRAY);
		int[] pixels=new int[imageWidth*imageHeight];
		int[] pixelsRed=new int[imageWidth*imageHeight*3];
		int[] pixelsGray=new int[imageWidth*imageHeight];
		
		for(int i=0;i<imageWidth;i++)
			for(int j=0;j<imageHeight;j++)
			{
				pixels[i*imageWidth+j]=featuresForEvaluation.get(i*imageWidth+j).intValue();
				pixelsRed[i*imageWidth*3+j*3]=featuresForEvaluation.get(i*imageWidth+j).intValue();
				pixelsRed[i*imageWidth*3+j*3+1]=0;
				pixelsRed[i*imageWidth*3+j*3+2]=0;
				pixelsGray[i*imageWidth+j]=pixels[i*imageWidth+j]/2 +127;
			}
		digitImage.getRaster().setPixels(0, 0, imageWidth, imageHeight, pixels);
		digitImageRed.getRaster().setPixels(0, 0, imageWidth, imageHeight, pixelsRed);
		digitImageGray.getRaster().setPixels(0, 0, imageWidth, imageHeight, pixelsGray);
		digitImage_=digitImage;
		digitImageRed_=digitImageRed;
		digitImageGray_=digitImageGray;
	}
	
	public Image GetDigitImage()
	{
		return digitImage_;
	}
	
	public Image GetDigitImageRed()
	{
		return digitImageRed_;
	}
	
	public Image GetDigitImageGray()
	{
		return digitImageGray_;
	}
}

