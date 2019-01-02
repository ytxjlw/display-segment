package com.zkzy_doctor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.zkzy_doctor.widget.ErrorDialog;
import com.zkzy_doctor.widget.MyProgressDialog;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * 基础activity
 */
public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    //错误弹窗
    protected ErrorDialog mErrorDialog;
    protected Activity mContext;
    //第一次按返回键时间
    private long firstTime;
    //是否显示loading框
    boolean isShow = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        BaseApplication.getInstance().addAct(this);
        initData(savedInstanceState);
    }


    @Override
    public void onContentChanged() {
        super.onContentChanged();
        findViews();
        initViews();
    }

    protected abstract void initData(@Nullable Bundle savedInstanceState);

    protected abstract void findViews();

    protected abstract void initViews();


    /**
     * 显示进度
     */
    public void showProgress() {
        if (isShow)
            MyProgressDialog.showDialog(mContext);
    }

    /**
     * 显示进度
     */
    public void showProgress(String tip) {
        if (isShow)
            MyProgressDialog.showDialog(mContext,tip);
    }

    public void dismissProgress() {
        MyProgressDialog.dismiss();
    }

    @Override
    public void onDestroy() {
        if (mErrorDialog != null && mErrorDialog.isShowing()) {
            mErrorDialog.dismiss();
        }
        super.onDestroy();
    }

    /**
     * 弹出Toast
     *
     * @param msg
     */
    protected void showToast(String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    public <T> Observable.Transformer<T, T> loadingManager() {
        return observable -> observable
                .doOnSubscribe(this::showProgress)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe(this::dismissProgress)
                .doOnCompleted(this::dismissProgress)
                .doOnError(
                        throwable -> {
                            Toast.makeText(mContext, throwable.toString(), Toast.LENGTH_SHORT).show();
                            Log.e("-------------throwable", throwable.toString());
                            dismissProgress();
                        });
    }


    public void openActivity(Class c, boolean isFnish) {
        Intent i = new Intent(this, c);
        startActivity(i);
        if (isFnish)
            finish();
    }

    public void openActivity(Class c, boolean isFnish, Intent intent) {
        intent.setClass(this, c);
        startActivity(intent);
        if (isFnish)
            finish();
    }

    @Override
    public void onClick(View v) {

    }

    /**
     * 连续快速点2次返回键退出应用
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN && (this instanceof MainActivity)) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                firstTime = secondTime;
                return true;
            } else {
                BaseApplication.getInstance().finishAllActivity();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
