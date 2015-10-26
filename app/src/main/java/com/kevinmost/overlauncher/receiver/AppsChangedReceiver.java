package com.kevinmost.overlauncher.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.kevinmost.overlauncher.app.App;
import com.kevinmost.overlauncher.event.RequestAppsListEvent;
import com.squareup.otto.Bus;

public class AppsChangedReceiver extends BroadcastReceiver {

  private final Bus bus;

  public AppsChangedReceiver() {
    bus = App.provideComponent().provideBus();
    bus.register(this);
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    bus.post(new RequestAppsListEvent(true));
  }
}
