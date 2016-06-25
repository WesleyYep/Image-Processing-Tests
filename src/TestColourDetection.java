import java.awt.FlowLayout;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.opencv.core.*;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.videoio.VideoCapture;

/**
 * Created by Wesley on 24/04/2016.
 */
public class TestColourDetection {

    static {
        // Load the native OpenCV library
        System.out.println(System.getProperty("java.library.path"));
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
    }
    
    public static void setupSlider(RangeSlider slider, int majorSpacing, JFrame frame) {
    	slider.setMajorTickSpacing(majorSpacing);
    	slider.setMinorTickSpacing(10);
    	slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setValue(140);
        slider.setUpperValue(255);
        frame.getContentPane().add(slider);
    }
	
    public static void main ( String[] argv) {
	    VideoCapture cap = new VideoCapture(0); //capture the video from webcam
	    MyFrame thresholdFrame = new MyFrame();
        thresholdFrame.setVisible(true);
        MyFrame imgFrame = new MyFrame();
        imgFrame.setVisible(true);
        JFrame controlsFrame = new JFrame();
        controlsFrame.setVisible(true);
        controlsFrame.getContentPane().setLayout(new FlowLayout());
        RangeSlider hSlider = new RangeSlider(0,180);
        RangeSlider sSlider = new RangeSlider(0,255);
        RangeSlider vSlider = new RangeSlider(0,255);
        setupSlider(hSlider, 60, controlsFrame);
        setupSlider(sSlider, 50, controlsFrame);
        setupSlider(vSlider, 50, controlsFrame);
        hSlider.setValue(0);
        hSlider.setUpperValue(10);
//        sSlider.setValue(160);
//        vSlider.setValue(140);
        controlsFrame.pack();

        if(!cap.isOpened()) {
            System.out.println("cannot open camera");
            return;
        }

	    int iLastX = -1; 
	    int iLastY = -1;

	    //Capture a temporary image from the camera
	 	Mat imgTmp = new Mat();
	 	cap.read(imgTmp); 

	 	
	 	
	 	//Create a black image with the size as the camera output
	 	Mat imgLines = new Mat( imgTmp.size(), CvType.CV_8UC3 );
	 
	    while (true) {
	        Mat imgOriginal = new Mat( imgTmp.size(), CvType.CV_8UC3 );;
	        boolean bSuccess = cap.read(imgOriginal); // read a new frame from video
	        if (!bSuccess) {//if not success, break loop
	        	 System.out.println("Cannot read a frame from video stream");
	             break;
	        }
	        Mat imgHSV = new Mat( imgTmp.size(), CvType.CV_8UC3 );;
	        Imgproc.cvtColor(imgOriginal, imgHSV, Imgproc.COLOR_BGR2HSV); //Convert the captured frame from BGR to HSV
	        Mat imgThresholded = new Mat( imgTmp.size(), CvType.CV_8UC3 );;
	        Core.inRange(imgHSV, new Scalar(hSlider.getValue(), sSlider.getValue(), vSlider.getValue()), 
	        		new Scalar(hSlider.getUpperValue(), sSlider.getUpperValue(), vSlider.getUpperValue()), imgThresholded); //Threshold the image
	      
	        //morphological opening (removes small objects from the foreground)
	        Imgproc.erode(imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5)) );
	        Imgproc.dilate( imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5)) ); 
	
	        //morphological closing (removes small holes from the foreground)
	        Imgproc.dilate( imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5)) ); 
	        Imgproc.erode(imgThresholded, imgThresholded, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5)) );
	
	        //see if we can find blobs based on contours
//	        MatOfKeyPoint keyPoints = new MatOfKeyPoint();
//	        FeatureDetector blobDetector = FeatureDetector.create(FeatureDetector.SIMPLEBLOB);
//	        blobDetector.detect(imgThresholded, keyPoints);
	        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
	        Mat hierarchy = new Mat();
	        Imgproc.findContours(imgThresholded, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);;
