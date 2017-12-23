package cn.com.uama.imageeditor;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import cn.com.uama.imageeditor.path.PathInfo;

/**
 * Created by liwei on 2017/4/1 15:19
 * Email: liwei@uama.com.cn
 * Description: 可以编辑的图片信息实体类
 */

public class EditImageInfo implements Parcelable {
    private String orgPath; // 原图路径
    private String path; // 图片路径
    private List<TextInfo> textInfos; // 图片上的文字信息
    private List<PathInfo> pathInfos; // 图片上的涂鸦信息

    public EditImageInfo(String path) {
        this.orgPath = path;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public String getOrgPath() {
        return orgPath;
    }

    public void setOrgPath(String orgPath) {
        this.orgPath = orgPath;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<TextInfo> getTextInfos() {
        return textInfos;
    }

    public void setTextInfos(List<TextInfo> textInfos) {
        this.textInfos = textInfos;
    }

    public List<PathInfo> getPathInfos() {
        return pathInfos;
    }

    public void setPathInfos(List<PathInfo> pathInfos) {
        this.pathInfos = pathInfos;
    }

    /**
     * 图片上增加的文字的信息
     */
    static class TextInfo implements Parcelable {
        public String text; // 文字
        public int left; // 位置信息，左边
        public int top; // 位置信息，上边

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.text);
            dest.writeInt(this.left);
            dest.writeInt(this.top);
        }

        public TextInfo() {
        }

        protected TextInfo(Parcel in) {
            this.text = in.readString();
            this.left = in.readInt();
            this.top = in.readInt();
        }

        public static final Creator<TextInfo> CREATOR = new Creator<TextInfo>() {
            @Override
            public TextInfo createFromParcel(Parcel source) {
                return new TextInfo(source);
            }

            @Override
            public TextInfo[] newArray(int size) {
                return new TextInfo[size];
            }
        };
    }

    public EditImageInfo() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.orgPath);
        dest.writeString(this.path);
        dest.writeTypedList(this.textInfos);
        dest.writeTypedList(this.pathInfos);
    }

    protected EditImageInfo(Parcel in) {
        this.orgPath = in.readString();
        this.path = in.readString();
        this.textInfos = in.createTypedArrayList(TextInfo.CREATOR);
        this.pathInfos = in.createTypedArrayList(PathInfo.CREATOR);
    }

    public static final Creator<EditImageInfo> CREATOR = new Creator<EditImageInfo>() {
        @Override
        public EditImageInfo createFromParcel(Parcel source) {
            return new EditImageInfo(source);
        }

        @Override
        public EditImageInfo[] newArray(int size) {
            return new EditImageInfo[size];
        }
    };
}
