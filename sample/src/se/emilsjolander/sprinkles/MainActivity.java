package se.emilsjolander.sprinkles;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import se.emilsjolander.sprinkles.Query.OnQueryResultHandler;
import se.emilsjolander.sprinkles.models.Note;
import se.emilsjolander.sprinkles.models.NoteTagLink;

public class MainActivity extends Activity {

	private ListView mListView;
	private NotesAdapter mAdapter;

	private OnQueryResultHandler<CursorList<Note>> onNotesLoaded = new OnQueryResultHandler<CursorList<Note>>() {
		@Override
		public void onResult(CursorList<Note> result) {
			mAdapter.setNotes(result.asList());
            result.close();
		}
	};

	private OnItemClickListener onNoteSelected = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> l, View v, int position, long id) {
			Intent i = new Intent(MainActivity.this, CreateNoteActivity.class);
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

		Query.many(Note.class, "select * from Notes order by created_at desc")
				.getAsyncWithUpdates(getLoaderManager(), onNotesLoaded,
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
			startActivity(new Intent(MainActivity.this,
					CreateNoteActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
