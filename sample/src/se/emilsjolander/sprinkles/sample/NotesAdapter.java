package se.emilsjolander.sprinkles.sample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import se.emilsjolander.sprinkles.CursorList;
import se.emilsjolander.sprinkles.sample.models.Note;

public class NotesAdapter extends BaseAdapter {

    private Context mContext;
	private LayoutInflater mInflater;
	private CursorList<Note> mNotes;
	
	public NotesAdapter(Context context) {
        mContext = context;
		mInflater = LayoutInflater.from(context);
	}
	
	public void swapNotes(CursorList<Note> notes) {
        if (mNotes != null) {
            mNotes.close();
        }
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
            holder.tagCount = (TextView) convertView.findViewById(R.id.tag_count);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		String content = getItem(position).getContent();
		if (content == null || content.isEmpty()) {
			holder.content.setTextColor(mContext.getResources().getColor(R.color.text_gray));
			holder.content.setText(R.string.untitled);
		} else {
			holder.content.setTextColor(mContext.getResources().getColor(R.color.text_black));
			holder.content.setText(content);	
		}

        holder.tagCount.setText(""+getItem(position).getTagCount());
		
		return convertView;
	}
	
	private static class ViewHolder {
		TextView content;
        TextView tagCount;
	}

}
