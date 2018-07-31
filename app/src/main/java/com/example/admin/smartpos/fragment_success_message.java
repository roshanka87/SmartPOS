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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.justtide.osapp.util.HttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import okhttp3.Response;

import static com.justtide.osapp.util.HideKeyBoard.hideKeyboard;

public class fragment_success_message extends Fragment {

	String Para = "";

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
		hideKeyboard(getActivity());

		return inflater.inflate(R.layout.fragment_success_message, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ImageButton relative_Back = (ImageButton) view.findViewById(R.id.relative_BacktoBillPaySuccess);
		relative_Back.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				((activity_main) getActivity()).displaySelectedScreen(R.id.relative_BacktoBillPaySuccess,null);
			}
		});

		Para = getArguments().getString("Para");
		AsyncTask_Validation validation = new AsyncTask_Validation();
		validation.execute();

		getActivity().setTitle("Transaction Success");
	}


	class AsyncTask_Validation extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {

			String[] separated = Para.split("-");

			TextView textview_TransactionId=(TextView)getActivity().findViewById(R.id.textview_TransactionId);
			textview_TransactionId.setText(": "+separated[0]);

			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat mdformat = new SimpleDateFormat("yyyy/MM/dd HH:mm ss");

			TextView textview_TransactionDate=(TextView)getActivity().findViewById(R.id.textview_TransactionDate);
			textview_TransactionDate.setText(": "+mdformat.format(calendar.getTime()));

			TextView textview_TransactionAmount=(TextView)getActivity().findViewById(R.id.textview_TransactionAmount);
			textview_TransactionAmount.setText(": LKR "+separated[1]);

			TextView textview_TransactionPaymentType=(TextView)getActivity().findViewById(R.id.textview_TransactionPaymentType);
			textview_TransactionPaymentType.setText(": "+separated[2]);

			return null;
		}
	}
}
