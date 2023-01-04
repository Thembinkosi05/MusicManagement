import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

// class song
public class Song {
    // has TID, cdID, trackNum, name and artist
    int TID;
    int cdID;
    SimpleIntegerProperty trackNum = new SimpleIntegerProperty();
    SimpleStringProperty name = new SimpleStringProperty();
    SimpleStringProperty artist = new SimpleStringProperty();

    // constructor to create song
    public Song(int TID,int cdID,Integer trackNum, String name, String artist){
        this.TID = TID;
        this.cdID=cdID;
        this.trackNum.set(trackNum);
        this.name.set(name);
        this.artist.set(artist);
    }

    // toString method as well as get and set methods for the attributes
    @Override
    public String toString() {
        return " Track Number: " + getTrackNum() +
                " Name: " + getName() + " Artist: " + getArtist();
    }

    public int getTID() {
        return TID;
    }

    public int getCdID() {
        return cdID;
    }

    public int getTrackNum() {
        return trackNum.get();
    }

    public String getName() {
        return name.get();
    }

    public String getArtist() {
        return artist.get();
    }

    public void setTrackNum(int trackNum) {
        this.trackNum.set(trackNum);
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public void setArtist(String artist) {
        this.artist.set(artist);
    }

    public SimpleIntegerProperty trackNumProperty() {
        return trackNum;
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public SimpleStringProperty artistProperty() {
        return artist;
    }
}
