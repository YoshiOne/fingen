package com.yoshione.fingen;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.yoshione.fingen.adapter.AdapterSms;
import com.yoshione.fingen.dao.SmsDAO;
import com.yoshione.fingen.interfaces.IAbstractModel;
import com.yoshione.fingen.model.Events;
import com.yoshione.fingen.model.Sms;
import com.yoshione.fingen.model.Transaction;
import com.yoshione.fingen.utils.PrefUtils;
import com.yoshione.fingen.utils.RequestCodes;
import com.yoshione.fingen.widgets.ContextMenuRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * A placeholder fragment containing a simple view.
 */
public class FragmentSmsList extends Fragment {
    @BindView(R.id.recycler_view)  ContextMenuRecyclerView mRecyclerView;
    private AdapterSms mAdapter;
//    private List<Sms> mSmsList;

    public FragmentSmsList() {
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sms_list, container, false);
        ButterKnife.bind(this,view);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);

        SmsDAO mSmsDAO = SmsDAO.getInstance(getActivity());

        //adapter
        List<Sms> smses;
        try {
            smses = mSmsDAO.getAllSmss();
        } catch (Exception e) {
            smses = new ArrayList<>();
        }
        mAdapter = new AdapterSms(smses,getActivity());
        mAdapter.setSmsEventsListener(new AdapterSms.SmsEventsListener() {
            @Override
            public void OnSmsClick(View view1) {
                int position = mRecyclerView.getChildAdapterPosition(view1);
                Intent intent = new Intent(FragmentSmsList.this.getActivity(), ActivityEditTransaction.class);
                SmsDAO smsDAO = SmsDAO.getInstance(FragmentSmsList.this.getActivity());
                Sms sms;
                try {
                    sms = smsDAO.getAllSmss().get(position);
                } catch (Exception e) {
                    return;
                }
                intent.putExtra("sms", sms);
                intent.putExtra("transaction", new Transaction(PrefUtils.getDefDepID(getActivity())));
                FragmentSmsList.this.startActivityForResult(intent, RequestCodes.REQUEST_CODE_EDIT_TRANSACTION);
            }
        });


        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        registerForContextMenu(mRecyclerView);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.REQUEST_CODE_EDIT_TRANSACTION & resultCode == Activity.RESULT_OK) {
            int smsListSize;
            try {
                smsListSize = SmsDAO.getInstance(getActivity()).getAllSmss().size();
            } catch (Exception e) {
                smsListSize = 0;
            }
            if (smsListSize == 0) {
                getActivity().finish();
            } else {
                fullUpdate();
            }
        } else {
            fullUpdate();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        // Inflate Menu from xml resource
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.context_menu_sms_list, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ContextMenuRecyclerView.RecyclerContextMenuInfo info = (ContextMenuRecyclerView.RecyclerContextMenuInfo) item.getMenuInfo();
        SmsDAO smsDAO = SmsDAO.getInstance(getActivity());
        switch (item.getItemId()){
            case R.id.action_create_transaction:{
                Intent intent = new Intent(getActivity(), ActivityEditTransaction.class);
                intent.putExtra("sms", smsDAO.getSmsByID(info.id));
                startActivityForResult(intent, RequestCodes.REQUEST_CODE_EDIT_TRANSACTION);
                break;
            }
            case R.id.action_delete:{
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.ttl_confirm_action);
                builder.setMessage(R.string.ttl_delete_sms_confirmation);
                List<Sms> smses = new ArrayList<>();
                smses.add(smsDAO.getSmsByID(info.id));

                OnDeleteSmsDialogOkClickListener clickListener = new OnDeleteSmsDialogOkClickListener(smses);
                builder.setPositiveButton("OK", clickListener);
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
                break;
            }
            case R.id.action_delete_all:{
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.ttl_confirm_action);
                builder.setMessage(R.string.ttl_delete_all_sms_confirmation);
                List<Sms> smses;
                SmsDAO smsDAO1 = SmsDAO.getInstance(getActivity());
                try {
                    smses = smsDAO1.getAllSmss();
                } catch (Exception e) {
                    smses = new ArrayList<>();
                }

                OnDeleteSmsDialogOkClickListener clickListener = new OnDeleteSmsDialogOkClickListener(smses);
                // Set up the buttons
                builder.setPositiveButton("OK", clickListener);
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
                break;
            }
        }
        return true;
    }

    private class OnDeleteSmsDialogOkClickListener implements DialogInterface.OnClickListener{
        private final List<Sms> mSmsList;

        OnDeleteSmsDialogOkClickListener(List<Sms> smsList) {
            this.mSmsList = smsList;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            SmsDAO smsDAO = SmsDAO.getInstance(getActivity());

            for (Sms sms : mSmsList) {
                smsDAO.deleteModel(sms, true, getActivity());
            }

        }
    }

     @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
     public void onEvent(Events.EventOnModelChanged event) {
         if (event.getModelType() == IAbstractModel.MODEL_TYPE_SMS) {
             fullUpdate();
         }
    }

    private void fullUpdate(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        SmsDAO smsDAO = SmsDAO.getInstance(FragmentSmsList.this.getActivity());
                        List<Sms> smsList;
                        try {
                            smsList = smsDAO.getAllSmss();
                        } catch (Exception e) {
                            smsList = new ArrayList<>();
                        }
                        mAdapter.setSmsList(smsList);
                        mAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();

    }
}
