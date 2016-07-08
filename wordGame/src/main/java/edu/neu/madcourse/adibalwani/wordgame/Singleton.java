package edu.neu.madcourse.adibalwani.wordgame;

import java.util.List;

public class Singleton {
    private static Singleton mInstance = null;
    private List<String[]> mDictionary;

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
}
