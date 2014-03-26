package se.emilsjolander.sprinkles.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import se.emilsjolander.sprinkles.CursorList;
import se.emilsjolander.sprinkles.ManyQuery;
import se.emilsjolander.sprinkles.Query;
import se.emilsjolander.sprinkles.sample.models.Note;
import se.emilsjolander.sprinkles.sample.models.NoteTagLink;
import se.emilsjolander.sprinkles.sample.models.Tag;

public class ChooseTagActivity extends Activity {

	public static final String EXTRA_NOTE_ID = "note_id";

	private ListView mListView;
	private TagsAdapter mAdapter;

	private CursorList<Tag> mTags;
	private CursorList<NoteTagLink> mLinks;

	private ManyQuery.ResultHandler<Tag> onTagsLoaded =
            new ManyQuery.ResultHandler<Tag>() {

		@Override
		public boolean handleResult(CursorList<Tag> result) {
            mTags = result;
			mAdapter.swapTags(result);
			updateCheckedPositions();
            return true;
		}
	};

	private ManyQuery.ResultHandler<NoteTagLink> onLinksLoaded =
            new ManyQuery.ResultHandler<NoteTagLink>() {

		@Override
		public boolean handleResult(CursorList<NoteTagLink> result) {
            if (mLinks != null) {
                mLinks.close();
            }
			mLinks = result;
			updateCheckedPositions();
            return true;
		}
	};

	private OnItemClickListener onListItemClicked = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> l, View v, int pos,
				long id) {
			NoteTagLink link = new NoteTagLink(mNoteId, id);
			if (mListView.isItemChecked(pos)) {
				link.saveAsync();
			} else {
				link.deleteAsync();
			}
		}
	};
	
	private long mNoteId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.acitivty_choose_tag);

		mNoteId = getIntent().getLongExtra(EXTRA_NOTE_ID, -1);

		Query.many(Tag.class, "select * from Tags").getAsync(
				getLoaderManager(), onTagsLoaded);
		Query.many(NoteTagLink.class,
				"select * from NoteTagLinks where note_id=?", mNoteId).getAsync(
				getLoaderManager(), onLinksLoaded, Note.class, Tag.class);

		mListView = (ListView) findViewById(R.id.list);
		mListView.setEmptyView(findViewById(R.id.empty));
		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		mListView.setOnItemClickListener(onListItemClicked);

		mAdapter = new TagsAdapter(this);
		mListView.setAdapter(mAdapter);
	}

	private void updateCheckedPositions() {
		for (int i = 0 ; i<mAdapter.getCount() ; i++) {
			if (mLinks != null) {
				for (NoteTagLink link : mLinks) {
					if (link.getTagId() == mTags.get(i).getId()) {
						mListView.setItemChecked(i, true);
						break;
					}
				}
			} else {
				mListView.setItemChecked(i, false);
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_choose_tag, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.new_tag:
			startActivity(new Intent(this, CreateTagActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
