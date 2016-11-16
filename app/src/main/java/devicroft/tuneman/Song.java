package devicroft.tuneman;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by m on 14-Nov-16.
 */

public class Song implements Parcelable {

    String title;
    String path;
    String length;

    public static final  Creator<Song> CREATOR = new Creator<Song>() {
        @Override
        public Song createFromParcel(Parcel parcel) {
            return new Song(parcel);
        }

        @Override
        public Song[] newArray(int i) {
            return new Song[i];
        }
    };


    public Song(String title, String path, String length) {
        this.title = title;
        this.path = path;
        this.length = length;
    }

    //parcel used to pass objects through intents

    public Song(Parcel in){
        this.title = in.readString();
        this.length = in.readString();
        this.path = in.readString();
    }

    //written in order of writing parces
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(length);
        parcel.writeString(path);
    }



    @Override
    public int describeContents() {
        return 0;
    }



    public String getTitle() {
        return title;
    }

    private void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    private void setPath(String path) {
        this.path = path;
    }

    public String getLength() {
        return length;
    }

    private void setLength(String length) {
        this.length = length;
    }
}
