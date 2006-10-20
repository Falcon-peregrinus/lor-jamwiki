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
package org.jamwiki.parser;

import java.util.Vector;
import java.util.LinkedHashMap;

/**
 * This class represents the output from the JAMWiki parser.  It holds parsed
 * output text as well as metadata that is generated by the parser.
 */
public class ParserDocument {

	private LinkedHashMap categories = new LinkedHashMap();
	private Vector links = new Vector();
	private String content = null;
	private String redirect = null;

	/**
	 *
	 */
	public ParserDocument() {
	}

	/**
	 * When a document contains a token indicating that the document belongs
	 * to a specific category this method should be called to add that
	 * category to the output metadata.
	 *
	 * @param categoryName The name of the category that the document belongs
	 *  to.
	 * @param sortKey The sort key for the category, or <code>null</code> if
	 *  no sort key has been specified.  The sort key determines what order
	 *  categories are sorted on category index pages, so a category for
	 *  "John Doe" might be given a sort key of "Doe, John".
	 */
	public void addCategory(String categoryName, String sortKey) {
		this.categories.put(categoryName, sortKey);
	}

	/**
	 * When a document contains a token indicating that the document links
	 * to another Wiki topic this method should be called to add that
	 * topic link to the output metadata.
	 *
	 * @param topicName The name of the topic that is linked to.
	 */
	public void addLink(String topicName) {
		this.links.add(topicName);
	}

	/**
	 * Return the current mapping of categories associated with the document
	 * being parsed.  The mapping contains key-value pairs with the category
	 * name as the key and the sort key (if any) as the value.
	 *
	 * @return A mapping of categories and their associated sort keys (if any)
	 *  for all categories that are associated with the document being parsed.
	 */
	public LinkedHashMap getCategories() {
		return this.categories;
	}

	/**
	 * Return the parsed content for the document currently being parsed.
	 *
	 * @return The parsed content for the document currently being parsed.
	 */
	public String getContent() {
		return this.content;
	}

	/**
	 * Set the parsed content for the document currently being parsed.
	 *
	 * @param content The parsed content for the document currently being
	 *  parsed.
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * For the document being parsed, return the current collection of topic
	 * names for all topics that are linked to from the current document.
	 *
	 * @return A collection of all topic names that are linked to from the
	 *  current document.
	 */
	public Vector getLinks() {
		return this.links;
	}

	/**
	 * If a document being parsed represents a redirect, return the name of
	 * the topic that this document redirects to.
	 *
	 * @return The name of the topic that this document redirects to, or
	 *  <code>null</code> if the document does not represent a redirect.
	 */
	public String getRedirect() {
		return this.redirect;
	}

	/**
	 * If a document being parsed represents a redirect, set the name of
	 * the topic that this document redirects to.
	 *
	 * @param redirect The name of the topic that this document redirects to,
	 *  or <code>null</code> if the document does not represent a redirect.
	 */
	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}
}
