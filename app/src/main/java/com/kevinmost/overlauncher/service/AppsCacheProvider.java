package com.kevinmost.overlauncher.service;

import android.app.Service;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import com.kevinmost.overlauncher.app.App;
import com.kevinmost.overlauncher.dagger.AppComponent;
import com.kevinmost.overlauncher.event.AppsListEvent;
import com.kevinmost.overlauncher.event.RequestAppsListEvent;
import com.kevinmost.overlauncher.model.InstalledApp;
import com.kevinmost.overlauncher.util.PackageUtil;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A long-lived Service that provides a list of apps that are installed on this device.
 * Since this list takes a while to retrieve (about 4 seconds on my personal device with around
 * 200 apps), it should not be refreshed unless necessary.
 *
 * To retrieve the list: Send a {@link RequestAppsListEvent} through the Event Bus in Dagger, and
 * listen for a {@link AppsListEvent} on the Bus.
 *
 * To refresh the list: Send a {@link RequestAppsListEvent} through the Event Bus in Dagger, but
 * provide a {@code true} value to the constructor boolean parameter. The
 * {@link AppsListEvent} will be fired back when the refresh has finished.
 */
public class AppsCacheProvider extends Service {
  private List<InstalledApp> cache = new ArrayList<>();

  private Bus bus;
  private PackageUtil packageUtil;

  public static void start(ContextWrapper context) {
    context.startService(new Intent(context, AppsCacheProvider.class));
  }

  @Override
  public void onCreate() {
    super.onCreate();
    final AppComponent appComponent = App.provideComponent();
    packageUtil = appComponent.providePackageUtil();
    bus = appComponent.provideBus();
    bus.register(this);
    Log.e("Overlauncher", "Service is up!");
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Subscribe
  public void onAppListRequested(RequestAppsListEvent event) {
    if (!event.shouldRefreshFirst && !cache.isEmpty()) {
      bus.post(new AppsListEvent(cache, false));
      return;
    }
    new AsyncTask<Void, Void, Void>() {
      @Override
      protected Void doInBackground(Void... params) {
        cache = packageUtil.getInstalledPackages();
        Collections.sort(cache, new Comparator<InstalledApp>() {
          @Override
          public int compare(InstalledApp lhs, InstalledApp rhs) {
            return lhs.label.toString().toLowerCase().compareTo(rhs.label.toString().toLowerCase());
          }
        });
        return null;
      }

      @Override
      protected void onPostExecute(Void aVoid) {
        bus.post(new AppsListEvent(cache, true));
      }
    }.execute();
  }

  @Override
  public void onDestroy() {
    bus.unregister(this);
    super.onDestroy();
    start(this);
  }
}
