package md;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTP;

public class DownloadFtp extends Download {
	public DownloadFtp(URL url) {
		super(url);
	}

	@Override
	public void run() {
		int adressEnd = getUrl().indexOf('/', 8);
		int fileNameBeginning = getUrl().lastIndexOf('/')+1;
		int port = 21;
		String server = getUrl().substring(6, adressEnd); 
		String filepath = getUrl().substring(adressEnd); 
		String filename = getUrl().substring(fileNameBeginning);
        String user = "anonymous";
        String pass = "anonymous";
 
        FTPClient ftpClient = new FTPClient();
        try {
 
            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            
            
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(new File(filename)));
            InputStream inputStream = ftpClient.retrieveFileStream(filepath);
            
            long vol = ftpClient.mlistFile(filepath).getSize();
	    	if (vol < 1) {
	    		error();
	    	}

	    	if (size == -1) {
	    		size = (int) vol;
	    		stateChanged();
	    	}
	    	
	    	int bytesRead = -1;
	    	byte buffer[] = new byte[4096];
	    	
            while ((bytesRead = inputStream.read(buffer)) != -1 && status == DOWNLOADING) {
	    		outputStream.write(buffer, 0, bytesRead);
	    		downloaded += bytesRead;
	    		stateChanged();
	    	}
            
            if (ftpClient.completePendingCommand()) {
	    		status = COMPLETE;
	    		stateChanged();
	    	}
            outputStream.close();
            inputStream.close();
        } catch (IOException ex) {
            error();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
	}
}
