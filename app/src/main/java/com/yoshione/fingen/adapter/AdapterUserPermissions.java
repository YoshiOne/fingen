package com.yoshione.fingen.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.yoshione.fingen.R;
import com.yoshione.fingen.adapter.viewholders.UserPermissionViewHolder;
import com.yoshione.fingen.model.UserPermission;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by slv on 15.03.2017.
 * AdapterUserPermissions
 */

public class AdapterUserPermissions extends RecyclerView.Adapter {

    private List<UserPermission> mList;
    private OnUserPermissionChangeListener mOnUserPermissionChangeListener;

    public AdapterUserPermissions() {
        setHasStableIds(true);
        mList = new ArrayList<>();
    }

    public void setOnUserPermissionChangeListener(OnUserPermissionChangeListener onUserPermissionChangeListener) {
        mOnUserPermissionChangeListener = onUserPermissionChangeListener;
    }

    public void setList(List<UserPermission> list) {
        mList = list;
    }

    public List<UserPermission> getList() {
        return mList;
    }

    public int getCount() {
        return mList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item_user_permission, parent, false);

        vh = new UserPermissionViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        UserPermissionViewHolder vh = (UserPermissionViewHolder) holder;
        final UserPermission userPermission = mList.get(position);
        vh.setEmail(userPermission.getEmail());
        vh.setRead(userPermission.isRead());
        vh.setWrite(userPermission.isWrite());
        vh.setOnReadChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                userPermission.setRead(b);
                if (mOnUserPermissionChangeListener != null) {
                    mOnUserPermissionChangeListener.onChange(userPermission);
                }
            }
        });
        vh.setOnWriteChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                userPermission.setWrite(b);
                if (mOnUserPermissionChangeListener != null) {
                    mOnUserPermissionChangeListener.onChange(userPermission);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public long getItemId(int position) {
        return mList.get(position).getEmail().toLowerCase().hashCode();
    }

    public interface OnUserPermissionChangeListener {
        void onChange(UserPermission newUserPermission);
    }
}
