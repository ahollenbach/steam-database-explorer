/**
 * This code is free software; you can redistribute it and/or modify it under
 * the terms of the new BSD License.
 *
 * Copyright (c) 2010-2011, Sebastian Staudt
 */

package com.github.koraktor.steamcondenser.steam.community;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.github.koraktor.steamcondenser.exceptions.WebApiException;

/**
 * This class represents Steam news and can be used to load a list of current
 * news about specific games
 *
 * @author Sebastian Staudt
 */
public class AppNews {

    private int appId;

    private String author;

    private String contents;

    private Date date;

    private boolean external;

    private String feedLabel;

    private String feedName;

    private long gid;

    private String title;

    private String url;

    /**
     * Loads the news for the given game with the given restrictions
     *
     * @param appId The unique Steam Application ID of the game (e.g.
     *        <code>440</code> for Team Fortress 2). See
     *        http://developer.valvesoftware.com/wiki/Steam_Application_IDs for
     *        all application IDs
     * @return A list of news for the specified game with the given options
     * @throws WebApiException if a request to Steam's Web API fails
     */
    public static List<AppNews> getNewsForApp(int appId)
            throws WebApiException {
        return getNewsForApp(appId, 5, null);
    }

    /**
     * Loads the news for the given game with the given restrictions
     *
     * @param appId The unique Steam Application ID of the game (e.g.
     *        <code>440</code> for Team Fortress 2). See
     *        http://developer.valvesoftware.com/wiki/Steam_Application_IDs for
     *        all application IDs
     * @param count The maximum number of news to load (default: 5). There's no
     *        reliable way to load all news. Use really a really great number
     *        instead
     * @return A list of news for the specified game with the given options
     * @throws WebApiException if a request to Steam's Web API fails
     */
    public static List<AppNews> getNewsForApp(int appId, int count)
            throws WebApiException {
        return getNewsForApp(appId, count, null);
    }

    /**
     * Loads the news for the given game with the given restrictions
     *
     * @param appId The unique Steam Application ID of the game (e.g.
     *        <code>440</code> for Team Fortress 2). See
     *        http://developer.valvesoftware.com/wiki/Steam_Application_IDs for
     *        all application IDs
     * @param count The maximum number of news to load (default: 5). There's no
     *        reliable way to load all news. Use really a really great number
     *        instead
     * @param maxLength The maximum content length of the news (default: nil).
     *        If a maximum length is defined, the content of the news will only
     *        be at most <code>maxLength</code> characters long plus an
     *        ellipsis
     * @return A list of news for the specified game with the given options
     * @throws WebApiException if a request to Steam's Web API fails
     */
    public static List<AppNews> getNewsForApp(int appId, int count, Integer maxLength)
            throws WebApiException {
        try {
            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("appid", appId);
            params.put("count", count);
            params.put("maxlength", maxLength);
            JSONObject data = new JSONObject(WebApi.getJSON("ISteamNews", "GetNewsForApp", 2, params));

            List<AppNews> newsItems = new ArrayList<AppNews>();
            JSONArray newsData = data.getJSONObject("appnews").getJSONArray("newsitems");
            for(int i = 0; i < newsData.length(); i ++) {
                newsItems.add(new AppNews(appId, newsData.getJSONObject(i)));
            }

            return newsItems;
        } catch(JSONException e) {
            throw new WebApiException("Could not parse JSON data.", e);
        }
    }

    /**
     * Creates a new instance of an AppNews news item with the given data
     *
     * @param appId The unique Steam Application ID of the game (e.g.
     *        <code>440</code> for Team Fortress 2). See
     *        http://developer.valvesoftware.com/wiki/Steam_Application_IDs for
     *        all application IDs
     * @param newsData The news data extracted from JSON
     * @throws WebApiException if the JSON data cannot be parsed
     */
    private AppNews(int appId, JSONObject newsData) throws WebApiException {
        try {
            this.appId     = appId;
            this.author    = newsData.getString("author");
            this.contents  = newsData.getString("contents").trim();
            this.date      = new Date(newsData.getLong("date"));
            this.external  = newsData.getBoolean("is_external_url");
            this.feedLabel = newsData.getString("feedlabel");
            this.feedName  = newsData.getString("feedname");
            this.gid       = newsData.getLong("gid");
            this.title     = newsData.getString("title");
            this.url       = newsData.getString("url");
        } catch(JSONException e) {
            throw new WebApiException("Could not parse JSON data.", e);
        }
    }

    /**
     * Returns the Steam Application ID of the game this news belongs to
     *
     * @return The application ID of the game this news belongs to
     */
    public int getAppId() {
        return this.appId;
    }

    /**
     * Returns the name of the author of this news
     *
     * @return The author of this news
     */
    public String getAuthor() {
        return this.author;
    }

    /**
     * Returns the contents of this news
     * <p>
     * This might contain HTML code.
     * <p>
     * <strong>Note:</strong> Depending on the setting for the maximum length
     * of a news (see {@link #getNewsForApp}, the contents might be truncated.
     *
     * @return [String] The contents of this news
     */
    public String getContents() {
        return this.contents;
    }

    /**
     * Returns the date this news item has been published
     *
     * @return The date this news has been published
     */
    public Date getDate() {
        return this.date;
    }

    /**
     * Returns the name of the feed this news item belongs to
     *
     * @return The name of the feed this news belongs to
     */
    public String getFeedLabel() {
        return this.feedLabel;
    }

    /**
     * Returns the symbolic name of the feed this news item belongs to
     *
     * @return [String] The symbolic name of the feed this news belongs to
     */
    public String getFeedName() {
        return this.feedName;
    }

    /**
     * Returns a unique identifier for this news
     *
     * @return A unique identifier for this news
     */
    public long getGid() {
        return this.gid;
    }

    /**
     * Returns the title of this news
     *
     * @return The title of this news
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Returns the URL of the original news
     * <p>
     * This is a direct link to the news on the Steam website or a redirecting
     * link to the external post.
     *
     * @return The URL of the original news
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Returns whether this news item originates from a source other than Steam
     * itself (e.g. an external blog)
     *
     * @return <code>true</code> if this news item is from an external source
     */
    public boolean isExternal() {
        return this.external;
    }

    /**
     * Returns a simple textual representation of this news item
     * <p>
     * Will consist of the name of the feed this news belongs to and the title
     * of the news.
     *
     * @return A simple text representing this news
     */
    public String toString() {
        return this.feedLabel + ": " + this.getTitle();
    }

}
