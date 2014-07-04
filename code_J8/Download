package md;

import java.net.*;
import java.util.*;

abstract class Download extends Observable implements Runnable {
  public static final String STATUSES[] = {"Downloading", "Paused", "Complete", "Cancelled", "Error"};
  public static final int DOWNLOADING = 0, PAUSED = 1, COMPLETE = 2, CANCELLED = 3, ERROR = 4;
  URL url; 
  int size; 
  int downloaded;
  int status; 
  
  abstract public void run();

  public Download(URL url) {
    this.url = url;
    size = -1;
    downloaded = 0;
    status = DOWNLOADING;
    download();
  }

  public String getUrl() {
    return url.toString();
  }

  public float getProgress() {
    return ((float) downloaded/size)*100;
  }

  public void pause() {
    status = PAUSED;
    stateChanged();
  }

  public void resume() {
    status = DOWNLOADING;
    stateChanged();
    download();
  }

  public void cancel() {
    status = CANCELLED;
    stateChanged();
  }

  public void error() {
    status = ERROR;
    stateChanged();
  }

  private void download() {
    Thread thread = new Thread(this);
    thread.start();
  }

  void stateChanged() {
    setChanged();
    notifyObservers();
  }
}
