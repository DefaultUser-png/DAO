package application;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import mslinks.ShellLinkException;
import mslinks.ShellLinkHelper;

public class Controller implements Initializable{
	@FXML
	private Button btnFileFind;

	@FXML
	private Button btnAdd;

	@FXML
	private Button btnRemove;

	@FXML
	private Button btnRename;

	@FXML
	private Button btnRestore;

	@FXML
	private CheckBox checkBackup;

	@FXML
	private CheckBox checkFolders;

	@FXML
	private CheckBox checkSubFolders;

	@FXML
	private CheckBox checkReverse;

	@FXML
	private ComboBox<apply_mode> comboApply;

	@FXML
	private ComboBox<log> comboLogging;

	@FXML
	private ComboBox<sort> comboSort;

	@FXML
	private TextField fieldPath;

	@FXML
	private TextField fieldTo;

	@FXML
	private TableView<List<String>> table;

	@FXML
	private TableColumn<List<String>, String> fileFrom;

	@FXML
	private TableColumn<List<String>, String> fileTo;

	@FXML
	private Tab tab1;

	@FXML
	private Tab tab2;

	@FXML
	private Tab tab3;

	@FXML
	private TabPane tabPane;


	private RenameEngine rOBJ = new RenameEngine();
	private boolean include_folders = true;
	private boolean include_subfolders = false;
	private String delimeter = "[$]";
	private List<File> args_list;
	private String shortcut_path = System.getenv("APPDATA") + "\\Microsoft\\Windows\\SendTo";
	private String shortcut_name = "DAO.lnk";
	private String exec_path = System.getProperty("user.dir");



	// Creates a file choosing window
	@FXML
	void open_file(ActionEvent event) {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Open File");
		List<File> list = chooser.showOpenMultipleDialog(new Stage());
		if (list == null || list.isEmpty()) {
			return;
		}

		proceed(list);
	}

	// Opens files with the bar
	@FXML
	void open_path(ActionEvent event) {
		String s = fieldPath.getText();
		if(s.isEmpty()) {
			Alert alert = new Alert(AlertType.WARNING, "Enter a valid path!", ButtonType.OK);
			alert.setHeaderText(null);
			alert.show();
			return;
		};

		List<File> files = new ArrayList<>();
		String[] tokens = s.split(delimeter);
		for(String path : tokens) {
			File f = new File(path);
			if(!f.exists()) continue;
			files.add(f);
		}
		proceed(files);
	}



	// SendTo menu
	@FXML
	void add_to_menu(ActionEvent event) {
		String f_path = exec_path + "\\run.bat";
		if(!new File(f_path).exists()) {
			Alert alert = new Alert(AlertType.ERROR, "Failed to add program to the context menu!\n\"run.bat\" isn't found!", ButtonType.OK);
			alert.setHeaderText(null);
			alert.show();
			return;
		};

		try {
			ShellLinkHelper.createLink(f_path, shortcut_path + "\\" + shortcut_name);
			Alert alert = new Alert(AlertType.INFORMATION, "Added program to the context menu.\n(Accessed through \"Right Click > Send To > DAO\")", ButtonType.OK);
			alert.setHeaderText(null);
			alert.show();
		} catch (IOException | ShellLinkException e) {
			Alert alert = new Alert(AlertType.ERROR, "Failed to add program to the context menu!", ButtonType.OK);
			alert.setHeaderText(null);
			alert.show();
			e.printStackTrace();
		}
	}

	@FXML
	void remove_from_menu(ActionEvent event) {
		File shortcut = new File(shortcut_path + "\\" + shortcut_name);
		Alert alert = new Alert(AlertType.INFORMATION, "Program has been removed from the context menu.", ButtonType.OK);
		alert.setHeaderText(null);
		alert.show();
		if(!shortcut.exists()) return;
		shortcut.delete();
	}



	// Misc listeners
	@FXML
	void name_changed(ActionEvent event) {
		rOBJ.name = fieldTo.getText();
		table.setItems(rOBJ.table_data());
	}

	@FXML
	void rename(ActionEvent event) {
		rOBJ.renameAll();
		table.setItems(rOBJ.table_data());
		btnRestore.setDisable(!checkBackup.isSelected() || rOBJ.filesBuffer.isEmpty());
	}

	@FXML
	void restore_backup(ActionEvent event) {
		rOBJ.undo();
		table.setItems(rOBJ.table_data());
		btnRestore.setDisable(true);
	}

	@FXML
	void checkFolders_listener(ActionEvent event) {
		include_folders = checkFolders.isSelected();
		checkSubFolders.setDisable(!include_folders);
	}

	@FXML
	void checkSubFolders_listener(ActionEvent event) {
		include_subfolders = checkSubFolders.isSelected();
	}

