package dm;
import java.io.*;
import java.net.*;
import java.util.*;

public class Download extends Observable implements Runnable{

	// i checked SO for optimal buffer size. 1024 is being suggested
	//along with 8k, but i would test against benchmarks later
	
	private static final int MAX_BUFFER_SIZE = 1024;
	
	//onto the status of the process
	//names:
	public static final String STATUSES[]= {"Downloading","Paused","Complete","Cancelled","Error"};
	//codes:
	public static final int DOWNLOADING = 0;
    public static final int PAUSED = 1;
    public static final int COMPLETE = 2;
    public static final int CANCELLED = 3;
    public static final int ERROR = 4;
    
    private URL  url; // the url to be downloaded
    private int size; // size of download in bytes
    private int downloaded; // number of bytes downloaded
    private int status; // current status of download
     
    // Constructor for Download
    //set the url to calling object's url; intialize the size and abhi tak ke downloaded bytes
    //change the status
    public Download(URL url) {
        this.url = url;
        size = -1;
        downloaded = 0;
        status = DOWNLOADING;
         
        // Begin the download.
        download();// declared later obviously
    }
	//for getting the url of download. just for the string
    public String getUrl() {
        return url.toString();
    }
    // Get this download's size.
    public int getSize() {
        return size;
    }
     
    // Get this download's progress.
    public float getProgress() {
        return ((float) downloaded / size) * 100;
    }
     
    // Get this download's status.
    public int getStatus() {
        return status;
    }
    public void pause() {
    	status=PAUSED;
    	stateChanged();  // note that this is an Observer command
    	
    }
    public void resume() {
    	status=DOWNLOADING;
    	stateChanged();  // note that this is an Observer command
    	download();
    }
    public void cancel() {
    	status=CANCELLED;
    	stateChanged();  // note that this is an Observer command
    	
    }
    private void error() {   //only the object of this instance should set the error status
    	status=ERROR;
    	stateChanged();  // note that this is an Observer command
    	
    }
    private void download() {
        Thread thread = new Thread(this);   //start download on a new thread
        thread.start();
    }
 // Get file name portion of URL.
    private String getFileName(URL url) {
        String fileName = url.getFile();
        return fileName.substring(fileName.lastIndexOf('/') + 1);
    }
    public void run() {
    	RandomAccessFile file = null;
    	InputStream stream = null;
    	try {
    		//opening the connection
    		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
    		connection.setRequestProperty("Range","bytes=" + downloaded + "-");
    		//now establish connection
    		connection.connect();
    		 // Make sure response code is in the 200 range. i.e. all possible successful codes
            if (connection.getResponseCode() / 100 != 2) {
                error();
            }
             
            // Check for valid content length.
            int contentLength = connection.getContentLength();
            if (contentLength < 1) {
                error();
            }
            if (size==-1)
            {
            	size=contentLength;
            	stateChanged();
            }
         // Open file and seek to the end of it.
         // A little tweak to send the txt or jpeg files to the download folder
            String home = System.getProperty("user.home");
            file = new RandomAccessFile(home+"/Downloads/" +getFileName(url), "rw");
            file.seek(downloaded);// initially zero
            stream = connection.getInputStream();
            while(status==DOWNLOADING)
            {
            	byte buffer[];
            	if(size-downloaded > MAX_BUFFER_SIZE)
            	{
            		buffer = new byte[MAX_BUFFER_SIZE];
            	}
            	else
            	{
            		buffer = new byte[size-downloaded];
            	}
            	int read = stream.read(buffer);
            	if(read==-1)
            		break;
            	file.write(buffer,0,read);
            	downloaded+=read;
            	stateChanged();
            }
            if(status==DOWNLOADING)
            {
            	status=COMPLETE;
            	stateChanged();
            }
    	} 
    	catch(Exception e)
    	{
    		error();
    	}
         finally 
         {
        	 //close the damn file. You always miss this part.
        	 if(file!=null)
        	 {
        		 try {
        			 file.close();
        		 }catch(Exception e){}}
        	 if (stream!=null)
        	 {
        		 try {stream.close();}catch(Exception e) {}}
        	 }
        	
         } 	
    	
    	
    
    private void stateChanged() {
        setChanged();
        notifyObservers();}

       
}
