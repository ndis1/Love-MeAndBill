package com.ndis.loveMeAndBill;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.LinkedList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

public class MainActivity extends Activity implements OnTouchListener {
	private Generator currentGenerator;
	private  GeneratorBuilder gb;
	private int whichSource;
	private int seedSize;
	private AssetManager assetManager;
	private int n;
	private Resources res;
	static final int SHAKES_SHOWN = 10;
	//constants for swipe detection
	static final int MIN_DISTANCEH = 5;
	static final int MIN_DISTANCEV = 10;
    private float downX, downY, upX, upY;
    //constants for which source is used
    private static final int COMEDY = 0;
    private final int TRAGEDY = 1;
    private final int SONNET = 2;
    private boolean shakesInputOn;
    private LinkedList<String> forwardStrings;
    private LinkedList<String> backwardStrings;
    private ViewSwitcher switcher;
    //the text that is displayed
    private String savedStringMine;
    private String savedStringBill;
    private Spinner nSetterspinner;
    private  Animation yourAnimation;
    RelativeLayout rlM;

    //shakespeare text that is up for consideration
    private String liveCandidate;
     ImageView imageView;
    BuildGeneratorTask BGT;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        forwardStrings = new LinkedList<String>();
        backwardStrings = new LinkedList<String>();
        savedStringMine = "";
        savedStringBill = "";
        liveCandidate = "";
        res = getResources();
        whichSource = COMEDY;
        n = 2;
        setContentView(R.layout.activity_main);
        assetManager = getAssets();
        gb = new GeneratorBuilder(assetManager, res);
        currentGenerator = gb.getNgramDistribution(R.array.comedy, n);
        seedSize = currentGenerator.getNValue();
    	nSetterspinner = (Spinner) findViewById(R.id.n_level);
        setUpSpinner();
        shakesInputOn = false;
        switcher = (ViewSwitcher) findViewById(R.id.main_view);
		switcher.setOnTouchListener(this);
		SharedPreferences prefs = this.getSharedPreferences(
			      "com.ndis.loveMeAndBill", Context.MODE_PRIVATE);
		int spinnerValue = prefs.getInt("userChoicenSetterspinner",-1);
		if(spinnerValue != -1) {
			n = spinnerValue;
		  nSetterspinner.setSelection(spinnerValue);
		}
		yourAnimation  = AnimationUtils.loadAnimation(this, R.layout.loadinganim);
		yourAnimation.setDuration(1000);
		yourAnimation.setRepeatCount(Animation.INFINITE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	switch(whichSource){
	    	case 0:{
	            menu.findItem(R.id.source_icon).setIcon(R.drawable.comedy);
	
	            menu.findItem(R.id.use_comedy).setVisible(false);
	            menu.findItem(R.id.use_sonnets).setVisible(true);
	            menu.findItem(R.id.use_tragedy).setVisible(true);
	            break;
	    	}
	    	case 1:{
	            menu.findItem(R.id.source_icon).setIcon(R.drawable.tragedy);
	            menu.findItem(R.id.use_comedy).setVisible(true);
	            menu.findItem(R.id.use_sonnets).setVisible(true);
	            menu.findItem(R.id.use_tragedy).setVisible(false);
	            break;
	    	}
	    	case 2:{
	            menu.findItem(R.id.source_icon).setIcon(R.drawable.rose);
	            menu.findItem(R.id.use_comedy).setVisible(true);
	            menu.findItem(R.id.use_sonnets).setVisible(false);
	            menu.findItem(R.id.use_tragedy).setVisible(true);
	            break;
	    	}
    	}
    	if(shakesInputOn){
            menu.findItem(R.id.settings).setVisible(false);
            menu.findItem(R.id.use_comedy).setVisible(false);
            menu.findItem(R.id.use_tragedy).setVisible(false);
            menu.findItem(R.id.use_sonnets).setVisible(false);


    	}else{
            menu.findItem(R.id.settings).setVisible(true);
            menu.findItem(R.id.use_comedy).setVisible(true);
            menu.findItem(R.id.use_tragedy).setVisible(true);
            menu.findItem(R.id.use_sonnets).setVisible(true);
    	}
    	return true;
    }
    
