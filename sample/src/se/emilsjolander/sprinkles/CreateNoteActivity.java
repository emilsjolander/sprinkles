package se.emilsjolander.sprinkles;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import se.emilsjolander.sprinkles.models.Note;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
		if (mNote.save()) {
			super.finish();
		} else {
			Toast.makeText(this, R.string.could_not_save_note, Toast.LENGTH_SHORT).show();
		}
	}

}
