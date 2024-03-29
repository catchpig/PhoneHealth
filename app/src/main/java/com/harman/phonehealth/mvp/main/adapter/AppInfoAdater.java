package com.harman.phonehealth.mvp.main.adapter;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.harman.phonehealth.R;
import com.harman.phonehealth.base.adapter.BaseViewHolder;
import com.harman.phonehealth.base.adapter.RecyclerAdapter;
import com.harman.phonehealth.entity.PackageInfoBean;
import com.harman.phonehealth.utils.IconUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AppInfoAdater extends RecyclerAdapter<PackageInfoBean, AppInfoAdater.ViewHolder> {

    @Override
    protected int layoutId() {
        return R.layout.item_app_info;
    }

    @Override
    protected Class<ViewHolder> viewHolderClass() {
        return ViewHolder.class;
    }

    @Override
    public void bindViewHolder(ViewHolder holder, PackageInfoBean packageInfoBean, int position) {
        Drawable drawable = IconUtils.getAppIcon(holder.mLogo.getContext(), packageInfoBean.getPackageName());
        holder.mLogo.setImageDrawable(drawable);
        holder.mAppName.setText(packageInfoBean.getAppName());
        holder.mUseCount.setText(String.format("Use Count:%d", packageInfoBean.getUsedCount()));
        holder.rankText.setText("No." + (position + 1));
        long useTime = packageInfoBean.getUsedTime() / (1000 * 60);
        if (useTime < 1) {
            holder.mUseTime.setText("Use Time:less than 1 minute");
        } else if (useTime > 60) {
            long hour = useTime / 60;
            long minute = useTime % 60;
            holder.mUseTime.setText(String.format("Use Time:%d hours %d minutes", hour, minute));
        } else {
            holder.mUseTime.setText(String.format("Use Time:%d minutes", useTime));
        }
    }

    public static class ViewHolder extends BaseViewHolder {
        @BindView(R.id.logo)
        ImageView mLogo;
        @BindView(R.id.app_name)
        TextView mAppName;
        @BindView(R.id.use_time)
        TextView mUseTime;
        @BindView(R.id.user_count)
        TextView mUseCount;
        @BindView(R.id.rank)
        TextView rankText;


        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
