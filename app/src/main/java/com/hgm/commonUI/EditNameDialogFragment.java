package com.hgm.commonUI;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import com.hgm.androidengine.R;

public class EditNameDialogFragment extends DialogFragment
{
  private EditText mEditText;

  public EditNameDialogFragment() {}

  public static EditNameDialogFragment newInstance(String title)
  {
    EditNameDialogFragment fragment = new EditNameDialogFragment();
    Bundle args = new Bundle();
    args.putString("title", title);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
  {
    return inflater.inflate(R.layout.fragment_edit_name, container);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
  {
    super.onViewCreated(view, savedInstanceState);
    //  Get field from view
    mEditText = view.findViewById(R.id.transparent_alert);
    //  Fetch arguments from bundle and set title
    String title = getArguments().getString("title", "Enter Name");
    getDialog().setTitle(title);
    //  Show soft keyboard automatically and request focus and field
    mEditText.requestFocus();
    getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
  }


}
