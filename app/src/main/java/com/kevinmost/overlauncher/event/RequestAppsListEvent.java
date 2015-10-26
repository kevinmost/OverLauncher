package com.kevinmost.overlauncher.event;

public class RequestAppsListEvent {
  public final boolean shouldRefreshFirst;

  public RequestAppsListEvent() {
    this(false);
  }
  
  public RequestAppsListEvent(boolean shouldRefreshFirst) {
    this.shouldRefreshFirst = shouldRefreshFirst;
  }
}
