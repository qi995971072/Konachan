package com.ess.anime.wallpaper.http.parser;

import android.content.Context;
import android.text.Html;

import com.ess.anime.wallpaper.bean.CommentBean;
import com.ess.anime.wallpaper.bean.ImageBean;
import com.ess.anime.wallpaper.bean.PoolListBean;
import com.ess.anime.wallpaper.bean.PostBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.utils.TimeFormat;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class GelbooruParser extends HtmlParser {

    GelbooruParser(Context context, Document doc) {
        super(context, doc);
    }

    @Override
    public ArrayList<ThumbBean> getThumbList() {
        ArrayList<ThumbBean> thumbList = new ArrayList<>();
        Elements elements = mDoc.getElementsByTag("post");
        for (Element e : elements) {
            try {
                String id = e.id().replaceAll("[^0-9]", "");
                String thumbUrl = e.attr("preview_url");
                if (!thumbUrl.startsWith("http")) {
                    thumbUrl = "https:" + thumbUrl;
                }
                String realSize = e.attr("width") + " x " + e.attr("height");
                String linkToShow = "https://gelbooru.com/index.php?page=post&s=view&id=" + id;
                ThumbBean thumbBean = new ThumbBean(id, thumbUrl, realSize, linkToShow);
                thumbBean.tempPost = parseTempPost(e);
                thumbList.add(thumbBean);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return thumbList;
    }

    private PostBean parseTempPost(Element e) {
        try {
            PostBean postBean = new PostBean();
            postBean.id = e.attr("id");
            postBean.tags = e.attr("tags").trim();
            postBean.creatorId = e.attr("creator_id");
            postBean.change = e.attr("change");
            postBean.source = e.attr("source");
            postBean.score = Integer.parseInt(e.attr("score"));
            postBean.md5 = e.attr("md5");
            postBean.fileUrl = e.attr("file_url");
            postBean.fileSize = -1;
            postBean.previewUrl = e.attr("preview_url");
            postBean.previewWidth = Integer.parseInt(e.attr("preview_width"));
            postBean.previewHeight = Integer.parseInt(e.attr("preview_height"));
            postBean.sampleUrl = e.attr("sample_url");
            postBean.sampleWidth = Integer.parseInt(e.attr("sample_width"));
            postBean.sampleHeight = Integer.parseInt(e.attr("sample_height"));
            postBean.sampleFileSize = -1;
            postBean.jpegUrl = e.attr("file_url");
            postBean.jpegWidth = Integer.parseInt(e.attr("width"));
            postBean.jpegHeight = Integer.parseInt(e.attr("height"));
            postBean.jpegFileSize = -1;
            postBean.rating = e.attr("rating");
            postBean.hasChildren = Boolean.parseBoolean(e.attr("has_children"));
            postBean.parentId = e.attr("parent_id");
            postBean.status = e.attr("status");
            postBean.width = Integer.parseInt(e.attr("width"));
            postBean.height = Integer.parseInt(e.attr("height"));
            return postBean;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public String getImageDetailJson() {
        ImageBean.ImageJsonBuilder builder = new ImageBean.ImageJsonBuilder();
        try {
            // 解析基础图片信息
            for (Element li : mDoc.getElementsByTag("li")) {
                if (li.text().startsWith("Id:")) {
                    // Id
                    builder.id(li.text().replaceAll("[^0-9]", ""));
                } else if (li.text().startsWith("Posted:")) {
                    // 解析时间字符串，格式：2019-02-07 19:30:27
                    // 注意PostBean.createdTime单位为second
                    String text = li.text();
                    String createdTime = text.substring(text.indexOf(":") + 1, text.indexOf("by")).trim();
                    long mills = TimeFormat.timeToMills(createdTime, "yyyy-MM-dd HH:mm:ss");
                    createdTime = String.valueOf(mills / 1000);
                    builder.createdTime(createdTime);

                    // 作者
                    String author = li.getElementsByTag("a").first().text().trim();
                    builder.author(author);
                }
            }

            // 防止为null的参数
            builder.source("").parentId("");

            // tags
            for (Element copyright : mDoc.getElementsByClass("tag-type-copyright")) {
                builder.addCopyrightTags(copyright.getElementsByTag("a")
                        .get(1).text().replace(" ", "_"));
            }
            for (Element character : mDoc.getElementsByClass("tag-type-character")) {
                builder.addCharacterTags(character.getElementsByTag("a")
                        .get(1).text().replace(" ", "_"));
            }
            for (Element artist : mDoc.getElementsByClass("tag-type-artist")) {
                builder.addArtistTags(artist.getElementsByTag("a")
                        .get(1).text().replace(" ", "_"));
            }
            for (Element general : mDoc.getElementsByClass("tag-type-metadata")) {
                builder.addGeneralTags(general.getElementsByTag("a")
                        .get(1).text().replace(" ", "_"));
            }
            for (Element general : mDoc.getElementsByClass("tag-type-general")) {
                builder.addGeneralTags(general.getElementsByTag("a")
                        .get(1).text().replace(" ", "_"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.build();
    }

    @Override
    public ArrayList<CommentBean> getCommentList() {
        ArrayList<CommentBean> commentList = new ArrayList<>();
        Elements elements = mDoc.getElementsByClass("commentBox");
        for (Element e : elements) {
            try {
                String id = "#" + e.id();
                Element img = e.getElementsByTag("img").first();
                String avatar = img == null
                        ? "https://gelbooru.com/user_avatars/avatar_anonymous.jpg"
                        : Constants.BASE_URL_GELBOORU + img.attr("src");
                String author = e.getElementsByTag("a").first().text().trim();
                String date = e.getElementsByTag("b").first().text().trim();
                int index = date.indexOf("(");
                if (index != -1) {
                    date = date.substring(0, index).trim();
                }
                String html = e.html();
                String startTag = "Up</a>)\n<br>";
                String endTag = "<br>";
                int startIndex = html.indexOf(startTag) + startTag.length();
                int endIndex = html.lastIndexOf(endTag);
                html = html.substring(startIndex, endIndex);
                CharSequence quote = "";
                CharSequence comment = Html.fromHtml(html);
                commentList.add(new CommentBean(id, author, date, avatar, quote, comment));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return commentList;
    }

    @Override
    public ArrayList<PoolListBean> getPoolList() {
        ArrayList<PoolListBean> poolList = new ArrayList<>();
        Element table = mDoc.getElementsByClass("highlightable").first();
        if (table != null) {
            Elements pools = table.getElementsByTag("tr");
            for (Element pool : pools) {
                try {
                    PoolListBean poolListBean = new PoolListBean();
                    Elements tds = pool.getElementsByTag("td");
                    Element detail = tds.get(1);
                    Element link = detail.getElementsByTag("a").first();
                    String href = link.attr("href");
                    poolListBean.id = href.substring(href.lastIndexOf("=") + 1);
                    poolListBean.name = link.text();
                    poolListBean.linkToShow = Constants.BASE_URL_GELBOORU + href;
                    poolListBean.creator = detail.getElementsByTag("a").get(1).text();
                    poolListBean.updateTime = detail.text().substring(detail.text().lastIndexOf("about"));
                    poolListBean.postCount = tds.get(2).text().replaceAll("[^0-9]", "");
                    poolList.add(poolListBean);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return poolList;
    }
}
