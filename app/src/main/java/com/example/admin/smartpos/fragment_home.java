package com.example.admin.smartpos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.messaging.FirebaseMessaging;
import com.justtide.osapp.util.DialogService;
import com.justtide.osapp.util.HttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;

import static com.justtide.osapp.util.HideKeyBoard.hideKeyboard;

/**
 * Created by admin on 10/07/2018.
 */

public class fragment_home extends Fragment {

	String Balance = "";
	List<String> BindData = null;
	JSONArray ResultArray = null;
	JSONArray ListViewArray = null;
	private AlertDialog dialog;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		hideKeyboard(getActivity());
		DialogService.progressDialog((activity_main)getContext(),true);
		return inflater.inflate(R.layout.fragment_home, container, false);
	}


	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);


		Button button_CardPayment = (Button) view.findViewById(R.id.button_cardpayment);
		button_CardPayment.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				((activity_main) getActivity()).displaySelectedScreen(R.id.nav_cardpayment, null);
			}
		});

		Button button_DigitalPayment = (Button) view.findViewById(R.id.button_digitalpayment);
		button_DigitalPayment.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				((activity_main) getActivity()).displaySelectedScreen(R.id.nav_digitalpayment, null);
			}
		});

		Button button_BillPayment = (Button) view.findViewById(R.id.button_billpayment);
		button_BillPayment.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				((activity_main) getActivity()).displaySelectedScreen(R.id.nav_billpayment, null);
			}
		});


		AsyncTask_Validation validation = new AsyncTask_Validation();
		validation.execute();


		getActivity().setTitle("Home");
	}


	class AsyncTask_Validation extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			try {

				String url = "";
				HttpClient client = new HttpClient();
				Response response = null;
				//Get Balnce Amount
				url = "http://" + getString(R.string.server_name) + "/smartpos/WebAPI.php?Type=BalanceAmount";

				response = client.get(url);

				if (!response.isSuccessful()) {
					return null;
				} else {

					String result = response.body().string();
					JSONObject reader = new JSONObject(result);
					JSONArray nameTable = reader.getJSONArray("Result");
					JSONObject item = nameTable.getJSONObject(0);
					Balance = item.getString("Balance");

					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							TextView txtAmount = (TextView) getActivity().findViewById(R.id.textView_amount);
							txtAmount.setText(Balance);
						}
					});

				}


				//Get Balnce Amount
				url = "http://" + getString(R.string.server_name) + "/smartpos/WebAPI.php?Type=Top10Transaction";
				response = client.get(url);

				if (!response.isSuccessful()) {
					return null;
				} else {

					String result = response.body().string();
					JSONObject reader = new JSONObject(result);
					ResultArray = reader.getJSONArray("Result");


					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							int length = ResultArray.length();
							BindData = new ArrayList<String>(length);



							for (int i = 0; i < length; i++) {

								try {
									JSONObject item = ResultArray.getJSONObject(i);
									String TransactionType = item.getString("TransactionType").toString();
									String Value = "";
									if (TransactionType.equals("CP")) {
										Value = "Transaction Id    : CP" + item.getString("TransactionId") +
												"\nDate                     : " + item.getString("TransactionDateTime") +
												"\nAmount               : LKR	" + item.getString("Amount") + "  |  Card";
									}
									if (TransactionType.equals("DP")) {
										Value = "Transaction Id    : DP" + item.getString("TransactionId") +
												"\nDate                     : " + item.getString("TransactionDateTime") +
												"\nAmount               : LKR	+" + item.getString("Amount") + "  |  Digital";
									}
									if (TransactionType.equals("BP")) {
										Value = "Transaction Id    : BP" + item.getString("TransactionId") +
												"\nDate                     : " + item.getString("TransactionDateTime") +
												"\nAmount               : LKR	-" + item.getString("Amount") + "  |  Bill";
									}
									if (TransactionType.equals("CO")) {
										Value = "Transaction Id    : CO" + item.getString("TransactionId") +
												"\nDate                     : " + item.getString("TransactionDateTime") +
												"\nAmount               : LKR	+" + item.getString("Amount") + "  |  Commission";
									}

									BindData.add(Value);
								} catch (JSONException e) {

								}
							}
							DialogService.progressDialog((activity_main)getContext(),false);

							ListView myListView = (ListView) getActivity().findViewById(R.id.listView_TopTen);
							myListView.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, BindData));
						}
					});

				}


			} catch (IOException e) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						DialogService.progressDialog((activity_main)getContext(),false);
						AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
						builder.setMessage("IOException\nPlease check the connection.");
						dialog = builder.create();
						dialog.show();
					}});
			} catch (JSONException e) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						DialogService.progressDialog((activity_main)getContext(),false);
						AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
						builder.setMessage("JSONException\nPlease check the connection.");
						dialog = builder.create();
						dialog.show();
					}});
			} catch (NetworkOnMainThreadException e) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						DialogService.progressDialog((activity_main)getContext(),false);
						AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
						builder.setMessage("NetworkOnMainThreadException\nPlease check the connection.");
						dialog = builder.create();
						dialog.show();
					}});
			}
			return null;
		}
	}



}
