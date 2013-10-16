package se.emilsjolander.sprinkles;

import se.emilsjolander.sprinkles.models.Tag;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class CreateTagActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_tag);
		
		final EditText tagName = (EditText) findViewById(R.id.tag_name);

		findViewById(R.id.create).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Tag tag = new Tag();
				tag.setName(tagName.getText().toString());
				tag.save();
				finish();
			}
		});
	}

}
