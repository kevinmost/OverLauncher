package com.kevinmost.overlauncher.util;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;
import android.widget.Toast;
import com.kevinmost.overlauncher.BuildConfig;
import com.kevinmost.overlauncher.app.App;
import com.kevinmost.overlauncher.model.InstalledApp;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class PackageUtil {

  private final PackageManager packageManager;

  @Inject
  PackageUtil(PackageManager packageManager) {
    this.packageManager = packageManager;
  }

  /**
   * Given an InstalledApp object (which can be obtained from {@link this#getInstalledPackages()},
   * returns an Intent that can be fired to open that object.
   */
  public Intent getStartAppIntent(InstalledApp app) {
    final ActivityInfo activityInfo = app.info.activityInfo;
    final String packageName = activityInfo.packageName;
    final String name = activityInfo.name;
    final ComponentName componentName = new ComponentName(packageName, name);
    return new Intent(Intent.ACTION_MAIN)
        .addCategory(Intent.CATEGORY_LAUNCHER)
        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        .setComponent(componentName);
  }

  @WorkerThread
  @NonNull
  public List<InstalledApp> getInstalledPackages() {
    final Intent launcherIntent = new Intent(Intent.ACTION_MAIN, null);
    launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
    final List<ResolveInfo> packages = packageManager.queryIntentActivities(launcherIntent, 0);
    final List<InstalledApp> installedApps = new ArrayList<>(packages.size());
    for (ResolveInfo aPackage : packages) {
      installedApps.add(new InstalledApp(aPackage));
    }
    return installedApps;
  }

  public enum FilterMode {
    BASIC {
      @Override
      public boolean shouldRetainApp(String appLabel, String filterString) {
        return appLabel.contains(filterString);
      }
    },
    ONLY_START_OF_STRING {
      @Override
      public boolean shouldRetainApp(String appLabel, String filterString) {
        return appLabel.startsWith(filterString);
      }
    },
    ONLY_START_OF_WORDS {
      @Override
      public boolean shouldRetainApp(String appLabel, String filterString) {
        return appLabel.startsWith(filterString) || appLabel.contains(" " + filterString);
      }
    },
    ;

    public abstract boolean shouldRetainApp(String appLabel, String filterString);
  }
}
