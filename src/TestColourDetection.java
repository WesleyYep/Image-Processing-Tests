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
    
    public static void setupSlider(RangeSlider slider, int majorSpacing, MyFrame frame) {
    	slider.setMajorTickSpacing(majorSpacing);
    	slider.setMinorTickSpacing(10);
    	slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setValue(120);
        slider.setUpperValue(255);
        frame.add(slider);
    }
	
    public static void main ( String[] argv) {
	    VideoCapture cap = new VideoCapture(0); //capture the video from webcam
	    MyFrame frame = new MyFrame();
        frame.setVisible(true);
        MyFrame frame2 = new MyFrame();
        frame2.setVisible(true);
        RangeSlider hSlider = new RangeSlider(0,180);
        RangeSlider sSlider = new RangeSlider(0,255);
        RangeSlider vSlider = new RangeSlider(0,255);
        setupSlider(hSlider, 60, frame);
        setupSlider(sSlider, 50, frame);
        setupSlider(vSlider, 50, frame);
        hSlider.setValue(0);
        hSlider.setUpperValue(5);

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
	        MatOfKeyPoint keyPoints = new MatOfKeyPoint();
	        FeatureDetector blobDetector = FeatureDetector.create(FeatureDetector.SIMPLEBLOB);
	        blobDetector.detect(imgThresholded, keyPoints);
	        System.out.println("blob count: " + keyPoints.size());
	        
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
	        	System.out.println("x: " + posX + " - y: " + posY);
		        
	        	if (iLastX >= 0 && iLastY >= 0 && posX >= 0 && posY >= 0) {
	        		//Draw a red line from the previous point to the current point
	        		Imgproc.line(imgLines, new Point(posX, posY), new Point(iLastX, iLastY), new Scalar(0,0,255), 2);
	        	}
	
	        	iLastX = posX;
	        	iLastY = posY;
	        }
        	frame.render(imgThresholded);//show the thresholded image
        	Core.add(imgOriginal,imgLines, imgOriginal);
        	frame2.render(imgOriginal); //show the original image
	    }
	    return;
	}
   
}