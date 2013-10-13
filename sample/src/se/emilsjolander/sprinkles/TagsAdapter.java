package se.emilsjolander.sprinkles;

import java.util.List;

import se.emilsjolander.sprinkles.models.Tag;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class TagsAdapter extends BaseAdapter {
	
	private List<Tag> mTags;
	private LayoutInflater mInflater;
	
	public TagsAdapter(Context context) {
		mInflater = LayoutInflater.from(context);
	}
	
	public void setTags(List<Tag> tags) {
		mTags = tags;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mTags == null ? 0 : mTags.size();
	}

	@Override
	public Object getItem(int position) {
		return mTags.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mTags.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.list_item_tag, parent, false);
			
			// TODO
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		// TODO
		
		return convertView;
	}
	
	private static class ViewHolder {
		// TODO
	}

}
