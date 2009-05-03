package org.sadko.gestures;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Manager extends Activity {
	ListView lv;
	Cursor c;
	private static final int ADD_NEW_ID = 0;
	private static final int EXIT_ID = 1;
	private static final int KILL_SERVICE_ID = 2;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, ADD_NEW_ID, 0, "Add new");
		menu.add(0, EXIT_ID, 0, "Exit");
		menu.add(0, KILL_SERVICE_ID, 0, "Kill handling service");
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
		case KILL_SERVICE_ID: {
			//Manager.this.finish();
			lb.mh.killNotification();
			stopService(new Intent(Manager.this, MotionHandler1.class));
			lb=null;
			break;
		}
		}
		return super.onMenuItemSelected(featureId, item);
	}

	SimpleCursorAdapter motions;
	int selectedItem = -1;
	ListnerBinder lb = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		c = getContentResolver().query(MotionsDB.MOTIONS_CONTENT_URI,
				new String[] { "_id", MotionColumns.NAME }, null, null, null);
		startManagingCursor(c);
		motions = new SimpleCursorAdapter(this, R.layout.motions_row, c,
				new String[] { MotionColumns.NAME },
				new int[] { R.id.motion_name });
		lv = (ListView) findViewById(R.id.motions_list);
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
		final Button startMyService = (Button) findViewById(R.id.service_start);
		startMyService.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Cursor c = getContentResolver().query(
						MotionsDB.MOTIONS_CONTENT_URI,
						new String[] { "count(_ID)" }, null, null, null);
				c.moveToFirst();

				if (c.getInt(0) == 0 && (lb==null ||!lb.mh.isEnabled))
					return;

				if (lb == null) {
					ServiceConnection sc = new ServiceConnection() {
						public void onServiceConnected(ComponentName arg0,
								IBinder arg1) {
							lb = (ListnerBinder) arg1;
							lb.ms = new MotionListener() {
								public void onMotionRecieved(int motion) {
									Cursor c = getContentResolver().query(
											MotionsDB.TASKS_CONTENT_URI,
											new String[] {
													ActivityColumns.PACK,
													ActivityColumns.ACTIVITY },
											ActivityColumns.MOTION_ID + "="
													+ motion, null, null);
									while (!c.isLast()) {
										c.moveToNext();
										Intent i = new Intent();
										i.setClassName(c.getString(0), c
												.getString(1));
										try{
										startActivity(i);
										}catch(Exception e){
											Toast.makeText(lb.mh, "cant't start activity", 1000).show();
										}
										
									}
								}
							};
						}
						public void onServiceDisconnected(ComponentName arg0) {
						}
					};
					Intent i = new Intent(Manager.this, MotionHandler1.class);
					bindService(i, sc, Context.BIND_AUTO_CREATE);
					startMyService.setText("stop service");
				} else {
					try {
						Parcel p = Parcel.obtain();
						lb.transact(ListnerBinder.GET_STATUS, null, p, 0);
						startMyService.setText(p.readBundle().getBoolean(
								"on/off") ? "stop" : "resume");
						lb.mh.showNotification();
					} catch (RemoteException e) {

						e.printStackTrace();
					}
				}

			}

		});
		
	}
	void switchService(){
		try {
			lb.transact(ListnerBinder.SWITCH_CODE, null, null, 0);
		} catch (RemoteException e) {}
	}
}