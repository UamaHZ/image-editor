package cn.com.uama.imageeditor.path;

import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by liwei on 2017/4/5 15:46
 * Email: liwei@uama.com.cn
 * Description: 表示涂鸦时的 Quad 点的实体类
 */

public class QuadInfo implements Parcelable {
    public PointF controlPoint;
    public PointF endPoint;

    public QuadInfo() {
        controlPoint = new PointF();
        endPoint = new PointF();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.controlPoint, flags);
        dest.writeParcelable(this.endPoint, flags);
    }

    protected QuadInfo(Parcel in) {
        this.controlPoint = in.readParcelable(PointF.class.getClassLoader());
        this.endPoint = in.readParcelable(PointF.class.getClassLoader());
    }

    public static final Creator<QuadInfo> CREATOR = new Creator<QuadInfo>() {
        @Override
        public QuadInfo createFromParcel(Parcel source) {
            return new QuadInfo(source);
        }

        @Override
        public QuadInfo[] newArray(int size) {
            return new QuadInfo[size];
        }
    };
}
