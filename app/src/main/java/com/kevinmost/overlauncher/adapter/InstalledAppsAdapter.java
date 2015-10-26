package com.kevinmost.overlauncher.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.kevinmost.overlauncher.R;
import com.kevinmost.overlauncher.app.App;
import com.kevinmost.overlauncher.dagger.AppComponent;
import com.kevinmost.overlauncher.event.AppsListEvent;
import com.kevinmost.overlauncher.event.FilterChangedEvent;
import com.kevinmost.overlauncher.event.RequestAppsListEvent;
import com.kevinmost.overlauncher.model.InstalledApp;
import com.kevinmost.overlauncher.util.PackageUtil;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class InstalledAppsAdapter extends BaseAdapter {

  private String filter;
  private List<InstalledApp> allAppsCache = new ArrayList<>();
  private List<InstalledApp> shownAppsCache = new ArrayList<>();

  private final LayoutInflater inflater;
  private final App app;

  public InstalledAppsAdapter() {
    final AppComponent component = App.provideComponent();
    inflater = component.provideLayoutInflater();
    app = component.provideApp();
    final Bus bus = component.provideBus();
    bus.register(this);
    bus.post(new RequestAppsListEvent());
  }

  @Subscribe
  public void onFilterTextChangedEvent(FilterChangedEvent event) {
    this.filter = event.newFilter;
    notifyDataSetChanged();
  }

  @Subscribe
  public void onAppsCacheUpdated(AppsListEvent event) {
    allAppsCache = event.apps;
    notifyDataSetChanged();
  }

  @Override
  public int getCount() {
    return shownAppsCache.size();
  }

  @Override
  public InstalledApp getItem(int position) {
    return shownAppsCache.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    final ViewHolder holder;
    if (convertView != null) {
      holder = ((ViewHolder) convertView.getTag());
    } else {
      convertView = inflater.inflate(R.layout.installed_app_grid, parent, false);
      holder = new ViewHolder(convertView);
      convertView.setTag(holder);
    }
    final InstalledApp thisApp = getItem(position);

    holder.icon.setImageDrawable(thisApp.icon);
    holder.label.setText(thisApp.label);

    convertView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // TODO: Should we also finish() the OverlayActivity that's open here?
        app.startActivity(thisApp.startAppIntent);
      }
    });

    return convertView;
  }

  @Override
  public void notifyDataSetChanged() {
    shownAppsCache = filter(allAppsCache, filter, PackageUtil.FilterMode.ONLY_START_OF_WORDS);
    super.notifyDataSetChanged();
  }

  // TODO: Give the user a way to change the filter mode
  @NonNull
  private static List<InstalledApp> filter(@NonNull List<InstalledApp> unfiltered,
                                           @Nullable String filter,
                                           @Nullable PackageUtil.FilterMode filterMode) {
    final List<InstalledApp> filtered = new ArrayList<>();
    filter = (filter == null ? "" : filter.trim().toLowerCase());
    if (filter.isEmpty()) {
      return unfiltered;
    }
    if (filterMode == null) {
      filterMode = PackageUtil.FilterMode.BASIC;
    }
    for (InstalledApp anApp : unfiltered) {
      final String appLabel = anApp.label.toString().toLowerCase();
      if (filterMode.shouldRetainApp(appLabel, filter)) {
        filtered.add(anApp);
      }
    }
    return filtered;
  }


  static class ViewHolder {
    @Bind(R.id.icon)
    ImageView icon;

    @Bind(R.id.label)
    TextView label;

    public ViewHolder(View root) {
      ButterKnife.bind(this, root);
    }
  }
}
