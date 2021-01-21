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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class CompactProjectionStep
{
	String name_;
	CompactProjectedPointSet projectedPointSet_;
	double redAndGrayTrustworthiness_;
	double redTrustworthiness_;
	
	public String GetName()
	{
		return name_;
	}
	
	public CompactProjectedPointSet GetProjectedPointSet()
	{
		return projectedPointSet_;
	}
	
	public CompactProjectionStep(String name, CompactProjectedPointSet projectedPointSet) throws Exception
	{
		this(name,projectedPointSet,1);
	}
	
	public CompactProjectionStep(String name, CompactProjectedPointSet projectedPointSet, int numberOfThreads) throws Exception
	{
		name_=name;
		projectedPointSet_=projectedPointSet;
		redAndGrayTrustworthiness_=-1;
		computeMetrics(numberOfThreads);
	}
	
	private void computeMetrics(int numberOfThreads) throws Exception
	{
		var projecteionEvaluator=new CompactProjectionEvaluator();
		
		if(numberOfThreads==1)
		{
			redAndGrayTrustworthiness_=projecteionEvaluator.EvaluateProjectionTrustworthinessForStrictRedGrayProjection(projectedPointSet_, false);
			redTrustworthiness_=projecteionEvaluator.EvaluateProjectionTrustworthinessForStrictRedGrayProjection(projectedPointSet_, true);
		}
		else
		{
			redAndGrayTrustworthiness_=projecteionEvaluator.EvaluateProjectionTrustworthinessMultiThreadedForStrictRedGrayProjection(
					projectedPointSet_, false,numberOfThreads);
							
			redTrustworthiness_=projecteionEvaluator.EvaluateProjectionTrustworthinessMultiThreadedForStrictRedGrayProjection(
					projectedPointSet_, true, numberOfThreads);
			
		}
		
	}
	
	public double GetRedAndGrayTrustworthiness()
	{
		return redAndGrayTrustworthiness_;
	}
	
	public double GetRedTrustworthiness()
	{
		return redTrustworthiness_;
	}
		
	public void DrawOnGraphics(Graphics graphics, int frameDistance,int frameWidth,int frameHeight,
			CompactProjectionDisplayOptions projectionDisplayOptions, boolean displayKeys, boolean showLines, boolean showNeighborhoodLines, boolean showSampledNeighborhood)
	{
		
		var projectionPointSet=this.GetProjectedPointSet();		
		
		
		graphics.setColor(Color.WHITE);
		graphics.fillRect(frameDistance, frameDistance, frameWidth-frameDistance*2, frameHeight-frameDistance*2);
		graphics.setColor(Color.BLACK);
		var graphics2d=(Graphics2D)graphics;
		graphics2d.setStroke(new BasicStroke(2));
		graphics.drawRect(frameDistance, frameDistance, frameWidth-frameDistance*2, frameHeight-frameDistance*2);
		
		var projectedPoints=projectionPointSet.GetProjectedPoints();
		
		// Colors based on "Twenty-Two colors of Maximum Contrast" published work by Kenneth L. Kelly and "sRGB Centroids for the ISCC-NBS Colour System" online PDF by Paul Centore.
		var colors22=new Color[] {
				new Color(231, 225, 233), // White
				new Color(43, 41, 43), // Black
				new Color(241, 191, 21), // Yellow 
				new Color(147, 82, 168), // Purple 
				new Color(247, 118, 11), // Orange 
				new Color(153, 198, 249), // Light blue 
				new Color(213, 28, 60), // Red 
				new Color(200, 177, 139), // Buff
				new Color(138, 132, 137), // Gray 
				new Color(35, 234, 165), // Green 
				new Color(244, 131, 205), // Purplish pink 
				new Color(39, 108, 189 ), // Blue 
				new Color(245, 144, 128), // Yellowish pink
				new Color(97, 65, 156), // Violet 
				null, // Orange yellow 66 (Don't know RGB)
				new Color(184, 55, 115), // Purplish red 
				new Color(235, 221, 33), // Greenish yellow
				new Color(139, 28, 14), // Reddish brown 
				new Color(167, 220, 38), // Yellow green 
				new Color(103, 63, 11), // Yellowish brown 
				new Color(232, 59, 27), // Reddish orange 
				new Color(32, 52, 11) // Olive green
		};
		
		var colors15=new Color[15];
		int color=0;
		for(int i=0;i<22;i++)
			if(i!=14 && i!=8 && i!=0 && i!=1 && i!=6 && i!=4 && i!=2)
			{
				colors15[color]=colors22[i];
				color++;
			}
		
		var classColors=colors15;
				
		
		double maximumX=0;
		double maximumY=0;
		
		double minimumX=Double.MAX_VALUE;
		double minimumY=Double.MAX_VALUE;
		
		for(var i=0;i<projectedPoints.size()*3;i++)
		{
			var projectedPoint=projectedPoints.get(i%projectedPoints.size());
			
			if(projectionDisplayOptions==CompactProjectionDisplayOptions.RED_GRAY ||
					projectionDisplayOptions==CompactProjectionDisplayOptions.RED_GRAY_CLASS_COLORED || projectionDisplayOptions==CompactProjectionDisplayOptions.DIGITS_RED_GRAY)
			{
				if(projectedPoint.IsGray() && i>=projectedPoints.size())
					continue;
				
				if(!projectedPoint.IsGray() && i<projectedPoints.size())
					continue;
				
				if(i>=2*projectedPoints.size())
					continue;
			}
			else
			{
				if(projectedPoint.GetProjectionIndex()==0 && !projectedPoint.IsGray() && i>=projectedPoints.size())
					continue;
				
				if(projectedPoint.GetProjectionIndex()==0 && projectedPoint.IsGray() && i>=projectedPoints.size()*2)
					continue;
								
				if(projectedPoint.GetProjectionIndex()>0 && i<projectedPoints.size()*2)
					continue;
			}
			
			
			maximumX=Math.max(projectedPoint.GetX(), maximumX);
			maximumY=Math.max(projectedPoint.GetY(), maximumY);
			
			minimumX=Math.min(projectedPoint.GetX(), minimumX);
			minimumY=Math.min(projectedPoint.GetY(), minimumY);
			
			if(projectionDisplayOptions==CompactProjectionDisplayOptions.DIGITS)
			{
				int digitWidth=28;
				graphics.drawImage(projectedPoint.GetDataInstance().GetDigitImage(), (int)projectedPoint.GetX()+frameDistance-digitWidth/2, (int) projectedPoint.GetY()+frameDistance - digitWidth/2, null);
			}
			else if(projectionDisplayOptions==CompactProjectionDisplayOptions.DIGITS_RED_GRAY)
			{
				int digitWidth=28;
				if(projectedPoint.IsGray())
					graphics.drawImage(projectedPoint.GetDataInstance().GetDigitImageGray(), (int)projectedPoint.GetX()+frameDistance-digitWidth/2, (int) projectedPoint.GetY()+frameDistance - digitWidth/2, null);
				else
					graphics.drawImage(projectedPoint.GetDataInstance().GetDigitImageRed(), (int)projectedPoint.GetX()+frameDistance-digitWidth/2, (int) projectedPoint.GetY()+frameDistance - digitWidth/2, null);	
			}
			else if(projectedPoint.GetDataInstance().GetClasses().size()>0)
			{
				int firstClass=projectedPoint.GetDataInstance().GetClass(0);
									
				if(firstClass< classColors.length)
					graphics.setColor(classColors[firstClass]);
				else
					graphics.setColor(Color.BLACK);
				
				int ovalWidth=20;
				
				if(projectionDisplayOptions==CompactProjectionDisplayOptions.RED_GRAY)
				{
					if(projectedPoint.IsGray())
					{
						graphics.setColor(Color.GRAY);
						ovalWidth-=5;
					}
					else
						graphics.setColor(Color.RED);
				}
				
				if(projectionDisplayOptions==CompactProjectionDisplayOptions.RED_GRAY_CLASS_COLORED)
					if(projectedPoint.IsGray())
						ovalWidth-=5;
				
				graphics.fillOval((int)projectedPoint.GetX()+frameDistance-ovalWidth/2, (int) projectedPoint.GetY()+frameDistance - ovalWidth/2, ovalWidth, ovalWidth);
				
				if(projectionDisplayOptions==CompactProjectionDisplayOptions.RED_GRAY)
				{
					
				}
				else if(projectionDisplayOptions==CompactProjectionDisplayOptions.RED_GRAY_CLASS_COLORED)
				{
					if(!projectedPoint.IsGray())
					{
						graphics.setColor(Color.BLACK);
						graphics.drawOval((int)projectedPoint.GetX()+frameDistance-ovalWidth/2, (int) projectedPoint.GetY()+frameDistance - ovalWidth/2, ovalWidth, ovalWidth);
					}
				}
				else
				{
					if(projectedPoint.GetProjectionIndex()>0)
					{
						ovalWidth-=10;
						graphics.setColor(Color.BLACK);
						graphics.fillOval((int)projectedPoint.GetX()+frameDistance-ovalWidth/2, (int) projectedPoint.GetY()+frameDistance - ovalWidth/2, ovalWidth, ovalWidth);
						ovalWidth+=10;
					}
					
					if(projectedPoint.IsGray())
					{
						graphics.setColor(Color.BLACK);
						graphics.drawOval((int)projectedPoint.GetX()+frameDistance-ovalWidth/2, (int) projectedPoint.GetY()+frameDistance - ovalWidth/2, ovalWidth, ovalWidth);
					}
				}
				
			}
		}
		
		if(showLines)
		{
			for(var i=0;i<projectedPoints.size();i++)
			{
				var projectedPoint1=projectedPoints.get(i);
				
				for(int j=i+1;j<projectedPoints.size();j++)
				{
					var projectedPoint2=projectedPoints.get(j);
					if(projectedPoint1.GetDataInstance()==projectedPoint2.GetDataInstance())
					{
						graphics.setColor(Color.BLACK);
						graphics.drawLine((int)projectedPoint1.GetX()+frameDistance, (int)projectedPoint1.GetY()+frameDistance, 
								(int)projectedPoint2.GetX()+frameDistance, (int)projectedPoint2.GetY()+frameDistance);
					}
				}
			}
		}
		else if(showNeighborhoodLines)
		{
			int increaseSize=1;
			if(showSampledNeighborhood)
				increaseSize=60;
			for(var i=0;i<projectedPoints.size();i++)
			{
				var projectedPoint1=projectedPoints.get(i);
				
				for(int j=0;j<projectedPoint1.GetNeighbors().size();j+=increaseSize)
				{
					var projectedPoint2=projectedPoint1.GetNeighbors().get(j);
					
					graphics.setColor(Color.BLACK);
					graphics.drawLine((int)projectedPoint1.GetX()+frameDistance, (int)projectedPoint1.GetY()+frameDistance, 
							(int)projectedPoint2.GetX()+frameDistance, (int)projectedPoint2.GetY()+frameDistance);
				}
			}
		}
		
	}
}
