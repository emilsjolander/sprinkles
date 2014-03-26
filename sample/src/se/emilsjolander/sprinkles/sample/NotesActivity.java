package se.emilsjolander.sprinkles.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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

public class NotesActivity extends FragmentActivity {

	private ListView mListView;
	private NotesAdapter mAdapter;

	private ManyQuery.ResultHandler<Note> onNotesLoaded =
            new ManyQuery.ResultHandler<Note>() {

        @Override
		public boolean handleResult(CursorList<Note> result) {
            mAdapter.swapNotes(result);
            return true;
		}
	};

	private OnItemClickListener onNoteSelected =
            new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> l, View v, int position, long id) {
			Intent i = new Intent(NotesActivity.this, CreateNoteActivity.class);
			i.putExtra(CreateNoteActivity.EXTRA_NOTE_ID, id);
			startActivity(i);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mListView = (ListView) findViewById(R.id.list);

		mAdapter = new NotesAdapter(this);
		mListView.setAdapter(mAdapter);
		mListView.setEmptyView(findViewById(R.id.empty));
		mListView.setOnItemClickListener(onNoteSelected);

		Query.many(Note.class,
                "select Notes.*, " +
                        "(select count(*) from NoteTagLinks where NoteTagLinks.note_id = Notes.id) as tag_count " +
                        "from Notes order by created_at desc"
        )
				.getAsync(getLoaderManager(), onNotesLoaded,
						NoteTagLink.class);

    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.new_note:
			startActivity(new Intent(NotesActivity.this,
					CreateNoteActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
