package com.lsjwzh.orm.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.lsjwzh.orm.QueryBuilder;
import com.lsjwzh.orm.sample.models.NoteTagLink;
import com.lsjwzh.orm.sample.models.Tag;

import java.util.List;

import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import se.emilsjolander.sprinkles.sample.R;

public class ChooseTagActivity extends Activity {

    public static final String EXTRA_NOTE_ID = "note_id";

    private ListView mListView;
    private TagsAdapter mAdapter;

    private List<Tag> mTags;
    private List<NoteTagLink> mLinks;


    private OnItemClickListener onListItemClicked = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> l, View v, int pos,
                                long id) {
            NoteTagLink link = new NoteTagLink(mNoteId, id);
            if (mListView.isItemChecked(pos)) {
                MyApplication.getApplication().rxSprinkles
                        .save(link)
                        .subscribeOn(Schedulers.io())
                        .subscribe();
            } else {
                MyApplication.getApplication().rxSprinkles
                        .delete(link)
                        .subscribeOn(Schedulers.io())
                        .subscribe();
            }
        }
    };

    private long mNoteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivty_choose_tag);

        mNoteId = getIntent().getLongExtra(EXTRA_NOTE_ID, -1);

        MyApplication.getApplication().rxSprinkles
                .query(QueryBuilder.from(Tag.class).where().end())
                .toList()
                .subscribe(new Consumer<List<Tag>>() {
                  @Override
                  public void accept(List<Tag> tags) throws Exception {
                    mTags = tags;
                    mAdapter.swapTags(tags);
                    updateCheckedPositions();
                  }
                });
        MyApplication.getApplication().rxSprinkles
                .query(QueryBuilder.from(NoteTagLink.class).where()
                        .equalTo("note_id", mNoteId).end())
                .toList()
                .subscribe(new Consumer<List<NoteTagLink>>() {
                  @Override
                  public void accept(List<NoteTagLink> noteTagLinks) throws Exception {
                    mLinks = noteTagLinks;
                    updateCheckedPositions();
                  }
                });

        mListView = (ListView) findViewById(R.id.list);
        mListView.setEmptyView(findViewById(R.id.empty));
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mListView.setOnItemClickListener(onListItemClicked);

        mAdapter = new TagsAdapter(this);
        mListView.setAdapter(mAdapter);
    }

    private void updateCheckedPositions() {
        for (int i = 0; i < mAdapter.getCount(); i++) {
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
