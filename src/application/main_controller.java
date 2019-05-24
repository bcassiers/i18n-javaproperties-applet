package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.util.Callback;

public class main_controller implements Initializable {
	

    @FXML private Button addFileButton;
    @FXML private Button newFileButton;
    @FXML private Button clearFilesButton;
    @FXML private Button saveFilesButton;
    @FXML private Button newSlugButton;
    
    @FXML private TableView<Slug> langTableView;
    private Map<String,Slug> slugsMap;
    private ObservableList<Slug> slugsList;
    private Callback<TableColumn<Slug, String>, TableCell<Slug, String>> cellFactory;
    @FXML
    void buttonHandle(ActionEvent event) {
    	Node n = (Node) event.getSource();
    	switch (n.getId()) {
    	case "addFileButton" :
    		FileChooser fc = new FileChooser();
    		fc.getExtensionFilters().add(new ExtensionFilter("JAVA Properties File", "*.properties"));
    		File f = fc.showOpenDialog(n.getScene().getWindow());
    		importFile(f.getPath(),f.getName().split(".properties")[0]);
    		break;
    	}
    }

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		slugsMap = new HashMap<String, Slug>();
		slugsList = FXCollections.observableArrayList();
		langTableView.setEditable(true);
        // use fully detailed type for Map.Entry<String, String> 

        cellFactory = (TableColumn<Slug, String> param) -> new EditingCell();
       
        TableColumn<Slug, String> slugCol = new TableColumn("Slug");
        slugCol.setMinWidth(100);
        slugCol.setCellValueFactory(cellData -> cellData.getValue().getSlug());
        slugCol.setCellFactory(cellFactory);
        slugCol.setOnEditCommit(
                (TableColumn.CellEditEvent<Slug, String> t) -> {
                	Slug s = (Slug) t.getTableView().getItems().get(t.getTablePosition().getRow());
                    s.setSlug(t.getNewValue());
                    slugsMap.get(s.getSlug().toString()).setSlug(t.getNewValue());
                });
        
        langTableView.getColumns().clear();
        langTableView.getColumns().add(slugCol);
        langTableView.setItems(slugsList);
	}
	
	private void importFile(String path, String lang) {
		Properties p = newProperties(path);
		slugsList.clear();
		for(Object k : p.keySet()) {
			if(!slugsMap.containsKey(k)) {
				slugsMap.put((String) k, new Slug((String) k));
			}
			slugsMap.get(k).setVal(lang, p.getProperty((String) k));
		}
		for(Object k : slugsMap.keySet()) {
			slugsList.add(slugsMap.get(k));
		}
        langTableView.getColumns().add(newTabColumn(lang));
        langTableView.setItems(slugsList);
        
	}
		
	private TableColumn<Slug, String> newTabColumn(String lang){
		
		TableColumn<Slug, String> col = new TableColumn<Slug, String>(lang);
        col.setMinWidth(100);
        col.setCellValueFactory(cellData -> cellData.getValue().getVal(lang));
        col.setCellFactory(cellFactory);
        col.setOnEditCommit(
                (TableColumn.CellEditEvent<Slug, String> t) -> {
                    Slug s = (Slug) t.getTableView().getItems().get(t.getTablePosition().getRow());
                    s.setVal(lang, t.getNewValue());
                    System.out.println("val map  =" + slugsMap.get("dashboard.rainfil.button").getVal(lang).get());
                });
		return col;
	}
	
	private Properties newProperties(String path) {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(path);
			prop.load(input);
		} catch (IOException ex) {
			ex.printStackTrace();
			prop=null;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return prop;
	}

	private class Slug{
		
		private Map<String, StringProperty> langMap;
		private StringProperty slug;
		
		public Slug (String slug) {
			this.slug=new SimpleStringProperty(slug);
			langMap = new HashMap<String, StringProperty>();
		}
		
		public void setSlug(String newValue) {
			this.slug.set(newValue);
		}

		public void setVal(String lang, String val) {
			langMap.put(lang, new SimpleStringProperty(val));
		}
		
		public StringProperty getVal(String lang){
			return langMap.get(lang);
		}
		
		public StringProperty getSlug() {
			return slug;
		}
		
	}
	
	
    class EditingCell extends TableCell<Slug, String> {

        private TextField textField;

        private EditingCell() {
        }

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                textField.selectAll();
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            setText((String) getItem());
            setGraphic(null);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(item);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
//                        setGraphic(null);
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(null);
                }
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            textField.setOnAction((e) -> commitEdit(textField.getText()));
            textField.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                if (!newValue) {
                    System.out.println("Commiting " + textField.getText());
                    commitEdit(textField.getText());
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem();
        }
    }
}
