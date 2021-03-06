/*
 * Copyright (C) 2015-2016 Daniel Schaal <daniel@schaal.email>
 *
 * This file is part of OCReader.
 *
 * OCReader is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OCReader is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OCReader.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package email.schaal.ocreader.api.json;

import android.support.annotation.Nullable;
import android.util.Log;

import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import email.schaal.ocreader.database.model.Item;
import email.schaal.ocreader.util.StringUtils;

/**
 * TypeAdapter to deserialize the JSON response for feed Items.
 */
public class ItemTypeAdapter extends NewsTypeAdapter<Item> {
    private final static String TAG = ItemTypeAdapter.class.getName();

    @Override
    public void toJson(JsonWriter out, Item item) throws IOException {
        out.beginObject();

        out.name(Item.ID).value(item.getId());
        out.name(Item.CONTENT_HASH).value(item.getContentHash());

        if(item.isUnreadChanged())
            out.name("isUnread").value(item.isUnread());

        if(item.isStarredChanged())
            out.name("isStarred").value(item.isStarred());

        out.endObject();
    }

    @Override
    public Item fromJson(JsonReader in) throws IOException {
        if (in.peek() == JsonReader.Token.NULL) {
            in.nextNull();
            return null;
        }
        Item item = new Item();
        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            switch (name) {
                case "id":
                    item.setId(in.nextLong());
                    break;
                case "guid":
                    item.setGuid(in.nextString());
                    break;
                case "guidHash":
                    item.setGuidHash(in.nextString());
                    break;
                case "url":
                    item.setUrl(nullSafeString(in));
                    break;
                case "title":
                    item.setTitle(StringUtils.cleanString(in.nextString()));
                    break;
                case "author":
                    item.setAuthor(StringUtils.nullIfEmpty(in.nextString()));
                    break;
                case "pubDate":
                    item.setPubDate(new Date(in.nextLong() * 1000));
                    break;
                case "body":
                    item.setBody(in.nextString());
                    break;
                case "enclosureMime":
                    if(in.peek() != JsonReader.Token.NULL)
                        item.setEnclosureMime(StringUtils.nullIfEmpty(in.nextString()));
                    else
                        in.skipValue();
                    break;
                case "enclosureLink":
                    if(in.peek() != JsonReader.Token.NULL)
                        item.setEnclosureLink(StringUtils.nullIfEmpty(in.nextString()));
                    else
                        in.skipValue();
                    break;
                case "publishedAt":
                    item.setPubDate(parseDate(in.nextString()));
                    break;
                case "updatedAt":
                    item.setUpdatedAt(parseDate(in.nextString()));
                    break;
                case "enclosure":
                    parseEnclosure(in, item);
                    break;
                case "feedId":
                    item.setFeedId(in.nextLong());
                    break;
                case "isUnread":
                case "unread":
                    item.setUnread(in.nextBoolean());
                    break;
                case "starred":
                case "isStarred":
                    item.setStarred(in.nextBoolean());
                    break;
                case "lastModified":
                    item.setLastModified(in.nextLong());
                    break;
                case "rtl":
                    in.skipValue();
                    break;
                case "fingerprint":
                    item.setFingerprint(in.nextString());
                    break;
                case "contentHash":
                    item.setContentHash(in.nextString());
                    break;
                case "updatedDate":
                    item.setUpdatedAt(new Date(in.nextLong() * 1000));
                    break;
                default:
                    Log.w(TAG, "Unknown value in item json: " + name);
                    in.skipValue();
                    break;
            }
        }
        in.endObject();
        return item;
    }

    private void parseEnclosure(JsonReader in, Item item) throws IOException {
        in.beginObject();
        while(in.hasNext()) {
            switch (in.nextName()) {
                case "mimeType":
                    item.setEnclosureMime(nullSafeString(in));
                    break;
                case "url":
                    item.setEnclosureLink(nullSafeString(in));
                    break;
            }
        }
        in.endObject();
    }

    private final static DateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZ", Locale.US);

    @Nullable
    private Date parseDate(String source) {
        try {
            return iso8601Format.parse(source);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
