import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

// class that hold an album
public class Album {
    // album has a cdID, title and year
    Integer cdID;
    SimpleStringProperty title = new SimpleStringProperty();
    SimpleIntegerProperty year = new SimpleIntegerProperty();

    // constructor to create a new album
    public Album(int cdID,String title, Integer year){
        this.cdID=cdID;
        this.title.set(title);
        this.year.set(year);
    }

    // toString method as well as get and set methods for the different attributes
    @Override
    public String toString() {
        return " Title: " + getTitle() + " Year: "+ getYear();
    }

    public Integer getCdID() {
        return cdID;
    }

    public String getTitle() {
        return title.get();
    }

    public int getYear() {
        return year.get();
    }

    public void setCdID(Integer cdID) {
        this.cdID = cdID;
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public void setYear(int year) {
        this.year.set(year);
    }

    public SimpleStringProperty titleProperty() {
        return title;
    }

    public SimpleIntegerProperty yearProperty() {
        return year;
    }
}
