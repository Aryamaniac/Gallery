package cs1302.gallery;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;
import java.io.InputStreamReader;
import com.google.gson.*;
import javafx.scene.layout.TilePane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.application.Platform;
import java.util.Random;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import java.util.Scanner;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressBar;
import javafx.stage.Modality;
import java.util.ArrayList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.io.IOException;

/**
 * Represents an iTunes GalleryApp.
 */
public class GalleryApp extends Application {

    private int defHeight = 100;
    private int defWidth = 100;
    public HBox holder;
    public VBox total;
    public TilePane pane;
    public String itunes = "https://itunes.apple.com/search?term=";
    String input;
    Image[] images;
    boolean pause = false;
    Image[] replacements;
    ProgressBar p1;
    Double progress = 0.0;
    String delay = "https://deelay.me/1000/";
    Button searchButton;
    Boolean updating = false;

    /** 
     * Sets up the stage, scene, buttons, fields, and container elements.
     * {@inheritdoc} 
     */
    @Override
    public void start(Stage stage) {
        holder = new HBox(10);
        total = new VBox();
        TextField searchTerm = new TextField("pop");
        Label searchQuery = new Label("Search Query:");
        searchButton = new Button("Update Images");
        EventHandler<ActionEvent> handler1 = (ActionEvent value) -> {
            input = prepare(searchTerm.getText());
            Runnable imageGet = () -> {
                images = imageLoad(itunes + prepare(input) + "&media=music&limit=500");
                addImages(images);
            }; //gets and displays the first 500 corresponding images from iTunes
            progress = 0.0;
            runNow(imageGet);
        };
        searchButton.setOnAction(handler1);
        Button pauseButton = new Button("Pause");
        EventHandler<ActionEvent> handler2 = (ActionEvent v) -> {
            pause = !pause;
            if (pauseButton.getText().equals("Pause")) {
                pauseButton.setText("Start");
            } else {
                pauseButton.setText("Pause");
            }
        }; // pauses/unpauses and shifts button text
        pauseButton.setOnAction(handler2);
        holder.getChildren().addAll(pauseButton, searchQuery, searchTerm, searchButton);
        pane = new TilePane();
        pane.setPrefColumns(5);
        Menu menu = new Menu("File");
        Menu help = new Menu("Help");
        MenuItem about = new MenuItem("About");
        MenuItem leave = new MenuItem("Exit");
        leave.setOnAction((ActionEvent ae) -> {
            System.exit(0);
        });
        menu.getItems().add(leave);
        help.getItems().add(about);
        MenuBar bar = new MenuBar();
        bar.getMenus().addAll(menu, help);
        about.setOnAction((e) -> helpMenu());
        p1 = new ProgressBar();
        p1.setProgress(progress);
        total.getChildren().addAll(bar, holder, pane, p1);
        total.setSpacing(10);
        total.setAlignment(Pos.CENTER_LEFT); 
        Runnable starter = () -> {
            initialize();
        };
        runNow(starter);
        Scene scene = new Scene(total);
        stage.setMaxWidth(1280);
        stage.setMinWidth(510);
        stage.setMinHeight(530);
        stage.setMaxHeight(720);
        stage.setTitle("GalleryApp!");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    } // start

    /**
     * Downloads a JSON based on a query.
     *
     * @param search the keywords to search for in iTunes
     * @return an array of the first 50 images associated with the keyword
     */
    public Image[] imageLoad(String search) {
        Image[] images;
        try {
            URL url = new URL(search);
            InputStreamReader reader = new InputStreamReader(url.openStream());
            JsonElement je = JsonParser.parseReader(reader); //get json from the reader
            JsonObject test = je.getAsJsonObject(); // turn the json element into an object
            JsonArray results = test.getAsJsonArray("results"); // get the results field as an array
            int index = 0;
            ArrayList<String> urlList = new ArrayList<String>();
            for (int i = 0; i < results.size(); i++) {
                if (results.get(i).getAsJsonObject().has("artworkUrl100")) {
                    String toGet = results.get(i).getAsJsonObject().get("artworkUrl100").toString();
                    toGet = toGet.replace("\"", "");
                    if (!urlList.contains(toGet)) {
                        urlList.add(toGet);
                    }
                }
            } //creates an arrayList of unique images URLs
            double inc = (double) (1.0 / urlList.size());
            images = new Image[urlList.size()];
            if (urlList.size() < 21) {
                errorAlert(new IOException("Less than 21 results for query"));
                return null;
            } //throws an error if the seach doesn't return 21 results
            for (int i = 0; i < urlList.size(); i++) {
                images[i] = new Image(urlList.get(i), defHeight, defWidth, false, false);
                progress += inc;
                p1.setProgress(progress);
            } //downloads images and updates the progress bar
            
            return images;
        } catch (Exception e) {
            System.out.println(e.getMessage()); 
        }
        return null;
    }

