package com.ess.anime.wallpaper.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class ThumbBean implements Parcelable {

    public String thumbUrl;
    public String realSize;
    public String linkToShow;
    public ImageBean imageBean;

    public ThumbBean(String thumbUrl, String realSize, String linkToShow) {
        this.thumbUrl = thumbUrl;
        this.realSize = realSize;
        this.linkToShow = linkToShow;
    }

    // 判断ImageBean是否为此ThumbBean所属
    public boolean checkImageBelongs(ImageBean imageBean) {
        try {
            String previewUrl = imageBean.posts[0].previewUrl;
            previewUrl = previewUrl.substring(previewUrl.lastIndexOf("/"));
            return thumbUrl.contains(previewUrl);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    protected ThumbBean(Parcel in) {
        thumbUrl = in.readString();
        realSize = in.readString();
        linkToShow = in.readString();
        imageBean = in.readParcelable(ImageBean.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(thumbUrl);
        dest.writeString(realSize);
        dest.writeString(linkToShow);
        dest.writeParcelable(imageBean, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ThumbBean> CREATOR = new Creator<ThumbBean>() {
        @Override
        public ThumbBean createFromParcel(Parcel in) {
            return new ThumbBean(in);
        }

        @Override
        public ThumbBean[] newArray(int size) {
            return new ThumbBean[size];
        }
    };

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof ThumbBean) {
            ThumbBean thumbBean = (ThumbBean) obj;
            return !(this.linkToShow == null || thumbBean.linkToShow == null) && this.linkToShow.equals(thumbBean.linkToShow);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return linkToShow.hashCode();
    }
}
