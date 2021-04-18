package com.njupt.filemanager;

import android.Manifest;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.njupt.filemanager.util.FileUtil;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.njupt.filemanager.adapter.FileHolder;
import com.njupt.filemanager.adapter.FileAdapter;
import com.njupt.filemanager.adapter.TitleAdapter;
import com.njupt.filemanager.adapter.base.RecyclerViewAdapter;
import com.njupt.filemanager.bean.FileBean;
import com.njupt.filemanager.bean.TitlePath;
import com.njupt.filemanager.bean.FileType;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class MainActivity extends AppCompatActivity {
    private RecyclerView title_recycler_view;
    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private List<FileBean> beanList = new ArrayList<>();
    private File rootFile;
    private LinearLayout empty_rel;
    private int PERMISSION_CODE_WRITE_EXTERNAL_STORAGE = 100;
    private String rootPath;
    private TitleAdapter titleAdapter;

    public static boolean isSelectMode = false;
    public static boolean isCopyMode = false;
    private String currentTitlePath;

    private LinearLayout modeBar;
    private LinearLayout modeBarNavigation;
    private LinearLayout modeBarMove;
    private LinearLayout modeBarCopy;
    private LinearLayout modeBarDelete;

    // menu
    private MenuItem searchMenuItem;
    private MenuItem suffixMenuItem;
    private MenuItem sortMenuItem;

    // copy
    private LinearLayout copyBar;
    private LinearLayout copyBarPaste;
    private LinearLayout copyBarCancel;
    private List<File> canList;


    // 1 cut 2 copy
    private int copyOrCut;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //设置Title
        title_recycler_view = (RecyclerView) findViewById(R.id.title_recycler_view);
        title_recycler_view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        titleAdapter = new TitleAdapter(MainActivity.this, new ArrayList<TitlePath>());
        title_recycler_view.setAdapter(titleAdapter);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        fileAdapter = new FileAdapter(this, beanList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(fileAdapter);
        empty_rel = (LinearLayout) findViewById(R.id.empty_rel);

        modeBar = (LinearLayout) findViewById(R.id.mode_bar);
        modeBarCopy = (LinearLayout) findViewById(R.id.mode_bar_copy);
        modeBarDelete = (LinearLayout) findViewById(R.id.mode_bar_delete);
        modeBarMove = (LinearLayout) findViewById(R.id.mode_bar_move);
        modeBarNavigation = (LinearLayout) findViewById(R.id.mode_bar_navigation);

        copyBar = (LinearLayout) findViewById(R.id.copy_bar);
        copyBarPaste = (LinearLayout) findViewById(R.id.copy_bar_paste);
        copyBarCancel = (LinearLayout) findViewById(R.id.copy_bar_cancel);

        fileAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                if (viewHolder instanceof FileHolder) {
                    FileBean file = beanList.get(position);
                    FileType fileType = file.getFileType();
                    if (fileType == FileType.directory) {
                        getFile(file.getPath());
                        refreshTitleState(file.getName(), file.getPath());
                    } else if (fileType == FileType.apk) {
                        //安装app
                        FileUtil.openAppIntent(MainActivity.this, new File(file.getPath()));
                    } else if (fileType == FileType.image) {
                        FileUtil.openImageIntent(MainActivity.this, new File(file.getPath()));
                    } else if (fileType == FileType.txt) {
                        FileUtil.openTextIntent(MainActivity.this, new File(file.getPath()));
                    } else if (fileType == FileType.music) {
                        FileUtil.openMusicIntent(MainActivity.this, new File(file.getPath()));
                    } else if (fileType == FileType.video) {
                        FileUtil.openVideoIntent(MainActivity.this, new File(file.getPath()));
                    } else {
                        FileUtil.openApplicationIntent(MainActivity.this, new File(file.getPath()));
                    }
                }
            }
        });

        fileAdapter.setOnItemLongClickListener(new RecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                if (viewHolder instanceof FileHolder) {
                    FileBean fileBean = (FileBean) fileAdapter.getItem(position);
                    FileType fileType = fileBean.getFileType();
                    if (!isSelectMode) {
                        openSelectMode();
                    }
                }
                return true;
            }
        });

        titleAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                TitlePath titlePath = (TitlePath) titleAdapter.getItem(position);
                getFile(titlePath.getPath());
                currentTitlePath = titlePath.getPath();

                int count = titleAdapter.getItemCount();
                int removeCount = count - position - 1;
                for (int i = 0; i < removeCount; i++) {
                    titleAdapter.removeLast();
                }
            }
        });

        rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();

        refreshTitleState("内部存储设备", rootPath);

        // 先判断是否有权限。
        if (AndPermission.hasPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // 有权限，直接do anything.
            getFile(rootPath);
        } else {
            //申请权限。
            AndPermission.with(this)
                    .requestCode(PERMISSION_CODE_WRITE_EXTERNAL_STORAGE)
                    .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .send();
        }

        modeBarNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileAdapter.navigate(MainActivity.this, MainActivity.this);
            }
        });

        modeBarMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCopyMode();
                canList = fileAdapter.performCopy();
                copyOrCut = 2;
            }
        });

        modeBarDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileAdapter.delete(MainActivity.this);
                getFile(currentTitlePath);
            }
        });

        modeBarCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCopyMode();
                canList = fileAdapter.performCopy();
                copyOrCut = 1;
            }
        });

        copyBarPaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (copyOrCut == 2) {
                    fileAdapter.pasteAndDelete(canList, currentTitlePath);
                } else if (copyOrCut == 1) {
                    fileAdapter.paste(canList, currentTitlePath);
                }

                closeCopyMode();
                getFile(currentTitlePath);
            }
        });


        copyBarCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeCopyMode();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        suffixMenuItem = menu.findItem(R.id.menu_suffix);
        searchMenuItem = menu.findItem(R.id.menu_search);
        sortMenuItem = menu.findItem(R.id.menu_sort);
        return super.onCreateOptionsMenu(menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                return true;
            case R.id.menu_sort:
                if (sortMenuItem.getTitle().equals("按大小排序")) {
                    sortMenuItem.setTitle("按时间排序");
                } else {
                    sortMenuItem.setTitle("按大小排序");
                }
                getFile(currentTitlePath);
                return true;
            case R.id.menu_suffix:
                if (suffixMenuItem.getTitle().equals("隐藏后缀")) {
                    suffixMenuItem.setTitle("显示后缀");
                } else {
                    suffixMenuItem.setTitle("隐藏后缀");
                }
                getFile(currentTitlePath);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void getFile(String path) {
        closeSelectMode();
        rootFile = new File(path + File.separator);
        currentTitlePath = rootFile.getPath();
        new MyTask(rootFile).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
    }

    class MyTask extends AsyncTask {
        File file;

        MyTask(File file) {
            this.file = file;
        }

        @Override
        protected Object doInBackground(Object[] params) {
            List<FileBean> fileBeenList = new ArrayList<>();
            if (file.isDirectory()) {
                File[] filesArray = file.listFiles();
                if (filesArray != null) {
                    List<File> fileList = new ArrayList<>();
                    Collections.addAll(fileList, filesArray);  //把数组转化成list
                    Collections.sort(fileList, FileUtil.comparator);  //按照名字排序

                    if (sortMenuItem != null && sortMenuItem.getTitle().equals("按时间排序")) {
                        fileList.sort(((o1, o2) -> (int)o1.length() - (int)o2.length()));
                    }
                    for (File f : fileList) {
                        if (f.isHidden()) continue;

                        FileBean fileBean = new FileBean();
                        fileBean.setName(f.getName());
                        fileBean.setPath(f.getAbsolutePath());
                        fileBean.setFileType(FileUtil.getFileType(f));
                        fileBean.setChildCount(FileUtil.getFileChildCount(f));
                        fileBean.setSize(f.length());
                        fileBean.setHolderType(0);

                        fileBeenList.add(fileBean);

                        FileBean lineBean = new FileBean();
                        lineBean.setHolderType(1);
                        fileBeenList.add(lineBean);

                    }
                }
            }

            if (suffixMenuItem != null && suffixMenuItem.getTitle().equals("显示后缀")) {
                fileBeenList = fileBeenList.stream().map(o -> {
                    if (o == null || o.getName() == null) {
                        return o;
                    }
                    String name = o.getName();
                    int index = name.lastIndexOf('.');
                    if (index != -1) {
                        o.setName(name.substring(0, index));
                    }
                    return o;
                }).collect(Collectors.toList());
            }

            beanList = fileBeenList;
            return fileBeenList;
        }

        @Override
        protected void onPostExecute(Object o) {
            if (beanList.size() > 0) {
                empty_rel.setVisibility(View.GONE);
            } else {
                empty_rel.setVisibility(View.VISIBLE);
            }
            fileAdapter.refresh(beanList);
        }
    }

    void refreshTitleState(String title, String path) {
        TitlePath filePath = new TitlePath();
        filePath.setNameState(title + " > ");
        filePath.setPath(path);
        titleAdapter.addItem(filePath);
        title_recycler_view.smoothScrollToPosition(titleAdapter.getItemCount());
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {

            List<TitlePath> titlePathList = (List<TitlePath>) titleAdapter.getAdapterData();
            if (titlePathList.size() == 1) {
                finish();
            } else {
                titleAdapter.removeItem(titlePathList.size() - 1);
                getFile(titlePathList.get(titlePathList.size() - 1).getPath());
            }

            closeSelectMode();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // 只需要调用这一句，其它的交给AndPermission吧，最后一个参数是PermissionListener。
        AndPermission.onRequestPermissionsResult(requestCode, permissions, grantResults, listener);
    }

    private PermissionListener listener = new PermissionListener() {
        @Override
        public void onSucceed(int requestCode, List<String> grantedPermissions) {
            // 权限申请成功回调。
            if (requestCode == PERMISSION_CODE_WRITE_EXTERNAL_STORAGE) {
                getFile(rootPath);
            }
        }

        @Override
        public void onFailed(int requestCode, List<String> deniedPermissions) {
            // 权限申请失败回调。
            AndPermission.defaultSettingDialog(MainActivity.this, PERMISSION_CODE_WRITE_EXTERNAL_STORAGE)
                    .setTitle("权限申请失败")
                    .setMessage("我们需要的一些权限被您拒绝或者系统发生错误申请失败，请您到设置页面手动授权，否则功能无法正常使用！")
                    .setPositiveButton("好，去设置")
                    .show();
        }
    };

    public void closeSelectMode() {
        isSelectMode = false;
        modeBar.setVisibility(View.GONE);
        fileAdapter.refresh();
    }

    public void openSelectMode() {
        isSelectMode = true;
        modeBar.setVisibility(View.VISIBLE);
        fileAdapter.refresh();
    }

    public void openCopyMode() {
        isCopyMode = true;
        modeBar.setVisibility(View.GONE);
        copyBar.setVisibility(View.VISIBLE);
        fileAdapter.setOnItemLongClickListener(null);
    }

    public void closeCopyMode() {
        isCopyMode = false;
        copyBar.setVisibility(View.GONE);
        fileAdapter.setOnItemLongClickListener(new RecyclerViewAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder viewHolder, int position) {
                if (viewHolder instanceof FileHolder) {
                    FileBean fileBean = (FileBean) fileAdapter.getItem(position);
                    FileType fileType = fileBean.getFileType();
                    if (!isSelectMode) {
                        openSelectMode();
                    }
                }
                return true;
            }
        });
    }
}
