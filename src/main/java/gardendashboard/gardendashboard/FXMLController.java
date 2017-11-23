package gardendashboard.gardendashboard;

import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;
import helpers.PubnubConfig;
import helpers.Utils;
import java.awt.Color;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

public class FXMLController implements Initializable {
    
    PubnubConfig pubnubConfig;
    PubNub pubnub;
    
    private Label label;
    @FXML
    private Button btn_up;
    @FXML
    private Button btn_left;
    @FXML
    private Button btn_down;
    @FXML
    private Button btn_right;
    @FXML
    private Label activity_label;
    
    //publishing channels
    String direction = "direction"; 
    @FXML
    private Button btn_start;
    @FXML
    private ImageView currentFrame;
    
    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;

    // the OpenCV object that realizes the video capture
private VideoCapture capture = new VideoCapture();

// a flag to change the button behavior
private boolean cameraActive = false;

// the id of the camera to be used
private static int cameraId = 0;
        
        //static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
    
String currentDir = new File("").getAbsolutePath();
String dirHaar = new File("").getAbsolutePath();
String dirLip = new File("").getAbsolutePath();

@FXML
private ImageView histogram;

@FXML
private CheckBox grayscale;

@FXML
private CheckBox haarClassifier;

@FXML
private CheckBox lbpClassifier;
    
// face cascade classifier
private CascadeClassifier faceCascade;
private int absoluteFaceSize;
    @FXML
    private Button connect_btn;
    @FXML
    private Label connect_label;
    
    
    

    //pubnub channels
    private final String operation = "operation";
    private final String feedbacks = "feedbacks";
    private final String a_temp = "a_temp";
    private final String s_temp = "s_temp";
    private final String humdity = "humdity";
    private final String soil_moisture = "soil_moisture";

    PubConSubThread pubConSubThread;// new PubConSubThread();
    PubPublishThread pubPublishThread;// new PubPublishThread();

    // message and channel that will be used during publishing
    String payload;
    String channel;
    @FXML
    private Label s_temp_label;
    @FXML
    private Label humdity_label;
    @FXML
    private Label soil_moisture_label;
    @FXML
    private Label a_temp_label;
    @FXML
    private Label status_label;
    @FXML
    private ToggleButton arm_btn;
    @FXML
    private ToggleButton water_btn;
    @FXML
    private ToggleButton torch_btn;
	
	/**
	 * Init the controller, at start time
	 */
	protected void init()
	{
		
	}
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.load(currentDir + "\\opencv\\x64\\opencv_java331.dll");
        
        
		this.faceCascade = new CascadeClassifier();
		this.absoluteFaceSize = 0;
		
		// set a fixed width for the frame
		currentFrame.setFitWidth(600);
		// preserve image ratio
		currentFrame.setPreserveRatio(true);
        
