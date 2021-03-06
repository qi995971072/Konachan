package com.ess.anime.wallpaper.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class ThumbBean implements Parcelable {

    public String id;
    public String thumbUrl;
    public String realSize;
    public String linkToShow;
    public ImageBean imageBean;

    //临时存储部分图片信息
    //部分网站（如Gelbooru）在获取ThumbBean时即可解析出一部分图片信息
    //在解析完ImageBean后将此临时信息覆盖到imageBean.posts[0]上
    public PostBean tempPost;

    public ThumbBean(String id, String thumbUrl, String realSize, String linkToShow) {
        this.id = id;
        this.thumbUrl = thumbUrl;
        this.realSize = realSize;
        this.linkToShow = linkToShow;
    }

    // 判断ImageBean是否为此ThumbBean所属
    public boolean checkImageBelongs(ImageBean imageBean) {
        try {
            return TextUtils.equals(id, imageBean.posts[0].id);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // 在解析完ImageBean后将tempPost临时信息覆盖到imageBean.posts[0]上
    public synchronized void checkToReplacePostData() {
        try {
            if (tempPost != null && imageBean != null
                    && imageBean.posts != null && imageBean.posts.length > 0) {
                PostBean postBean = imageBean.posts[0];
                if (postBean != null) {
                    postBean.replaceDataIfNotNull(tempPost);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected ThumbBean(Parcel in) {
        id = in.readString();
        thumbUrl = in.readString();
        realSize = in.readString();
        linkToShow = in.readString();
        imageBean = in.readParcelable(ImageBean.class.getClassLoader());
        tempPost = in.readParcelable(PostBean.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(thumbUrl);
        dest.writeString(realSize);
        dest.writeString(linkToShow);
        dest.writeParcelable(imageBean, flags);
        dest.writeParcelable(tempPost, flags);
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
        if (obj instanceof ThumbBean) {
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
