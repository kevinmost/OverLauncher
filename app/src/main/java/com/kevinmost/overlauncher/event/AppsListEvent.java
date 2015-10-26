package com.kevinmost.overlauncher.event;

import com.kevinmost.overlauncher.model.InstalledApp;

import java.util.List;

public class AppsListEvent {
  public final List<InstalledApp> apps;
  public final boolean hadToRefreshFirst;

  public AppsListEvent(List<InstalledApp> apps, boolean hadToRefreshFirst) {
    this.apps = apps;
    this.hadToRefreshFirst = hadToRefreshFirst;
  }
}
