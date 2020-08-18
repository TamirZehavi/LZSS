import javafx.geometry.Insets;

import java.io.File;
import java.io.IOException;

import javafx.application.Application;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
//import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class GUI extends Application 
{
	private Stage window = null;
	private int maxMatch = 0;
	private int minMatch = 0;
	private int windowSize = 0;
	private String compressionMethod = "LZSS";
	
	public static void main(String[] args)
	{
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception 
	{
		this.window = primaryStage;
		this.window.setTitle("LZSS");
		BorderPane mainLayout = new BorderPane();
		Scene mainScene = new Scene(mainLayout, 520, 320);
		
//		Image img = new Image("/wink.png");
//		this.window.getIcons().add(img);
		
		this.window.setScene(mainScene);
		
		//Define two grids, one for adding files and the other for the center of the window
		GridPane fileGrid = new GridPane();
		fileGrid.setPadding(new Insets(20,20,20,20));
		fileGrid.setVgap(10);
		fileGrid.setHgap(10);
		
		GridPane centerGrid = new GridPane();
		centerGrid.setPadding(new Insets(20,20,20,20));
		centerGrid.setVgap(5);
		centerGrid.setHgap(10);
		
		
		//File selection
		Label inFileLabel = new Label("In file path: ");
		GridPane.setConstraints(inFileLabel, 0, 0);
		
		TextField inFilePathText = new TextField();
		inFilePathText.setPrefWidth(250);
		inFilePathText.setPromptText("Input path goes here");
		GridPane.setConstraints(inFilePathText, 1, 0);
		
		Button addFileButton = new Button("File");
		GridPane.setConstraints(addFileButton, 2, 0);
		
		//Out path
		Label outFileLabel = new Label("Out file path: ");
		GridPane.setConstraints(outFileLabel, 0, 1);
		
		TextField outFilePathText = new TextField();
		outFilePathText.setPrefWidth(250);
		GridPane.setConstraints(outFilePathText, 1, 1);
		
		fileGrid.getChildren().addAll(inFileLabel, inFilePathText, addFileButton, outFileLabel, outFilePathText);
		
		//Window size, min & max size of match
		Label windowSizeLabel = new Label("Window Size");
		GridPane.setConstraints(windowSizeLabel, 0, 0);
		
		ComboBox<String> windowSizeBox = new ComboBox<String>();
		windowSizeBox.getItems().addAll("32 Bytes","64 Bytes","128 Bytes","256 Bytes","512 Bytes", "1024 Bytes", "2048 Bytes", "4096 Bytes", "8192 Bytes", "16384 Bytes", "32768 Bytes");
		windowSizeBox.setValue("4096 Bytes");
		this.windowSize = 4096; //initial value
		GridPane.setConstraints(windowSizeBox, 0, 1);
		
		Label maxMatchLabel = new Label("Max Match Size");
		GridPane.setConstraints(maxMatchLabel, 1, 0);
		
		ComboBox<String> LZSSmaxMatchBox = new ComboBox<String>();
		LZSSmaxMatchBox.getItems().addAll("32 Bytes","64 Bytes","128 Bytes","256 Bytes","512 Bytes", "1024 Bytes", "2048 Bytes", "4096 Bytes", "8192 Bytes", "16384 Bytes", "32768 Bytes");
		LZSSmaxMatchBox.setValue("256 Bytes");
		this.maxMatch = 256; //initial size
		GridPane.setConstraints(LZSSmaxMatchBox, 1, 1);
		
		Label minMatchLabel = new Label("Min Match Size");
		GridPane.setConstraints(minMatchLabel, 2, 0);
		
		ComboBox<String> minMatchBox = new ComboBox<String>();
		for(int i = 2 ; i < 21 ; i ++)
		{
			minMatchBox.getItems().add( i + " Bytes");
		}
		minMatchBox.setValue("2 Bytes");
		this.minMatch = 2; //initial size
		GridPane.setConstraints(minMatchBox, 2, 1);
		
		//Radio buttons for compression type
		Label compressionType = new Label("Compression Type");
		GridPane.setConstraints(compressionType, 3, 0);
		
		VBox radioButtons = new VBox();
		ToggleGroup Options = new ToggleGroup();
		
		RadioButton LZSSButton = new RadioButton("LZSS");
		LZSSButton.setPadding(new Insets(5));
		LZSSButton.setToggleGroup(Options);
		LZSSButton.setSelected(true);
		
		RadioButton LZ77Button = new RadioButton("LZ77");
		LZ77Button.setPadding(new Insets(5));
		LZ77Button.setToggleGroup(Options);
		
		radioButtons.getChildren().addAll(LZ77Button, LZSSButton);
		GridPane.setConstraints(radioButtons, 3, 1);
		
		//Compress button
		Button compressButton = new Button("Compress");
		GridPane.setConstraints(compressButton, 0, 2);
		compressButton.setDisable(true);
		compressButton.getStyleClass().add("BlueButtons");
		
		//Ratio
		Label compressionRatio = new Label("Ratio: ");
		GridPane.setConstraints(compressionRatio, 0, 3);
		
		//Decompress button
		Button decompressButton = new Button("Decompress");
		GridPane.setConstraints(decompressButton, 0, 5);
		decompressButton.setDisable(true);
		decompressButton.getStyleClass().add("BlueButtons");
		
		centerGrid.getChildren().addAll(windowSizeLabel , windowSizeBox , maxMatchLabel , LZSSmaxMatchBox ,  
				minMatchLabel , minMatchBox , compressionType , radioButtons , compressButton , compressionRatio , decompressButton);
		
		mainLayout.setCenter(centerGrid);
		mainLayout.setTop(fileGrid);
		
		//Button functionality 
		LZSSmaxMatchBox.setOnAction( click -> this.maxMatch = Integer.parseInt(LZSSmaxMatchBox.getValue().substring(0 , LZSSmaxMatchBox.getValue().length() - 6)) );
		minMatchBox.setOnAction( click -> this.minMatch = Integer.parseInt(minMatchBox.getValue().substring(0, minMatchBox.getValue().length() - 6)));
		windowSizeBox.setOnAction( click -> this.windowSize = Integer.parseInt(windowSizeBox.getValue().substring(0, windowSizeBox.getValue().length() - 6)));
		
		LZSSButton.setOnAction( click -> {
			this.compressionMethod = "LZSS";
			minMatchBox.setDisable(false);
		});
		
		LZ77Button.setOnAction( click -> {
			this.compressionMethod = "LZ77";
			minMatchBox.setDisable(true);
		});
		
		addFileButton.setOnAction(click -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Select an input File");
			File inFile = fileChooser.showOpenDialog(this.window);
			if(inFile != null)
			{
				inFilePathText.setText(inFile.getPath());
				outFilePathText.setText(inFile.getPath());
				compressButton.setDisable(false);
				decompressButton.setDisable(false);
			}
		});
		
		compressButton.setOnAction(click -> {
			String outPath = SetOutPath(outFilePathText.getText(), "Compress");
			String inPath = inFilePathText.getText();
			if(this.compressionMethod == "LZSS")
			{
				LZSS lzss = new LZSS(inPath, outPath, this.windowSize, this.maxMatch, this.minMatch);
				try {
					lzss.Compress();
					compressionRatio.setText( "Ratio: " + String.valueOf(lzss.GetRatio()) );
					Alert doneWindow = new Alert(AlertType.INFORMATION);
					doneWindow.setTitle(null);
					doneWindow.setHeaderText(null);
					doneWindow.setContentText("Compression Done");
					doneWindow.showAndWait();
					
				} catch (IOException e) {
					Alert errorWindow = new Alert(AlertType.INFORMATION);
					errorWindow.setTitle(null);
					errorWindow.setHeaderText(null);
					errorWindow.setContentText("Something went wrong");
					errorWindow.showAndWait();
				}
			}
			else
			{
				try {
					LZ77 lz77 = new LZ77(inPath, outPath, this.windowSize, this.maxMatch);
					lz77.compress();
//					compressionRatio.setText( "Ratio: " + String.valueOf(lz77.GetRatio()) );
					Alert doneWindow = new Alert(AlertType.INFORMATION);
					doneWindow.setTitle(null);
					doneWindow.setHeaderText(null);
					doneWindow.setContentText("Compression Done");
					doneWindow.showAndWait();
					
				} catch (IOException e) {
					Alert errorWindow = new Alert(AlertType.INFORMATION);
					errorWindow.setTitle(null);
					errorWindow.setHeaderText(null);
					errorWindow.setContentText("Something went wrong");
					errorWindow.showAndWait();
				}
			}
		});
		
		decompressButton.setOnAction( click -> {
			String outPath = SetOutPath(outFilePathText.getText(), "Decompress");
			String inPath = inFilePathText.getText();
			if(this.compressionMethod == "LZSS")
			{
				LZSS lzss = new LZSS(inPath, outPath, 0 , 0 , 0);
				try {
					lzss.Decompress();
					compressionRatio.setText( "Ratio: " );
					Alert doneWindow = new Alert(AlertType.INFORMATION);
					doneWindow.setTitle(null);
					doneWindow.setHeaderText(null);
					doneWindow.setContentText("Decompression Done");
					doneWindow.showAndWait();
					
				} catch (IOException | ClassNotFoundException e) {
					Alert errorWindow = new Alert(AlertType.INFORMATION);
					errorWindow.setTitle(null);
					errorWindow.setHeaderText(null);
					errorWindow.setContentText("Something went wrong");
					errorWindow.showAndWait();
				}
			}
			else
			{
				try {
					LZ77 lz77 = new LZ77(inPath, outPath, 0 , 0);
					lz77.decompress();
					compressionRatio.setText( "Ratio: " );
					Alert doneWindow = new Alert(AlertType.INFORMATION);
					doneWindow.setTitle(null);
					doneWindow.setHeaderText(null);
					doneWindow.setContentText("Decompression Done");
					doneWindow.showAndWait();
					
				} catch (IOException | ClassNotFoundException e) {
					Alert errorWindow = new Alert(AlertType.INFORMATION);
					errorWindow.setTitle(null);
					errorWindow.setHeaderText(null);
					errorWindow.setContentText("Something went wrong");
					errorWindow.showAndWait();
				}
			}
			
		});

		mainLayout.getStylesheets().add("StyleSheets.css");
		this.window.show();
	}

	private String SetOutPath(String path, String mode) 
	{
		if(mode == "Compress")
		{
			if(path.contains("DECOMPRESSED_" + this.compressionMethod))
			{
				path = path.replace("_DECOMPRESSED_" + this.compressionMethod, "_COMPRESSED_" + this.compressionMethod);
			}
			else
			{
				String extention = path.substring(path.lastIndexOf('.'));
				path = path.substring(0 , path.lastIndexOf('.'));
				path += "_COMPRESSED_" + this.compressionMethod + extention;
			}
		}
		if(mode == "Decompress")
		{
			if(path.contains("COMPRESSED_" + this.compressionMethod))
			{
				path = path.replace("_COMPRESSED_" + this.compressionMethod, "_DECOMPRESSED_" + this.compressionMethod);
			}
			else
			{
				String extention = path.substring(path.lastIndexOf('.'));
				path = path.substring(0 , path.lastIndexOf('.'));
				path += "_DECOMPRESSED_" + this.compressionMethod + extention;
			}
		}
		return path;
	}
	
	

}

