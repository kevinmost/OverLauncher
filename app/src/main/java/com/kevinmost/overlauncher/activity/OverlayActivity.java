package com.kevinmost.overlauncher.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import com.kevinmost.overlauncher.R;
import com.kevinmost.overlauncher.adapter.InstalledAppsAdapter;
import com.kevinmost.overlauncher.app.App;
import com.kevinmost.overlauncher.dagger.AppComponent;
import com.kevinmost.overlauncher.event.FilterChangedEvent;
import com.kevinmost.overlauncher.util.ViewUtil;
import com.rey.material.app.BottomSheetDialog;
import com.squareup.otto.Bus;

public class OverlayActivity extends AppCompatActivity {

  private Bus bus;
  private BottomSheetViewHolder bottomSheetViewHolder;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    final AppComponent component = App.provideComponent();
    final ViewUtil viewUtil = component.provideViewUtil();
    initBottomSheetDialog(viewUtil.getDisplayDimensions().y);
    bus = component.provideBus();
    bus.register(this);
    makeWindowTransparent();
  }

  private void initBottomSheetDialog(int screenHeight) {
    final int animationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
    final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
    bottomSheetDialog.contentView(R.layout.activity_overlay)
        .heightParam((int) (screenHeight * 0.75F))
        .inDuration(animationDuration)
        .outDuration(animationDuration)
        .show();
    bottomSheetViewHolder = new BottomSheetViewHolder(bottomSheetDialog);
  }

  private void makeWindowTransparent() {
    final Window window = getWindow();
    final int flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
    window.setFlags(flags, flags);
    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    final WindowManager.LayoutParams params = window.getAttributes();

    // Lower -> more transparent
    params.alpha = 1F;

    // Higher -> behind window is dimmer
    params.dimAmount = 0.75F;
    window.setAttributes(params);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    bus.unregister(this);

    // Try to avoid mem leaks
    ButterKnife.unbind(bottomSheetViewHolder);
    bottomSheetViewHolder = null;
  }

  // TODO(Kevin): Is it a mem leak that we're using a non-static inner class? I suspect this is a Bad Idea.
  // TODO(Kevin): I want to be able to swipe this sheet down like Google Maps; how do they do that?
  class BottomSheetViewHolder {

    @Bind(R.id.appList)
    AbsListView appList;

    public BottomSheetViewHolder(Dialog root)  {
      ButterKnife.bind(this, root);
      root.setOnDismissListener(new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
          OverlayActivity.this.finish();
        }
      });
      appList.setAdapter(new InstalledAppsAdapter());
    }

    @OnTextChanged(R.id.filterInput)
    void onFilterTextChanged(CharSequence text) {
      bus.post(new FilterChangedEvent(text.toString()));
    }
  }
}
