package me.kennydude.transtimetable.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

public class SimpleFragmentPagerAdapter extends FragmentPagerAdapter {
	List<String> items;
	FragmentManager mFragmentManager;
	FragmentTransaction mCurTransaction;
	Context mContext;

	public SimpleFragmentPagerAdapter(Activity fm) {
		super(fm.getFragmentManager());
		mFragmentManager = fm.getFragmentManager();
		items = new ArrayList<String>();
		mContext = fm;
	}
	
	public void addItem( Class<? extends Fragment> cls ){
		items.add(cls.getName());
		notifyDataSetChanged();
	}

	@Override
	public Fragment getItem(int position) {
		return Fragment.instantiate(mContext, items.get(position));
	}
	
	public Fragment getLiveItem(int position){
		return mFragmentManager.findFragmentByTag( makeFragmentName(position) );
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (mCurTransaction == null) {
            mCurTransaction = mFragmentManager.beginTransaction();
        }

        final long itemId = getItemId(position);

        // Do we already have this fragment?
        String name = makeFragmentName(itemId);
        Fragment fragment = mFragmentManager.findFragmentByTag(name);
        if (fragment != null) {
            mCurTransaction.attach(fragment);
        } else {
            fragment = getItem(position);
            mCurTransaction.add(container.getId(), fragment,
                    makeFragmentName(itemId));
        }

        return fragment;
    }
	
	@Override
    public void finishUpdate(ViewGroup container) {
		super.finishUpdate(container);
        if (mCurTransaction != null) {
            mCurTransaction.commitAllowingStateLoss();
            mCurTransaction = null;
            mFragmentManager.executePendingTransactions();
        }
    }
	
	private static String makeFragmentName(long id) {
		return "page:" + id;
	}
}
