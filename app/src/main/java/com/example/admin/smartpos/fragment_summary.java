package com.example.admin.smartpos;


import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.justtide.osapp.util.DialogService;
import com.justtide.osapp.util.HttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Response;

import static com.justtide.osapp.util.HideKeyBoard.hideKeyboard;

/**
 * Created by admin on 11/07/2018.
 */

public class fragment_summary extends Fragment {
	private AlertDialog dialog;
	String TransactionType = "";
	String Amount = "";
	JSONArray nameTable = null;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		hideKeyboard(getActivity());
		DialogService.progressDialog((activity_main)getContext(),true);
		return inflater.inflate(R.layout.fragment_summary, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		AsyncTask_Validation validation = new AsyncTask_Validation();
		validation.execute();

		getActivity().setTitle("Summary");
	}

	class AsyncTask_Validation extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			try {

				String url = "http://" + getString(R.string.server_name) + "/smartpos/WebAPI.php?Type=TransactionSummary&TerminalId=&DocumentType=&Amount=&AccountNo=";
				HttpClient client = new HttpClient();
				Response response = client.get(url);

				if (!response.isSuccessful()) {
					return null;
				} else {

					String result = response.body().string();
					JSONObject reader = new JSONObject(result);
					nameTable = reader.getJSONArray("Result");


					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {

							for (int i = 0; i < nameTable.length(); i++) {

								try {

									JSONObject item = nameTable.getJSONObject(i);
									TransactionType = item.getString("TransactionType");
									Amount = item.getString("Amount");

									if (TransactionType.equals("CP")) {
										TextView textview_CardPayment = (TextView) getActivity().findViewById(R.id.textview_SuCardPayment);
										textview_CardPayment.setText(Amount);
									}
									if (TransactionType.equals("DP")) {
										TextView textview_DigitlPayment = (TextView) getActivity().findViewById(R.id.textview_SuDigitlPayment);
										textview_DigitlPayment.setText(Amount);
									}
									if (TransactionType.equals("BP")) {
										TextView textview_Bill = (TextView) getActivity().findViewById(R.id.textview_SuBill);
										textview_Bill.setText(Amount);
									}
									if (TransactionType.equals("CO")) {
										TextView textview_Commission = (TextView) getActivity().findViewById(R.id.textview_SuCommission);
										textview_Commission.setText("+"+Amount);
									}
								} catch (JSONException e) {
								}
							}
						}

					});

					DialogService.progressDialog((activity_main)getContext(),false);
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
					}
				});
			} catch (JSONException e) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						DialogService.progressDialog((activity_main)getContext(),false);
						AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
						builder.setMessage("JSONException\nPlease check the connection.");
						dialog = builder.create();
						dialog.show();
					}
				});
			} catch (NetworkOnMainThreadException e) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						DialogService.progressDialog((activity_main)getContext(),false);
						AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
						builder.setMessage("NetworkOnMainThreadException\nPlease check the connection.");
						dialog = builder.create();
						dialog.show();
					}
				});
			}
			return null;
		}
	}
}
