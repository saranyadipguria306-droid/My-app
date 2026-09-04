package com.example.mydialer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout root, content;
    TextView number;
    final ArrayList<Contact> contacts = new ArrayList<>();
    final ArrayList<Contact> favorites = new ArrayList<>();
    final int GREEN=0xFF34C759, BG=0xFFF8F8FA, MUTED=0xFF77777C;

    int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    TextView text(String s,float z){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(0xFF151517);return t;}
    Button button(String s,float z){Button b=new Button(this);b.setText(s);b.setTextSize(z);b.setAllCaps(false);b.setPadding(0,0,0,0);return b;}

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        showDialer();
        if(checkSelfPermission(Manifest.permission.READ_CONTACTS)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},10);
        } else loadContacts();
    }

    void shell(String titleText){
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(BG);
        LinearLayout header=new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(20),dp(10),dp(20),0);
        TextView h=text(titleText,29); h.setTypeface(null,1);
        header.addView(h,new LinearLayout.LayoutParams(0,dp(55),1));
        root.addView(header);
        content=new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(dp(16),0,dp(16),0);
        root.addView(content,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout nav=new LinearLayout(this); nav.setGravity(Gravity.CENTER); nav.setBackgroundColor(0xFFFFFFFF);
        Button d=button("⌨\nKeypad",12), f=button("★\nFavorites",12), c=button("👥\nContacts",12);
        d.setOnClickListener(v->showDialer()); f.setOnClickListener(v->showFavorites()); c.setOnClickListener(v->showContacts());
        nav.addView(d,new LinearLayout.LayoutParams(0,dp(64),1));
        nav.addView(f,new LinearLayout.LayoutParams(0,dp(64),1));
        nav.addView(c,new LinearLayout.LayoutParams(0,dp(64),1));
        root.addView(nav);
        setContentView(root);
    }

    void showDialer(){
        shell("Phone");
        number=text("",32); number.setGravity(Gravity.CENTER); content.addView(number,new LinearLayout.LayoutParams(-1,dp(75)));
        String[] keys={"1","2\nABC","3\nDEF","4\nGHI","5\nJKL","6\nMNO","7\nPQRS","8\nTUV","9\nWXYZ","*","0\n+","#"};
        LinearLayout grid=new LinearLayout(this); grid.setOrientation(LinearLayout.VERTICAL);
        for(int r=0;r<4;r++){
            LinearLayout row=new LinearLayout(this);
            for(int col=0;col<3;col++){
                int idx=r*3+col; Button b=button(keys[idx],21);
                b.setOnClickListener(v->{String s=((Button)v).getText().toString();number.setText(number.getText().toString()+s.substring(0,1));});
                row.addView(b,new LinearLayout.LayoutParams(0,dp(62),1));
            }
            grid.addView(row,new LinearLayout.LayoutParams(-1,0,1));
        }
        content.addView(grid,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout actions=new LinearLayout(this);actions.setGravity(Gravity.CENTER);
        Button call=button("●",30);call.setTextColor(0xFFFFFFFF);call.setBackgroundColor(GREEN);call.setOnClickListener(v->call(number.getText().toString()));
        Button del=button("⌫",24);del.setOnClickListener(v->{String s=number.getText().toString();if(!s.isEmpty())number.setText(s.substring(0,s.length()-1));});
        actions.addView(call,new LinearLayout.LayoutParams(dp(78),dp(68)));
        actions.addView(del,new LinearLayout.LayoutParams(dp(78),dp(68)));
        content.addView(actions);
    }

    void call(String n){
        if(n==null||n.trim().isEmpty()){Toast.makeText(this,"Enter a number",Toast.LENGTH_SHORT).show();return;}
        if(checkSelfPermission(Manifest.permission.CALL_PHONE)!=PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE},11); return;
        }
        startActivity(new Intent(Intent.ACTION_CALL,Uri.parse("tel:"+Uri.encode(n))));
    }

    void showContacts(){
        shell("Contacts");
        EditText search=new EditText(this);search.setHint("Search contacts");search.setSingleLine(true);
        content.addView(search,new LinearLayout.LayoutParams(-1,dp(52)));
        ScrollView scroll=new ScrollView(this);LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);scroll.addView(list);
        content.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        Runnable refresh=()->{list.removeAllViews();String q=search.getText().toString().toLowerCase(Locale.ROOT);for(Contact x:contacts)if(x.name.toLowerCase(Locale.ROOT).contains(q)||x.phone.contains(q))addRow(list,x);};
        refresh.run();
        search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int b,int c){}public void onTextChanged(CharSequence s,int a,int b,int c){refresh.run();}public void afterTextChanged(android.text.Editable e){}});
    }

    void addRow(LinearLayout list,Contact x){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(dp(5),dp(5),dp(5),dp(5));
        TextView av=text(initials(x.name),18);av.setGravity(Gravity.CENTER);av.setTextColor(0xFFFFFFFF);av.setBackgroundColor(0xFF8E8E93);
        row.addView(av,new LinearLayout.LayoutParams(dp(48),dp(48)));
        LinearLayout info=new LinearLayout(this);info.setOrientation(LinearLayout.VERTICAL);info.setPadding(dp(12),0,0,0);
        TextView n=text(x.name,17);n.setTypeface(null,1);TextView p=text(x.phone,14);p.setTextColor(MUTED);info.addView(n);info.addView(p);
        row.addView(info,new LinearLayout.LayoutParams(0,dp(58),1));
        TextView star=text(isFav(x)?"★":"☆",27);star.setTextColor(GREEN);star.setGravity(Gravity.CENTER);star.setOnClickListener(v->{toggle(x);showContacts();});row.addView(star,new LinearLayout.LayoutParams(dp(45),dp(55)));
        row.setOnClickListener(v->showContact(x));list.addView(row);
    }

    void showContact(Contact x){
        shell("Contact");
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setGravity(Gravity.CENTER);box.setPadding(0,dp(20),0,dp(20));
        TextView av=text(initials(x.name),38);av.setTextColor(0xFFFFFFFF);av.setGravity(Gravity.CENTER);av.setBackgroundColor(0xFF8E8E93);box.addView(av,new LinearLayout.LayoutParams(dp(105),dp(105)));
        TextView n=text(x.name,27);n.setTypeface(null,1);n.setGravity(Gravity.CENTER);box.addView(n);
        TextView p=text(x.phone,17);p.setTextColor(MUTED);p.setGravity(Gravity.CENTER);box.addView(p);
        content.addView(box);
        LinearLayout actions=new LinearLayout(this);actions.setGravity(Gravity.CENTER);
        Button call=button("📞\nCall",15);call.setOnClickListener(v->call(x.phone));
        Button msg=button("💬\nMessage",15);msg.setOnClickListener(v->startActivity(new Intent(Intent.ACTION_SENDTO,Uri.parse("smsto:"+Uri.encode(x.phone)))));
        Button fav=button((isFav(x)?"★":"☆")+"\nFavorite",15);fav.setOnClickListener(v->{toggle(x);showContact(x);});
        actions.addView(call,new LinearLayout.LayoutParams(0,dp(80),1));actions.addView(msg,new LinearLayout.LayoutParams(0,dp(80),1));actions.addView(fav,new LinearLayout.LayoutParams(0,dp(80),1));content.addView(actions);
    }

    void showFavorites(){
        shell("Favorites");
        if(favorites.isEmpty()){TextView e=text("No favorites yet",18);e.setGravity(Gravity.CENTER);content.addView(e,new LinearLayout.LayoutParams(-1,-1));}
        else for(Contact x:favorites)addRow(content,x);
    }

    String initials(String s){if(s==null||s.trim().isEmpty())return "?";String[] a=s.trim().split("\\s+");return a.length==1?a[0].substring(0,1).toUpperCase(Locale.ROOT):(""+a[0].charAt(0)+a[a.length-1].charAt(0)).toUpperCase(Locale.ROOT);}
    boolean isFav(Contact x){for(Contact f:favorites)if(f.phone.equals(x.phone))return true;return false;}
    void toggle(Contact x){if(isFav(x)){for(int i=0;i<favorites.size();i++)if(favorites.get(i).phone.equals(x.phone)){favorites.remove(i);break;}}else favorites.add(x);}
    void loadContacts(){contacts.clear();try{Cursor c=getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,ContactsContract.CommonDataKinds.Phone.NUMBER},null,null,ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" ASC");if(c!=null){while(c.moveToNext()){String n=c.getString(0),p=c.getString(1);contacts.add(new Contact(n==null?"Unknown":n,p==null?"":p));}c.close();}}catch(Exception ignored){}}
    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==10&&g.length>0&&g[0]==PackageManager.PERMISSION_GRANTED)loadContacts();if(r==11&&g.length>0&&g[0]==PackageManager.PERMISSION_GRANTED)call(number==null?"":number.getText().toString());}
    static class Contact{String name,phone;Contact(String n,String p){name=n;phone=p;}}
}