    @Override
  	public boolean onOptionsItemSelected(MenuItem item) {
  	    // Handle item selection
  	    switch (item.getItemId()){
  	    	case R.id.clearText:
  	    		reallyClear();
  	    		return true;
  	    	case R.id.use_comedy:
  	    		useComedy();
  	    		return true;
  	    	case R.id.use_tragedy:
  	    		useTragedy();
  	    		return true;
  	    	case R.id.use_sonnets:
  	    		useSonnets();
  	    		return true;
  	    	case R.id.send_email:
  	    		signoffQuestion(false);
  	    		return true;
  	    	case R.id.send_sms:
  	    		signoffQuestion(true);
  	    		return true;
  	    	case R.id.settings:
  	    		settingsVisible(true);
  	    		return true;
  	    	
  	        default:
  	            return super.onOptionsItemSelected(item);
  	    }  	    
  	}
    
    @Override
    public void onBackPressed(){
    	//back does nothing
    }
    public String readText(){
    	EditText mainView =(EditText) findViewById(R.id.main_edittext);
    	String currentText = mainView.getText().toString();
    	String[] currentTokens = currentText.split(" ");
    	int currentTokensSize = currentTokens.length;
    	LinkedList<String> seed = null;
    	if(currentTokensSize < seedSize){
    		seedSize = currentTokensSize;
    	}
    	for(int w = 0; w < seedSize; w++){
    		if(seed == null){
    			seed = new LinkedList<String>();
    		}
    		seed.add( currentTokens[currentTokensSize  - (seedSize - w)]);
    	}

    	ArrayList<String> gentextList = currentGenerator.generateString(seed,-1);

    	String gentextString = "";
    	for (String s : gentextList){
    		gentextString = gentextString +" "+ s;
    	}
    	return gentextString;
    }
    
   public void changeNPrepare(int n){
	   currentGenerator = null;
	   if(imageView == null){
		   imageView = (ImageView) findViewById(R.id.blankImageViewM);  
	   }
	   imageView.setVisibility(View.VISIBLE);
	   rlM = (RelativeLayout)(findViewById(R.id.bac_dim_layoutM));
	   rlM.setVisibility(View.VISIBLE);
	   imageView.startAnimation(yourAnimation);  
	   BGT = new BuildGeneratorTask();
	   BGT.execute(new GeneratorBuilder[]{gb});
	   
   }
   public void changeSourcePrepare(int source){
	   whichSource = source;
	   currentGenerator = null;
	   this.invalidateOptionsMenu();
	   if(imageView == null){
		   imageView = (ImageView) findViewById(R.id.blankImageViewM);  
	   }
	   if(source == COMEDY){
		   imageView.setImageDrawable(res.getDrawable(R.drawable.comedy));
	   }else if(source == TRAGEDY){
		   imageView.setImageDrawable(res.getDrawable(R.drawable.tragedy));
	   }else{
		   imageView.setImageDrawable(res.getDrawable(R.drawable.rose));
	   }
	   imageView.setVisibility(View.VISIBLE);
	   rlM = (RelativeLayout)(findViewById(R.id.bac_dim_layoutM));
	   rlM.setVisibility(View.VISIBLE);
	   imageView.startAnimation(yourAnimation);  
	   BGT = new BuildGeneratorTask();
	   BGT.execute(new GeneratorBuilder[]{gb});
   }
   public void useTragedy(){
	   if(whichSource != TRAGEDY){
		   changeSourcePrepare(TRAGEDY);
	   }
   }
   public void useComedy(){
	   if(whichSource != COMEDY){
		   changeSourcePrepare(COMEDY);
	   }
    }
    public void useSonnets(){
	   if(whichSource != SONNET){
		   changeSourcePrepare(SONNET);
	   }
    }
    public void clearText(){
    	TextView mainView = getTextView();
    	mainView.setText("");
    	liveCandidate = "";
        savedStringMine = "";
        savedStringBill = "";
		backwardStrings.clear();
		forwardStrings.clear();
    }
    
