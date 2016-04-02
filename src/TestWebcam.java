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

		//get template
		Mat templ = Imgcodecs.imread("H.png");
		Imgproc.cvtColor(templ, templ, Imgproc.COLOR_RGB2GRAY);
		Imgproc.threshold(templ, templ, 200, 255, Imgproc.THRESH_BINARY);
		int match_method = Imgproc.TM_SQDIFF_NORMED;
		System.out.println("width is " + image.cols() + "  and height is " + image.rows());

		// Main loop
		while (true) {
		//	Thread.sleep(10); //remove this if using actual webcam
			// Read current camera frame into matrix
			cap.read(image);
			Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
			Imgproc.threshold(image, image, 200, 255, Imgproc.THRESH_BINARY);

			// / Create the result matrix
			int result_cols = image.cols() - templ.cols() + 1;
			int result_rows = image.rows() - templ.rows() + 1;
			Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

			//	findMatches(image, templ);
			Point matchLoc = new Point();
			double currentMax = -1;

			for (float i = 1; i < 2; i += 0.2) {
				Mat resized = new Mat();
				Imgproc.resize(templ, resized, new Size(), i, i, Imgproc.INTER_LINEAR);
				Imgproc.matchTemplate(image, resized, result, match_method);
				Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());

				// / Localizing the best match with minMaxLoc
				Core.MinMaxLocResult mmr = Core.minMaxLoc(result);

				if (/*match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED && */ mmr.maxVal > currentMax) {
					matchLoc = mmr.minLoc;
					currentMax = mmr.maxVal;
					System.out.println(mmr.maxVal);
				} /*else {
					matchLoc = mmr.maxLoc;
				}*/
			}

	//		System.out.println(matchLoc.x + " - " + matchLoc.y);
				// / Show me what you got
			Imgproc.rectangle(image, matchLoc, new Point(matchLoc.x + templ.cols(),
					matchLoc.y + templ.rows()), new Scalar(255, 255, 255));
			// Render frame if the camera is still acquiring images
			if (image.rows() > 0) {
				frame.render(image);
			} else {
				System.out.println("No captured frame -- camera disconnected");
				break;
			}
		}

	}

	private static void findMatches(Mat img_scene, Mat img_object) {

//		FeatureDetector detector = FeatureDetector.create(FeatureDetector.BRISK); //4 = SURF
//
		MatOfKeyPoint keypoints_object = new MatOfKeyPoint();
		MatOfKeyPoint keypoints_scene  = new MatOfKeyPoint();
//
//		detector.detect(img_object, keypoints_object);
//		detector.detect(img_scene, keypoints_scene);
//
//		DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.BRISK); //2 = SURF;
//
//		Mat descriptor_object = new Mat();
//		Mat descriptor_scene = new Mat() ;
//
//		extractor.compute(img_object, keypoints_object, descriptor_object);
//		extractor.compute(img_scene, keypoints_scene, descriptor_scene);
//
//		DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT); // 1 = FLANNBASED
//		MatOfDMatch matches = new MatOfDMatch();
//
//		matcher.match(descriptor_object, descriptor_scene, matches);
//		List<DMatch> matchesList = matches.toList();
//
//		Double max_dist = 0.0;
//		Double min_dist = 100.0;
//
//		for(int i = 0; i < descriptor_object.rows(); i++){
//			Double dist = (double) matchesList.get(i).distance;
//			if(dist < min_dist) min_dist = dist;
//			if(dist > max_dist) max_dist = dist;
//		}
//
//		System.out.println("-- Max dist : " + max_dist);
//		System.out.println("-- Min dist : " + min_dist);
//
//		LinkedList<DMatch> good_matches = new LinkedList<DMatch>();
//		MatOfDMatch gm = new MatOfDMatch();
//
//		for(int i = 0; i < descriptor_object.rows(); i++){
//			if(matchesList.get(i).distance < 3*min_dist){
//				good_matches.addLast(matchesList.get(i));
//			}
//		}
//
//		gm.fromList(good_matches);
//
//		Mat img_matches = new Mat();
//		Features2d.drawMatches(
//				img_object,
//				keypoints_object,
//				img_scene,
//				keypoints_scene,
//				gm,
//				img_matches,
//				new Scalar(255,0,0),
//				new Scalar(0,0,255),
//				new MatOfByte(),
//				2);
//
//		LinkedList<Point> objList = new LinkedList<Point>();
//		LinkedList<Point> sceneList = new LinkedList<Point>();
//
//		List<KeyPoint> keypoints_objectList = keypoints_object.toList();
//		List<KeyPoint> keypoints_sceneList = keypoints_scene.toList();
//
//		for(int i = 0; i<good_matches.size(); i++){
//			objList.addLast(keypoints_objectList.get(good_matches.get(i).queryIdx).pt);
//			sceneList.addLast(keypoints_sceneList.get(good_matches.get(i).trainIdx).pt);
//		}
//
//		MatOfPoint2f obj = new MatOfPoint2f();
//		obj.fromList(objList);
//
//		MatOfPoint2f scene = new MatOfPoint2f();
//		scene.fromList(sceneList);
//
//		Mat hg = Calib3d.findHomography(obj, scene);
//
//		Mat obj_corners = new Mat(4,1,CvType.CV_32FC2);
//		Mat scene_corners = new Mat(4,1,CvType.CV_32FC2);
//
//		obj_corners.put(0, 0, new double[] {0,0});
//		obj_corners.put(1, 0, new double[] {img_object.cols(),0});
//		obj_corners.put(2, 0, new double[] {img_object.cols(),img_object.rows()});
//		obj_corners.put(3, 0, new double[] {0,img_object.rows()});
//
//		System.out.println(obj_corners.size() + " - " + scene_corners.size() + " - " + hg.size());
//		if (hg.cols() == 0) {
//			return;
//		}
//		Core.perspectiveTransform(obj_corners,scene_corners, hg);
//
//		Imgproc.line(img_matches, new Point(scene_corners.get(0,0)), new Point(scene_corners.get(1,0)), new Scalar(255, 255, 255),4);
//		Imgproc.line(img_matches, new Point(scene_corners.get(1,0)), new Point(scene_corners.get(2,0)), new Scalar(255, 255, 255),4);
//		Imgproc.line(img_matches, new Point(scene_corners.get(2,0)), new Point(scene_corners.get(3,0)), new Scalar(255, 255, 255),4);
//		Imgproc.line(img_matches, new Point(scene_corners.get(3,0)), new Point(scene_corners.get(0,0)), new Scalar(255, 255, 255),4);



		long startTime = System.currentTimeMillis();
		FeatureDetector fd = FeatureDetector.create(FeatureDetector.BRISK);
		final MatOfKeyPoint keyPointsLarge = new MatOfKeyPoint();
		final MatOfKeyPoint keyPointsSmall = new MatOfKeyPoint();
		fd.detect(img_scene, keyPointsLarge);
		fd.detect(img_object, keyPointsSmall);
	//	System.out.println("feature detect time is: " + (System.currentTimeMillis() - startTime));
		Mat descriptorsLarge = new Mat();
		Mat descriptorsSmall = new Mat();
		startTime = System.currentTimeMillis();
		DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.BRISK);
		extractor.compute(img_scene, keyPointsLarge, descriptorsLarge);
		extractor.compute(img_object, keyPointsSmall, descriptorsSmall);
