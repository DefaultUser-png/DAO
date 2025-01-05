package application;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;


public class Main extends Application {
	@Override
	public void start(Stage stage) throws Exception {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Scene.fxml"));
		Parent root = (Parent)fxmlLoader.load(); 
		Scene scene = new Scene(root);
		String css = this.getClass().getResource("Scene.css").toExternalForm();
		scene.getStylesheets().add(css);
		stage.setTitle("DAO");
		stage.setScene(scene);
		stage.setMinWidth(640);
		stage.setMinHeight(480);
		stage.show();

		Parameters params = getParameters();
		if(params == null) return;

		// Import CMD arguments
		List<String> args = params.getRaw();
		List<File> files = new ArrayList<>();
		for(int i=0; i<args.size(); i++) {
			File f = new File(args.get(i));
			if(!f.exists()) continue;
			files.add(f);
		}

		Controller controller = fxmlLoader.<Controller>getController();
		controller.setArgs_list(files);
	}

	public static void main(String[] args) {
		launch(args);
	}
}
