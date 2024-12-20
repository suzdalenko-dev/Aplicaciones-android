// Generated by view binder compiler. Do not edit!
package suzdalenko.photolapse.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.camera.view.PreviewView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;
import suzdalenko.photolapse.R;

public final class ActivityCameraBinding implements ViewBinding {
  @NonNull
  private final RelativeLayout rootView;

  @NonNull
  public final Button captureButton;

  @NonNull
  public final TextView photosCreated;

  @NonNull
  public final TextView secondsLeft;

  @NonNull
  public final SwitchCompat switchCompat;

  @NonNull
  public final SwitchCompat switchImageVideo;

  @NonNull
  public final SwitchCompat switchSound;

  @NonNull
  public final TextView uploadedPhotos;

  @NonNull
  public final PreviewView viewFinder;

  private ActivityCameraBinding(@NonNull RelativeLayout rootView, @NonNull Button captureButton,
      @NonNull TextView photosCreated, @NonNull TextView secondsLeft,
      @NonNull SwitchCompat switchCompat, @NonNull SwitchCompat switchImageVideo,
      @NonNull SwitchCompat switchSound, @NonNull TextView uploadedPhotos,
      @NonNull PreviewView viewFinder) {
    this.rootView = rootView;
    this.captureButton = captureButton;
    this.photosCreated = photosCreated;
    this.secondsLeft = secondsLeft;
    this.switchCompat = switchCompat;
    this.switchImageVideo = switchImageVideo;
    this.switchSound = switchSound;
    this.uploadedPhotos = uploadedPhotos;
    this.viewFinder = viewFinder;
  }

  @Override
  @NonNull
  public RelativeLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityCameraBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityCameraBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_camera, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityCameraBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.captureButton;
      Button captureButton = ViewBindings.findChildViewById(rootView, id);
      if (captureButton == null) {
        break missingId;
      }

      id = R.id.photos_created;
      TextView photosCreated = ViewBindings.findChildViewById(rootView, id);
      if (photosCreated == null) {
        break missingId;
      }

      id = R.id.seconds_left;
      TextView secondsLeft = ViewBindings.findChildViewById(rootView, id);
      if (secondsLeft == null) {
        break missingId;
      }

      id = R.id.switchCompat;
      SwitchCompat switchCompat = ViewBindings.findChildViewById(rootView, id);
      if (switchCompat == null) {
        break missingId;
      }

      id = R.id.switchImageVideo;
      SwitchCompat switchImageVideo = ViewBindings.findChildViewById(rootView, id);
      if (switchImageVideo == null) {
        break missingId;
      }

      id = R.id.switchSound;
      SwitchCompat switchSound = ViewBindings.findChildViewById(rootView, id);
      if (switchSound == null) {
        break missingId;
      }

      id = R.id.uploaded_photos;
      TextView uploadedPhotos = ViewBindings.findChildViewById(rootView, id);
      if (uploadedPhotos == null) {
        break missingId;
      }

      id = R.id.viewFinder;
      PreviewView viewFinder = ViewBindings.findChildViewById(rootView, id);
      if (viewFinder == null) {
        break missingId;
      }

      return new ActivityCameraBinding((RelativeLayout) rootView, captureButton, photosCreated,
          secondsLeft, switchCompat, switchImageVideo, switchSound, uploadedPhotos, viewFinder);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
