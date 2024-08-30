// Generated by view binder compiler. Do not edit!
package suzdalenko.photolapse.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;
import suzdalenko.photolapse.R;

public final class ActivityMainBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final Button btnAutoCapture;

  @NonNull
  public final Button btnCamara;

  @NonNull
  public final Button btnGuardar;

  @NonNull
  public final Button btnTakePhoto;

  @NonNull
  public final EditText etEmail;

  @NonNull
  public final Guideline guideline;

  @NonNull
  public final ConstraintLayout main;

  @NonNull
  public final TimePicker timePicker;

  @NonNull
  public final TextView tvAutoImage;

  private ActivityMainBinding(@NonNull ConstraintLayout rootView, @NonNull Button btnAutoCapture,
      @NonNull Button btnCamara, @NonNull Button btnGuardar, @NonNull Button btnTakePhoto,
      @NonNull EditText etEmail, @NonNull Guideline guideline, @NonNull ConstraintLayout main,
      @NonNull TimePicker timePicker, @NonNull TextView tvAutoImage) {
    this.rootView = rootView;
    this.btnAutoCapture = btnAutoCapture;
    this.btnCamara = btnCamara;
    this.btnGuardar = btnGuardar;
    this.btnTakePhoto = btnTakePhoto;
    this.etEmail = etEmail;
    this.guideline = guideline;
    this.main = main;
    this.timePicker = timePicker;
    this.tvAutoImage = tvAutoImage;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityMainBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityMainBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_main, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityMainBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.btnAutoCapture;
      Button btnAutoCapture = ViewBindings.findChildViewById(rootView, id);
      if (btnAutoCapture == null) {
        break missingId;
      }

      id = R.id.btnCamara;
      Button btnCamara = ViewBindings.findChildViewById(rootView, id);
      if (btnCamara == null) {
        break missingId;
      }

      id = R.id.btnGuardar;
      Button btnGuardar = ViewBindings.findChildViewById(rootView, id);
      if (btnGuardar == null) {
        break missingId;
      }

      id = R.id.btnTakePhoto;
      Button btnTakePhoto = ViewBindings.findChildViewById(rootView, id);
      if (btnTakePhoto == null) {
        break missingId;
      }

      id = R.id.etEmail;
      EditText etEmail = ViewBindings.findChildViewById(rootView, id);
      if (etEmail == null) {
        break missingId;
      }

      id = R.id.guideline;
      Guideline guideline = ViewBindings.findChildViewById(rootView, id);
      if (guideline == null) {
        break missingId;
      }

      ConstraintLayout main = (ConstraintLayout) rootView;

      id = R.id.timePicker;
      TimePicker timePicker = ViewBindings.findChildViewById(rootView, id);
      if (timePicker == null) {
        break missingId;
      }

      id = R.id.tvAutoImage;
      TextView tvAutoImage = ViewBindings.findChildViewById(rootView, id);
      if (tvAutoImage == null) {
        break missingId;
      }

      return new ActivityMainBinding((ConstraintLayout) rootView, btnAutoCapture, btnCamara,
          btnGuardar, btnTakePhoto, etEmail, guideline, main, timePicker, tvAutoImage);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
