package com.kevinmost.overlauncher.util;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import com.kevinmost.overlauncher.BuildConfig;
import com.kevinmost.overlauncher.app.App;
import com.kevinmost.overlauncher.dagger.AppComponent;
import com.kevinmost.overlauncher.dagger.DaggerAppComponent;
import com.kevinmost.overlauncher.event.AppsCacheRequestUpdateEvent;
import com.kevinmost.overlauncher.event.AppsCacheUpdatedEvent;
import com.kevinmost.overlauncher.model.InstalledApp;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppsCache {
  private final Bus bus;
  private final PackageUtil packageUtil;
  private final App app;

  private final List<InstalledApp> installedApps = new ArrayList<>();

  /**
   * @return an instance of AppsCache, with any installed apps that have been persisted to disk
   * loaded.
   */
  @NonNull
  public static AppsCache get() {
    AppsCache appsCache;
    try {
      final FileInputStream fis = App.provideComponent().provideApp().openFileInput(CACHE_FILENAME);
      final ObjectInputStream is = new ObjectInputStream(fis);
      final List<InstalledApp> cached = (List<InstalledApp>) is.readObject();
      is.close();
      fis.close();
      appsCache = get(cached);
    } catch (IOException | ClassNotFoundException e) {
      Log.e("OverLauncher", e.getMessage(), e);
      appsCache = get(null);
    }
    appsCache.bus.post(new AppsCacheRequestUpdateEvent());
    return appsCache;
  }

  @NonNull
  private static AppsCache get(@Nullable List<InstalledApp> cachedApps) {
    final AppComponent appComponent = App.provideComponent();
    final AppsCache appsCache = new AppsCache(appComponent.provideBus(),
        appComponent.providePackageUtil(),
        appComponent.provideApp());
    if (cachedApps != null) {
      appsCache.installedApps.addAll(cachedApps);
    }
    return appsCache;
  }

  private AppsCache(Bus bus, PackageUtil packageUtil, App app) {
    this.bus = bus;
    this.packageUtil = packageUtil;
    this.app = app;
    bus.register(this);
  }

  @NonNull
  public List<InstalledApp> getInstalledApps() {
    return installedApps;
  }

  /**
   * When a {@link AppsCacheRequestUpdateEvent} is fired, we will refresh our local cache, and then
   * fire a {@link AppsCacheUpdatedEvent} when it is completed.
   */
  @Subscribe
  public void onAppsCacheUpdateRequested(AppsCacheRequestUpdateEvent event) {
    refreshInstalledAppsCache();
  }

  private void refreshInstalledAppsCache() {
    if (BuildConfig.DEBUG) {
      Toast.makeText(app, "Apps updating... ", Toast.LENGTH_SHORT).show();
    }
    installedApps.clear();
    installedApps.addAll(packageUtil.getInstalledPackages());
    Collections.sort(installedApps, new Comparator<InstalledApp>() {
      @Override
      public int compare(InstalledApp lhs, InstalledApp rhs) {
        return lhs.label.toString().toLowerCase().compareTo(rhs.label.toString().toLowerCase());
      }
    });
    new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void... params) {
        final InstalledApp[] installedAppsArray = new InstalledApp[installedApps.size()];
        installedApps.toArray(installedAppsArray);
//        saveToDisk();
        return null;
      }
    }.execute();
    bus.post(new AppsCacheUpdatedEvent());
  }

  private static final String CACHE_FILENAME = "AppsCacheCachedAppsList";

  // TODO(Kevin) XXX: This doesn't work right now because we are trying to save a non-serializable object.
//  private void saveToDisk() {
//    try {
//      final FileOutputStream fos = app.openFileOutput(CACHE_FILENAME, Context.MODE_PRIVATE);
//      final ObjectOutputStream os = new ObjectOutputStream(fos);
//      os.writeObject(installedApps);
//      os.close();
//      fos.close();
//    } catch (java.io.IOException e) {
//      Log.e("OverLauncher", e.getMessage(), e);
//    }
//  }
}