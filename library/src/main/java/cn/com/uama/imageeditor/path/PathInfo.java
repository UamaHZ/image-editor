package cn.com.uama.imageeditor.path;

import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liwei on 2017/4/5 15:48
 * Email: liwei@uama.com.cn
 * Description: 表示涂鸦时每一笔的实体类
 */

public class PathInfo implements Parcelable {
    public PointF startPoint; // 起始点
    public List<QuadInfo> quadInfos; // 笔画列表

    public PathInfo() {
        startPoint = new PointF();
        quadInfos = new ArrayList<>();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.startPoint, flags);
        dest.writeTypedList(this.quadInfos);
    }

    protected PathInfo(Parcel in) {
        this.startPoint = in.readParcelable(PointF.class.getClassLoader());
        this.quadInfos = in.createTypedArrayList(QuadInfo.CREATOR);
    }

    public static final Creator<PathInfo> CREATOR = new Creator<PathInfo>() {
        @Override
        public PathInfo createFromParcel(Parcel source) {
            return new PathInfo(source);
        }

        @Override
        public PathInfo[] newArray(int size) {
            return new PathInfo[size];
        }
    };
}
