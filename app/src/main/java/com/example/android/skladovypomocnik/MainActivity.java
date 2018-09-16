package com.example.android.skladovypomocnik;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getName();
    private ListView list;
    private ArrayList<Article> articles;
    private ArticleListAdapter listViewAdapter;
    private boolean containsEan = false;
    private AlertDialog deleteDialog;
    private AlertDialog ipDialog;
    private int selectedIndex;
    private Settings settings;
    private EditText inputEanText;
    private EditText inputIpAddress;
    private EditText inputAmount;
    private EditText inputFilename;
    private TextView ipAddressTextView;
    private TextView loadingInfoTextView;
    private TextView bookNameTextView;
    private TextView amountTextView;
    private HashMap<String, String> mapOfNames = Model.getInstance().getMapOfNames();
    private ProgressBar progress;
    private AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobileAds.initialize(this, "ca-app-pub-6403268384265634~1982638427");
        settings = new Settings(this);

        createLoadingDialog();
        loadDatabaseData();


        inputEanText = (EditText) findViewById(R.id.inputEanText);
        inputEanText.requestFocus();

        inputFilename = (EditText) findViewById(R.id.filenameEditText);

        articles = new ArrayList<>();
        listViewAdapter = new ArticleListAdapter(this, R.layout.adapter_layout, articles);


        list = (ListView) findViewById(R.id.listView);
        list.setAdapter(listViewAdapter);
        list.setFocusable(false);

        handleAds();


        createIpAddressAlertDialog();
        createListItemAlertDialog();
        setButtonListeners();
        setInputListener();
        setSelectedItemListener();


    }

    private void loadDatabaseData() {
        if (mapOfNames == null) {
            DatabaseLoaderAsyncTask databaseLoader = new DatabaseLoaderAsyncTask(this, loadingDialog, progress, loadingInfoTextView);
            databaseLoader.execute();
        }
    }

    private void handleAds() {
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        adView.loadAd(adRequest);
    }

    // Converts array list to a String and saves it in SharedPreferences.
    @Override
    public void onStop() {
        super.onStop();
        settings.setArticles(convertArrayListToString());
    }
    // It takes name and  list of Articles. Changes them into String. ean and amount are divided by . and each object by ;     Filename;ean.amount.name;ean.amount.name;ean.amount.name; etc
    private String convertArrayListToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(inputFilename.getText() + ";");
        for (Article a : articles) {
            System.out.println("amount " + a.getAmount());
            sb.append(a.getEan()).append(".").append(a.getAmount()).append(".").append(a.getName()).append(";");
        }
        return sb.toString();
    }

    // Loads String from SharedPreferences, recreates ArrayList from it and refreshes UI.
    @Override
    public void onStart() {
        super.onStart();
        articles.clear();
        String data = settings.getArticles();
        if (data.length() > 0) {
            articles.addAll(convertDataToArrayList(settings.getArticles()));
            listViewAdapter.notifyDataSetChanged();
        }
        refreshAmountTextView();
    }
    // takes a string a changes it to arrayList.  The format of string goes like this  ean.amount.name;ean.amount.name;ean.amount.name; etc
    public ArrayList<Article> convertDataToArrayList(String data) {
        String[] dataAsArray = data.split(";");
        boolean first = true;
        ArrayList<Article> articles = new ArrayList<>();
        for (String s : dataAsArray) {
            if (first) {
                first = false;
                continue;
            }
            String[] temp = s.split("\\.");

            articles.add(new Article(temp[0], Integer.parseInt(temp[1]), temp[2]));
        }
        return articles;
    }

    private void refreshAmountTextView() {
        TextView totalAmountTextView = (TextView) findViewById(R.id.totalAmountTextView);
        TextView totalEanAmountTextView = (TextView) findViewById(R.id.totalEanAmountTextView);
        totalAmountTextView.setText("Celkové množství:  " + calculateAmount());
        totalEanAmountTextView.setText("Počet titulů: " + articles.size());
    }

    private int calculateAmount() {
        int amount = 0;
        for (Article a : articles) {
            amount += a.getAmount();
        }
        return amount;
    }









    // when enter key is pressed, value in inputEanTextview is added to listView and amount info is refreshed
    private void setInputListener() {
        inputEanText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent keyevent) {
                return handleEnterKey(keyCode, keyevent);
            }
        });
    }

    private boolean handleEnterKey(int keyCode, KeyEvent keyevent) {
        if ((keyevent.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
            addEan();
            hideKeyboard(inputEanText);
            refreshAmountTextView();
            return true;
        }
        return false;
    }

    private void addEan() {
        String ean = inputEanText.getText().toString();
        if (!ean.isEmpty()) {
            handleAddingEan(ean);
        }
    }


    private void handleAddingEan(String ean) {
        if (articles.isEmpty()) {
            articles.add(0, new Article(ean, 1, lookUpNameInDatabase(ean)));
        } else {
            iterateListForMatch(ean);
        }
        listViewAdapter.notifyDataSetChanged();
        inputEanText.setText("");
    }


    private void iterateListForMatch(String ean) {
        searchListForItemByEan(ean);
        addNewItemIfNotFound(ean);
    }

    private void searchListForItemByEan(String ean) {
        for (int i = 0; i < articles.size(); i++) {
            Article a = articles.get(i);
            containsEan = false;
            if (ean.equalsIgnoreCase(a.getEan())) {
                increaseAmountIfFound(a);
                break;
            }
        }
    }

    private void increaseAmountIfFound(Article a) {
        incrementAmount(a);
        containsEan = true;
    }

    private void incrementAmount(Article a) {
        int amount = a.getAmount() + 1;
        String currentEan = a.getEan();
        articles.remove(a);
        articles.add(0, new Article(currentEan, amount, a.getName()));
    }

    private void addNewItemIfNotFound(String ean) {
        if (!containsEan) {
            articles.add(0, new Article(ean, 1, lookUpNameInDatabase(ean)));
        }
    }

    private String lookUpNameInDatabase(String ean) {
        String name = Model.getInstance().getName(ean);
        if (name == null) {
            name = "NÁZEV NENALEZEN";
        }
        return name;
    }


    // This one is called when user selects item from listView, it calls a new window that offers delete row, edit ammount and add amount functions.
    private void setSelectedItemListener() {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                handleItemSelected(position);
            }
        });
    }

    private void handleItemSelected(int position) {
        selectedIndex = position;
        bookNameTextView.setText(articles.get(selectedIndex).getName());
        amountTextView.setText("Současné množství: " + articles.get(selectedIndex).getAmount());
        deleteDialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancelButton:
                handleCancelButton();
                break;
            case R.id.editButton:
                handleEditAmountButton();
                refreshAmountTextView();
                break;
            case R.id.deleteButton:
                handleDeleteItemButton();
                refreshAmountTextView();
                break;
            case R.id.editIpButton:
                handleEditIpButton();
                break;
            case R.id.backButton:
                handleBackButton();
                break;
            case R.id.exportButton:
                handleExportButton();
                break;
            case R.id.deleteAllButton:
                handleDeleteAllItemsButton();
                break;
            case R.id.networkSettingsButton:
                handleNetworkSettingsButton();
                break;
            case R.id.addButton:
                handleAddButton();
                refreshAmountTextView();
                break;
            default:
                break;
        }

    }

    private void handleAddButton() {
        hideKeyboard(inputAmount);
        if (!inputAmount.getText().toString().isEmpty()) {
            addAmount();
        }
        deleteDialog.dismiss();
    }

    private void addAmount() {
        BigInteger amount = new BigInteger(inputAmount.getText().toString());
        if (amount.intValue() > 100000 || amount.intValue() < 0) {
            Toast.makeText(this, "Příliš velké nebo malé číslo", Toast.LENGTH_SHORT).show();
        } else {
            inputAmount.setText("");
            String ean = articles.get(selectedIndex).getEan();
            int newAmount = articles.get(selectedIndex).getAmount() + amount.intValue();
            articles.set(selectedIndex, new Article(ean, newAmount, articles.get(selectedIndex).getName()));
            listViewAdapter.notifyDataSetChanged();
        }
    }

    private void handleCancelButton() {
        hideKeyboard(inputAmount);
        deleteDialog.dismiss();
    }

    private void handleEditAmountButton() {
        hideKeyboard(inputAmount);
        if (!inputAmount.getText().toString().isEmpty()) {
            changeAmount();
        }
        deleteDialog.dismiss();
    }

    private void changeAmount() {
        BigInteger amount = new BigInteger(inputAmount.getText().toString());
        if (amount.intValue() > 100000 || amount.intValue() < 0) {
            Toast.makeText(this, "Příliš velké nebo malé číslo", Toast.LENGTH_SHORT).show();
        } else {
            makeChangeAndNotifyAdapter(amount);
        }
    }

    private void makeChangeAndNotifyAdapter(BigInteger amount) {
        inputAmount.setText("");
        String ean = articles.get(selectedIndex).getEan();
        articles.set(selectedIndex, new Article(ean, amount.intValue(), articles.get(selectedIndex).getName()));
        listViewAdapter.notifyDataSetChanged();
    }


    private void handleDeleteItemButton() {
        hideKeyboard(inputAmount);
        articles.remove((articles.get(selectedIndex)));
        listViewAdapter.notifyDataSetChanged();
        deleteDialog.dismiss();
    }

    private void handleEditIpButton() {
        hideKeyboard(inputIpAddress);
        String ip = inputIpAddress.getText().toString().replace(" ", "");
        if (ip.length() != 0) {
            setNewIpAddress(ip);
        } else {
            Toast.makeText(this, "Vlož IP Adresu", Toast.LENGTH_SHORT).show();
        }
    }

    private void hideKeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }


    private void setNewIpAddress(String ip) {
        settings.setIP(ip);
        ipAddressTextView.setText("IP Adresa : " + settings.getIp());
        ipDialog.dismiss();
        Toast.makeText(this, "IP Adresa : " + settings.getIp() + " uložena", Toast.LENGTH_SHORT).show();
    }


    private void handleBackButton() {
        hideKeyboard(inputIpAddress);
        ipDialog.dismiss();
    }


    private void handleExportButton() {
        hideKeyboard(inputEanText);
        String data = convertArrayListToString();
        if (listViewAdapter.getCount() == 0) {
            createAndDisplayToast("Není vložený žádný ean");
        } else if (settings.getIp().length() == 0) {
            createAndDisplayToast("Není vložená IP adresa");
        } else if (!fileHasName()) {
            createAndDisplayToast("Není vložený název souboru");
        } else {
            sendData(data);
        }
    }

    private void createAndDisplayToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }


    private boolean fileHasName() {
        return inputFilename.getText().length() > 0;
    }


    private void sendData(String data) {
        Client sender = new Client(settings.getIp(), this);
        sender.execute(data.toString());
    }


    private void handleDeleteAllItemsButton() {
        hideKeyboard(inputEanText);
        if (articles.isEmpty()) {
            Toast.makeText(this, "List je prázdný", Toast.LENGTH_SHORT).show();
        } else {
            showDeleteAllAlertDialog();
        }
    }

    private void showDeleteAllAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Opravdu chcete smazat všechny položky?").setNegativeButton("Ne", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).setPositiveButton("Ano", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteAll();
            }
        }).create();
        builder.show();

    }

    private void deleteAll() {
        articles.clear();
        listViewAdapter.notifyDataSetChanged();
        inputFilename.setText("");
        refreshAmountTextView();
    }

    private void handleNetworkSettingsButton() {
        ipDialog.show();
    }

    // This creates a dialog window  that shows up after clicking on item in listView
    private void createListItemAlertDialog() {
        View view = getLayoutInflater().inflate(R.layout.edit_listview_dialog, null);
        view.findViewById(R.id.deleteButton).setOnClickListener(MainActivity.this);
        view.findViewById(R.id.cancelButton).setOnClickListener(MainActivity.this);
        view.findViewById(R.id.editButton).setOnClickListener(MainActivity.this);
        view.findViewById(R.id.addButton).setOnClickListener(MainActivity.this);
        amountTextView = (TextView) view.findViewById(R.id.currentAmountTextView);
        bookNameTextView = (TextView) view.findViewById(R.id.bookNameTextView);
        inputAmount = (EditText) view.findViewById(R.id.amountInput);
        deleteDialog = new AlertDialog.Builder(this).setView(view).create();
    }

    // This creates a dialog window that shows up after pressing network settings button
    private void createIpAddressAlertDialog() {
        View view = getLayoutInflater().inflate(R.layout.edit_ip_dialog, null);
        view.findViewById(R.id.editIpButton).setOnClickListener(MainActivity.this);
        view.findViewById(R.id.backButton).setOnClickListener(MainActivity.this);
        inputIpAddress = (EditText) view.findViewById(R.id.editIpText);
        ipAddressTextView = (TextView) view.findViewById(R.id.ipAddressTextView);
        ipAddressTextView.setText("IP Adresa : " + settings.getIp());
        ipDialog = new AlertDialog.Builder(this).setView(view).create();
    }

    // This create a dialog windows that shows up after application is started for first time
    private void createLoadingDialog() {
        View view = getLayoutInflater().inflate(R.layout.start_loading, null);
        progress = view.findViewById(R.id.progressBar);
        loadingInfoTextView = (TextView) view.findViewById(R.id.loadingInfoTextView);
        loadingDialog = new AlertDialog.Builder(this).setView(view).create();
    }

    private void setButtonListeners() {
        this.findViewById(R.id.exportButton).setOnClickListener(this);
        this.findViewById(R.id.deleteAllButton).setOnClickListener(this);
        this.findViewById(R.id.networkSettingsButton).setOnClickListener(this);
    }

}
