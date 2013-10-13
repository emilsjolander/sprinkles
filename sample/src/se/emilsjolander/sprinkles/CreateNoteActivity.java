package se.emilsjolander.sprinkles;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import se.emilsjolander.sprinkles.models.Note;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class CreateNoteActivity extends Activity {

	public static final String EXTRA_NOTE_ID = "note_id";
	
	private EditText mNoteContent;
	private Note mNote;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_note);
		
		long noteId = getIntent().getLongExtra(EXTRA_NOTE_ID, -1);
		if (noteId < 0) {
			mNote = new Note();
			mNote.save();
		} else {
			mNote = Query.one(Note.class, "select * from Notes where id=?", noteId).get();
		}
		
		final TextView lastUpdatedAt = (TextView) findViewById(R.id.last_updated);
		mNoteContent = (EditText) findViewById(R.id.note_content);
		
		if (mNote.exists()) {
			String updatedAtString = new SimpleDateFormat("HH:mm EEEE", Locale.getDefault()).format(new Date(mNote.getUpdatedAt()));
			lastUpdatedAt.setText(getString(R.string.last_updated, updatedAtString));
		} else {
			lastUpdatedAt.setVisibility(View.GONE);
		}
		
		mNoteContent.setText(mNote.getContent());
	}
	
	@Override
	public void finish() {
		mNote.setContent(mNoteContent.getText().toString());
		mNote.save();
		super.finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_create_note, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.tags:
			Intent i = new Intent(this, ChooseTagActivity.class);
			i.putExtra(ChooseTagActivity.EXTRA_NOTE_ID, mNote.getId());
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
