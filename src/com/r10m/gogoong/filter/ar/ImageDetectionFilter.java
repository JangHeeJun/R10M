package com.r10m.gogoong.filter.ar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.r10m.gogoong.filter.Filter;

import android.content.Context;
import android.util.Log;


public class ImageDetectionFilter implements Filter {
	private static final String TAG = "Detection";
    private final Mat mReferenceImage;
    private final MatOfKeyPoint mReferenceKeypoints =
            new MatOfKeyPoint();
    private final Mat mReferenceDescriptors = new Mat();
    private final Mat mReferenceCorners =
            new Mat(4, 1, CvType.CV_32FC2);
    
    private final MatOfKeyPoint mSceneKeypoints =
            new MatOfKeyPoint();
    private final Mat mSceneDescriptors = new Mat();
    
    private final Mat mCandidateSceneCorners=new Mat(4,1,CvType.CV_32FC2);
    private final Mat mSceneCorners=new Mat(4,1,CvType.CV_32FC2);
    private final MatOfPoint mIntScenecorners=new MatOfPoint();
    
    
    private final Mat mGraySrc = new Mat();
    private final MatOfDMatch mMatches = new MatOfDMatch();
    
    private final FeatureDetector mFeatureDetector =
            FeatureDetector.create(FeatureDetector.STAR);
    private final DescriptorExtractor mDescriptorExtractor =
            DescriptorExtractor.create(DescriptorExtractor.FREAK);
    private final DescriptorMatcher mDescriptorMatcher =
            DescriptorMatcher.create(
                    DescriptorMatcher.BRUTEFORCE_HAMMING);
    
    private final Scalar mLineColor = new Scalar(0,255,0);
    private Context mContext;
    
