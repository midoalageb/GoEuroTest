package com.test.goeurotest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class MainActivity extends Activity {
AutoCompleteTextView ACTV_start,ACTV_end;
EditText ET_date;
ImageButton IB_pick_date;
Button Btn_search;
Location location;
Criteria criteria;
final static int CRITER=Criteria.NO_REQUIREMENT;
final static String TAG="GoEuro_Test_MainActivity";
final static String API_URL="http://pre.dev.goeuro.de:12345/api/v1/suggest/position/en/name/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Initialize Views
		ACTV_start=(AutoCompleteTextView) findViewById(R.id.ACTV_Start_location);
		ACTV_end=(AutoCompleteTextView) findViewById(R.id.ACTV_End_location);
		ET_date=(EditText) findViewById(R.id.ET_Date);
		IB_pick_date=(ImageButton) findViewById(R.id.IB_Calendar_start);
		Btn_search=(Button) findViewById(R.id.Btn_Search);
		
		
		//Set Criteria
		criteria=new Criteria();
		criteria.setAccuracy(CRITER);
		
		//Get Location
		location=getLocation();
		if(location==null) Toast.makeText(getApplicationContext(), getResources().getString(R.string.enable_gps), Toast.LENGTH_LONG).show();
		//Setting TextWatcher
		ACTV_start.addTextChangedListener(TW1);
		ACTV_end.addTextChangedListener(TW2);
		
		//Start DialogPicker dialog on Click
		IB_pick_date.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Calendar cal=Calendar.getInstance();
				DatePickerDialog dialog=new DatePickerDialog(MainActivity.this,
						date_chng_listener,
						cal.get(Calendar.YEAR),
						cal.get(Calendar.MONTH),
						cal.get(Calendar.DAY_OF_MONTH));
				dialog.show();
			}
		});
		
		Btn_search.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String msg="";
				if(ACTV_start.getText().toString()==null||ACTV_start.getText().toString().equals("")){
					msg=getResources().getString(R.string.start_missing);
				}else if(ACTV_end.getText().toString()==null||ACTV_end.getText().toString().equals("")){
					msg=getResources().getString(R.string.end_missing);
				}else if(ET_date.getText().toString()==null||ET_date.getText().toString().equals("")){
					msg=getResources().getString(R.string.date_missing);
				}else{
					msg=getResources().getString(R.string.search_result);
				}
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
			}
		});
		
	}

	
	//Gets last known location 
	private Location getLocation(){
		LocationManager loc=(LocationManager) getSystemService(Context.LOCATION_SERVICE);
		String provider=loc.getBestProvider(criteria, true);
		return loc.getLastKnownLocation(provider);
	}
	
	//TextWatcher for ACTV_start
	private TextWatcher TW1=new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			if(s.length()>1&&s.length()<3) new Asyn_get_JSON().execute(API_URL+s.toString(),"1");
		}
	};
	
	//TextWatcher for ACTV_end
	private TextWatcher TW2=new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			
		}
		
		@Override
		public void afterTextChanged(Editable s) {
			if(s.length()>1&&s.length()<3) new Asyn_get_JSON().execute(API_URL+s.toString(),"2");
		}
	};
	
	
	
	
	private class Asyn_get_JSON extends AsyncTask<String, Void, String[]>{
		ProgressDialog dialog_glob;
		String TV_id;
		protected void onPreExecute() {
			dialog_glob= new ProgressDialog(MainActivity.this);
			dialog_glob.setMessage(getResources().getString(R.string.loading));
			dialog_glob.setCancelable(false);
			dialog_glob.dismiss();
			dialog_glob.show();
			super.onPreExecute();
		}
		
		@Override
		protected String[] doInBackground(String... params) {
			String urll = params[0];
			TV_id="";
			TV_id=params[1];
			JSONArray jsonArray=null;
			String[] result=null;
			try{
	            
	            HttpGet method= new HttpGet(new URI(urll));
	            HttpClient client = new DefaultHttpClient();
	            HttpGet request = new HttpGet();
	            request.setURI(new URI(urll));
	            HttpResponse httpResponse = client.execute(method);
	            
				InputStream in = httpResponse.getEntity().getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				StringBuilder str = new StringBuilder();
				String line = null;
				while((line = reader.readLine()) != null){
					str.append(line + "\n");
				}
				in.close();
				reader.close();
				JSONObject JSON_obj=new JSONObject(str.toString());
				jsonArray=JSON_obj.getJSONArray("results");
				result=JSON_2_String_Array(jsonArray);
			}catch(Exception ex){
				Log.e(TAG, "Error loading JSON");
			}
			return result;
		}
		
		private String[] JSON_2_String_Array(
				JSONArray jsonArray) {
			List<Map<String,String>> list=new ArrayList<Map<String,String>>();
			
			for(int i=0;i<jsonArray.length();i++){
				Map<String,String> map=new HashMap<String,String>();
				try {
					JSONObject json_obj;
					map.put("name", (String) jsonArray.getJSONObject(i).get("name"));
					json_obj=jsonArray.getJSONObject(i).getJSONObject("geo_position");
					
					double longitude_d=(Double) json_obj.get("latitude");
					double latitude_d=(Double) json_obj.get("longitude");
					
					String distance=calc_distance(longitude_d,latitude_d);
					
					map.put("distance",distance);
					
				} catch (JSONException e) {
					Log.e(TAG, "Error converting response to List");
					e.printStackTrace();
				}
				list.add(map);
				map=null;
			}
			Collections.sort(list, new MapComparator("distance"));
			String[] result_array=new String[list.size()];
			for(int i=0;i<list.size();i++){
				result_array[i]=list.get(i).get("name");
			}
			return result_array;
		}

		private String calc_distance(double longit,double latit) {
			Location locat=new Location("");
			locat.setLatitude(latit);
			locat.setLongitude(longit);
			double distance=location.distanceTo(locat);
			return String.valueOf(distance);
		}

		@Override
		protected void onPostExecute(String[] result) {
			ArrayAdapter<String> adap= new ArrayAdapter<String>(getApplicationContext(),
					R.layout.list_item, 
					R.id.TV_item,
					result);
			if(TV_id.equals("1")){
				ACTV_start.setThreshold(2);
				ACTV_start.setAdapter(adap);
				ACTV_start.showDropDown();
			}else if(TV_id.equals("2")){
				ACTV_end.setThreshold(2);
				ACTV_end.setAdapter(adap);
				ACTV_end.showDropDown();
			}
			dialog_glob.dismiss();
		}
		
	}
	
	class MapComparator implements Comparator<Map<String, String>>
	{
	    private final String key;

	    public MapComparator(String key)
	    {
	        this.key = key;
	    }

	    public int compare(Map<String, String> first,
	                       Map<String, String> second)
	    {
	        
	        Double firstValue = Double.valueOf(first.get(key));
	        Double secondValue = Double.valueOf(second.get(key));
	        return firstValue.compareTo(secondValue);
	    }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	DatePickerDialog.OnDateSetListener date_chng_listener=new DatePickerDialog.OnDateSetListener() {
		
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			ET_date.setText(dayOfMonth+"/"+(monthOfYear+1)+"/"+year);
			
		}
	};
	
	
	
}
