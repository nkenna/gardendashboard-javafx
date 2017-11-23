package gardendashboard.gardendashboard;

import java.io.File;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.opencv.core.Core;


public class MainApp extends Application {
    
    String currentDir = new File("").getAbsolutePath();
    
    
    //static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    @Override
    public void start(Stage stage) throws Exception {
        System.load(currentDir + "\\opencv\\x64\\opencv_java331.dll");
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/DashboardFXML.fxml"));
        
        Scene scene = new Scene(root);
        scene.getStylesheets().add("/styles/Styles.css");
        
        stage.setTitle("JavaFX and Maven");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