    public ImageDetectionFilter(final Context context,
            final int referenceImageResourceID)
                    throws IOException {
    	mContext = context;
    	
        mReferenceImage = Utils.loadResource(context,
                referenceImageResourceID,
                Highgui.CV_LOAD_IMAGE_COLOR);
        
        final Mat referenceImageGray = new Mat();
        Imgproc.cvtColor(mReferenceImage, referenceImageGray,
                Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(mReferenceImage, mReferenceImage,
                Imgproc.COLOR_BGR2RGBA);
        
        mReferenceCorners.put(0, 0,
                new double[] {0.0, 0.0});
        mReferenceCorners.put(1, 0,
                new double[] {referenceImageGray.cols(), 0.0});
        mReferenceCorners.put(2, 0,
                new double[] {referenceImageGray.cols(),
                        referenceImageGray.rows()});
        mReferenceCorners.put(3, 0,
                new double[] {0.0, referenceImageGray.rows()});
        
        mFeatureDetector.detect(referenceImageGray,
                mReferenceKeypoints);
        mDescriptorExtractor.compute(referenceImageGray,
                mReferenceKeypoints, mReferenceDescriptors);
        
    }
    
    @Override
    public boolean apply(final Mat src, final Mat dst) {
        Imgproc.cvtColor(src, mGraySrc, Imgproc.COLOR_RGBA2GRAY);
        
        mFeatureDetector.detect(mGraySrc, mSceneKeypoints);
        mDescriptorExtractor.compute(mGraySrc, mSceneKeypoints,
                mSceneDescriptors);
        mDescriptorMatcher.match(mSceneDescriptors,
                mReferenceDescriptors, mMatches);
        Log.e(TAG, "  =====apply Start=====  ");
        return findPose(src, dst);
    }
    
    private boolean findPose(Mat src, Mat dst) {
    	Log.e(TAG, "  =====find Start=====0  ");
        List<DMatch> matchesList = mMatches.toList();
        if (matchesList.size() < 4) {
            // There are too few matches to find the pose.
            return false;
        }
        
        List<KeyPoint> referenceKeypointsList =
                mReferenceKeypoints.toList();
        List<KeyPoint> sceneKeypointsList =
                mSceneKeypoints.toList();
        
        // Calculate the max and min distances between keypoints.
        double maxDist = 0.0;
        double minDist = Double.MAX_VALUE;
        for(DMatch match : matchesList) {
            double dist = match.distance;
            if (dist < minDist) {
                minDist = dist;
            }
            if (dist > maxDist) {
                maxDist = dist;
            }
        }
        Log.e(TAG, "  =====find Start=====1  ");
        if (minDist > 50.0) {
            // The target is completely lost.
            mSceneCorners.create(0,0,mSceneCorners.type());
            return false;
        } else if (minDist > 25.0) {
            // The target is lost but maybe it is still close.
            // Keep using any previously found pose.
            return false;
        }
        Log.e(TAG, "  =====find Start=====2  ");
        // Identify "good" keypoints based on match distance.
        List<Point> goodReferencePointsList =
                new ArrayList<Point>();
        ArrayList<Point> goodScenePointsList =
                new ArrayList<Point>();
        double maxGoodMatchDist = 1.75 * minDist;
        for(DMatch match : matchesList) {
            if (match.distance < maxGoodMatchDist) {
                goodReferencePointsList.add(
                		referenceKeypointsList.get(match.trainIdx).pt);
                goodScenePointsList.add(
                        sceneKeypointsList.get(match.queryIdx).pt);
            }
        }
        Log.e(TAG, "  =====find Start=====3  ");
        if (goodReferencePointsList.size() < 4 ||
                goodScenePointsList.size() < 4) {
            // There are too few good points to find the pose.
            return false;
        }
        
        MatOfPoint2f goodReferencePoints = new MatOfPoint2f();
        goodReferencePoints.fromList(goodReferencePointsList);
        
        MatOfPoint2f goodScenePoints = new MatOfPoint2f();
        goodScenePoints.fromList(goodScenePointsList);
        
        Mat homography=Calib3d.findHomography(goodReferencePoints, goodScenePoints);
        Core.perspectiveTransform(mReferenceCorners, mCandidateSceneCorners, homography);
        Log.e(TAG, "  =====find Start=====4  ");
        mCandidateSceneCorners.convertTo(mIntScenecorners, CvType.CV_32S);
        if(Imgproc.isContourConvex(mIntScenecorners)){
        	mCandidateSceneCorners.copyTo(mSceneCorners);
        }
        return draw(src, dst);
    }
    
    protected boolean draw(Mat src, Mat dst) {
        
        if (dst != src) {
            src.copyTo(dst);
        }
        
        if (mSceneCorners.height()<4) {
            // The target has not been found.
            
            // Draw a thumbnail of the target in the upper-left
            // corner so that the user knows what it is.
            
            int height = mReferenceImage.height();
            int width = mReferenceImage.width();
            int maxDimension = Math.min(dst.width(),
                    dst.height()) / 2;
            double aspectRatio = width / (double)height;
            if (height > width) {
                height = maxDimension;
                width = (int)(height * aspectRatio);
            } else {
                width = maxDimension;
                height = (int)(width / aspectRatio);
            }
            Mat dstROI = dst.submat(0, height, 0, width);
            Imgproc.resize(mReferenceImage, dstROI, dstROI.size(),
                    0.0, 0.0, Imgproc.INTER_AREA);
            
            return false;
        }
        Log.e(TAG, "  =====Draw Start=====  ");
        Core.line(dst,new Point(mSceneCorners.get(0,0)),
        		new Point(mSceneCorners.get(1,0)),mLineColor,4);
        Core.line(dst,new Point(mSceneCorners.get(1,0)),
        		new Point(mSceneCorners.get(2,0)),mLineColor,4);
        Core.line(dst,new Point(mSceneCorners.get(2,0)),
        		new Point(mSceneCorners.get(3,0)),mLineColor,4);
        Core.line(dst,new Point(mSceneCorners.get(3,0)),
        		new Point(mSceneCorners.get(0,0)),mLineColor,4);
        return true;
        //Toast.makeText(mContext, "���� ���ð� ��� ȭ���� ��ȭ�� �Դϴ�.", Toast.LENGTH_SHORT).show();
    }
}
