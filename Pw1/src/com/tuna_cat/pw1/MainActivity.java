package com.tuna_cat.pw1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.*;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;

public class MainActivity extends Activity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    private Handler handler = new Handler();
	Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_main);
		
    	Thread cThread = new Thread(new ClientThread());
	 	cThread.start();	
    }
    
    protected void executeCommand(JSONObject json) {
    	try {
			if (json.get("command").equals("appendValue")){
				Object value = json.get("value");
				String msg = (String) ((JSONObject)value).get("message");
				EditText et = (EditText)findViewById(R.id.editText1);
				et.getText().append(msg);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public class ClientThread implements Runnable {

    	public void run() {
    		SocketChannel sc = null;
    		try {
    			//InetAddress serverAddr = InetAddress.getByName("183.181.20.215");
    			SocketAddress address = new InetSocketAddress("183.181.20.215", 4000);
    			sc = SocketChannel.open(address);
    			socket = sc.socket();
    			String cmdstr = "{\"command\":\"open\",\"protocol\":\"alpha1\",\"client\":\"JAVA\"}\0";
    		    BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF8"));
    		    wr.write(cmdstr);
    		    
    		    cmdstr = "{\"command\":\"loginContext\",\"name\":\"pw\"}\0";
    		    wr.write(cmdstr);

    		    cmdstr = "{\"command\":\"loginContext\",\"name\":\"old\"}\0";  
    		    wr.write(cmdstr);
    		    
    		    wr.flush();

    		} 
        	catch (UnknownHostException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    			return;
    		}
        	catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		
    		try {
    			//InetAddress serverAddr = InetAddress.getByName("www.tuna-cat.com");
    			//Log.d("ClientActivity", "C: Connecting...");
    			//SocketChannel sc = socket.getChannel();
    			ByteBuffer buf = ByteBuffer.allocateDirect(4096*2);
    			
    			Charset charset = Charset.forName("UTF-8");
    			//CharsetEncoder encoder = charset.newEncoder();
    			CharsetDecoder decoder = charset.newDecoder();

				try {
					while (true) {
    					//InputStream in = socket.getInputStream();

						int start = 0;
						int end = 0;
						
    					while (true){
    						buf.compact();
    						buf.position(end-start);
    						sc.read(buf);
    						for (int i= 0; i<buf.position(); i++){
    							byte b = buf.get(i);
    							if (b == 0){
    								ByteBuffer dup = buf.duplicate();
    								dup.position(start);
    								dup.limit(i);
    								String str = decoder.decode(dup).toString();
    								Log.v("TC",str);
    								try{
        								final JSONObject json = new JSONObject(str);
        	    						handler.post(new Runnable() {
        	    							@Override
        	    							public void run() {
        	    								executeCommand(json);
        	    							}
        	    						});
    								}
    								catch(Exception e) {
    				    				e.printStackTrace();
    				    			}
    	    						start = i+1;
    							}
    						}
    						end=buf.position();
    						buf.position(start);
    					}

    				}
				}catch (Exception e) {
    				handler.post(new Runnable() {
    					@Override
    					public void run() {
    						//serverStatus.setText("Oops. Connection interrupted. Please reconnect your phones.");
    					}
    				});
    				e.printStackTrace();
    			}
    			socket.close();
    			//Log.d("ClientActivity", "C: Closed.");
    		} catch (Exception e) {
    			//Log.e("ClientActivity", "C: Error", e);
    			//connected = false;
    		}
    	}
    }
}