//	        System.out.println("blob count: " + contours.size());
	        if (contours.size() >= 3) {
	        	List<Entry<Point, Double>> markers = new ArrayList<Entry<Point, Double>>();
	        	for (int i = 0; i < contours.size(); i++) {
	        		Moments moments = Imgproc.moments(contours.get(i));
	        		double dM01 = moments.m01;
	    	        double dM10 = moments.m10;
	    	        double dArea = moments.m00;
	    	        if (dArea > 50) {
	    	        	//calculate the position of the marker
	    	        	int posX = (int) (dM10 / dArea);
	    	        	int posY = (int) (dM01 / dArea);
	    	        	markers.add(new AbstractMap.SimpleEntry<Point,Double>(new Point(posX, posY), dArea));
	    	//        	System.out.println("BLOB " + i + ": x: " + posX + " - y: " + posY + " area: " + dArea);
	    	        } else {
	    	//        	System.out.println("area is only " + dArea);
	    	        }
	        	}
	        	double min = 999999998;
	        	int firstMarker = 0;  //top-left marker
	        	List<Point> actualMarkers = new ArrayList<Point>();
	        	for (int i = 0; i < markers.size(); i++) {
	        		double first = 0;
	        		double difference = 999999999;
	        		for (int j = 0; j < markers.size(); j++) {
	        			if (i != j) { 
	        				double ratio = distance(markers.get(i).getKey(), markers.get(j).getKey())/(markers.get(i).getValue());
	        				if (ratio < 0.2) {
	        					if (first == 0) {
	        						first = ratio;
	        					} else if (Math.abs(ratio - first) < difference){
	        						difference = Math.abs(ratio - first);
	        						actualMarkers.add(markers.get(i).getKey());
	        						break;
	        					}
	        				}
        				}
	        		}
	        		if (difference < min) {
	        			firstMarker = actualMarkers.size() - 1;
	        			min = difference;
	        		}
	        	}
	        	if (actualMarkers.size() == 3) {
//	        		System.out.println("FIRST: " + firstMarker);
//	        		Imgproc.circle(imgOriginal, actualMarkers.get(firstMarker), 2, new Scalar(0,255,0));
	        		double totalX = 0, totalY =0;
	        		for (Point p : actualMarkers) {
	        			totalX += p.x;
	        			totalY += p.y;
	        		}
	        		double avX = totalX / 3;
	        		double avY = totalY / 3;
		        	System.out.println("AVERAGE: x: " + avX + " - y: " + avY);
	        		Imgproc.circle(imgOriginal, new Point(avX, avY), 2, new Scalar(0,255,0),5);
	        	}
	        }
	        
	        //Calculate the moments of the thresholded image
	        Moments oMoments = Imgproc.moments(imgThresholded);
	
	        double dM01 = oMoments.m01;
	        double dM10 = oMoments.m10;
	        double dArea = oMoments.m00;
	
	        // if the area <= 10000, I consider that the there are no object in the image and it's because of the noise, the area is not zero 
	        if (dArea > 10000) {
	        	//calculate the position of the ball
	        	int posX = (int) (dM10 / dArea);
	        	int posY = (int) (dM01 / dArea);       
	        	System.out.println("OVERALL: x: " + posX + " - y: " + posY);
		        
	        	if (iLastX >= 0 && iLastY >= 0 && posX >= 0 && posY >= 0) {
	        		//Draw a red line from the previous point to the current point
//	        		Imgproc.line(imgLines, new Point(posX, posY), new Point(iLastX, iLastY), new Scalar(0,0,255), 2);
	        	}
	
	        	iLastX = posX;
	        	iLastY = posY;
	        }
        	thresholdFrame.render(imgThresholded);//show the thresholded image
      //  	Core.add(imgOriginal,imgLines, imgOriginal);
        	imgFrame.render(imgOriginal); //show the original image
	    }
	    return;
	}

	private static double distance(Point point, Point point2) {
		return Math.sqrt(Math.pow(point.x-point2.x, 2) + Math.pow(point.y-point2.y, 2));
	}
   
}