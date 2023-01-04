import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.*;

public class main extends Application {
    public static Connection connection = null;
    public static Statement statement = null;
    Database database;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // create scene, set title and show stage
        Scene scene = createScene(primaryStage);

        primaryStage.setTitle("Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // create scene
    public Scene createScene(Stage primaryStage) throws Exception{
        // first stage to show is the login page, the user must enter the
        // correct username and password to access the database
        GridPane root = new GridPane();

        // attributes
        VBox vBox = new VBox();
        Label username = new Label("Username:");
        TextField usernameText = new TextField();
        Label password = new Label("Password:");
        TextField passwordText = new TextField();
        Button connect = new Button("Connect To Database");

        vBox.getChildren().addAll(username,usernameText,password,passwordText,connect);
        root.addRow(0,vBox);

        // if connect button is chosen, then try to connect to database
        connect.setOnAction(event -> {

            // if the username and password is correct and a connection is set up,
            // close the current stage and create new stage to
            // display the stage to interact with the database
            if(connectToDB(usernameText.getText(),passwordText.getText())) {
                primaryStage.close();
                Stage stage = createNewStage();
                try {
                    database = new Database(stage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // alert message that shows when a connection to the database was not successful
            else
            {
                Alert message = new Alert(Alert.AlertType.INFORMATION);
                message.setTitle("Error Message");
                message.setContentText("Could not connect to database, please check your username and password and try again.");
                message.showAndWait();
            }
        });
        return new Scene(root);
    }

    // create stage for interaction with the database
    // this stage allows for albums and songs to viewed, added, deleted
    // and edited
    public Stage createNewStage(){
        // creates stage
        Stage newStage = new Stage();
        newStage.setTitle("Database");

        // vertical box that has the attributes for the interaction
        // this includes search bar, list view on albums, select, delete,
        // add and edit buttons for album
        // as well as a search bar for songs, table view of songs and
        // delete, add and edit buttons
        // for songs
        VBox temp = new VBox();
        Label searchAlbum = new Label("Search album name:");
        TextField searchNameBox = new TextField();
        searchNameBox.setPromptText("Search album here"); //to set the hint text
        //searchNameBox.getParent().requestFocus();
        searchNameBox.setId("searchbox");

        // list view of the albums
        ListView listView = new ListView();
        listView.setPadding(new Insets(10));
        listView.setId("list");

        // horizontal box for the buttons for the albums
        HBox hBox = new HBox();
        Button selectAlbum = new Button("Select songs in album");
        selectAlbum.setPadding(new Insets(10));
        selectAlbum.setId("select");

        Button deleteAlbum = new Button("Delete album");
        deleteAlbum.setId("deleteAlbum");
        deleteAlbum.setPadding(new Insets(10));

        Button addAlbum = new Button("Add album");
        addAlbum.setId("addAlbum");
        addAlbum.setPadding(new Insets(10));

        Button editAlbum = new Button("Edit album");
        editAlbum.setId("editAlbum");
        editAlbum.setPadding(new Insets(10));

        hBox.getChildren().addAll(selectAlbum,deleteAlbum,addAlbum,editAlbum);
        hBox.setSpacing(10);

        Label searchSong = new Label("Songs:");
        TextField searchSongBox = new TextField();
        searchSongBox.setPromptText("Search song here"); //to set the hint text
        searchSongBox.setId("songSearch");

        // table view for the songs
        TableView tableView = new TableView();
        tableView.setPadding(new Insets(10));
        tableView.setId("table");

        // horizontal box for the buttons for the songs
        HBox hBox1 = new HBox();
        Button deleteSong= new Button("Delete song");
        deleteSong.setId("deleteSong");
        deleteSong.setPadding(new Insets(10));

        Button addSong = new Button("Add song");
        addSong.setId("addSong");
        addSong.setPadding(new Insets(10));

        Button editSong= new Button("Edit song");
        editSong.setId("editSong");
        editSong.setPadding(new Insets(10));

        Button disconnect = new Button("Disconnect");
        disconnect.setId("disconnect");
        disconnect.setPadding(new Insets(10));
        disconnect.setStyle("-fx-border-color: #ff0000; -fx-border-width: 5px;");

        hBox1.getChildren().addAll(deleteSong,addSong,editSong);
        hBox1.setSpacing(10);

        // adds all the attributes to the pane
        temp.getChildren().addAll(searchAlbum,searchNameBox,listView,hBox,searchSong,searchSongBox,tableView,hBox1,disconnect);
        temp.setPadding(new Insets(10));
        temp.setSpacing(5);

        newStage.setScene(new Scene(temp));
        newStage.setWidth(500);
        newStage.setHeight(600);
        return newStage;
    }

    // connect to database
    //Nelson Mandela University VPN connection needed to connect to this database. 
    public boolean connectToDB(String Username,String password) {
        System.out.println("Establishing connection to database...");

        System.out.println("   Loading JDBC driver for MS SQL Server database...");
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
        } catch (Exception e) {
            System.out.printf("   Unable to load JDBC driver... '%s'\n", e.getMessage());
            return false;
        }

        System.out.println("   Use driver to connect to MS SQL Server (postsql\\WRR)...");
        try {
            System.out.println("   Locate database to open (using connection string)...");

            String connectionString = "jdbc:jtds:sqlserver://postsql.mandela.ac.za/WRAP301;instance=WRR";
            System.out.println("      Connection string = " + connectionString);

            // create connection to DB, including username & password
            // NEVER, EVER, include a username and password in your code!!!!
            connection = DriverManager.getConnection(connectionString, Username, password);

            // create statement object for manipulating DB
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            System.out.println("Connection Successful");
            return true;
        } catch (Exception e) {
            System.out.printf("   Unable to connect to DB... '%s'\n", e.getMessage());
            System.out.println("Make sure VPN and internet is connected");
        }
        System.out.println();
        return false;
    }

    //disconnects from the database
    public static void disconnectDataBase() {
        try {
            connection.close();
        } catch (Exception ex) {
            System.out.println("Unable to disconnect from database");
        }
        System.out.println("Disconnected from database");
    }
}