    //send an email to a provided email address with a report of the user's performance 
    private void sendEmail(){
    	final String reportString = getCurrentText(); 
	    final AlertDialog.Builder emailAlert = new AlertDialog.Builder(this);
	    final EditText input = new EditText(this);
	    emailAlert.setTitle("Email Results");
	    emailAlert.setMessage("Email to send results to:");
	    emailAlert.setView(input);
	    emailAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            String value = input.getText().toString().trim();
	            Intent email = new Intent(Intent.ACTION_SEND);
	        	email.putExtra(Intent.EXTRA_EMAIL, new String[]{value});		
	        	//use the parse user object to insert the name of the user into the email
	        	email.putExtra(Intent.EXTRA_TEXT, reportString);
	        	email.setType("message/rfc822");
	        	startActivity(Intent.createChooser(email, "Choose an Email client :"));
	        }
	    });
	    emailAlert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            dialog.cancel();
	        }
	    });
	    emailAlert.show();               
    }
    private void sendSMS(){
    	try {
    		 String currentString = getCurrentText();
		     Intent sendIntent = new Intent(Intent.ACTION_VIEW);
		     sendIntent.putExtra("sms_body", currentString); 
		     sendIntent.setType("vnd.android-dir/mms-sms");
		     startActivity(sendIntent);

		} catch (Exception e) {
			Toast.makeText(getApplicationContext(),
				"SMS faild, please try again later!",
				Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
    }
    private String getCurrentText(){
    	TextView mainView =getTextView();
    	String currentString = mainView.getText().toString();
		return currentString;
    }
    private TextView getTextView(){
    	TextView mainView;
    	if(shakesInputOn){
    		mainView =(TextView) findViewById(R.id.shake_text);
    	}else{
        	mainView =(TextView) findViewById(R.id.main_edittext);
    	}
    	return mainView;
    }
    private void signoffQuestion(boolean _sms){
    	final boolean sms = _sms;
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Add Closing?");
		builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   addSignoff();
	        	   if(sms){
	        		   sendSMS();
	        	   }else{
	        		   sendEmail();
	        	   }
	           }
	       });
		builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   if(sms){
	        		   sendSMS();
	        	   }else{
	        		   sendEmail();
	        	   }
	           }
	       });
		AlertDialog dialog = builder.create();
		dialog.show();
    }
    private void reallyClear(){
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Restart Message?");
		builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   clearText();
	           }
	       });
		builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   dialog.dismiss();
	           }
	       });
		AlertDialog dialog = builder.create();
		dialog.show();
    }
    private void  addSignoff(){
    	TextView mainView =getTextView();
    	String currentString = getCurrentText();
    	mainView.setText(currentString + res.getString(R.string.signoff));
    }
    
    private void setUpSpinner(){
        
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
             R.array.n_level, android.R.layout.simple_spinner_item);
     
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        // Apply the adapter to the spinner
        nSetterspinner.setAdapter(adapter);
        nSetterspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            	String val = (String)nSetterspinner.getSelectedItem();
            	if(val.contains("One")){
            		n = 1;
            	}else if(val.contains("Two")){
            		n = 2;
            	}else if(val.contains("Three")){
            		n = 3;
            	}
            	changeNPrepare(n);
            	SharedPreferences sharedPref = getSharedPreferences("com.ndis.loveMeAndBill",0);
            	SharedPreferences.Editor prefEditor = sharedPref.edit();
            	prefEditor.putInt("userChoicenSetterspinner",pos);
            	prefEditor.commit();
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        nSetterspinner.setPrompt(res.getString(R.string.choose_n_prompt));
        nSetterspinner.setVisibility(View.INVISIBLE);
    }
    
    private void settingsVisible(boolean on){
    	if(!shakesInputOn){
	    	final Spinner spinner = (Spinner) findViewById(R.id.n_level);
	    	if(on){
	    		spinner.performClick();
	    	}else{
	    		spinner.performClick();
	    	}
    	}
    }
    
    @Override
	public boolean onTouch(View v, MotionEvent event) {
    	if(shakesInputOn){
	    	switch(event.getAction()){
	        case MotionEvent.ACTION_DOWN: {
	            downX = event.getX();
	            downY = event.getY();
	            return true;
	        }
	        case MotionEvent.ACTION_UP: {
	            upX = event.getX();
	            upY = event.getY();

	            float deltaX = downX - upX;
	            float deltaY = downY - upY;
	            Log.w("XChange", deltaX+"");
	            Log.w("YChange", deltaY+"");

	            if(Math.abs(deltaX)*2>Math.abs(deltaY)){
		            if(Math.abs(deltaX) > MIN_DISTANCEH){
		                // left or right
		                if(deltaX < 0) { return this.onLeftToRightSwipe() ; }
		                if(deltaX > 0) {  return this.onRightToLeftSwipe(); }
		            }
	            }
	            // swipe vertical?
	            if(Math.abs(deltaY) > MIN_DISTANCEV){
	                // top or down
	                if(deltaY < 0) {  return this.onTopToBottomSwipe(); }
	                if(deltaY > 0) {  return this.onBottomToTopSwipe(); }
	            }
	            else {
	            	return false; // We don't consume the event
	            }
	            return true;
		        }
		    }
    	}
	    return false;
	}
    
    public boolean onRightToLeftSwipe(){  
    	String generatedText = "";
    	generatedText = forwardStrings.pollFirst();
    	if(generatedText == null){
    		generatedText = readText();
    	}
    	setText(generatedText);
    	liveCandidate = generatedText;
    	backwardStrings.addFirst(generatedText);
	    return true;
	}
	public boolean onLeftToRightSwipe(){ 
		String generatedText = "";
    	generatedText = backwardStrings.pollFirst();
    	if(generatedText == null){
    		generatedText = "";
    	}else{
    		forwardStrings.add(generatedText);
    	}
	    setText(generatedText);
	    liveCandidate = generatedText;
	    return true;
	}
	public boolean onTopToBottomSwipe(){ 
		//return to regular typing mode
		backwardStrings.clear();
		forwardStrings.clear();
		//keep live candidate
    	savedStringMine = savedStringMine + liveCandidate;
    	savedStringBill = savedStringBill + liveCandidate;
		setText("");
	    return true;
	}
	public boolean onBottomToTopSwipe(){
		//return to regular typing mode
		//shakesInputOn = false;
		backwardStrings.clear();
		forwardStrings.clear();
		//don't use any candidate
		setText("");
	    return true;
	}
	
	public void turnShakesInputOff(View view){
		shakesInputOn = false;
		setText("");
		switcher.showPrevious();
		invalidateOptionsMenu();
	}

	public void turnShakesInputOn(View view){
		TextView mainView =getTextView();
		String startText = readText();
		liveCandidate = startText;
		savedStringMine = mainView.getText().toString();
		String[] myTokens = savedStringMine.split(" ");
		int inputLenght = myTokens.length;
		if(inputLenght < SHAKES_SHOWN){
			savedStringBill = savedStringMine;
		}else{
			StringBuilder sb = new StringBuilder();
			for(int w = SHAKES_SHOWN; w > 0; w--){
				sb.append(myTokens[inputLenght -w]+" ");
			}
			savedStringBill = sb.toString();
		}
		backwardStrings.addFirst(liveCandidate);
		shakesInputOn = true;
		setText(liveCandidate);
		switcher.showNext();
		invalidateOptionsMenu();
	}
	
	private void setText(String newText){
		TextView mainView =getTextView();
		String savedString;
		if(shakesInputOn){
			savedString = savedStringBill;
		}else{
			savedString = savedStringMine;
		}
		mainView.setText(savedString+newText);
	}
	private class BuildGeneratorTask extends AsyncTask<GeneratorBuilder, Void, Generator> {
	   @Override
	   protected Generator doInBackground(GeneratorBuilder... _gb) {
		   Generator g = null;
		   GeneratorBuilder gb = _gb[0];
		   if(whichSource == COMEDY){
			   g =  gb.getNgramDistribution(R.array.comedy, n);
	    	}else if(whichSource == TRAGEDY){
	    		g =  gb.getNgramDistribution(R.array.tragedy, n);
	    	}else if(whichSource == SONNET){
	    		g =  gb.getNgramDistribution(R.array.sonnet, n);
	    	}
	    	return g;
	    }

	    @Override
	    protected void onPostExecute(Generator result) {
	    	currentGenerator = result;
			imageView.clearAnimation();
			imageView.setVisibility(View.INVISIBLE);
			rlM.setVisibility(View.GONE);
	    }
	 }
}