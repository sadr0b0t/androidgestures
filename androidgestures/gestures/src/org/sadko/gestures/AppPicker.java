package org.sadko.gestures;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class AppPicker extends ListActivity {
	int packChosen = 0;
	int actChosen = 0;
	public static final String RESULT_CONTENT_VALUES_NAME="org.sadko.gestures.AppPicker/val"; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.picker);
		final PackageManager pm = getPackageManager();
		Intent launcher=new Intent();
		launcher.setAction(android.content.Intent.ACTION_MAIN);
		launcher.addCategory(android.content.Intent.CATEGORY_LAUNCHER);
		//List<ResolveInfo> launchersIter=pm.queryIntentActivities(launcher, 0).iterator();
		Set<ApplicationInfo> appsWithLauncher=new HashSet<ApplicationInfo>();
		//while(launchersIter.hasNext()){
			//appsWithLauncher.add(launchersIter.next().activityInfo.applicationInfo);
		//}
		final List<PackageInfo> apps=pm.getInstalledPackages(PackageManager.GET_ACTIVITIES);
		Iterator<PackageInfo> appsIter=apps.iterator();
		final List<ActivityInfo[]> activities = new ArrayList<ActivityInfo[]>();
		int k=0;
		while(appsIter.hasNext()){			
			activities.add(k,appsIter.next().activities);
			k++;
		}
		MyExpandableListAdapter mAdapter=new MyExpandableListAdapter(apps,activities);
		//mAdapter.groups=apps;
		//mAdapter.children=activities;
		/*final List<AppRow> listForView = new ArrayList<AppRow>();
		while (iter.hasNext()) {
			ApplicationInfo ai = iter.next();
			
			AppRow ar=new AppRow();
			ar.name = ai.loadLabel(pm);
			ar.icon= pm.getApplicationIcon(ai.activityInfo.applicationInfo);
			ar.packName=ai.activityInfo.packageName;
			lst.add(ar);
		}*/
		//final MyExpandableListAdapter ad = new MyExpandableListAdapter();
		ListView applications = getListView();//this.getListView()
		applications.setAdapter(mAdapter);
		applications.setItemsCanFocus(false);
		applications.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		applications.setItemChecked(0, true);
		applications.setOnItemClickListener(new OnItemClickListener(){



			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
			/*	ContentValues cv = new ContentValues();
				cv.put(ActivityColumns.PACK, activities.get(arg2)[arg3].packageName);
				cv.put(ActivityColumns.ACTIVITY,activities.get(arg2)[arg3].name);
				Intent intent = new Intent();
				intent.putExtra(RESULT_CONTENT_VALUES_NAME, cv);
				setResult(1, intent);
				finish();
			
			finish();
		
	*/
				Intent i=new Intent();
				i.setAction(android.content.Intent.ACTION_MAIN);
				i.addCategory(android.content.Intent.CATEGORY_LAUNCHER);
				Iterator<ResolveInfo> iter=pm.queryIntentActivities(i,0).iterator();
				String needPackageName=apps.get(arg2).packageName;
				ContentValues cv = new ContentValues();
				cv.put(ActivityColumns.PACK, needPackageName);
				Intent intent = new Intent();
				while(iter.hasNext()){
					ResolveInfo info=iter.next();
					if(info.activityInfo.packageName.equals(needPackageName)){
						cv.put(ActivityColumns.ACTIVITY,info.activityInfo.name);
						Log.i("add app", needPackageName+"/"+info.activityInfo.name);
						intent.putExtra(RESULT_CONTENT_VALUES_NAME, cv);
						setResult(1, intent);
						finish();
						return;
					}
					//finish();
				}
				intent.putExtra(RESULT_CONTENT_VALUES_NAME, cv);
				setResult(1, intent);
				finish();
			}
			
		});
		/*applications.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent i=new Intent();
				i.setAction(android.content.Intent.ACTION_MAIN);
				i.addCategory(android.content.Intent.CATEGORY_LAUNCHER);
				Iterator<ResolveInfo> iter=pm.queryIntentActivities(i,0).iterator();
				String needPackageName=lst.get(arg2).packName;
				while(iter.hasNext()){
					ResolveInfo info=iter.next();
					if(info.activityInfo.packageName.equals(needPackageName)){
						ContentValues cv = new ContentValues();
						cv.put(ActivityColumns.PACK, needPackageName);
						cv.put(ActivityColumns.ACTIVITY,info.activityInfo.name);
						Intent intent = new Intent();
						intent.putExtra(RESULT_CONTENT_VALUES_NAME, cv);
						setResult(1, intent);
						finish();
					}
					finish();
				}

			}

		});*/


	}/*
	private class AppRow{
		Drawable icon;
		CharSequence name;
		String packName;
}
	class myAdapter extends ArrayAdapter {
		List lst;
		public myAdapter(Context context, int textViewResourceId, List objects) {
			super(context, textViewResourceId, objects);
			lst=objects;

		}

		@Override

	}*/
    public class MyExpandableListAdapter extends BaseAdapter {
        // Sample data set.  children[i] contains the children (String[]) for groups[i].
        List<PackageInfo> groups;
        List<ActivityInfo[]> children;
        PackageManager pm=getPackageManager();
        public MyExpandableListAdapter(List<PackageInfo> groups,List<ActivityInfo[]> children){
        	this.groups=groups;
        	this.children=children;
        }
   /*     public Object getChild(int groupPosition, int childPosition) {
            return children.get(groupPosition)[childPosition];
        }*/

        /*public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }*/

      /*  public int getChildrenCount(int groupPosition) {
        	try{
            return children.get(groupPosition).length;
        	}catch(NullPointerException e){
        		return 0;
        	}
        }*/

        public RelativeLayout getGenericView() {
            // Layout parameters for the ExpandableListView
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, 64);

            RelativeLayout lay = new RelativeLayout(AppPicker.this);
            lay.setLayoutParams(lp);
            // Center the text vertically
            lay.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            // Set the text starting position
            lay.setPadding(36, 0, 0, 0);
            return lay;
        }


      /*  public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {
			View childItem = null;
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) parent.getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				childItem = inflater.inflate(R.layout.picker_item,null);
				childItem.setPadding(72, 0, 0, 0);
			} else {
				childItem = convertView;
			}
            TextView tv=(TextView) childItem.findViewById(R.id.app_name);
            tv.setText(children.get(groupPosition)[childPosition].loadLabel(pm));
            ImageView iv=(ImageView)childItem.findViewById(R.id.app_icon);
            iv.setImageDrawable(children.get(groupPosition)[childPosition].loadIcon(pm));
			return childItem;
        }*/

        public Object getItem(int groupPosition) {
            return groups.get(groupPosition);
        }

        public int getCount() {
            return groups.size();
        }

        /*public long getItemId(int groupPosition) {
            return groupPosition;
        }*/

        public View getView(int groupPosition, View convertView,
                ViewGroup parent) {
			View groupItem = null;
			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) parent.getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				groupItem = inflater.inflate(R.layout.picker_item,null);
				groupItem.setPadding(36, 0, 0, 0);
			} else {
				groupItem = convertView;
			}

			//String destinationType = (String) getItem(position);
			TextView destinationTypeText = (TextView) groupItem
					.findViewById(R.id.app_name);
			destinationTypeText.setText(groups.get(groupPosition).applicationInfo.loadLabel(pm));
			destinationTypeText.setTextSize(20);
			ImageView typeSelectedImage = (ImageView) groupItem
					.findViewById(R.id.app_icon);
			typeSelectedImage.setImageDrawable(pm.getApplicationIcon(groups.get(groupPosition).applicationInfo));
			return groupItem;
        }

        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public boolean hasStableIds() {
            return true;
        }
	/*	public int getCount() {
			// TODO Auto-generated method stub
			return 0;
		}
	/*	public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}*/
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}
	/*	public View getView(int arg0, View arg1, ViewGroup arg2) {
			// TODO Auto-generated method stub
			return null;
		}*/

    }
}
