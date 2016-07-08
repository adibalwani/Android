package edu.neu.madcourse.adibalwani.dictionary;

import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainFragment extends Fragment implements View.OnClickListener {

    private List<String[]> mDictionary;
    private EditText mSearchBox;
    private ArrayAdapter<String> mWordListAdapter;
    private List<String> mWordListData;
    private ProgressBar mProgressBar;
    private int mProgressStatus;
    private LinearLayout mMenuFrame;
    private Singleton mSingletonInstance;
    private Loader mLoader;
    private ToneGenerator mToneGenerator;
    private boolean mWarmStart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dictionary_fragment_main, container, false);
        mSearchBox = (EditText) rootView.findViewById(R.id.dictionary_search_edit_text);
        mProgressBar = (ProgressBar) rootView.findViewById(R.id.dictionary_progress_bar);
        mMenuFrame = (LinearLayout) rootView.findViewById(R.id.dictionary_menu_frame);
        initButtonListener(rootView);
        initEditTextListener(rootView);
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSingletonInstance = Singleton.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        mToneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        initListViewAdapter(getView());
        List<String[]> dictionary = mSingletonInstance.getDictionary();
        if (dictionary == null) {
            loadDictionary();
        } else {
            String searchText = mSingletonInstance.getSearchText();
            mDictionary = dictionary;
            mWarmStart = true;
            mWordListData.addAll(mSingletonInstance.getWordListData());
            mWordListAdapter.notifyDataSetChanged();
            mSearchBox.setText(searchText);
            mSearchBox.setSelection(searchText.length());
            mProgressBar.setVisibility(View.GONE);
            mMenuFrame.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        int resourceId = v.getId();

        if (resourceId == R.id.dictionary_return_to_menu_button) {
            endActivity();
        } else if (resourceId == R.id.dictionary_clear_button) {
            clear();
        } else if (resourceId == R.id.dictionary_acknowledgements_button) {
            displayDialog(R.layout.dictionary_dialog_acknowledgement);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Save Instance state
        if (mDictionary != null) {
            mSingletonInstance.setDictionary(mDictionary);
            mSingletonInstance.setWordListData(mWordListData);
            mSingletonInstance.setSearchText(mSearchBox.getText().toString());
        }

        // Cancel all spawned threads
        if (mLoader != null && mLoader.getStatus() != AsyncTask.Status.FINISHED) {
            mLoader.cancel(true);
        }
    }

    /**
     * Initialize onClickListener for buttons
     *
     * @param view Fragment view
     */
    private void initButtonListener(View view) {
        view.findViewById(R.id.dictionary_return_to_menu_button).setOnClickListener(this);
        view.findViewById(R.id.dictionary_clear_button).setOnClickListener(this);
        view.findViewById(R.id.dictionary_acknowledgements_button).setOnClickListener(this);
    }

    /**
     * Initialize Text change Listener for EditText
     *
     * @param view Fragment view
     */
    private void initEditTextListener(View view) {
        mSearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mWarmStart) {
                    search();
                }
                mWarmStart = false;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Initialize tt View Adapter and data
     *
     * @param view Fragment view
     */
    private void initListViewAdapter(View view) {
        ListView wordListView = (ListView) view.findViewById(R.id.dictionary_word_list_view);
        mWordListData = new ArrayList<String>();
        mWordListAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                mWordListData);
        wordListView.setAdapter(mWordListAdapter);
    }

    /**
     * Display dialog box containing the given layout
     *
     * @param layoutId Layout to be displayed
     */
    private void displayDialog(int layoutId) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        DialogFragment dialogFragment = CustomDialogFragment.newInstance(layoutId);
        dialogFragment.show(fragmentTransaction, "dialog");
    }

    /**
     * Load dictionary into memory
     */
    private void loadDictionary() {
        mLoader = new Loader();
        mLoader.execute();
    }

    /**
     * Handle Return press by killing the current Activity
     */
    private void endActivity() {
        getActivity().finish();
    }

    /**
     * Search for the word in the search box and if found add it to the found word list and
     * play beep sound
     */
    private void search() {
        String word = mSearchBox.getText().toString();
        if (word.length() > 2 && wordInDictionary(word)) {
            if (!mWordListData.contains(word)) {
                mWordListData.add(word);
                mWordListAdapter.notifyDataSetChanged();
            }
            mToneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
        }
    }

    /**
     * Empty the found word list and the text in the search box
     */
    private void clear() {
        mSearchBox.setText("");
        mWordListData.clear();
        mWordListAdapter.notifyDataSetChanged();
    }

    /**
     * Check whether the given exists in dictionary
     *
     * @param word The word to search for
     * @return true, iff it exists. False, otherwise
     */
    private boolean wordInDictionary(String word) {
        if (mDictionary == null) {
            return false;
        }

        for (int i = 0; i < mDictionary.size(); i++) {
            if (Arrays.binarySearch(mDictionary.get(i), word) >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Class used to Load Dictionary in Memory
     */
    private class Dictionary extends Thread {
        private final int fileId;
        private String[] dictionary;

        Dictionary(int fileId) {
            this.fileId = fileId;
        }

        /**
         * Load dictionary stored in file in a string array
         *
         * @param fileId ResourceId of the file
         * @return The loaded dictionary
         */
        private String[] loadDictionary(int fileId) {
            String[] dictionary = new String[74325];
            int index = 0;
            InputStream inputStream = getResources().openRawResource(fileId);
            InputStreamReader reader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(reader);

            try {
                String currentLine;

                while ((currentLine = bufferedReader.readLine()) != null) {
                    dictionary[index] = currentLine;
                    index++;
                    if (index % 10000 == 0 && mProgressBar != null) {
                        mProgressStatus += 3;
                        mProgressBar.setProgress(mProgressStatus);
                    }
                }

                if (index != 74325) {
                    dictionary = Arrays.copyOfRange(dictionary, 0, index);
                }
            } catch (IOException e) {
                Log.e(Constants.TAG_IO_ERROR, "Failed to read: " + e.getMessage());
            } finally {
                try {
                    bufferedReader.close();
                    reader.close();
                    inputStream.close();
                } catch (IOException e) {
                    Log.e(Constants.TAG_IO_ERROR, "Failed to close: " + e.getMessage());
                }
            }

            return dictionary;
        }

        public String[] getDictionary() {
            return dictionary;
        }

        @Override
        public void run() {
            dictionary = loadDictionary(fileId);
        }
    }

    /**
     * Class used to load Dictionary
     */
    private class Loader extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            int[] files = {
                    R.raw.xaa,
                    R.raw.xab,
                    R.raw.xac,
                    R.raw.xad,
                    R.raw.xae
            };

            // Spawn Threads
            Dictionary[] dictionaries = new Dictionary[files.length];
            for (int i = 0; i < dictionaries.length; i++) {
                dictionaries[i] = new Dictionary(files[i]);
            }
            for (int i = 0; i < dictionaries.length; i++) {
                dictionaries[i].start();
            }

            // Wait for those threads to complete
            try {
                for (int i = 0; i < dictionaries.length; i++) {
                    dictionaries[i].join();
                }
            } catch (InterruptedException e) {
                Log.e(Constants.TAG_INTERRUPTED_ERROR, "Failed to join Thread: " + e.getMessage());
            }

            Dictionary dictionary = new Dictionary(R.raw.uia);
            dictionary.run();
            mDictionary = new ArrayList<String[]>();
            mDictionary.add(dictionary.getDictionary());
            for (int i = 0; i < dictionaries.length; i++) {
                mDictionary.add(dictionaries[i].getDictionary());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgressBar.setVisibility(View.GONE);
            mMenuFrame.setVisibility(View.VISIBLE);
        }
    }
}
