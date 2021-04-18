package com.njupt.filemanager.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.njupt.filemanager.util.FileUtil;
import com.njupt.filemanager.MainActivity;
import com.njupt.filemanager.R;
import com.njupt.filemanager.adapter.base.RecyclerViewAdapter;
import com.njupt.filemanager.bean.FileBean;
import com.njupt.filemanager.bean.FileType;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.njupt.filemanager.MainActivity.isSelectMode;

public class FileAdapter extends RecyclerViewAdapter {

    private Context context;
    private List<FileBean> list;
    private LayoutInflater mLayoutInflater;
    private Set<FileBean> selectSet = new HashSet<>();

    public FileAdapter(Context context, List<FileBean> list) {
        this.context = context;
        this.list = list;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            view = mLayoutInflater.inflate(R.layout.list_item_file, parent, false);
            return new FileHolder(view);
        } else {
            view = mLayoutInflater.inflate(R.layout.list_item_line, parent, false);
            return new LineHolder(view);
        }
    }

    @Override
    public void onBindViewHolders(final RecyclerView.ViewHolder holder,
                                  final int position) {
        if (holder instanceof FileHolder) {
            FileHolder fileHolder = (FileHolder) holder;
            fileHolder.onBindViewHolder(fileHolder, this, position);
            if (isSelectMode) {
                final FileBean fileBean = list.get(position);
                fileHolder.checkBox.setChecked(selectSet.contains(fileBean));
                fileHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            selectSet.add(fileBean);
                        } else {
                            selectSet.remove(fileBean);
                        }
                    }
                });
            } else {
                fileHolder.checkBox.setOnCheckedChangeListener(null);
            }
        } else if (holder instanceof LineHolder) {
            LineHolder lineHolder = (LineHolder) holder;
            lineHolder.onBindViewHolder(lineHolder, this, position);
        }
    }

    @Override
    public Object getAdapterData() {
        return list;
    }

    @Override
    public Object getItem(int positon) {
        return list.get(positon);
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).getHolderType();
    }

    @Override
    public int getItemCount() {
        if (list != null) {
            return list.size();
        } else {
            return 0;
        }
    }

    public void refresh(List<FileBean> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void refresh() {
        notifyDataSetChanged();
    }

    public void navigate(Context context, MainActivity mainActivity) {
        for (FileBean fileBean : selectSet) {
            FileType fileType = fileBean.getFileType();
            if (fileType != null && fileType != FileType.directory) {
                FileUtil.sendFile(context, new File(fileBean.getPath()));
            }
        }
        mainActivity.closeSelectMode();
        selectSet.clear();
    }

    public void delete(Context context, MainActivity mainActivity) {
        for (FileBean fileBean : selectSet) {
            list.remove(fileBean);
        }
        mainActivity.closeSelectMode();
        selectSet.clear();
    }
}