    /**
     * Continuously updates images in the grid until the image list is exhausted.
     *
     */
    public void updateImages() {
        try {
            while (true) {
                while (pause) {
                    Thread.sleep(100);
                } //keeps the thread paused
                Thread.sleep(2000); //2 second wait
                Random gen = new Random();
                Runnable adder = () -> {
                    int rint = gen.nextInt(replacements.length);
                    int rint2 = gen.nextInt(20); 
                    Image temp = ((ImageView) pane.getChildren().get(rint2)).getImage(); 
                    ((ImageView) pane.getChildren().get(rint2)).setImage(replacements[rint]); 
                    replacements[rint] = temp; 
                }; //switches image between the used and unused lists
                Platform.runLater(adder);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } 
    }
    
    /**
     * Takes a runnable and creates a thread using it.
     *
     * @param target a Runnable interface implementation
     */
    public static void runNow(Runnable target) {
        Thread thread = new Thread(target);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Adds the ImageViews to the initial TilePane.
     */
    public void initialize() {
        //Image[] defaults = imageLoad(itunes + "taylor+swift&limit=50");
        String defaultUrl = "https://lakelandescaperoom.com/wp-content/uploads/2016/09/image-placeholder-500x500.jpg";
        Runnable startUp = () -> {
            for (int i = 0; i < 20; i++) {
                Image temp = new Image(defaultUrl, defHeight, defWidth, false, false);
                //pane.getChildren().add(new ImageView(defaults[i]));
                pane.getChildren().add(new ImageView(temp));
            }
        }; //adds initial placeholder images
        Platform.runLater(startUp);
        searchButton.fire(); //starts default query of "rock"
    }
    
    /**
     * Sets a new batch of images.
     *
     * @param imageList the images to be put in the gallery
     */
    public void addImages(Image[] imageList) {
        if (imageList == null) {
            return;
        }
        Runnable initial = () -> {
            for (int i = 0; i < 20; i++) {
                ((ImageView) pane.getChildren().get(i)).setImage(imageList[i]);
                //pane.getChildren().add(new ImageView(imageList[i]));
            }
        };
        Platform.runLater(initial); //adds the first 20 images to the grid

        replacements = new Image[imageList.length - 20];
        for (int j = 0; j < replacements.length; j++) {
            replacements[j] = imageList[j + 20];
        } //creates a list of unused images
        if (!updating) {
            updating = true;
            Runnable update = () -> {
                updateImages();
            };
            runNow(update);
        } //starts the updater
    }

    /**
     * Prepares an input to put into a url.
     *
     * @param raw the input to be sanitized
     * @return the input string trimmed, lowercased, and spaces removed
     */
    public String prepare(String raw) {
        String clean = raw.trim().toLowerCase().replace(" ", "+");
        return clean;
    }

    /**
     * Displays an application modal stage containing project info.
     */
    public void helpMenu() {
        Stage stage2 = new Stage();
        stage2.initModality(Modality.APPLICATION_MODAL);
        Label label2 = new Label("Aryaman Singh | as58810@uga.edu | Version 2.0");
        String url = "https://media-exp1.licdn.com/dms/image/C4D03AQEpShbewGDATA/profile-displayphoto-shrink_100_100/0/"
            + "1595862097405?e=1624492800&v=beta&t=RcAwV1d_m8o8My15jHZB6FRiPnMgIBOFPTquZHUx5EU";
        // gets profile pic from linkedin
        ImageView profile = new ImageView(new Image(url));
        VBox profileHolder = new VBox();
        profileHolder.getChildren().addAll(profile, label2);

        Scene profileScene = new Scene(profileHolder);
        stage2.setTitle("About Aryaman Singh");
        stage2.setScene(profileScene);
        stage2.show();
    }

    /**
     * Shows an alert for invalid inputs.
     *
     * @param e the exception whose message should be displayed
     */
    public void errorAlert(Exception e) {
        //System.out.println("made it here");
        Runnable alerter = () -> {
            Alert a = new Alert(AlertType.ERROR);
            a.setContentText(e.getMessage());
            a.setResizable(true);
            a.showAndWait();
        };
        Platform.runLater(alerter);
    }

} // GalleryApp























