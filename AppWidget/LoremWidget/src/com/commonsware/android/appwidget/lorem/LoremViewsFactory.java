package com.commonsware.android.appwidget.lorem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

public class LoremViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    List<Entries> allMessages = new ArrayList<Entries>();
    HashMap<Integer, Integer> threadCount = new HashMap<Integer, Integer>();
    private Context ctxt = null;

    public LoremViewsFactory(Context ctxt, Intent intent) {
        this.ctxt=ctxt;
        getSMS();
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public int getCount() {
        return allMessages.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {

        RemoteViews row=new RemoteViews(ctxt.getPackageName(), R.layout.row);

        row.setTextViewText(R.id.contact, allMessages.get(position).contact + " - " + threadCount.get(allMessages.get(position).thread_id));
        row.setTextViewText(R.id.date, allMessages.get(position).date);
        row.setTextViewText(R.id.content, allMessages.get(position).content);

        Intent i=new Intent();
        Bundle extras=new Bundle();

        extras.putInt(WidgetProvider.EXTRA_WORD, allMessages.get(position).thread_id);
        i.putExtras(extras);
        row.setOnClickFillInIntent(R.id.wid_row, i);

        return row;
    }

    class Entries {
        String contact;
        String content;
        String date;
        int thread_id;
        Entries (String p, String s, String d, int id) {
            contact = p;
            content = s;
            date = d;
            thread_id = id;
        }
    }

    public void getSMS() {
        Uri uriSMSURI = Uri.parse("content://sms/inbox");
        Cursor cur = ctxt.getContentResolver().query(uriSMSURI, null, null, null, "date");
        cur.moveToLast();
        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd");
        Date iDate = new Date();
        while (cur.moveToPrevious()) {
            int thread_id = cur.getInt(cur.getColumnIndexOrThrow("thread_id"));
            if (threadCount.get(thread_id) != null) {
                threadCount.put(thread_id, threadCount.get(thread_id) != null ? threadCount.get(thread_id) + 1 : 1);
                continue;
            }
            String address = cur.getString(cur.getColumnIndex("address"));
            String body = cur.getString(cur.getColumnIndexOrThrow("body"));
            long date = cur.getLong(cur.getColumnIndexOrThrow("date"));
            Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
            Cursor c = ctxt.getContentResolver().query(uri, new String[]{PhoneLookup.DISPLAY_NAME}, null , null, null);
            if (threadCount.get(thread_id) == null) {
                iDate.setTime(date);
                allMessages.add(new Entries((c.moveToFirst() ? c.getString(0) : address), body, formatter.format(iDate), thread_id));
            }
            threadCount.put(thread_id, threadCount.get(thread_id) != null ? threadCount.get(thread_id) + 1 : 1);
            c.close();
        }
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return(position);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onDataSetChanged() {
    }
}