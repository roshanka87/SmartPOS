package com.example.admin.smartpos;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;


public class activity_main extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //FirebaseApp.initializeApp(this);
        FirebaseMessaging.getInstance().subscribeToTopic("all");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        displaySelectedScreen(R.id.nav_home,null);
//        TextView lbl_userid = (TextView) findViewById(R.id.textView_userid);
//        TextView lbl_fullname = (TextView) findViewById(R.id.textView_fullname);
//
//        String UserId=getString(R.string.userid);
//        String FullName=getString(R.string.fullname);
//
//        lbl_userid.setText(UserId);
//        lbl_userid.setText(FullName);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);





    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        displaySelectedScreen(id,null);

        return super.onOptionsItemSelected(item);
    }


    public void displaySelectedScreen(int id,String Para) {
        Fragment fragment = null;
		Bundle bundle = new Bundle();

        switch (id) {
            case R.id.nav_home:
                fragment = new fragment_home();
                break;
            case R.id.nav_cardpayment:
                fragment = new fragment_cardpayment();

                break;
            case R.id.nav_digitalpayment:
                fragment = new fragment_digitalpayment();
                break;
            case R.id.nav_billpayment:
                fragment = new fragment_billpayment();
                break;
            case R.id.nav_history:
                fragment = new fragment_summary();
                break;



            case R.id.button_CashPayment:
                fragment = new fragment_billpayment_cash();
                break;


			//Bill type
			case R.id.button_Mobile:
				fragment = new fragment_billpayment_category();
				bundle.putString("1","mobile");
				fragment.setArguments(bundle);
				break;
			case R.id.button_Telephone:
				fragment = new fragment_billpayment_category();
				bundle.putString("1","telephone");
				fragment.setArguments(bundle);
				break;
			case R.id.button_Internet:
				fragment = new fragment_billpayment_category();
				bundle.putString("1","internet");
				fragment.setArguments(bundle);
				break;
			case R.id.button_Utility:
				fragment = new fragment_billpayment_category();
				bundle.putString("1","utility");
				fragment.setArguments(bundle);
				break;
			case R.id.button_Television:
				Toast.makeText(getApplicationContext(), "This Service will be Available Soon.", Toast.LENGTH_SHORT).show();
				break;
			case R.id.button_Finance:
				fragment = new fragment_billpayment_category();
				bundle.putString("1","finance");
				fragment.setArguments(bundle);
				break;
			case R.id.button_OtherService:
				Toast.makeText(getApplicationContext(), "This Service will be Available Soon.", Toast.LENGTH_SHORT).show();
				break;



			//Bill Pay Interface
			case R.id.bill_one:
				fragment = new fragment_billpayment_detail();
				bundle.putString("billtype",Para);
				fragment.setArguments(bundle);
				break;
			case R.id.bill_two:
				fragment = new fragment_billpayment_detail();
				bundle.putString("billtype",Para);
				fragment.setArguments(bundle);
				break;
			case R.id.bill_four:
				fragment = new fragment_billpayment_detail();
				bundle.putString("billtype",Para);
				fragment.setArguments(bundle);
				break;
			case R.id.bill_five:
				fragment = new fragment_billpayment_detail();
				bundle.putString("billtype",Para);
				fragment.setArguments(bundle);
				break;
			case R.id.bill_six:
				fragment = new fragment_billpayment_detail();
				bundle.putString("billtype",Para);
				fragment.setArguments(bundle);
				break;
			case R.id.bill_seven:
				fragment = new fragment_billpayment_detail();
				bundle.putString("billtype",Para);
				fragment.setArguments(bundle);
				break;





			//Back Keys
			case R.id.relative_BacktoHome:
				fragment = new fragment_home();
				break;
            case R.id.relative_BacktoBillPay:
                fragment = new fragment_billpayment();
                break;
			case R.id.relative_BacktoBillPayCash:
				fragment = new fragment_billpayment_cash();
				break;
			case R.id.relative_BacktoBillPayCashCategory:
				fragment = new fragment_billpayment_cash();
				break;
			case R.id.relative_BacktoBillPaySuccess:
				fragment = new fragment_home();
				break;



			case R.id.button_Pay:
				fragment = new fragment_success_message();
				bundle.putString("Para",Para);
				fragment.setArguments(bundle);
				break;


            case R.id.nav_logout:
                LogOut();
                break;
        }

        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.rootLayout, fragment);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        displaySelectedScreen(id,null);

        return true;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitByBackKey();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void exitByBackKey() {
        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setMessage("Do you want to exit Application?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        System.exit(0);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {

                    // do something when the p_button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
                    }
                })
                .show();

    }

    protected void LogOut() {
        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setMessage("Do you want to LogOut Application?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        Intent Main = new Intent(activity_main.this, activity_login.class);
                        startActivity(Main);
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {

                    // do something when the p_button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                })
                .show();

    }
}
