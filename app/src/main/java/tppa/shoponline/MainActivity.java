package tppa.shoponline;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    // ui related
    private ListView listView;
    private TextView textView;
    private ArrayAdapter<String> lvAdapter;


    // data related
    private ArrayList<String> products;
    private boolean listChanged = false;


    // bundle related
    private static final String DETAILS_DATA = "details_content";
    private static final String LV_DATA = "lv_content";
    private static final String BG_COLOR = "bg_color";


    // intent related
    public static final String EXTRA_MESSAGE = "tppa.shoponline";


    // debug tags
    private static final String TAG = "MainActivity";
    private static final String MENU = "MenuItem";


    // lv item listener - buy dialog
    private AdapterView.OnItemClickListener mMessageClickedHandler = (parent, v, position, id) -> {
        // Do something in response to the click
        String itemName = ((TextView) v).getText().toString();
        textView.setText(itemName);

        showBuyDialog(itemName);
    };


    // dialogs
    private void showAddDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add product");
        LayoutInflater inflater = this.getLayoutInflater();
        @SuppressLint("InflateParams") View addProductView = inflater.inflate(R.layout.add_product, null);
        final EditText productInput = addProductView.findViewById(R.id.pname_et);
        builder.setView(addProductView);
        builder.setPositiveButton("Ok", (dialogInterface, i) -> {
            String productName = productInput.getText().toString();
            if (!productName.isEmpty()){
                products.add(productName);
                listChanged = true;
                updateProducts();
            }
        });
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> {

        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showBuyDialog(String itemName){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle(itemName);
        builder.setMessage("Buy "+itemName+" ?");
        builder.setPositiveButton("Yes", (dialogInterface, i) -> {

        });
        builder.setNegativeButton("No", (dialogInterface, i) -> {

        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    // product count activity
    private void sendCount() {
        int count = products.size();
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_GET_CONTENT);
        sendIntent.putExtra(EXTRA_MESSAGE, Integer.toString(count));
        // Verify that the intent will resolve to an activity
        if (sendIntent.resolveActivity(getPackageManager()) != null)
            startActivity(sendIntent);
    }

    private void showSensors() {
        startActivity(new Intent(this, SensorActivity.class));
    }

    private void showSettings(){
        startActivity(new Intent(this, ShopPrefActivity.class));
    }

    private void showCam(){
        startActivity(new Intent(this, CameraActivity.class));
    }


    private int getDetailsBgColorPref(){
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        String lpKey = getResources().getString(R.string.bg_color_key);
        String prefValue = sharedPreferences.getString(lpKey,"");
        int colorId;
        switch (prefValue) {
            case "smart":
                colorId = R.color.colorSmart;
                break;
            case "fresh":
                colorId = R.color.colorFresh;
                break;
            case "hacker":
                colorId = R.color.colorHacker;
                break;
            default:
                colorId = R.color.colorPure;
                break;
        }
        return ContextCompat.getColor(this, colorId);
    }

    private void saveProductsToInternalStorage(){
        FileOutputStream fos;
        String fileName = getResources().getString(R.string.products_file);
        try {
            fos = openFileOutput(fileName, Context.MODE_PRIVATE);
            for (String s : products){
                fos.write((s+"\n").getBytes());
            }
            fos.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void loadProductsFromInternalStorage(){
        FileInputStream fis;
        String fileName = getResources().getString(R.string.products_file);
        String s;
        try {
            fis = openFileInput(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            while ((s = reader.readLine()) != null){
                products.add(s.trim());
            }
            fis.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.lv);
        textView = findViewById(R.id.tv);

        Log.d(TAG, "onCreate: Shop created...");

        products = new ArrayList<>();
        loadProductsFromInternalStorage();

        lvAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, products);
        listView.setAdapter(lvAdapter);
        listView.setOnItemClickListener(mMessageClickedHandler);

//        Context context = getApplicationContext();
//        SharedPreferences sharedPrefs = context.getSharedPreferences(
//                getString(R.string.shared_prefs), Context.MODE_PRIVATE);

        // restore details bg
        int detailsColor = getDetailsBgColorPref();
        textView.setBackgroundColor(detailsColor);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: Shop started...");
    }

    @Override
    protected void onResume() {
        super.onResume();

        int detailsColor = getDetailsBgColorPref();
        textView.setBackgroundColor(detailsColor);

        Log.d(TAG, "onResume: Shop resumed...");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: Shop paused...");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: Shop stopped...");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listChanged){
            saveProductsToInternalStorage();
        }
        Log.d(TAG, "onDestroy: Shop destroyed...");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.shop_menu, menu);
        return true;
    }

    private void updateProducts() {
        lvAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, products);
        listView.setAdapter(lvAdapter);
        lvAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(DETAILS_DATA, textView.getText());
        outState.putStringArrayList(LV_DATA, products);

        ColorDrawable detailsBg = (ColorDrawable)textView.getBackground();
        outState.putInt(BG_COLOR, detailsBg.getColor());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedState) {
        super.onRestoreInstanceState(savedState);
        CharSequence details = savedState.getCharSequence(DETAILS_DATA);
        textView.setText(details);

        products = savedState.getStringArrayList(LV_DATA);
        updateProducts();

        int bgColor = savedState.getInt(BG_COLOR, -1);
        textView.setBackgroundColor(bgColor);
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.add:
                showAddDialog();
                return true;
            case R.id.sort:
                Log.d(MENU, "onOptionsItemSelected: ");
                products.sort(String::compareToIgnoreCase);
                updateProducts();
                return true;
            case R.id.cam:
                showCam();
                return true;
            case R.id.count:
                sendCount();
                return true;
            case R.id.sensors:
                showSensors();
                return true;
            case R.id.settings:
                // e.g.: show sorted, display color etc.
                showSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

