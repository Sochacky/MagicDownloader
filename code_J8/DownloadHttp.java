package md;

import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadHttp extends Download{
	private String filename;
	
	DownloadHttp(URL url){
		super(url);
	}
	
	@Override
	public void run() {
		RandomAccessFile file = null;
	    InputStream stream = null;

	    try {
	    	HttpURLConnection connection = (HttpURLConnection) url.openConnection();

	    	connection.setRequestProperty("Range",
	        "bytes=" + downloaded + "-");

	    	connection.connect();

	    	if (connection.getResponseCode() / 100 != 2) {
	    		error();
	    	}

	    	int contentLength = connection.getContentLength();
	    	if (contentLength < 1) {
	    		error();
	    	}

	    	if (size == -1) {
	    		size = contentLength;
	    		stateChanged();
	    	}

	    	filename = url.getFile();
	    	file = new RandomAccessFile(filename.substring(filename.lastIndexOf('/') + 1), "rw");
	    	file.seek(downloaded);

	    	int read =-1;
	    	byte buffer[] = new byte[4096];
	    	stream = connection.getInputStream();
	    	while (status == DOWNLOADING) {
	    		if (size - downloaded > 4096) {
	    			buffer = new byte[4096];
	    		} else {
	    			buffer = new byte[size - downloaded];
	    		}
	    		read = stream.read(buffer);
	    		if (read == -1)
	    			break;

	    		file.write(buffer, 0, read);
	    		downloaded += read;
	    		stateChanged();
	    	}

	    	if (status == DOWNLOADING) {
	    		status = COMPLETE;
	    		stateChanged();
	    	}
	    } catch (Exception e) {
	    	error();
	    } finally {
	    	if (file != null) {
	    		try {
	    			file.close();
	    		} catch (Exception e) {}
	    	}
	    	if (stream != null) {
	    		try {
	    			stream.close();
	    		} catch (Exception e) {}
	    	}
	    }	    
	}
}
