package net.sharksystem.persons.android;

import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import net.sharksystem.R;
import net.sharksystem.SharkException;
import net.sharksystem.asap.android.Util;
import net.sharksystem.crypto.ASAPCertificate;
import net.sharksystem.sharknet.android.SharkNetActivity;
import net.sharksystem.sharknet.android.SharkNetApp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CertificateListActivity extends SharkNetActivity {
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private CertificateListContentAdapter mAdapter;

    public CertificateListActivity() {
        super(SharkNetApp.getSharkNetApp());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(this.getLogStart(), "onCreate");

        setContentView(R.layout.certificate_list_drawer_layout);

        List<ASAPCertificate> certList = null;

        // find out what to do
        try {
            PersonIntent intent = new PersonIntent(this.getIntent());
            if(intent.isOwnerIDSet()) {
                if(intent.explainIdentityAssurance()) {
                    certList = this.produceListToExplain(intent.getSubjectID());
                } else {
                    certList = this.produceListBySubject(intent.getSubjectID());
                }
            } else {
                if(intent.isSignerIDSet()) {
                    certList = this.produceListByIssuer(intent.getIssuerID());
                } else {
                    Toast.makeText(this,
                            "internal failure: neither owner nor signer id set",
                            Toast.LENGTH_SHORT).show();
                    this.finish();
                    return;
                }
            }

            ////////////////////////////////////////////////////////////////////////
            //                         prepare action bar                         //
            ////////////////////////////////////////////////////////////////////////
            // setup toolbar
            Toolbar myToolbar = (Toolbar) findViewById(R.id.certificate_list_with_toolbar);
            setSupportActionBar(myToolbar);

            ////////////////////////////////////////////////////////////////////////
            //                         prepare recycler view                      //
            ////////////////////////////////////////////////////////////////////////
            mRecyclerView = (RecyclerView) findViewById(R.id.certificate_list_recycler_view);

            mAdapter = new CertificateListContentAdapter(this, certList);
            RecyclerView.LayoutManager mLayoutManager =
                    new LinearLayoutManager(getApplicationContext());

            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setAdapter(mAdapter);
            Log.d(this.getLogStart(), "attached content adapter");
        }
        catch(Exception e) {
            Log.d(this.getLogStart(), "problems while setting up activity and content adapter: "
                    + e.getLocalizedMessage());
            // debug break
            int i = 42;
        }
    }

    private List<ASAPCertificate> produceCertList(Collection<ASAPCertificate> certColl) {
        List<ASAPCertificate> certList = new ArrayList<>();

        for(ASAPCertificate cert : certColl) {
            certList.add(cert);
        }

        return certList;
    }

    private List<ASAPCertificate> produceListBySubject(CharSequence userID) throws SharkException {
        Collection<ASAPCertificate> certColl =
                PersonsStorageAndroid.getPersonsApp().getCertificateBySubject(userID);

        return this.produceCertList(certColl);
    }

    private List<ASAPCertificate> produceListByIssuer(CharSequence userID) throws SharkException {
        Collection<ASAPCertificate> certColl =
                PersonsStorageAndroid.getPersonsApp().getCertificateByIssuer(userID);

        return this.produceCertList(certColl);
    }

    private List<ASAPCertificate> produceListToExplain(CharSequence userID)
            throws SharkException {

        PersonsStorageAndroid personsApp = PersonsStorageAndroid.getPersonsApp();

        List<ASAPCertificate> certList = new ArrayList<>();
        List<CharSequence> idPath = personsApp.getIdentityAssurancesCertificationPath(userID);

        if(idPath.isEmpty()) {
            Toast.makeText(this, "Person can not be verified", Toast.LENGTH_LONG).show();
            this.finish();
            return null;
        }

        if(idPath.get(0).toString().equalsIgnoreCase(personsApp.getOwnerID().toString())) {
            // direct certificate
            Toast.makeText(this, "You met this person and signed a certificate",
                    Toast.LENGTH_LONG).show();
            this.finish();
            return null;
        }

        for(CharSequence id : idPath) {
            certList.add(personsApp.getCertificateBySubject(id).iterator().next());
        }

        return certList;
    }

    /////////////////////////////////////////////////////////////////////////////////
    //                              toolbar methods                                //
    /////////////////////////////////////////////////////////////////////////////////

    /**
     * connect menu with menu items and make them visible
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(this.getLogStart(), "onCreateOptionsMenu called");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.person_list_selection_action_buttons, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        try {
            switch (item.getItemId()) {
                case R.id.personListSelectionDoneButton:
                    Toast.makeText(this, "something clicked", Toast.LENGTH_SHORT).show();
                    return true;

                case R.id.abortButton:
                    this.finish();
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

    protected void onResume() {
        super.onResume();
        Log.d(Util.getLogStart(this), "onResume: assume data set changed.");
        this.mAdapter.notifyDataSetChanged();
    }
}
