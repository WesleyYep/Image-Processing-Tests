import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TestWebcam {

	static {
		// Load the native OpenCV library
		//System.out.println(System.getProperty("java.library.path"));
		System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
	}

	public static void main(String[] args) throws InterruptedException {
		// Register the default camera
		VideoCapture cap = new VideoCapture();
		cap.open("lol.mp4");
		cap.set(Videoio.CAP_PROP_FRAME_COUNT, 2);

		// Check if video capturing is enabled
		if (!cap.isOpened()) {
			System.out.println("camera failed to open");
			System.exit(-1);
		}

		// Matrix for storing image
		Mat image = new Mat();
		// Frame for displaying image
		MyFrame frame = new MyFrame();
		frame.setVisible(true);
		double count = 0;
		long startTime = System.currentTimeMillis();

		// Main loop
		try {
			while (true) {
				//	Thread.sleep(10); //remove this if using actual webcam
				// Read current camera frame into matrix
				cap.read(image);

				//templateMatching(image);
				//featureDetect(image);
				//blobDetect(image);
				contourDetect(image);

				// Render frame if the camera is still acquiring images
				if (image.rows() > 0) {
					frame.render(image);
				} else {
					System.out.println("No captured frame -- camera disconnected");
					break;
				}
				count++;
			}
		}catch (Exception e) {}
		long endTime = System.currentTimeMillis();
		System.out.println("frames: " + count);
		System.out.println("time was: " + (endTime-startTime));
		System.out.println("fps was: " + count/((endTime-startTime)/1000.0));
	}

	private static void contourDetect(Mat image) {
		Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		MatOfInt4 hierarchy = new MatOfInt4();
		Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE); // Find contours with hierarchy
	}

	private static void blobDetect(Mat image) {
		MatOfKeyPoint matOfKeyPoints = new MatOfKeyPoint();
		FeatureDetector blobDetector = FeatureDetector.create(FeatureDetector.SIMPLEBLOB);
		blobDetector.detect(image, matOfKeyPoints);
	}

	private static void featureDetect(Mat objectImage) {
		Mat sceneImage = Imgcodecs.imread("H.png");

		MatOfKeyPoint objectKeyPoints = new MatOfKeyPoint();
		FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.BRISK);
		featureDetector.detect(objectImage, objectKeyPoints);

		MatOfKeyPoint objectDescriptors = new MatOfKeyPoint();
		DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.BRISK);
		descriptorExtractor.compute(objectImage, objectKeyPoints, objectDescriptors);

		// Match object image with the scene image
		MatOfKeyPoint sceneKeyPoints = new MatOfKeyPoint();
		MatOfKeyPoint sceneDescriptors = new MatOfKeyPoint();
		featureDetector.detect(sceneImage, sceneKeyPoints);
		descriptorExtractor.compute(sceneImage, sceneKeyPoints, sceneDescriptors);
	}

	private static void templateMatching(Mat image) {
		Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
		Imgproc.threshold(image, image, 200, 255, Imgproc.THRESH_BINARY);
		//get template
		Mat templ = Imgcodecs.imread("H.png");
		Imgproc.cvtColor(templ, templ, Imgproc.COLOR_RGB2GRAY);
		Imgproc.threshold(templ, templ, 200, 255, Imgproc.THRESH_BINARY);
		int match_method = Imgproc.TM_SQDIFF_NORMED;
		// / Create the result matrix
		int result_cols = image.cols() - templ.cols() + 1;
		int result_rows = image.rows() - templ.rows() + 1;
		Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);
		Imgproc.matchTemplate(image, templ, result, match_method);
	}

}