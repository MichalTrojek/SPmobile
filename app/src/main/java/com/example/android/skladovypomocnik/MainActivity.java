package com.example.android.skladovypomocnik;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigInteger;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getName();
    private ListView list;
    private ArrayList<Article> articles;
    private ArticleListAdapter adapter;
    private boolean containsEan = false;
    private AlertDialog deleteDialog;
    private AlertDialog ipDialog;
    private int selectedIndex;
    private Settings settings;
    private EditText inputEanText;
    private EditText inputIpAddress;
    private EditText amountInput;
    private TextView ipAddressTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        settings = new Settings(this);
        inputEanText = (EditText) findViewById(R.id.inputEanText);
        inputEanText.requestFocus();


        articles = new ArrayList<>();
        adapter = new ArticleListAdapter(this, R.layout.adapter_layout, articles);


        list = (ListView) findViewById(R.id.listView);
        list.setAdapter(adapter);
        list.setFocusable(false);


        createIpAddressAlertDialog();
        createListItemAlertDialog();
        setButtonListeners();
        setInputListener();
        setSelectedItemListener();


    }

    @Override
    public void onStop() {
        super.onStop();
        settings.setArticles(convertArrayListToString());
    }

    @Override
    public void onStart() {
        super.onStart();
        articles.clear();
        String data = settings.getArticles();
        if (data.length() > 0) {
            articles.addAll(convertDataToArrayList(settings.getArticles()));
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("articles", convertArrayListToString());
        super.onSaveInstanceState(outState);
    }


    private String convertArrayListToString() {
        StringBuffer sb = new StringBuffer();
        for (Article a : articles) {
            sb.append(a.getEan()).append(".").append(a.getAmount()).append(";");
        }
        return sb.toString();
    }


    @Override
    public void onRestoreInstanceState(Bundle in) {
        super.onRestoreInstanceState(in);
        String data = in.getString("articles");
        articles.addAll(convertDataToArrayList(data));
        adapter.notifyDataSetChanged();
    }

    public ArrayList<Article> convertDataToArrayList(String data) {
        String[] splittedData = data.split(";");
        ArrayList<Article> articles = new ArrayList<>();
        for (String s : splittedData) {
            String[] temp = s.split("\\.");
            articles.add(new Article(temp[0], Integer.parseInt(temp[1])));
        }
        return articles;
    }


    private void createListItemAlertDialog() {
        View view = getLayoutInflater().inflate(R.layout.edit_listview_dialog, null);
        view.findViewById(R.id.deleteButton).setOnClickListener(MainActivity.this);
        view.findViewById(R.id.cancelButton).setOnClickListener(MainActivity.this);
        view.findViewById(R.id.editButton).setOnClickListener(MainActivity.this);
        amountInput = (EditText) view.findViewById(R.id.amountInput);
        deleteDialog = new AlertDialog.Builder(this).setView(view).create();
    }


    private void createIpAddressAlertDialog() {
        View view = getLayoutInflater().inflate(R.layout.edit_ip_dialog, null);
        view.findViewById(R.id.editIpButton).setOnClickListener(MainActivity.this);
        view.findViewById(R.id.backButton).setOnClickListener(MainActivity.this);
        inputIpAddress = (EditText) view.findViewById(R.id.editIpText);
        ipAddressTextView = (TextView) view.findViewById(R.id.ipAddressTextView);
        ipAddressTextView.setText("IP Adresa : " + settings.getIp());
        ipDialog = new AlertDialog.Builder(this).setView(view).create();
    }

    private void setButtonListeners() {
        this.findViewById(R.id.exportButton).setOnClickListener(this);
        this.findViewById(R.id.deleteAllButton).setOnClickListener(this);
        this.findViewById(R.id.networkSettingsButton).setOnClickListener(this);
    }


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
            articles.add(0, new Article(ean, 1));
        } else {
            iterateListForMatch(articles, ean);
        }
        adapter.notifyDataSetChanged();
        inputEanText.setText("");
    }


    private void iterateListForMatch(ArrayList<Article> articles, String ean) {
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
        articles.add(0, new Article(currentEan, amount));
    }

    private void addNewItemIfNotFound(String ean) {
        if (!containsEan) {
            articles.add(0, new Article(ean, 1));
        }
    }


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
                break;
            case R.id.deleteButton:
                handleDeleteItemButton();
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
            default:
                break;
        }
    }

    private void handleCancelButton() {
        deleteDialog.dismiss();
    }

    private void handleEditAmountButton() {
        if (!amountInput.getText().toString().isEmpty()) {
            changeAmount();
        }
        deleteDialog.dismiss();
    }

    private void changeAmount() {
        BigInteger amount = new BigInteger(amountInput.getText().toString());
        if (amount.intValue() > 100000 || amount.intValue() < 0) {
            Toast.makeText(this, "Příliš velké nebo malé číslo", Toast.LENGTH_SHORT).show();
        } else {
            amountInput.setText("");
            String ean = articles.get(selectedIndex).getEan();
            articles.set(selectedIndex, new Article(ean, amount.intValue()));
            adapter.notifyDataSetChanged();
        }
    }


    private void handleDeleteItemButton() {
        articles.remove((articles.get(selectedIndex)));
        adapter.notifyDataSetChanged();
        deleteDialog.dismiss();
    }

    private void handleEditIpButton() {
        String ip = inputIpAddress.getText().toString().replace(" ", "");
        if (ip.length() != 0) {
            setNewIpAddress(ip);
        } else {
            Toast.makeText(this, "Vlož IP Adresu", Toast.LENGTH_SHORT).show();
        }
    }

    private void setNewIpAddress(String ip) {
        settings.setIP(ip);
        ipAddressTextView.setText("IP Adresa : " + settings.getIp());
        ipDialog.dismiss();
        Toast.makeText(this, "IP Adresa : " + settings.getIp() + " uložena", Toast.LENGTH_SHORT).show();
    }


    private void handleBackButton() {
        ipDialog.dismiss();
    }


    private void handleExportButton() {
        String data = convertArrayListToString();
        if (data.length() == 0) {
            Toast.makeText(this, "Není vložený žádný ean", Toast.LENGTH_SHORT).show();
        } else {
            sendData(data);
        }
    }


    private void sendData(String data) {
        Sender sender = new Sender(settings.getIp());
        sender.execute(data.toString());
        Toast.makeText(this, "Data byla vyexportována", Toast.LENGTH_SHORT).show();
    }


    private void handleDeleteAllItemsButton() {
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
                articles.clear();
                adapter.notifyDataSetChanged();
            }
        }).create();
        builder.show();

    }

    private void handleNetworkSettingsButton() {
        ipDialog.show();
    }


}
