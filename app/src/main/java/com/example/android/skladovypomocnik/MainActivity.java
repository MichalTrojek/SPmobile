package com.example.android.skladovypomocnik;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.skladovypomocnik.auth.Authentication;
import com.google.android.gms.ads.MobileAds;
import com.google.gson.Gson;

import java.math.BigInteger;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ListView listView;
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
    private TextView downloadDatabaseTextview;
    private ProgressBar progress;
    private AlertDialog loadingDialog;
    private AlertDialog downloadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobileAds.initialize(this, "ca-app-pub-6403268384265634~1982638427");
        settings = new Settings(this);
        inputEanText = (EditText) findViewById(R.id.inputEanText);
        inputEanText.requestFocus();

        inputFilename = (EditText) findViewById(R.id.filenameEditText);

        articles = new ArrayList<>();
        listViewAdapter = new ArticleListAdapter(this, R.layout.adapter_layout, articles);


        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(listViewAdapter);
        listView.setFocusable(false);


        createLoadingDialog();
        createIpAddressAlertDialog();
        createListItemAlertDialog();
        setButtonListeners();
        setInputListener();
        setSelectedItemListener();

        Authentication.init(this);
        Authentication.getInstance().check();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.alphabetButton:
                if (articles.isEmpty()) {
                    Toast.makeText(this, "List je prázdný.", Toast.LENGTH_SHORT).show();
                } else {
                    Collections.sort(articles, new Comparator<Article>() {
                        @Override
                        public int compare(Article article, Article t1) {
                            return removeAccents(article.getName()).compareToIgnoreCase(removeAccents(t1.getName()));
                        }
                    });
                    listViewAdapter.notifyDataSetChanged();
                }
                return true;
            case R.id.reverseButton:
                if (articles.isEmpty()) {
                    Toast.makeText(this, "List je prázdný.", Toast.LENGTH_SHORT).show();
                } else {
                    Collections.reverse(articles);
                    listViewAdapter.notifyDataSetChanged();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String removeAccents(String wordWithAccents) {
        String cleanString = Normalizer.normalize(wordWithAccents, Normalizer.Form.NFD);
        cleanString = cleanString.replaceAll("[^\\p{ASCII}]", "");
        return cleanString;
    }


    @Override
    public void onStart() {
        super.onStart();
        articles.clear();
        articles.addAll(settings.getArticles());
        listViewAdapter.notifyDataSetChanged();
        refreshAmountTextView();
        askForPermission();
    }


    public void askForPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // you already have a permission
                checkForDatabaseUpdate();
            } else {
                // asks for permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        } else { //you dont need to worry about these stuff below api level 23
        }
    }


    //  After user allows permissions, it checks if the database is up to date. If not, it offers update.
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkForDatabaseUpdate();
            } else {
                preloadDatabaseData();
            }
        }
    }


    int onlineDbVersionNumber;

    private void checkForDatabaseUpdate() {

        Retrofit retrofit = new Retrofit.Builder().baseUrl(Api.BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        Api api = retrofit.create(Api.class);
        Call<DatabaseVersion> call = api.getDatabaseVersionInfo();
        call.enqueue(new Callback<DatabaseVersion>() {
            @Override
            public void onResponse(Call<DatabaseVersion> call, Response<DatabaseVersion> response) {

                if (!response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Dotaz nebyl úspešný." + response.code(), Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    DatabaseVersion version = response.body();
                    onlineDbVersionNumber = Integer.valueOf(version.getDatabaseVersion());
                    if (onlineDbVersionNumber > settings.getCurrentDatabaseVersion()) {
                        updateFoundDialog(MainActivity.this);
                    } else {
                        preloadDatabaseData();
                    }
                }
            }

            @Override
            public void onFailure(Call<DatabaseVersion> call, Throwable t) {
                preloadDatabaseData();
                Toast.makeText(getApplicationContext(), "Nepřipojeno k internetu", Toast.LENGTH_LONG).show();
            }
        });
    }


    private void updateFoundDialog(Context context) {
        new AlertDialog.Builder(context).setTitle("Aktualizace databaze").setMessage(String.format("Je dostupná %d. verze databáze.\nPo aktualizaci se aplikace sama restartuje a data, se kterýma pracujete se vymažou.", onlineDbVersionNumber)).setPositiveButton("Aktualizovat", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                createDownloadingDatabaseDialog();
                updateDatabase();
            }
        }).setNegativeButton("Neaktualizovat", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                preloadDatabaseData();
            }
        }).setIcon(R.drawable.ic_file_download).show();
    }

    private ProgressBar downloadingDatabaseProgressBar;


    private void createDownloadingDatabaseDialog() {
        View view = getLayoutInflater().inflate(R.layout.start_downloading_dialog, null);
        downloadingDatabaseProgressBar = view.findViewById(R.id.progressBar);
        downloadDatabaseTextview = (TextView) view.findViewById(R.id.loadingInfoTextView);
        downloadingDialog = new AlertDialog.Builder(this).setView(view).setCancelable(false).create();
    }


    private void updateDatabase() {
        try {
            DatabaseDownloader databaseDownloader = new DatabaseDownloader(this, onlineDbVersionNumber, downloadingDialog, downloadingDatabaseProgressBar, settings);
            databaseDownloader.download("http://www.skladovypomocnik.cz/BooksDatabase.db");
        } catch (Exception e) {
            Toast.makeText(this, "Aktualizace se nezdařila.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void preloadDatabaseData() {
        try {
            if (Model.getInstance().getNamesAndPrices() == null) {
                Toast.makeText(MainActivity.this, "Čekejte", Toast.LENGTH_LONG).show();
                DatabaseLoaderAsyncTask databaseLoader = new DatabaseLoaderAsyncTask(this, loadingDialog, progress, loadingInfoTextView);
                databaseLoader.execute();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        settings.setArticles(articles);
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
            listView.smoothScrollToPosition(0);
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
            articles.add(0, new Article(ean, 1, lookUpNameInDatabase(ean), lookUpPriceInDatabase(ean)));
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
        articles.add(0, new Article(currentEan, amount, a.getName(), a.getPrice()));
    }

    private void addNewItemIfNotFound(String ean) {
        if (!containsEan) {
            articles.add(0, new Article(ean, 1, lookUpNameInDatabase(ean), lookUpPriceInDatabase(ean)));
        }
    }

    private String lookUpNameInDatabase(String ean) {
        String name = Model.getInstance().getName(ean);
        if (name == null) {
            name = "NÁZEV NENALEZEN";
        }
        return name;
    }

    private String lookUpPriceInDatabase(String ean) {
        String price = Model.getInstance().getPrice(ean);
        if (price == null) {
            price = "***";
        }
        return price;
    }


    // This one is called when user selects item from listView, it calls a new window that offers delete row, edit ammount and add amount functions.
    private void setSelectedItemListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

        addEanClearInputAmountAndCloseDialog();

        deleteDialog.show();
    }


    private void addEanClearInputAmountAndCloseDialog() {
        inputAmount.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent keyevent) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    String ean = inputAmount.getText().toString();
                    if (!ean.isEmpty()) {
                        handleAddingEan(ean);
                    }
                    inputAmount.setText("");
                    deleteDialog.dismiss();
                }
                return false;
            }
        });
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
            articles.set(selectedIndex, new Article(ean, newAmount, articles.get(selectedIndex).getName(), articles.get(selectedIndex).getPrice()));
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
        articles.set(selectedIndex, new Article(ean, amount.intValue(), articles.get(selectedIndex).getName(), articles.get(selectedIndex).getPrice()));
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
        if (listViewAdapter.getCount() == 0) {
            createAndDisplayToast("Není vložený žádný ean");
        } else if (settings.getIp().length() == 0) {
            createAndDisplayToast("Není vložená IP adresa");
        } else if (!fileHasName()) {
            createAndDisplayToast("Není vložený název souboru");
        } else {

            String data = convertListToJson();
            sendData(data);
        }
    }


    private String convertListToJson() {
        Gson gson = new Gson();
        String fileName = inputFilename.getText().toString();
        Map<String, ArrayList<Article>> nameAndList = new HashMap<>();
        nameAndList.put(fileName, articles);
        String json = gson.toJson(nameAndList);
        return json;
    }


    private void createAndDisplayToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }


    private boolean fileHasName() {
        return inputFilename.getText().length() > 0;
    }


    private void sendData(String data) {
        if (Authentication.getInstance().isTurnedOff()) {
            Toast.makeText(this, "Exportovaní selhalo.", Toast.LENGTH_SHORT).show();
        } else {
            Client sender = new Client(settings.getIp(), this);
            sender.execute(data.toString());
        }
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
        loadingDialog = new AlertDialog.Builder(this).setCancelable(false).setView(view).create();
    }

    private void setButtonListeners() {
        this.findViewById(R.id.exportButton).setOnClickListener(this);
        this.findViewById(R.id.deleteAllButton).setOnClickListener(this);
        this.findViewById(R.id.networkSettingsButton).setOnClickListener(this);
    }

}
