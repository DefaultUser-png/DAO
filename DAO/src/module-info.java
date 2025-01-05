module t {
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires javafx.base;
	requires mslinks;
	
	opens application to javafx.graphics, javafx.fxml;
}
