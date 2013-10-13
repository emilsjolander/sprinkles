package se.emilsjolander.sprinkles;

import java.util.List;

import se.emilsjolander.sprinkles.models.Note;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class NotesAdapter extends BaseAdapter {
	
	private LayoutInflater mInflater;
	private List<Note> mNotes;
	
	public NotesAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
	}
	
	public void setNotes(List<Note> notes) {
		mNotes = notes;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mNotes == null ? 0 : mNotes.size();
	}

	@Override
	public Note getItem(int position) {
		return mNotes.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mNotes.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_note, parent, false);
			
			holder.content = (TextView) convertView.findViewById(R.id.content);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.content.setText(getItem(position).getContent());
		
		return convertView;
	}
	
	private static class ViewHolder {
		TextView content;
	}

}
