/**
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the latest version of the GNU Lesser General
 * Public License as published by the Free Software Foundation;
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program (LICENSE.txt); if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jamwiki.utils;

import java.io.File;
import org.apache.commons.io.FilenameUtils;
import org.jamwiki.Environment;
import org.jamwiki.WikiBase;
import org.jamwiki.model.Topic;
import org.jamwiki.model.WikiFile;
import org.jamwiki.model.WikiImage;
import org.springframework.util.StringUtils;

/**
 *
 */
public class LinkUtil {

	private static final WikiLogger logger = WikiLogger.getLogger(LinkUtil.class.getName());

	/**
	 * Build a query parameter.  If root is empty, this method returns
	 * "?param=value".  If root is not empty this method this method
	 * returns root + "&amp;param=value".  Note that parma and value will be
	 * URL encoded, and if "query" does not start with a "?" then one will be
	 * pre-pended.
	 */
	public static String appendQueryParam(String query, String param, String value) {
		String url = "";
		if (StringUtils.hasText(query)) {
			if (!query.startsWith("?")) {
				query = "?" + query;
			}
			url = query + "&amp;";
		} else {
			url = "?";
		}
		if (!StringUtils.hasText(param)) return query;
		url += Utilities.encodeForURL(param) + "=";
		if (StringUtils.hasText(value)) {
			url += Utilities.encodeForURL(value);
		}
		return url;
	}

	/**
	 *
	 */
	public static String buildEditLinkUrl(String context, String virtualWiki, String topic, String query, int section) throws Exception {
		query = LinkUtil.appendQueryParam(query, "topic", topic);
		if (section > 0) {
			query += "&amp;section=" + section;
		}
		WikiLink wikiLink = new WikiLink();
		// FIXME - hard coding
		wikiLink.setDestination("Special:Edit");
		wikiLink.setQuery(query);
		return LinkUtil.buildInternalLinkUrl(context, virtualWiki, wikiLink);
	}

	/**
	 *
	 */
	public static String buildImageLinkHtml(String context, String virtualWiki, String topicName, boolean frame, boolean thumb, String align, String caption, int maxDimension, boolean suppressLink, String style, boolean escapeHtml) throws Exception {
		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
		if (topic == null) {
			WikiLink uploadLink = LinkUtil.parseWikiLink("Special:Upload");
			return LinkUtil.buildInternalLinkHtml(context, virtualWiki, uploadLink, topicName, "edit", null, true);
		}
		WikiFile wikiFile = WikiBase.getDataHandler().lookupWikiFile(virtualWiki, topicName);
		if (topic.getTopicType() == Topic.TYPE_FILE) {
			// file, not an image
			if (!StringUtils.hasText(caption)) {
				caption = topicName.substring(NamespaceHandler.NAMESPACE_IMAGE.length() + 1);
			}
			String url = FilenameUtils.normalize(Environment.getValue(Environment.PROP_FILE_DIR_RELATIVE_PATH) + "/" + wikiFile.getUrl());
			url = FilenameUtils.separatorsToUnix(url);
			return "<a href=\"" + url + "\">" + Utilities.escapeHTML(caption) + "</a>";
		}
		String html = "";
		WikiImage wikiImage = ImageUtil.initializeImage(wikiFile, maxDimension);
		if (caption == null) caption = "";
		if (frame || thumb || StringUtils.hasText(align)) {
			html += "<div class=\"";
			if (thumb || frame) {
				html += "imgthumb ";
			}
			if (align != null && align.equalsIgnoreCase("left")) {
				html += "imgleft ";
			} else if (align != null && align.equalsIgnoreCase("center")) {
				html += "imgcenter ";
			} else {
				// default right alignment
				html += "imgright ";
			}
			html = html.trim() + "\">";
		}
		if (wikiImage.getWidth() > 0) {
			html += "<div style=\"width:" + (wikiImage.getWidth() + 2) + "px\">";
		}
		if (!suppressLink) html += "<a class=\"wikiimg\" href=\"" + LinkUtil.buildInternalLinkUrl(context, virtualWiki, topicName) + "\">";
		if (!StringUtils.hasText(style)) style = "wikiimg";
		html += "<img class=\"" + style + "\" src=\"";
		String url = new File(Environment.getValue(Environment.PROP_FILE_DIR_RELATIVE_PATH), wikiImage.getUrl()).getPath();
		url = FilenameUtils.separatorsToUnix(url);
		html += url;
		html += "\"";
		html += " width=\"" + wikiImage.getWidth() + "\"";
		html += " height=\"" + wikiImage.getHeight() + "\"";
		html += " alt=\"" + Utilities.escapeHTML(caption) + "\"";
		html += " />";
		if (!suppressLink) html += "</a>";
		if (StringUtils.hasText(caption)) {
			html += "<div class=\"imgcaption\">";
			if (escapeHtml) {
				html += Utilities.escapeHTML(caption);
			} else {
				html += caption;
			}
			html += "</div>";
		}
		if (wikiImage.getWidth() > 0) {
			html += "</div>";
		}
		if (frame || thumb || StringUtils.hasText(align)) {
			html += "</div>";
		}
		return html;
	}

