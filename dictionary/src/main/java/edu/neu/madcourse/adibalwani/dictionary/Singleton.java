package edu.neu.madcourse.adibalwani.dictionary;

import java.util.List;

/**
 * Created by Adib on 2/5/2016.
 */
public class Singleton {
    private static Singleton mInstance = null;

    private List<String[]> mDictionary;
    private List<String> mWordListData;
    private String mSearchText;

    private Singleton() {}

    public static Singleton getInstance() {
        if (mInstance == null) {
            mInstance = new Singleton();
        }
        return mInstance;
    }

    public List<String[]> getDictionary() {
        return mDictionary;
    }

    public void setDictionary(List<String[]> dictionary) {
        mDictionary = dictionary;
    }

    public List<String> getWordListData() {
        return mWordListData;
    }

    public void setWordListData(List<String> wordListData) {
        mWordListData = wordListData;
    }

    public String getSearchText() {
        return mSearchText;
    }

    public void setSearchText(String searchText) {
        mSearchText = searchText;
    }
}