//		System.out.println("feature extract time is: " + (System.currentTimeMillis() - startTime));

	//	System.out.println("descriptorsA.size() : "+descriptorsLarge.size());
	//	System.out.println("descriptorsB.size() : "+descriptorsSmall.size());

		MatOfDMatch matches = new MatOfDMatch();
		startTime = System.currentTimeMillis();
		DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
		matcher.match(descriptorsLarge, descriptorsSmall, matches);
	//	System.out.println("feature match time is: " + (System.currentTimeMillis() - startTime));

	//	System.out.println("matches.size() : "+matches.size());

		MatOfDMatch matchesFiltered = new MatOfDMatch();

		List<DMatch> matchesList = matches.toList();
		List<DMatch> bestMatches= new ArrayList<DMatch>();

		Double max_dist = 0.0;
		Double min_dist = 100.0;

		for (int i = 0; i < matchesList.size(); i++)
		{
			Double dist = (double) matchesList.get(i).distance;

			if (dist < min_dist && dist != 0)
			{
				min_dist = dist;
			}

			if (dist > max_dist)
			{
				max_dist = dist;
			}

		}

	//	System.out.println("max_dist : "+max_dist);
	//	System.out.println("min_dist : "+min_dist);

		if(min_dist > 50 )
		{
			System.out.println("No match found");
	//		System.out.println("Just return ");
		//	return false;
		}

		double threshold = 3 * min_dist;
		double threshold2 = 2 * min_dist;

		if (threshold > 75)
		{
			threshold  = 75;
		}
		else if (threshold2 >= max_dist)
		{
			threshold = min_dist * 1.1;
		}
		else if (threshold >= max_dist)
		{
			threshold = threshold2 * 1.4;
		}

	//	System.out.println("Threshold : "+threshold);

		for (int i = 0; i < matchesList.size(); i++)
		{
			Double dist = (double) matchesList.get(i).distance;

			if (dist < threshold)
			{
				bestMatches.add(matches.toList().get(i));
				//System.out.println(String.format(i + " best match added : %s", dist));
			}
		}

		matchesFiltered.fromList(bestMatches);



		if(matchesFiltered.rows() >= 1)
		{
			System.out.println("match found");
			System.out.println("matchesFiltered.size() : " + matchesFiltered.size());


			LinkedList<DMatch> good_matches = new LinkedList<DMatch>();
			MatOfDMatch gm = new MatOfDMatch();

			for(int i = 0; i < descriptorsLarge.rows(); i++){
				if(matchesList.get(i).distance < 3*min_dist){
					good_matches.addLast(matchesList.get(i));
				}
			}

			gm.fromList(good_matches);

			Mat img_matches = new Mat();
			Features2d.drawMatches(
					img_object,
					keypoints_object,
					img_scene,
					keypoints_scene,
					gm,
					img_matches,
					new Scalar(255,0,0),
					new Scalar(0,0,255),
					new MatOfByte(),
					2);

			LinkedList<Point> objList = new LinkedList<Point>();
			LinkedList<Point> sceneList = new LinkedList<Point>();

			List<KeyPoint> keypoints_objectList = keypoints_object.toList();
			List<KeyPoint> keypoints_sceneList = keypoints_scene.toList();

			for(int i = 0; i<good_matches.size(); i++){
				objList.addLast(keypoints_objectList.get(good_matches.get(i).queryIdx).pt);
				sceneList.addLast(keypoints_sceneList.get(good_matches.get(i).trainIdx).pt);
			}

			MatOfPoint2f obj = new MatOfPoint2f();
			obj.fromList(objList);

			MatOfPoint2f scene = new MatOfPoint2f();
			scene.fromList(sceneList);

			Mat hg = Calib3d.findHomography(obj, scene);

			Mat obj_corners = new Mat(4,1,CvType.CV_32FC2);
			Mat scene_corners = new Mat(4,1,CvType.CV_32FC2);

			obj_corners.put(0, 0, new double[] {0,0});
			obj_corners.put(1, 0, new double[] {img_object.cols(),0});
			obj_corners.put(2, 0, new double[] {img_object.cols(),img_object.rows()});
			obj_corners.put(3, 0, new double[] {0,img_object.rows()});

			System.out.println(obj_corners.size() + " - " + scene_corners.size() + " - " + hg.size());
			if (hg.cols() == 0) {
				return;
			}
			Core.perspectiveTransform(obj_corners,scene_corners, hg);

			Imgproc.line(img_scene, new Point(scene_corners.get(0,0)), new Point(scene_corners.get(1,0)), new Scalar(255, 255, 255),4);
			Imgproc.line(img_scene, new Point(scene_corners.get(1,0)), new Point(scene_corners.get(2,0)), new Scalar(255, 255, 255),4);
			Imgproc.line(img_scene, new Point(scene_corners.get(2,0)), new Point(scene_corners.get(3,0)), new Scalar(255, 255, 255),4);
			Imgproc.line(img_scene, new Point(scene_corners.get(3,0)), new Point(scene_corners.get(0,0)), new Scalar(255, 255, 255),4);
			//return true;
		}
		else
		{
			//return false;
		}
	}
}