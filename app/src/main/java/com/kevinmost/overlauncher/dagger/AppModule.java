package com.kevinmost.overlauncher.dagger;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.WindowManager;
import com.kevinmost.overlauncher.BuildConfig;
import com.kevinmost.overlauncher.app.App;
import com.kevinmost.overlauncher.util.PostFromAnywhereBus;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Bus;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

@Module
public class AppModule {
  private final App app;

  public AppModule(App app) {
    this.app = app;
  }

  @Provides
  @Singleton
  App provideApplication() {
    return app;
  }

  @Provides
  @Singleton
  LayoutInflater provideLayoutInflater() {
    return LayoutInflater.from(app);
  }

  @Provides
  @Singleton
  PackageManager providePackageManager() {
    return app.getPackageManager();
  }

  @Provides
  @Singleton
  WindowManager provideWindowManager() {
    return getSystemService(Context.WINDOW_SERVICE);
  }

  @Provides
  @Singleton
  SharedPreferences provideSharedPreferences() {
    return PreferenceManager.getDefaultSharedPreferences(app);
  }

  @Provides
  @Singleton
  OkHttpClient provideOkHttpClient() {
    final OkHttpClient okHttpClient = new OkHttpClient();
    okHttpClient.setReadTimeout(10, TimeUnit.SECONDS);
    okHttpClient.setWriteTimeout(10, TimeUnit.SECONDS);
    okHttpClient.setConnectTimeout(10, TimeUnit.SECONDS);
    return okHttpClient;
  }

  @Provides
  @Singleton
  Picasso providePicasso(OkHttpClient okHttp) {
    return new Picasso.Builder(app)
        .downloader(new OkHttpDownloader(okHttp))
        .indicatorsEnabled(BuildConfig.DEBUG)
        .build();
  }

  @Provides
  @Singleton
  Bus provideBus() {
    return new PostFromAnywhereBus();
  }

  private <T> T getSystemService(final String service) {
    //noinspection unchecked
    return (T) app.getSystemService(service);
  }
}
