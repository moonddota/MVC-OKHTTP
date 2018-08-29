package com.skylin.uav.drawforterrain.dialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.skylin.uav.R;


/**
 * 只有一个按钮的对话框
 */
public class SingleOptDialogFragment extends BaseDialogFragment {
    /**
     * 消息标题
     */
    public static final String DIALOG_TITLE = "dialog_title";
    /**
     *消息内容
     */
    public static final String DIALOG_MSG = "dialog_message";
    /**
     *最下面的单个按钮显示内容
     */
    public static final String DIALOG_BUTTON = "dialog_button";

    public static final String BUTTON_CANCEL = "cancel";

    private TextView tvTitle, tvMsg, tvBtnContent,tv_single_cancel;

    private String title;
    private String msg;
    private String btn;
    private String cancel;

    private OnClickDialogListener onClickDialogListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        title = bundle.getString(DIALOG_TITLE);
        msg = bundle.getString(DIALOG_MSG);
        btn = bundle.getString(DIALOG_BUTTON);
        cancel = bundle.getString(BUTTON_CANCEL);
    }

    @Override
    protected void initView(View view) {
        tvTitle = (TextView) view.findViewById(R.id.tv_single_operation_dialog_title);
        tvMsg = (TextView) view.findViewById(R.id.tv_single_operation_dialog_message);
        tvBtnContent = (TextView) view.findViewById(R.id.tv_single_operation_dialog_confirm);
        tv_single_cancel = (TextView) view.findViewById(R.id.tv_single_cancel);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.view_dialog_single_operation;
    }

    @Override
    protected void initEvent() {
        //此处在子类中设置点击对话框外部，对话框不消失
        getDialog().setCancelable(true);
        getDialog().setCanceledOnTouchOutside(true);

        tvBtnContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickDialogListener != null) {
                    onClickDialogListener.onClick(v);
                }
            }
        });

        tv_single_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickDialogListener !=null){
                    onClickDialogListener.onClick(v);
                }
            }
        });
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected void setSubView() {
        if (!TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
            tvTitle.setVisibility(View.VISIBLE);
        }
        tvMsg.setText(msg);
        tvBtnContent.setText(btn);
        tv_single_cancel.setText(cancel);
    }

    public interface OnClickDialogListener {
        void onClick(View view);
    }

    /**
     * 对外开放的方法
     *
     * @param onClickDialogListener
     */
    public void setOnClickDialogListener(OnClickDialogListener onClickDialogListener) {
        this.onClickDialogListener = onClickDialogListener;
    }
}