	/**
	 *
	 */
	public static String buildInternalLinkHtml(String context, String virtualWiki, WikiLink wikiLink, String text, String style, String target, boolean escapeHtml) throws Exception {
		String url = LinkUtil.buildInternalLinkUrl(context, virtualWiki, wikiLink);
		String topic = wikiLink.getDestination();
		if (!StringUtils.hasText(text)) text = topic;
		if (StringUtils.hasText(topic) && !StringUtils.hasText(style)) {
			if (InterWikiHandler.isInterWiki(virtualWiki)) {
				style = "interwiki";
			} else if (!WikiBase.exists(virtualWiki, topic)) {
				style = "edit";
			}
		}
		if (StringUtils.hasText(style)) {
			style = " class=\"" + style + "\"";
		} else {
			style = "";
		}
		if (StringUtils.hasText(target)) {
			target = " target=\"" + target + "\"";
		} else {
			target = "";
		}
		String html = "<a title=\"" + Utilities.escapeHTML(text) + "\" href=\"" + url + "\"" + style + target + ">";
		if (escapeHtml) {
			html += Utilities.escapeHTML(text);
		} else {
			html += text;
		}
		html += "</a>";
		return html;
	}

	/**
	 *
	 */
	public static String buildInternalLinkUrl(String context, String virtualWiki, String topic) throws Exception {
		if (!StringUtils.hasText(topic)) {
			return null;
		}
		WikiLink wikiLink = LinkUtil.parseWikiLink(topic);
		return LinkUtil.buildInternalLinkUrl(context, virtualWiki, wikiLink);
	}

	/**
	 *
	 * @param context The servlet context path.  If this value is
	 *  <code>null</code> then the resulting URL will NOT include context path,
	 *  which breaks HTML links but is useful for servlet redirection URLs.
	 */
	public static String buildInternalLinkUrl(String context, String virtualWiki, WikiLink wikiLink) throws Exception {
		String topic = wikiLink.getDestination();
		String section = wikiLink.getSection();
		String query = wikiLink.getQuery();
		if (!StringUtils.hasText(topic) && StringUtils.hasText(section)) {
			return "#" + Utilities.encodeForURL(section);
		}
		if (!WikiBase.exists(virtualWiki, topic)) {
			return LinkUtil.buildEditLinkUrl(context, virtualWiki, topic, query, -1);
		}
		String url = "";
		if (context != null) {
			url += context;
		}
		// context never ends with a "/" per servlet specification
		url += "/";
		// get the virtual wiki, which should have been set by the parent servlet
		url += Utilities.encodeForURL(virtualWiki);
		url += "/";
		url += Utilities.encodeForURL(topic);
		if (StringUtils.hasText(section)) {
			if (!section.startsWith("#")) url += "#";
			url += Utilities.encodeForURL(section);
		}
		if (StringUtils.hasText(query)) {
			if (!query.startsWith("?")) url += "?";
			url += query;
		}
		return url;
	}

	/**
	 *
	 */
	public static String interWiki(WikiLink wikiLink) {
		// remove namespace from link destination
		String destination = wikiLink.getDestination();
		String namespace = wikiLink.getNamespace();
		destination = destination.substring(wikiLink.getNamespace().length() + NamespaceHandler.NAMESPACE_SEPARATOR.length());
		String url = InterWikiHandler.formatInterWiki(namespace, destination);
		String text = (StringUtils.hasText(wikiLink.getText())) ? wikiLink.getText() : wikiLink.getDestination();
		return "<a class=\"interwiki\" rel=\"nofollow\" title=\"" + text + "\" href=\"" + url + "\">" + text + "</a>";
	}

	/**
	 * Parse a topic name of the form "Topic#Section?Query", and return a WikiLink
	 * object representing the link.
	 *
	 * @param raw The raw topic link text.
	 * @return A WikiLink object that represents the link.
	 */
	public static WikiLink parseWikiLink(String raw) {
		// note that this functionality was previously handled with a regular
		// expression, but the expression caused CPU usage to spike to 100%
		// with topics such as "Urnordisch oder Nordwestgermanisch?"
		String processed = raw.trim();
		WikiLink wikiLink = new WikiLink();
		if (!StringUtils.hasText(processed)) {
			return new WikiLink();
		}
		// first see if the link ends with a query param - "?..."
		int queryPos = processed.indexOf('?', 1);
		if (queryPos != -1 && queryPos < processed.length()) {
			String queryString = processed.substring(queryPos + 1);
			wikiLink.setQuery(queryString);
			processed = processed.substring(0, queryPos);
		}
		// now look for a section param - "#..."
		int sectionPos = processed.indexOf('#', 1);
		if (sectionPos != -1 && sectionPos < processed.length()) {
			String sectionString = processed.substring(sectionPos + 1);
			wikiLink.setSection(sectionString);
			processed = processed.substring(0, sectionPos);
		}
		// since we're having so much fun, let's find a namespace (default empty).
		String namespaceString = "";
		int namespacePos = processed.indexOf(':', 1);
		if (namespacePos != -1 && namespacePos < processed.length()) {
			namespaceString = processed.substring(0, namespacePos);
		}
		wikiLink.setNamespace(namespaceString);
		String topic = processed;
		if (namespacePos > 0) {
			topic = processed.substring(namespacePos + 1);
		}
		wikiLink.setArticle(Utilities.decodeFromURL(topic));
		// destination is namespace + topic
		wikiLink.setDestination(Utilities.decodeFromURL(processed));
		return wikiLink;
	}
}
