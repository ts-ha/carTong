package com.neighbor.ex.tong.ui.activity;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;

import com.neighbor.ex.tong.R;
import com.neighbor.ex.tong.provider.DataProvider;

public class CarSearchActivity extends AppCompatActivity {

    private AutoCompleteTextView inputCarNo;
    private AutoCompleteDbAdapter autoCompleteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_search);

        Bundle bundle = getIntent().getExtras();
        String inputCarNoString = "";
        if (null != bundle) {
            inputCarNoString = bundle.getString("inputCarNo", "");
        }
        String condition = "PLATE_NUMBER LIKE '%" + inputCarNoString + "'";
        Cursor query = getContentResolver().query(DataProvider.PLATE_URI, null,
                condition, null, null);
        ListView listView = (ListView) findViewById(R.id.listViewCarSearch);
        MessageAdapter mAdapter = new MessageAdapter(getApplicationContext().getApplicationContext(), query, false);
        listView.setAdapter(mAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                String number = cursor.getString(cursor.getColumnIndexOrThrow("PLATE_NUMBER"));
                Intent intent = new Intent(CarSearchActivity.this, SendMsgActivity.class);
                intent.putExtra("destNo", number);
                startActivity(intent);
                inputCarNo.setText("");
            }
        });
        autoCompleteAdapter = new AutoCompleteDbAdapter(CarSearchActivity.this,
                null, false);
        inputCarNo = (AutoCompleteTextView) findViewById(R.id.title);
        inputCarNo.setAdapter(autoCompleteAdapter);
        inputCarNo.setThreshold(4);
        inputCarNo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(i);
                String number = cursor.getString(cursor.getColumnIndexOrThrow("PLATE_NUMBER"));
                Intent intent = new Intent(CarSearchActivity.this, SendMsgActivity.class);
                intent.putExtra("destNo", number);
                startActivity(intent);
                inputCarNo.setText("");
            }
        });

        inputCarNo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH:
                        Intent intent = new Intent(CarSearchActivity.this, CarSearchActivity.class);
                        intent.putExtra("inputCarNo", inputCarNo.getText().toString());
                        inputCarNo.setText("");
                        startActivity(intent);
                        break;
                    default:
                        return false;
                }
                return false;
            }
        });

    }

    class AutoCompleteDbAdapter extends CursorAdapter implements Filterable {
        private LayoutInflater inflater;
        private ContentResolver mContent;

        public AutoCompleteDbAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mContent = context.getContentResolver();
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            return inflater.inflate(R.layout.autocomplete_item_row,
                    viewGroup, false);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            View v = view;
            TextView carNo = (TextView) v.findViewById(R.id.subscriber_carNo);
            if (cursor != null) {
                String plateNumber = cursor.getString(cursor.getColumnIndex("PLATE_NUMBER"));
                carNo.setText(plateNumber);
            }
        }

        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            if (getFilterQueryProvider() != null) {
                return getFilterQueryProvider().runQuery(constraint);
            }
            String condition = "PLATE_NUMBER LIKE '%" + inputCarNo.getText().toString() + "'";
            Log.d("hts", "condition : " + condition);
            return mContent.query(DataProvider.PLATE_URI, null,
                    condition, null, null);
        }

        @Override
        public CharSequence convertToString(Cursor cursor) {
            final int columnIndex = cursor.getColumnIndexOrThrow("PLATE_NUMBER");
            final String str = cursor.getString(columnIndex);
            return str;
        }
    }


    class MessageAdapter extends CursorAdapter {

        private final LayoutInflater mLayoutInflater;
        private Context _cont;
        private Cursor _cs;


        public MessageAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
            _cs = c;
            _cont = context;
            mLayoutInflater = LayoutInflater.from(context);

        }

        @Override
        public int getCount() {
            return _cs.getCount();
        }


        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            try {
//                g("MEMBER_GMAIL");
//                String carNo = obj.getString("MEMBER_CAR_NUM");
                TextView textView = (TextView) view.findViewById(R.id.textViewCar);
                textView.setText(cursor.getString(cursor.getColumnIndex("PLATE_NUMBER")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            return mLayoutInflater.inflate(R.layout.row_car, viewGroup, false);
        }
    }
}
