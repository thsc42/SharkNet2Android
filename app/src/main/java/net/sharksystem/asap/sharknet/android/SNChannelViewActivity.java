package net.sharksystem.asap.sharknet.android;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import net.sharksystem.R;
import net.sharksystem.SharkException;
import net.sharksystem.asap.ASAPChannel;
import net.sharksystem.android.ASAPChannelIntent;

public class SNChannelViewActivity extends SNChannelsActivity {
    private RecyclerView mRecyclerView;
    private SNChannelViewContentAdapter mAdapter;

    private CharSequence channelURI = null;
    private CharSequence name;
    private ASAPChannel asapChannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(this.getLogStart(), "onCreate");

        // get parameters
        try {
            ASAPChannelIntent intent = new ASAPChannelIntent(this.getIntent());
            this.channelURI = intent.getUri();
            this.name = intent.getName();

        } catch (SharkException e) {
            Log.d(this.getLogStart(), "could not read from intent (fatal): "
                    + e.getLocalizedMessage());

            return;
        }

        try {
//            setContentView(R.layout.makan_view_drawer_layout);
            setContentView(R.layout.sn_channel_view_drawer_layout);

            this.getASAPApplication().setupDrawerLayout(this);

            ////////////////////////////////////////////////////////////////////////
            //                         prepare action bar                         //
            ////////////////////////////////////////////////////////////////////////
            // setup toolbar
//            Toolbar myToolbar = (Toolbar) findViewById(R.id.makan_view_with_toolbar);
            Toolbar myToolbar = (Toolbar) findViewById(R.id.sn_channel_view_drawer_layout);
            setSupportActionBar(myToolbar);

            ////////////////////////////////////////////////////////////////////////
            //                         prepare recycler view                      //
            ////////////////////////////////////////////////////////////////////////

//            mRecyclerView = (RecyclerView) findViewById(R.id.makan_view_recycler_view);
            mRecyclerView = (RecyclerView) findViewById(R.id.sn_channel_view_recycler_view);

            this.asapChannel =
                    SNChannelsComponent.getSharkNetChannelComponent().getStorage(this.channelURI);

            mAdapter = new SNChannelViewContentAdapter(
                    this,
                    this.asapChannel,
                    this.channelURI, this.name);

            RecyclerView.LayoutManager mLayoutManager =
                    new LinearLayoutManager(getApplicationContext());
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setAdapter(mAdapter);
        }
        catch(Exception e) {
            // debug break
            int i = 42;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////
    //                               sn channel toolbar methods                    //
    /////////////////////////////////////////////////////////////////////////////////

    /**
     * connect menu with menu items and make them visible
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sn_channel_view_action_buttons, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        try {
            switch (item.getItemId()) {
                case R.id.snChannelViewMenuAddMessage:
                    this.doAddMessage();
                    return true;

                default:
                    // If we got here, the user's action was not recognized.
                    // Invoke the superclass to handle it.
                    return super.onOptionsItemSelected(item);
            }
        }
        catch(Exception e) {
            Log.d(this.getLogStart(), e.getLocalizedMessage());
        }

        return false;
    }

    private void doAddMessage() {
        String sampleLine = Long.toString(System.currentTimeMillis());
        Log.d(this.getLogStart(), "doAddMessageCalled");

        ASAPChannelIntent intent = new ASAPChannelIntent(this,
                this.name, this.channelURI,
                SNChannelAddMessageActivity.class);

        startActivity(intent);
    }

    protected void onResume() {
        super.onResume();
        this.resetAdapter(); // that pretty resource intensive but works
        //mAdapter.notifyDataSetChanged(); // simple refresh
    }

    private void resetAdapter() {
        // reset adapter to get access to new data
        mAdapter = new SNChannelViewContentAdapter(this,
                this.asapChannel,
                this.channelURI, this.name);
        Log.d(this.getLogStart(), "recreate adapter");
        this.mRecyclerView.setAdapter(this.mAdapter);
        Log.d(this.getLogStart(), "notify data set changed");
        mAdapter.notifyDataSetChanged();
    }

    public void asapUriContentChanged(CharSequence changedUri) {
        Log.d(this.getLogStart(), "uriContentChanged: " + changedUri);

        if(this.channelURI.toString().equalsIgnoreCase(changedUri.toString())) {
            this.resetAdapter();
        } else {
            Log.d(this.getLogStart(), "not my uri: " + this.channelURI);
        }
    }

}
