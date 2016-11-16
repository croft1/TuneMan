package devicroft.tuneman;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.TimeUnit;

/**
 * Created by m on 14-Nov-16.
 */

//used to populate the layout items that then are pushed into each row/col of a list
public class SongAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Song> songs;

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder vh;
        if(view == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_music_item, null);

            //set viewholder and attach view
            vh = new ViewHolder();
            vh.title = (TextView) view.findViewById(R.id.song_name);
            vh.length = (TextView) view.findViewById(R.id.song_length);
            view.setTag(vh);
        }else{
            //if it already exists we get vh
            vh = (ViewHolder) view.getTag();
        }

        ///update new items
        vh.title.setText(songs.get(i).getTitle());

        Long length = Long.parseLong(songs.get(i).getLength());
        vh.length.setText(TimeUnit.MILLISECONDS.toMinutes(length) +
                ":" + TimeUnit.MILLISECONDS.toSeconds(length) % 60);

        return view;
    }

    public SongAdapter(Context context, ArrayList<Song> songs) {
        this.context = context;
        this.songs = songs;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    public static class ViewHolder{
        TextView title;
        TextView length;
    }

    @Override
    public int getCount() {
        return songs.size();
    }

    @Override
    public Object getItem(int i) {
        return songs.get(i);
    }


}




