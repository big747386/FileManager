package com.njupt.filemanager.adapter;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.njupt.filemanager.util.FileUtil;
import com.njupt.filemanager.R;
import com.njupt.filemanager.adapter.base.RecyclerViewAdapter;
import com.njupt.filemanager.adapter.base.RecyclerViewHolder;
import com.njupt.filemanager.bean.FileBean;
import com.njupt.filemanager.bean.FileType;

import java.io.File;

import static com.njupt.filemanager.activity.MainActivity.isSelectMode;

// 文件holder
public class FileHolder extends RecyclerViewHolder<FileHolder> {

    ImageView fileIcon;
    TextView fileName;
    TextView fileChildCount;
    TextView fileSize;
    ImageView dir_enter_image;
    public CheckBox checkBox;

    public FileHolder(View view) {
        super(view);
        fileIcon = (ImageView) view.findViewById(R.id.fileIcon);
        fileName = (TextView) view.findViewById(R.id.fileName);
        fileChildCount = (TextView) view.findViewById(R.id.fileChildCount);
        fileSize = (TextView) view.findViewById(R.id.fileSize);
        dir_enter_image = (ImageView) view.findViewById(R.id.dir_enter_image);
        checkBox = (CheckBox) view.findViewById(R.id.checkBox);
    }

    @Override
    public void onBindViewHolder(final FileHolder fileHolder, RecyclerViewAdapter adapter, int position) {
        final FileBean fileBean = (FileBean) adapter.getItem(position);
        fileHolder.fileName.setText(fileBean.getName());

        FileType fileType = fileBean.getFileType();
        if (fileType == FileType.directory) {
            fileHolder.fileChildCount.setVisibility(View.VISIBLE);
            fileHolder.fileChildCount.setText(fileBean.getChildCount() + "项");

            fileHolder.fileSize.setVisibility(View.GONE);
            fileHolder.dir_enter_image.setVisibility(View.VISIBLE);

        } else {
            fileHolder.fileChildCount.setVisibility(View.GONE);

            fileHolder.fileSize.setVisibility(View.VISIBLE);
            fileHolder.fileSize.setText(FileUtil.sizeToChange(fileBean.getSize()));

            fileHolder.dir_enter_image.setVisibility(View.GONE);
        }

        //设置图标
        if (fileType == FileType.directory) {
            fileHolder.fileIcon.setImageResource(R.mipmap.file_icon_dir);
        } else if (fileType == FileType.music) {
            fileHolder.fileIcon.setImageResource(R.mipmap.file_icon_music);
        } else if (fileType == FileType.video) {
            fileHolder.fileIcon.setImageResource(R.mipmap.file_icon_video);
        } else if (fileType == FileType.txt) {
            fileHolder.fileIcon.setImageResource(R.mipmap.file_icon_txt);
        } else if (fileType == FileType.zip) {
            fileHolder.fileIcon.setImageResource(R.mipmap.file_icon_zip);
        } else if (fileType == FileType.image) {
            Glide.with(fileHolder.itemView.getContext()).load(new File(fileBean.getPath())).into(fileHolder.fileIcon);
        } else if (fileType == FileType.apk) {
            fileHolder.fileIcon.setImageResource(R.mipmap.file_icon_apk);
        } else {
            fileHolder.fileIcon.setImageResource(R.mipmap.file_icon_other);
        }

        checkBox.setVisibility(isSelectMode ? View.VISIBLE : View.GONE);
    }

}
