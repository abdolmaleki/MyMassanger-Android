package com.example.myapplication.dictionary;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class DataDictionary<K , T>
{

    //id , index of list
    private Dictionary<K, Integer> mDictionary;
    private List<T> mList;
    private final Object mLock = new Object();

    public DataDictionary()
    {
        mDictionary = new Hashtable<>();
        mList = new ArrayList<>();
    }

    public synchronized void add(K key, T object)
    {

        synchronized (mLock)
        {
            Integer index = mDictionary.get(key);
            if (index == null)
            {
                mList.add(object);
                //get index current item
                mDictionary.put(key, mList.size() - 1);
            }
            else
            {
                mDictionary.put(key, index);
            }

        }

    }

    public synchronized void update(K key, T object)
    {

        synchronized (mLock)
        {
            Integer index = mDictionary.get(key);
            if (index != null)
            {
                mList.set(index, object);
            }
        }

    }

    public synchronized void replace(K key, K newKey, T object)
    {

        synchronized (mLock)
        {
            Integer index = mDictionary.get(key);
            if (index == null)
            {
                add(newKey, object);
                return;
            }

            mDictionary.remove(key);
            mDictionary.put(newKey, index);

            mList.set(index, object);
        }

    }

    public synchronized T getByKey(K key)
    {

        synchronized (mLock)
        {
            Integer index = mDictionary.get(key);
            if (index == null)
            {
                return null;
            }
            return mList.get(index);
        }

    }

    public synchronized int getIndexByKey(K key)
    {

        synchronized (mLock)
        {
            Integer index = mDictionary.get(key);
            if (index == null)
            {
                return -1;
            }
            return index;
        }

    }

    public synchronized T get(int index)
    {

        synchronized (mLock)
        {
            return mList.get(index);
        }

    }

    public synchronized int size()
    {
        synchronized (mLock)
        {
            return mList.size();
        }
    }

	/*public synchronized void clear()
    {
		synchronized (mLock)
		{
			mList.clear();
			mDictionary = null;
			System.gc();
			mDictionary = new Hashtable<>();
		}
	}*/
}
