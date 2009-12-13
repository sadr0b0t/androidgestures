/*
  * Copyright (C) 2007 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
 
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
 
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 
  */
package org.sadko.gestures;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
//import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
//import android.util.Log;
//import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Manager extends ListActivity {
	ListView lv;
	Cursor c;
	private static final int ADD_NEW_ID = 0;
	private static final int EXIT_ID = 1;
	private static final int ABOUT_ID = 2;
	Button startMyService;
	TextView serviceState;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, ADD_NEW_ID, 0, "Add new").setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, ABOUT_ID, 0, "About").setIcon(android.R.drawable.ic_menu_info_details);
		menu.add(0, EXIT_ID, 0, "Exit").setIcon(android.R.drawable.ic_menu_revert);
		
		// menu.add(0, KILL_SERVICE_ID, 0, "Kill handling service");
		return super.onCreateOptionsMenu(menu);

	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case ADD_NEW_ID: {
			Intent i = new Intent(Manager.this, MotionEditor.class);
			i.setAction(android.content.Intent.ACTION_MAIN);
			startActivity(i);
			break;
		}
		case EXIT_ID: {
			Manager.this.finish();
			break;
		}
		case ABOUT_ID: {
			//TODO: call about activity
			break;
		}
			/*
			 * case KILL_SERVICE_ID: { //Manager.this.finish();
			 * lb.mh.killNotification(); stopService(new Intent(Manager.this,
			 * MotionHandler1.class)); lb=null; break; }
			 */
		}
		return super.onMenuItemSelected(featureId, item);
	}

	SimpleCursorAdapter motions;
	int selectedItem = -1;
	ListnerBinder lb = null;
	ServiceConnection con;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		setContentView(R.layout.main);
		super.onCreate(savedInstanceState);
		startMyService = (Button) findViewById(R.id.service_start);
		startService(new Intent(this, MotionHandler1.class));
		//startMyService.setEnabled(false);
		startMyService.setTextSize(20);
		serviceState = (TextView) findViewById(R.id.text_about_service_state);
		/*
		 * if(savedInstanceState!=null &&
		 * savedInstanceState.containsKey("process"))
		 * startMyService.setText(savedInstanceState
		 * .getBoolean("process")?"stop":"start");
		 */

		lv = getListView();
		fillListView();
		startMyService.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				switchService();
				startMyService.setText(isServiceEnabled() ? "stop" : "start");
				serviceState.setText("Gestures service is"
						+ (lb.mh.isEnabled ? " running" : " idle"));

				/*Cursor c = getContentResolver().query(
						MotionsDB.MOTIONS_CONTENT_URI,
						new String[] { "count(_ID)" }, null, null, null);
				c.moveToFirst();
				if (lb == null || lb.mh == null)
					return;
				if (c.getInt(0) == 0 && lb.mh.isEnabled)
					startMyService.setEnabled(true);
				if (c.getInt(0) == 0 && !lb.mh.isEnabled)
					startMyService.setEnabled(false);*/

			}

		});

	}

	@Override
	protected void onPause() {

		super.onPause();
		unbindService(con);
	}

	@Override
	protected void onStart() {
		super.onStart();
		con = new ServiceConnection() {
			public void onServiceConnected(ComponentName arg0, IBinder arg1) {
				lb = (ListnerBinder) arg1;
				//Log.i("handler", lb.mh + "");
				startMyService.setText(lb.mh.isEnabled ? "stop" : "start");
				serviceState.setText("Gestures service is"
						+ (lb.mh.isEnabled ? " running" : " idle"));
				Cursor c = getContentResolver().query(
						MotionsDB.MOTIONS_CONTENT_URI,
						new String[] { "count(_ID)" }, null, null, null);
				c.moveToFirst();
				if (c.getInt(0) == 0 && lb.mh.isEnabled)
					startMyService.setEnabled(true);
				if (c.getInt(0) == 0)
					return;
				c.close();
				lb.ms = new MotionListener() {
					public void onMotionRecieved(int motion) {
						Cursor c = getContentResolver().query(
								MotionsDB.TASKS_CONTENT_URI,
								new String[] { ActivityColumns.PACK,
										ActivityColumns.ACTIVITY },
								ActivityColumns.MOTION_ID + "=" + motion, null,
								null);
						c.moveToFirst();
						while (!c.isAfterLast()) {
							if (c.getString(0) != null
									&& c.getString(1) != null) {
								Intent i = new Intent();

								i.setClassName(c.getString(0), c.getString(1));
								i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

								try {
									//Log.i("startActivity", "begin");
									lb.mh.startActivity(i);

									//Log.i("startActivity", "end");
								} catch (Exception e) {

									//Log.i("startActivity", "failed");
									Toast.makeText(lb.mh,
											"cant't start activity", 1000)
											.show();
									e.printStackTrace();
								}
							}

							c.moveToNext();

						}
						c.close();
					}

				};
				startMyService.setEnabled(true);

			}

			public void onServiceDisconnected(ComponentName arg0) {
				startMyService.setEnabled(false);

			}
		};

		bindService(new Intent(this, MotionHandler1.class), con, 0);

	}

	@Override
	protected void onResume() {
		super.onResume();
		/*Cursor c = getContentResolver().query(MotionsDB.MOTIONS_CONTENT_URI,
				new String[] { "count(_ID)" }, null, null, null);
		c.moveToFirst();
		if (lb == null || lb.mh == null)
			return;
		if (c.getInt(0) == 0 && lb.mh.isEnabled)
			startMyService.setEnabled(true);
		if (c.getInt(0) == 0 && !lb.mh.isEnabled)
			startMyService.setEnabled(false);*/

	}

	private void fillListView() {
		c = getContentResolver().query(MotionsDB.MOTIONS_CONTENT_URI,
				new String[] { "_id", MotionColumns.NAME }, null, null, null);
		startManagingCursor(c);
		c.registerContentObserver(new ContentObserver(new Handler() {

		}) {

			@Override
			public void onChange(boolean selfChange) {
				if (c.getCount() == 0 && !lb.mh.isEnabled)
					startMyService.setEnabled(false);
				else
					startMyService.setEnabled(true);

			}

		});
		motions = new MySimpleAdapter(this, R.layout.motions_row, c,
				new String[] { MotionColumns.NAME },
				new int[] { R.id.motion_name });

		lv.setAdapter(motions);
		lv.setItemsCanFocus(false);
		lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		lv.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent i = new Intent(Manager.this, MotionEditor.class);
				i.setAction(android.content.Intent.ACTION_EDIT);
				i.putExtra("id", motions.getItemId(arg2));
				startActivity(i);
			}
		});
		//LayoutInflater inflater = (LayoutInflater) this
			//	.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//View empty = inflater.inflate(R.layout.start_view, null);
		// lv.setEmptyView(empty);
		Button addFirst = (Button) findViewById(R.id.add_first);
		addFirst.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				Intent i = new Intent(Manager.this, MotionEditor.class);
				i.setAction(android.content.Intent.ACTION_MAIN);
				startActivity(i);
			}

		});

	}

	class MySimpleAdapter extends SimpleCursorAdapter {

		public MySimpleAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			TextView tv = (TextView) super.getView(position, convertView,
					parent);
			tv.setTextSize(30);
			tv.setPadding(0, 3, 0, 3);
			return tv;

		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// c.close();
		super.onSaveInstanceState(outState);
		if (lb != null)
			outState.putBoolean("process", isServiceEnabled());
		// unbindService(con);
	}

	@Override
	protected void onDestroy() {
		stopManagingCursor(c);
		c.close();
		super.onDestroy();
	}

	boolean isServiceEnabled() {
		// Parcel p = Parcel.obtain();
		// try {
		return lb.mh.isEnabled;// .transact(ListnerBinder.GET_STATUS, null, p,
								// 0);
		// } catch (RemoteException e) {}
		// return p.readBundle().getBoolean("on/off");

	}

	void switchService() {
		// try {
		lb.mh.switchMe();// transact(ListnerBinder.SWITCH_CODE, null, null, 0);
		// } catch (RemoteException e) {}
	}
}