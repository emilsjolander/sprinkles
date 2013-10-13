package se.emilsjolander.sprinkles;

import java.util.List;

import se.emilsjolander.sprinkles.Query.OnQueryResultHandler;
import se.emilsjolander.sprinkles.models.Note;
import se.emilsjolander.sprinkles.models.NoteTagLink;
import se.emilsjolander.sprinkles.models.Tag;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Note myNote = new Note();
        myNote.setContent(""+System.currentTimeMillis());
        myNote.save();
        
        Tag myTag = Query.one(Tag.class, "select * from Tags").get();
        if (myTag == null) {
        	myTag = new Tag();
        	myTag.setName("MyTag");
        	myTag.setColor(0xffff3333);
        	myTag.save();
        }
        
        NoteTagLink link = new NoteTagLink();
        link.setNoteId(myNote.getId());
        link.setTagId(myTag.getId());
        link.save();
        
        Query.many(Note.class, "select Notes.* from Notes "
        		+ "inner join NoteTagLinks on Notes.id=NoteTagLinks.note_id "
        		+ "where NoteTagLinks.tag_id=?", myTag.getId())
        		.getAsync(getLoaderManager(), new OnQueryResultHandler<List<Note>>() {

					@Override
					public void onResult(List<Note> result) {
						Toast.makeText(MainActivity.this, ""+result.size(), Toast.LENGTH_SHORT).show();
					}
        			
				});
    }
    
}
