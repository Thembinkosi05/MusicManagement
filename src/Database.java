import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;

// controller class
public class Database {
    // arraylist and observable list that stores the albums
    private final ArrayList<Album> albumArrayList = new ArrayList<>();
    private ObservableList<Album> observableAlbums;
    ListView<Album> albums;
    Album selectedAlbum;
    Song selectedSong;

    // arraylist and observable list that stores the songs
    private final ArrayList<Song> songArrayList = new ArrayList<>();
    private ObservableList<Song> songObservableList;
    TableView<Song> songs;

    // constructor
    public Database(Stage stage) throws Exception{
        // sets up the albums and links the albums and songs
        albums = (ListView) stage.getScene().lookup("#list");
        setUpAlbums();
        linkAlbumList(stage);
        linkSongTable(stage);
        disconnect(stage);
    }

    // set up albums.
    private void setUpAlbums() throws Exception{
        // preforms sql query to extract all albums in the database
        // and adds them to the arraylist
        String sql = "SELECT * FROM CD";
        ResultSet resultSet = main.statement.executeQuery(sql);

        resultSet.beforeFirst();
        albumArrayList.clear();
        while (resultSet.next()){
            int cdID = resultSet.getInt("CDID");
            String title = resultSet.getString("Title");
            int year = resultSet.getInt("Year");

            albumArrayList.add(new Album(cdID,title,year));
        }

        Callback<Album, Observable[]> extractor = album -> new Observable[] {album.titleProperty(), album.yearProperty()};

        observableAlbums = FXCollections.observableArrayList(extractor);

        observableAlbums.addAll(albumArrayList);

        albums.setItems(observableAlbums);
    }