	@FXML
	void comboSort_listener(ActionEvent event) {
		rOBJ.sortBy = comboSort.getValue();
		rOBJ.sort();
		table.setItems(rOBJ.table_data());
	}

	@FXML
	void checkReverse_listener(ActionEvent event) {
		rOBJ.sortReverse = checkReverse.isSelected();
		rOBJ.sort();
		table.setItems(rOBJ.table_data());
	}

	@FXML
	void comboApply_listener(ActionEvent event) {
		rOBJ.mode = comboApply.getValue();
		table.setItems(rOBJ.table_data());
	}

	@FXML
	void comboLogging_listener(ActionEvent event) {
		rOBJ.logging = comboLogging.getValue();
	}

	@FXML
	void checkBackup_listener(ActionEvent event) {
		rOBJ.backup = checkBackup.isSelected();
		btnRestore.setDisable(!checkBackup.isSelected() || rOBJ.filesBuffer.isEmpty());
	}




	/*
		God knows I belong to hell
		That's why he left me here by myself
	 */
	@Override
	public void initialize(URL url, ResourceBundle resourceBundle) {
		// Table init
		fileFrom.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(0)));
		fileTo.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(1)));
		table.setItems(rOBJ.table_data());

		checkFolders.setSelected(include_folders);
		checkSubFolders.setDisable(!include_folders);
		checkSubFolders.setSelected(include_subfolders);
		btnRestore.setDisable(!checkBackup.isSelected() || rOBJ.filesBuffer.isEmpty());

		tab2.setDisable(true);
		tab3.setDisable(true);

		comboApply.getItems().removeAll(comboApply.getItems());
		comboApply.getItems().setAll(apply_mode.values());
		comboApply.getSelectionModel().select(rOBJ.mode);

		comboSort.getItems().removeAll(comboSort.getItems());
		comboSort.getItems().setAll(sort.values());
		comboSort.getSelectionModel().select(rOBJ.sortBy);

		comboLogging.getItems().removeAll(comboLogging.getItems());
		comboLogging.getItems().setAll(log.values());
		comboLogging.getSelectionModel().select(rOBJ.logging);

		if(rOBJ.loggingPath == null || rOBJ.loggingPath.isEmpty()) {
			rOBJ.loggingPath = exec_path + "\\logs\\";
		}

		Platform.runLater(() -> {
			if(args_list == null || args_list.isEmpty() ) return;
			/*
			for(File f : args_list) {
				System.out.println(f.getAbsolutePath());
			}
			 */
			proceed(args_list);
		});
	}



	// Feeds files into the Rename class
	void proceed(List<File> list) {
		List<File> all_files = scan_files(list, include_folders, include_subfolders);
		if(all_files == null || all_files.isEmpty()) {
			Alert alert = new Alert(AlertType.WARNING, "Enter a valid path!", ButtonType.OK);
			alert.setHeaderText(null);
			alert.show();
			return;
		};

		rOBJ.wrap(all_files);

		table.setItems(rOBJ.table_data());
		tab2.setDisable(false);
		tab3.setDisable(false);
		tabPane.getSelectionModel().select(1);
	}
	
	// Looks for files within folders
	List<File> scan_files(List<File> list, boolean folders, boolean subfolders) {
		if(list == null || list.isEmpty()) return null;

		// Blacklist when sub-folders are not wanted
		List<String> blacklist = new ArrayList<>();

		for(int i = 0; i < list.size(); i++) {
			File f = list.get(i);
			// File doesn't exist; remove
			if(!f.exists()) {
				list.remove(f); i--;
				continue;
			}
			// Is unwanted folder; remove
			if(!folders && f.isDirectory()) {
				list.remove(f); i--;
				continue;
			}

			// Is a file; keep
			if(f.isFile()) continue;
			// Is unwanted sub-folder; remove
			if(!subfolders) {
				if(blacklist_search(blacklist, f.toString())) {
					list.remove(f); i--;
					continue;
				}
			}

			File[] folder_files = f.listFiles();
			// Folder has no files; remove
			if(folder_files == null || folder_files.length == 0) {
				list.remove(f); i--;
				continue;
			}

			// Folder has files; add its files
			list.addAll(Arrays.asList(folder_files));
			// Exclude unwanted folders
			if(!subfolders) {
				blacklist.add(f.toString());
			}

			// Folder has been read; remove
			list.remove(f);
			i--;
		}
		return list;
	}

	boolean blacklist_search(List<String> blacklist, String s) {
		if(blacklist == null || blacklist.isEmpty()) {
			return false;
		}

		for(String path : blacklist) {
			if(s.contains(path)) {
				return true;
			}
		}
		return false;
	}

	public List<File> getArgs_list() {
		return args_list;
	}

	public void setArgs_list(List<File> args_list) {
		this.args_list = args_list;
	}
}