        pubnubConfig = new PubnubConfig();
        pubnub = new PubNub(pubnubConfig.pConfig());
        
       
       
    }    

    @FXML
    private void btn_up_action(ActionEvent event) {
        
        this.channel = this.direction;
        this.payload = "up";
        this.pubPublishThread.pubnubPublish(payload, channel);
       
         
    }
    
    @FXML
    private void btn_left_action(ActionEvent event) {
       this.channel = this.direction;
        this.payload = "left";
        this.pubPublishThread.pubnubPublish(payload, channel);
    }

    @FXML
    private void btn_down_action(ActionEvent event) {
        this.channel = this.direction;
        this.payload = "down";
        this.pubPublishThread.pubnubPublish(payload, channel);
    }

    @FXML
    private void btn_right_action(ActionEvent event) {
        
        this.channel = this.direction;
        this.payload = "right";
        this.pubPublishThread.pubnubPublish(payload, channel);
				
        
        
        
        
    }





    @FXML
    private void btn_start_action(ActionEvent event) {
        
        if (!this.cameraActive)
		{
                    
                    // disable setting checkboxes
			this.haarClassifier.setDisable(true);
			this.lbpClassifier.setDisable(true);
                        
			// start the video capture
			this.capture.open(cameraId);
			
			// is the video stream available?
			if (this.capture.isOpened())
			{
				this.cameraActive = true;
				
				// grab a frame every 33 ms (30 frames/sec)
				Runnable frameGrabber = new Runnable() {
					
					@Override
					public void run()
					{
						// effectively grab and process a single frame
						Mat frame = grabFrame();
						// convert and show the frame
						Image imageToShow = Utils.mat2Image(frame);
						updateImageView(currentFrame, imageToShow);
					}
				};
				
				this.timer = Executors.newSingleThreadScheduledExecutor();
				this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
				
				// update the button content
				this.btn_start.setText("Stop Camera");
			}
			else
			{
				// log the error
				System.err.println("Impossible to open the camera connection...");
			}
		}
		else
		{
			// the camera is not active at this point
			this.cameraActive = false;
			// update again the button content
			this.btn_start.setText("Start Camera");
                        
                        // enable classifiers checkboxes
			this.haarClassifier.setDisable(false);
			this.lbpClassifier.setDisable(false);
			
			// stop the timer
			this.stopAcquisition();
		}
    }
    
    
    
    /**
	 * Get a frame from the opened video stream (if any)
	 *
	 * @return the {@link Mat} to show
	 */
	private Mat grabFrame()
	{
		// init everything
		Mat frame = new Mat();
		
		// check if the capture is open
		if (this.capture.isOpened())
		{
			try
			{
				// read the current frame
				this.capture.read(frame);
				
				// if the frame is not empty, process it
				if (!frame.empty() && grayscale.isSelected())
				{
					Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2RGB);
                                        // face detection
					this.detectAndDisplay(frame);
                                        
				}
                               
                                
					// show the histogram
					this.showHistogram(frame, grayscale.isSelected());
				
			}
			catch (Exception e)
			{
				// log the error
				System.err.println("Exception during the image elaboration: " + e);
			}
		}
		
		return frame;
	}
    
    /**
	 * Stop the acquisition from the camera and release all the resources
	 */
	private void stopAcquisition()
	{
		if (this.timer!=null && !this.timer.isShutdown())
		{
			try
			{
				// stop the timer
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e)
			{
				// log any exception
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
		}
		
		if (this.capture.isOpened())
		{
			// release the camera
			this.capture.release();
		}
	}
    
    /**
	 * Update the {@link ImageView} in the JavaFX main thread
	 * 
	 * @param view
	 *            the {@link ImageView} to update
	 * @param image
	 *            the {@link Image} to show
	 */
	private void updateImageView(ImageView view, Image image)
	{
		Utils.onFXThread(view.imageProperty(), image);
	}
        
        
	
	/**
	 * On application close, stop the acquisition from the camera
	 */
	protected void setClosed()
	{
		this.stopAcquisition();
	}
        
        
        
        /**
	 * Compute and show the histogram for the given {@link Mat} image
	 * 
	 * @param frame
	 *            the {@link Mat} image for which compute the histogram
	 * @param gray
	 *            is a grayscale image?
	 */
	private void showHistogram(Mat frame, boolean gray)
	{
		// split the frames in multiple images
		List<Mat> images = new ArrayList<Mat>();
		Core.split(frame, images);
		
		// set the number of bins at 256
		MatOfInt histSize = new MatOfInt(256);
		// only one channel
		MatOfInt channels = new MatOfInt(0);
		// set the ranges
		MatOfFloat histRange = new MatOfFloat(0, 256);
		
		// compute the histograms for the B, G and R components
		Mat hist_b = new Mat();
		Mat hist_g = new Mat();
		Mat hist_r = new Mat();
		
		// B component or gray image
		Imgproc.calcHist(images.subList(0, 1), channels, new Mat(), hist_b, histSize, histRange, false);
		
		// G and R components (if the image is not in gray scale)
		if (!gray)
		{
			Imgproc.calcHist(images.subList(1, 2), channels, new Mat(), hist_g, histSize, histRange, false);
			Imgproc.calcHist(images.subList(2, 3), channels, new Mat(), hist_r, histSize, histRange, false);
		}
		
		// draw the histogram
		int hist_w = 150; // width of the histogram image
		int hist_h = 150; // height of the histogram image
		int bin_w = (int) Math.round(hist_w / histSize.get(0, 0)[0]);
		
		Mat histImage = new Mat(hist_h, hist_w, CvType.CV_8UC3, new Scalar(0, 0, 0));
		// normalize the result to [0, histImage.rows()]
		Core.normalize(hist_b, hist_b, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
		
		// for G and R components
		if (!gray)
		{
			Core.normalize(hist_g, hist_g, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
			Core.normalize(hist_r, hist_r, 0, histImage.rows(), Core.NORM_MINMAX, -1, new Mat());
		}
		
		// effectively draw the histogram(s)
		for (int i = 1; i < histSize.get(0, 0)[0]; i++)
		{
			// B component or gray image
			Imgproc.line(histImage, new Point(bin_w * (i - 1), hist_h - Math.round(hist_b.get(i - 1, 0)[0])),
					new Point(bin_w * (i), hist_h - Math.round(hist_b.get(i, 0)[0])), new Scalar(255, 0, 0), 2, 8, 0);
			// G and R components (if the image is not in gray scale)
			if (!gray)
			{
				Imgproc.line(histImage, new Point(bin_w * (i - 1), hist_h - Math.round(hist_g.get(i - 1, 0)[0])),
						new Point(bin_w * (i), hist_h - Math.round(hist_g.get(i, 0)[0])), new Scalar(0, 255, 0), 2, 8,
						0);
				Imgproc.line(histImage, new Point(bin_w * (i - 1), hist_h - Math.round(hist_r.get(i - 1, 0)[0])),
						new Point(bin_w * (i), hist_h - Math.round(hist_r.get(i, 0)[0])), new Scalar(0, 0, 255), 2, 8,
						0);
			}
		}
		
		// display the histogram...
		Image histImg = Utils.mat2Image(histImage);
		updateImageView(histogram, histImg);
		
	}

    @FXML
    private void haarSelected(ActionEvent event) {
        // check whether the lpb checkbox is selected and deselect it
		if (this.lbpClassifier.isSelected())
			this.lbpClassifier.setSelected(false);
			
		this.checkboxSelection(dirHaar + "\\classifiers\\haarcascades\\haarcascade_frontalface_alt.xml");
    }

    @FXML
    private void lbpSelected(ActionEvent event) {
        // check whether the haar checkbox is selected and deselect it
		if (this.haarClassifier.isSelected())
			this.haarClassifier.setSelected(false);
			
		this.checkboxSelection(dirLip + "\\classifiers\\lbpcascades\\lbpcascade_frontalface.xml");
    }
    
    /**
	 * Method for face detection and tracking
	 * 
	 * @param frame
	 *            it looks for faces in this frame
	 */
	private void detectAndDisplay(Mat frame)
	{
		MatOfRect faces = new MatOfRect();
		Mat grayFrame = new Mat();
		
		// convert the frame in gray scale
		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
		// equalize the frame histogram to improve the result
		Imgproc.equalizeHist(grayFrame, grayFrame);
		
		// compute minimum face size (20% of the frame height, in our case)
		if (this.absoluteFaceSize == 0)
		{
			int height = grayFrame.rows();
			if (Math.round(height * 0.2f) > 0)
			{
				this.absoluteFaceSize = Math.round(height * 0.2f);
			}
		}
		
		// detect faces
		this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE,
				new Size(this.absoluteFaceSize, this.absoluteFaceSize), new Size());
				
		// each rectangle in faces is a face: draw them!
		Rect[] facesArray = faces.toArray();
		for (int i = 0; i < facesArray.length; i++)
			Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0), 3);
			
	}
        
        /**
	 * Method for loading a classifier trained set from disk
	 * 
	 * @param classifierPath
	 *            the path on disk where a classifier trained set is located
	 */
	private void checkboxSelection(String classifierPath)
	{
		// load the classifier(s)
		this.faceCascade.load(classifierPath);
		
		// now the video capture can start
		this.btn_start.setDisable(false);
	}

    @FXML
    private void connect_btn_action(ActionEvent event) {
        

        pubPublishThread = new PubPublishThread();
        pubConSubThread = new PubConSubThread();
        
        


        try {

            pubConSubThread.start();
            pubPublishThread.start();
        }catch (Exception e){
            connect_label.setText(e.getMessage());
            
        }

    }
    
    public void updateConnectLabel(String col, String cStatus){
        this.connect_label.setStyle(col);
        this.connect_label.setText(cStatus);
    }
    
    public void updateStatusLabel(String col, String publishStatus){
        this.status_label.setStyle(col);
        this.status_label.setText(publishStatus);
    }
    
    public void updateActivityLabel(String message ){
        activity_label.setText(message);
    }
    
    public void updateAmbTempLabel(String message ){
        a_temp_label.setText(message);
    }
    
    public void updateSoilTempLabel(String message ){
        s_temp_label.setText(message);
    }
    
    public void updateHumdityLabel(String message ){
        humdity_label.setText(message);
    }
    
    public void updateSoilMositureLabel(String message ){
        soil_moisture_label.setText(message);
    }
    
    public void updateArmBtnText(String text){
        arm_btn.setText(text);
    }
    
    public void updateWaterBtnText(String text){
        water_btn.setText(text);
    }
    
    public void updateTorchBtnText(String text){
        torch_btn.setText(text);
    }

    @FXML
    private void arm_btn_action(ActionEvent event) {
        if(arm_btn.isSelected()){
        this.channel = this.operation;
        this.payload = "arm";
        this.pubPublishThread.pubnubPublish(payload, channel);
        }else{
            this.channel = this.operation;
        this.payload = "disarm";
        this.pubPublishThread.pubnubPublish(payload, channel);
        }
    }

    @FXML
    private void water_btn_action(ActionEvent event) {
         if(water_btn.isSelected()){
        this.channel = this.operation;
        this.payload = "water on";
        this.pubPublishThread.pubnubPublish(payload, channel);
        }else{
            this.channel = this.operation;
        this.payload = "water off";
        this.pubPublishThread.pubnubPublish(payload, channel);
        }
    }

    @FXML
    private void torch_btn_action(ActionEvent event) {
         if(torch_btn.isSelected()){
        this.channel = this.operation;
        this.payload = "torch on";
        this.pubPublishThread.pubnubPublish(payload, channel);
        }else{
            this.channel = this.operation;
        this.payload = "torch off";
        this.pubPublishThread.pubnubPublish(payload, channel);
        }
    }
        
        
        /**
     * Created by General Steinacoz on 11/21/2017.
     */
    public class PubConSubThread extends Thread {
        public static final String Tag = "Pubnub Connect Subscribe Thread";
        

        
        @Override
        public void run() {
            try {
                pubnubConSubscribe();
            }catch (Exception e){
                final String err = e.getMessage();
                
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        connectProgress("color: #d31313", "Error in Connection");
                    }
                    
                });
                
               
            }
        }

        //pubnub connect and subscribe
        private void pubnubConSubscribe(){

            connectProgress("color: #d31313", "Connecting");

            

            pubnub.addListener(new SubscribeCallback() {
                @Override
                public void status(PubNub pubnub, PNStatus status) {
                   

                    if (status.getCategory() == PNStatusCategory.PNUnexpectedDisconnectCategory){
                        connectProgress("color: #d31313", "Disconnected");
                        pubnub.reconnect();
                    }else if (status.getCategory() == PNStatusCategory.PNConnectedCategory){
                        if (status.getCategory() == PNStatusCategory.PNConnectedCategory){
                            connectProgress("color: #3dd612", "Connected");
                            pubnub.subscribe().channels(Arrays.asList(direction)).execute();
                        }else if (status.getCategory() == PNStatusCategory.PNReconnectedCategory){

                            connectProgress("color: #3dd612", "Reconnected");
                            pubnub.subscribe().channels(Arrays.asList(direction)).execute();
                        }else if (status.getCategory() == PNStatusCategory.PNDecryptionErrorCategory){
                            pubnub.reconnect();
                        }else if (status.getCategory() == PNStatusCategory.PNTimeoutCategory){
                            pubnub.reconnect();
                        }else {
                            pubnub.reconnect();

                            connectProgress("d31313", "No Connection");
                        }
                    }
                }

                @Override
                public void message(PubNub pubnub, final PNMessageResult message) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            if (message.getChannel().equalsIgnoreCase(feedbacks) || message.getChannel().equalsIgnoreCase(operation) || message.getChannel().equalsIgnoreCase(direction)){
                                
                                updateActivityLabel(message.getMessage().getAsString() );
                            }else if (message.getChannel().equalsIgnoreCase(a_temp)){
                               updateAmbTempLabel(message.getMessage().getAsString());
                            }else if (message.getChannel().equalsIgnoreCase(s_temp)){
                               updateSoilTempLabel(message.getMessage().getAsString());
                            }else if (message.getChannel().equalsIgnoreCase(humdity)){
                                updateHumdityLabel(message.getMessage().getAsString());
                            }else if (message.getChannel().equalsIgnoreCase(soil_moisture)){
                                updateSoilMositureLabel(message.getMessage().getAsString());
                            }
                            
                            
                            if(message.getChannel().equalsIgnoreCase(feedbacks) && message.getMessage().getAsString().equalsIgnoreCase("security armed")){
                                   Platform.runLater(new Runnable(){
                                       @Override
                                       public void run() {
                                          updateArmBtnText("Armed");
                                       }
                                       
                                   });
                            }else if(message.getChannel().equalsIgnoreCase(feedbacks) && message.getMessage().getAsString().equalsIgnoreCase("security disarmed")){
                                   Platform.runLater(new Runnable(){
                                       @Override
                                       public void run() {
                                          updateArmBtnText("Disarmed");
                                       }
                                       
                                   });
                            }else if(message.getChannel().equalsIgnoreCase(feedbacks) && message.getMessage().getAsString().equalsIgnoreCase("water switched on")){
                                Platform.runLater(new Runnable(){
                                       @Override
                                       public void run() {
                                          updateWaterBtnText("water on");
                                       }
                                       
                                   });
                            }else if(message.getChannel().equalsIgnoreCase(feedbacks) && message.getMessage().getAsString().equalsIgnoreCase("water switched off")){
                                Platform.runLater(new Runnable(){
                                       @Override
                                       public void run() {
                                          updateWaterBtnText("Water off");
                                       }
                                       
                                   });
                            }else if(message.getChannel().equalsIgnoreCase(feedbacks) && message.getMessage().getAsString().equalsIgnoreCase("torch switched on")){
                                Platform.runLater(new Runnable(){
                                       @Override
                                       public void run() {
                                          updateTorchBtnText("Torch on");
                                       }
                                       
                                   });
                            }else if(message.getChannel().equalsIgnoreCase(feedbacks) && message.getMessage().getAsString().equalsIgnoreCase("torch switched off")){
                                Platform.runLater(new Runnable(){
                                       @Override
                                       public void run() {
                                          updateTorchBtnText("Torch off");
                                       }
                                       
                                   });
                            }
                        }
                        
                    });
                    
                   
                }

                @Override
                public void presence(PubNub pubnub, PNPresenceEventResult presence) {

                }
            });

            pubnub.subscribe().channels(Arrays.asList(direction, operation, feedbacks, a_temp, s_temp, soil_moisture, humdity )).execute();
        }

        private void connectProgress(String colour, String connectStatus){
            final String col = colour;
            final String cStatus = connectStatus;
            
            Platform.runLater(new Runnable(){
                @Override
                public void run() {
                    updateConnectLabel(col, cStatus);
                }
                
            });
           

        }


    }



    /**
     * Created by General Steinacoz on 11/21/2017.
     */
    public class PubPublishThread extends Thread {
        public static final String Tag = "Pubnub Publish Thread";
        

        


        @Override
        public void run() {
            super.run();

        }



        public void pubnubPublish(String payload, String channel){

            try {
                pubnub.publish().message(payload).channel(channel).async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        if (!status.isError()) {
                            publishProgress("#3dd612", "publish success");
                        } else {
                            publishProgress("#d31313", status.getCategory().toString());
                            status.retry();
                        }

                    }
                });
            }catch (Exception e){
               final String err = e.getMessage();
               Platform.runLater(new Runnable(){
                   @Override
                   public void run() {
                       publishProgress("#d31313", err);
                   }
                   
               });
               
            }
        }

        private void publishProgress(final String colour, final String publishStatus){
            Platform.runLater(new Runnable(){
                   @Override
                   public void run() {
                       try{
                       updateStatusLabel(colour, publishStatus);
                       }catch(Exception e){
                          updateStatusLabel(colour, e.getMessage()); 
                       }
                   }
                   
               });
            
            
        }
    }



        
        
}
	




