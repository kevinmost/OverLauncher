package com.kevinmost.overlauncher.dagger;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.WindowManager;
import com.kevinmost.overlauncher.app.App;
import com.kevinmost.overlauncher.util.PackageUtil;
import com.kevinmost.overlauncher.util.ViewUtil;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
  App provideApp();
  LayoutInflater provideLayoutInflater();
  PackageManager providePackageManager();
  WindowManager provideWindowManager();
  SharedPreferences provideSharedPreferences();
  OkHttpClient provideOkHttpClient();
  Picasso providePicasso();
  Bus provideBus();

  PackageUtil providePackageUtil();
  ViewUtil provideViewUtil();
}