    // links the albums to the list
    private void linkAlbumList(Stage stage){
        // gets the list in the pane
        // and gets the items in the arraylist and adds them to the
        // observable list for the album

        // adds listener to the search box
        TextField searchAlbum = (TextField)stage.getScene().lookup("#searchbox");
        searchAlbum.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                searchAlbum(oldValue,newValue);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // if select button pushed, select the album and display the associated
        // songs
        Button select = (Button) stage.getScene().lookup("#select");
        select.setOnAction(event -> {
            selectedAlbum = albums.getSelectionModel().getSelectedItem();
            try {
                // displays the songs in the selected album
                displaySongsByAlbum(selectedAlbum);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        albums.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
           // System.out.printf("old = '%s', new = '%s'\n", oldValue, newValue);
            selectedAlbum = albums.getSelectionModel().getSelectedItem();
            try {
                // displays the songs in the selected album
                displaySongsByAlbum(selectedAlbum);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // if delete button pushed, deletes the selected album from
        // the database and the list view
        Button delete =(Button)stage.getScene().lookup("#deleteAlbum");
        delete.setOnAction(actionEvent -> {
            selectedAlbum = albums.getSelectionModel().getSelectedItem();
            try {
                // asks user if they sure they want to delete the album
                Alert message = new Alert(Alert.AlertType.CONFIRMATION);
                message.setTitle("Album delete");
                message.setContentText("Are you sure you want to delete the album?");
                Optional<ButtonType> action = message.showAndWait();

                // if yes, then album is deleted
                if(action.get()==ButtonType.OK) {
                    deleteAlbum(selectedAlbum);
                    deleteSongs(selectedAlbum.getCdID());
                    albums.getItems().remove(selectedAlbum);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // if add button pushed, adds an album to the database and listview
        Button add =(Button)stage.getScene().lookup("#addAlbum");

        // if pushed, creates a stage that has fields for the entering
        // of a new album
        add.setOnAction(actionEvent -> {
            try {
                Stage temp = createAlbumStage();
                temp.show();

                Button ok = (Button) temp.getScene().lookup("#ok");

                ok.setOnAction(actionEvent1 -> {
                    try {
                        TextField albumName = (TextField) temp.getScene().lookup("#albumName");
                        String name = albumName.getText();
                        TextField albumYear = (TextField) temp.getScene().lookup("#albumYear");
                        Integer year = Integer.parseInt(albumYear.getText());
                        addAlbum(name,year,temp);

                        // clears the current albums in the arraylist and observable list
                        // and re queries the database
                        albumArrayList.clear();
                        observableAlbums.clear();

                        // re queries database
                        setUpAlbums();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // if edit button pushed, edits the album in the database and listview
        Button edit =(Button)stage.getScene().lookup("#editAlbum");
        edit.setOnAction(actionEvent -> {
            selectedAlbum = albums.getSelectionModel().getSelectedItem();
            try {

                // creates a stage with fields to update the selected album
                Stage temp = editAlbumStage();
                temp.show();

                Button update = (Button) temp.getScene().lookup("#update");

                // updates the selected album
                update.setOnAction(actionEvent1 -> {
                    try {
                        TextField albumName = (TextField) temp.getScene().lookup("#albumName");
                        String name = albumName.getText();
                        TextField albumYear = (TextField) temp.getScene().lookup("#albumYear");
                        Integer year = Integer.parseInt(albumYear.getText());

                        int selectedCdID = selectedAlbum.getCdID();

                        // updates the album in the database and as well as in the list view
                        editAlbum(selectedAlbum,name,year,temp);
                        albums.getItems().remove(selectedAlbum);
                        albums.getItems().add(new Album(selectedCdID,name,year));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        stage.show();
    }

    // displays songs by album
    public void displaySongsByAlbum(Album album) throws Exception{
        // sql query that gets all the songs in an album in the database and adds
        // them to arraylist which is then added to the observable list
        int albumCdID = album.getCdID();
        String sql = "SELECT * FROM Track WHERE Track.CDID =" + albumCdID;
        ResultSet resultSet = main.statement.executeQuery(sql);

        resultSet.beforeFirst();
        songArrayList.clear();
        while (resultSet.next()){
            int TID = resultSet.getInt("TID"); //song ID
            int cdID = resultSet.getInt("CDID"); //album ID
            int trackNum = resultSet.getInt("TrackNumber");
            String name = resultSet.getString("Name");
            String artist = resultSet.getString("Artist");

            songArrayList.add(new Song(TID,cdID,trackNum,name,artist));
        }

        Callback<Song, Observable[]> extractor = song -> new Observable[] {song.nameProperty(),song.artistProperty()};
        songObservableList = FXCollections.observableArrayList(extractor);
        songObservableList.addAll(songArrayList);
        songs.setItems(songObservableList);
    }

    // search for an album
    public void searchAlbum(String oldValue, String newValue) throws Exception {
        setUpAlbums();
        if (oldValue != null && (newValue.length() < oldValue.length())) {
            albums.setItems(observableAlbums);
        }

        ObservableList<Album> albums = FXCollections.observableArrayList();
        for (Album album : observableAlbums) {
            boolean match = true;
            Album newAlbum = album;
            if (!album.getTitle().toUpperCase().contains(newValue.toUpperCase())) {
                match = false;
            }
            if (match) {
                albums.add(newAlbum);
            }
        }
        this.albums.setItems(albums);
    }

    // delete album
    public void deleteAlbum(Album album) throws Exception{
        // deletes the album from the database
        try {
            String sql = "DELETE FROM CD WHERE CD.CDID=" + album.getCdID();
            main.statement.execute(sql);
            // displays message when deleted

            Alert message = new Alert(Alert.AlertType.INFORMATION);
            message.setTitle("Album deleted");
            message.setContentText("Album successfully deleted");
            message.showAndWait();

            // re queries the database to repopulate the list
            setUpAlbums();
        } catch (Exception e) {
            System.out.println("Could not delete record... " + e.getMessage());
        }


    }

    // add album
    public int addAlbum(String title,int year,Stage stage){
        System.out.println("Adding new album...");
        try {
            String max = "SELECT MAX(CDID) AS maximum FROM CD ";
            ResultSet resultSet = main.statement.executeQuery(max);
            int CDID = 0;
            while (resultSet.next()){
                CDID=resultSet.getInt("maximum")+1;}
            String sql = "INSERT INTO CD VALUES ('"+CDID+"','"+title+"','"+year+"')";
            main.statement.execute(sql);
            System.out.println("\tAlbum added successful!");

            //display if successfully added
            Alert message = new Alert(Alert.AlertType.INFORMATION);
            message.setTitle("Album added");
            message.setContentText("Album successfully added");
            message.showAndWait();
            stage.close();
            return CDID;
        } catch (Exception e) {
            System.out.println("Could not add record... " + e.getMessage());
        }
        System.out.println();
        return 0;
    }

    // create stage to add a new album
    // holds attributes for name and year of album
    public Stage createAlbumStage(){
        Stage newStage = new Stage();

        VBox root = new VBox();
        root.setSpacing(10);
        root.setFillWidth(true);
        root.setPadding(new Insets(5));

        TextField albumName = new TextField();
        albumName.setId("albumName");
        TextField albumYear = new TextField();
        albumYear.setId("albumYear");

        Button btnOK = new Button("OK");
        btnOK.setId("ok");
        btnOK.setMaxWidth(Double.MAX_VALUE);

        root.getChildren().addAll(
                new Label("Album Name"), albumName,
                new Label("Album Year"), albumYear,
                btnOK
        );

        newStage.setScene(new Scene(root));
        newStage.setTitle("New Album");

        return newStage;
    }

    // edit album
    public void editAlbum(Album album,String name, Integer year,Stage stage) throws Exception{
        // sql that edits an album in the database
        try {
            String sql ="UPDATE CD SET Title ='" + name + "', Year =" + year + " WHERE CD.CDID = " + album.cdID;
            main.statement.execute(sql);
            // displays message when album is edited successfully
            Alert message = new Alert(Alert.AlertType.INFORMATION);
            message.setTitle("Album edited");
            message.setContentText("Album successfully edited");
            message.showAndWait();
            stage.close();
        }
        catch (Exception e){
            System.out.println("Cant update fields");
        }


    }

    // edit album stage
    // holds attributes to edit the album like name and year
    public Stage editAlbumStage(){
        Stage newStage = new Stage();

        VBox root = new VBox();
        root.setSpacing(10);
        root.setFillWidth(true);
        root.setPadding(new Insets(5));

        TextField albumName = new TextField();
        albumName.setId("albumName");
        TextField albumYear = new TextField();
        albumYear.setId("albumYear");

        Button btnOK = new Button("Update");
        btnOK.setId("update");
        btnOK.setMaxWidth(Double.MAX_VALUE);

        root.getChildren().addAll(
                new Label("Album Name"), albumName,
                new Label("Album Year"), albumYear,
                btnOK
        );

        newStage.setScene(new Scene(root));
        newStage.setTitle("New Album");

        return newStage;
    }

    // links song table
    private void linkSongTable(Stage stage) throws Exception{
        // gets the table in the pane
        // and gets the items in the arraylist and adds them to the
        // observable list for the songs
        songs = (TableView) stage.getScene().lookup("#table");
        TableColumn TID = new TableColumn("TID");
        TID.setCellValueFactory(new PropertyValueFactory<>("TID"));

        TableColumn cdID = new TableColumn("CDID");
        cdID.setCellValueFactory(new PropertyValueFactory<>("cdID"));

        TableColumn trackNum = new TableColumn("Track Number");
        trackNum.setCellValueFactory(new PropertyValueFactory<>("trackNum"));

        TableColumn name = new TableColumn("Name");
        name.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn artist = new TableColumn("Artist");
        artist.setCellValueFactory(new PropertyValueFactory<>("artist"));

        songs.getColumns().addAll(trackNum,name,artist);

        Callback<Song, Observable[]> extractor = song -> new Observable[] {song.trackNumProperty(), song.nameProperty(), song.artistProperty()};

        songObservableList = FXCollections.observableArrayList(extractor);

        songObservableList.addAll(songArrayList);

        songs.setItems(songObservableList);

        // search bar add listener
        TextField searchSong = (TextField)stage.getScene().lookup("#songSearch") ;
        searchSong.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                searchSongs(oldValue,newValue);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        // button delete that deletes song from the database and the table view
        Button delete =(Button)stage.getScene().lookup("#deleteSong");
        delete.setOnAction(actionEvent -> {
            Song song = (Song) songs.getSelectionModel().getSelectedItem();
            try {
                // asks if you want to delete before deleting
                Alert message = new Alert(Alert.AlertType.CONFIRMATION);
                message.setTitle("Song delete");
                message.setContentText("Are you sure you want to delete the song?");
                Optional<ButtonType> action = message.showAndWait();

                // if yes then deletes the song
                if(action.get()==ButtonType.OK) {
                    deleteSong(song);
                    songs.getItems().remove(song);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // button add that adds song to database
        Button add =(Button)stage.getScene().lookup("#addSong");
        add.setOnAction(actionEvent -> {
           // Song song = songs.getSelectionModel().getSelectedItem();
            try {
                // creates stage and gets the relevant information for a song
                Stage temp = createSongStage();
                temp.show();

                Button addButton = (Button) temp.getScene().lookup("#addSong");

                addButton.setOnAction(actionEvent1 -> {
                    try {
                        TextField songTrackNum = (TextField) temp.getScene().lookup("#trackNum");
                        Integer trackNumSong = Integer.parseInt(songTrackNum.getText());

                        TextField songName = (TextField) temp.getScene().lookup("#songName");
                        String nameSong = songName.getText();

                        TextField songArtist = (TextField) temp.getScene().lookup("#artistName");
                        String artistSong = songArtist.getText();

                        // adds song to the table view and the database
                        addSong(selectedAlbum,trackNumSong,nameSong,artistSong,temp);
                        //songs.getItems().add(song);
                        displaySongsByAlbum(selectedAlbum);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // button edit that edits song in database and table view
        Button edit =(Button)stage.getScene().lookup("#editSong");
        edit.setOnAction(actionEvent -> {
            Song song = songs.getSelectionModel().getSelectedItem();
            try {
                // creates stage that allows the relevant information to be edited
                Stage temp =editSongStage();
                temp.show();

                Button update = (Button) temp.getScene().lookup("#updateSong");
                selectedSong = songs.getSelectionModel().getSelectedItem();
                // if update button is clicked then update the database and table
                update.setOnAction(actionEvent1 -> {
                    try {
                        TextField songTrackNum = (TextField) temp.getScene().lookup("#trackNum");
                        Integer trackNumSong = Integer.parseInt(songTrackNum.getText());

                        TextField songName = (TextField) temp.getScene().lookup("#songName");
                        String nameSong = songName.getText();

                        TextField songArtist = (TextField) temp.getScene().lookup("#artistName");
                        String artistSong = songArtist.getText();

                        editSong(selectedSong,trackNumSong,nameSong,artistSong,temp);

                        int cdIDSong = song.getCdID();
                        int TIDSong = song.getTID();

                        songs.getItems().remove(song);
                        songs.getItems().add(new Song(TIDSong,cdIDSong,trackNumSong,nameSong,artistSong));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void deleteSong(Song song) {
        try {
            String sql = "DELETE FROM TRACK WHERE TRACK.TID=" + song.getTID();
            main.statement.execute(sql);

            // displays success message
            Alert message = new Alert(Alert.AlertType.INFORMATION);
            message.setTitle("Song deleted");
            message.setContentText("Song successfully deleted");
            message.showAndWait();
        } catch (Exception e) {
            System.out.println("Could not delete record... " + e.getMessage());
        }
    }

    public void allSongs() throws SQLException {
        String sql = "SELECT * FROM Track";
        ResultSet result = main.statement.executeQuery(sql);
        songArrayList.clear();
        while (result.next()) {
            // get values from current tuple
            int TID = result.getInt("TID");
            int CDID = result.getInt("CDID");
            int trackNumber = result.getInt("TrackNumber");
            String name = result.getString("Name");
            String artist = result.getString("Artist");
            songArrayList.add(new Song(TID,CDID,trackNumber,name,artist));
        }
        Callback<Song, Observable[]> extractor = song -> new Observable[] {song.nameProperty(),song.artistProperty()};
        songObservableList = FXCollections.observableArrayList(extractor);
        songObservableList.addAll(songArrayList);
        songs.setItems(songObservableList);
    }

    // delete songs
    public void deleteSongs(int CDID) throws Exception{
        // deletes songs when deleting an album
        try {
            String sql = "DELETE FROM TRACK WHERE TRACK.CDID=" + CDID;
            main.statement.execute(sql);

        } catch (Exception e) {
            System.out.println("Could not delete record... " + e.getMessage());
        }

    }

    // add song
    public void addSong(Album album, Integer trackNum, String name, String artist,Stage stage){
        // adds song to database
        try {
            String VALUES = "('" + album.cdID + "'," + trackNum + "," + "'" + name + "'," + "'" + artist + "" + "')";
            String sql = "INSERT INTO Track VALUES" + VALUES;
            main.statement.execute(sql);

            // displays success message
            Alert message = new Alert(Alert.AlertType.INFORMATION);
            message.setTitle("Song added");
            message.setContentText("Song successfully added");
            message.showAndWait();
            stage.close();
        } catch (Exception e) {
            System.out.println("Could not insert new record... " + e.getMessage());
        }


    }

    // create song stage
    public Stage createSongStage(){
        // stage to allow a song to be added
        // contains teh relevant fields
        Stage stage = new Stage();

        VBox vBox = new VBox();
        Label labelTrackNum = new Label("Track Number:");
        TextField trackNum = new TextField();
        trackNum.setId("trackNum");

        Label labelName = new Label("Song Name:");
        TextField songName = new TextField();
        songName.setId("songName");

        Label labelArtist = new Label("Artist Name:");
        TextField artistName = new TextField();
        artistName.setId("artistName");

        Button add = new Button("Add");
        add.setId("addSong");

        vBox.getChildren().addAll(labelTrackNum,trackNum,labelName,songName,labelArtist,artistName,add);
        stage.setTitle("New Song");
        stage.setScene(new Scene(vBox));

        return stage;
    }

    // edit song
    public void editSong(Song song, Integer trackNum, String name, String artist,Stage stage){
        // edit song in database
        try {
            String sql ="UPDATE TRACK SET TrackNumber =" + trackNum + ", Name ='" + name + "', Artist='" + artist + "' WHERE TRACK.TID = " + song.TID;
            main.statement.execute(sql);
            // displays success message
            Alert message = new Alert(Alert.AlertType.INFORMATION);
            message.setTitle("Song editing");
            message.setContentText("Song successfully edited");
            message.showAndWait();
            stage.close();
        }
        catch (Exception e){
            System.out.println("Cant update fields");
        }


    }

    // edit song stage
    public Stage editSongStage(){
        // allows a song to be edited with the relevant
        // information
        Stage stage = new Stage();

        VBox vBox = new VBox();
        Label labelTrackNum = new Label("Track Number:");
        TextField trackNum = new TextField();
        trackNum.setId("trackNum");

        Label labelName = new Label("Song Name:");
        TextField songName = new TextField();
        songName.setId("songName");

        Label labelArtist = new Label("Artist Name:");
        TextField artistName = new TextField();
        artistName.setId("artistName");

        Button update = new Button("Update");
        update.setId("updateSong");

        vBox.getChildren().addAll(labelTrackNum,trackNum,labelName,songName,labelArtist,artistName,update);
        stage.setTitle("Edit Song");
        stage.setScene(new Scene(vBox));

        return stage;
    }

    // search songs
    public void searchSongs(String oldValue, String newValue) throws SQLException {
        allSongs();
        if (oldValue != null && (newValue.length() < oldValue.length())) {
            songs.setItems(songObservableList);
        }

        ObservableList<Song> songs = FXCollections.observableArrayList();
        for (Song song : songObservableList) {
            boolean match = true;
            Song newSong = song;
            if (!song.getName().toUpperCase().contains(newValue.toUpperCase())) {
                match = false;
            }
            if (match) {
                songs.add(newSong);
            }
        }
        this.songs.setItems(songs);
    }

    // disconnecting from the database
    public void disconnect(Stage stage){
        Button disconnect =(Button)stage.getScene().lookup("#disconnect");
        disconnect.setOnAction(actionEvent -> {
            main.disconnectDataBase();
            Alert message = new Alert(Alert.AlertType.INFORMATION);
            message.setTitle("Disconnecting Database");
            message.setContentText("Database will be disconnected and the application will be closed");
            message.showAndWait();
            stage.close();
        });
    }

}
