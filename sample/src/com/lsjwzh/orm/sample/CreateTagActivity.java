package com.lsjwzh.orm.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.lsjwzh.orm.sample.models.Tag;

import rx.Subscriber;
import se.emilsjolander.sprinkles.sample.R;

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
                MyApplication.getApplication()
                        .rxSprinkles.save(tag)
                        .subscribe(new Subscriber<Tag>() {
                            @Override
                            public void onCompleted() {
                                finish();
                            }

                            @Override
                            public void onError(Throwable e) {
                                Toast.makeText(CreateTagActivity.this,
                                        R.string.could_not_save_tag, Toast.LENGTH_SHORT)
                                        .show();
                            }

                            @Override
                            public void onNext(Tag tag) {

                            }
                        });
            }
        });
    }

}
